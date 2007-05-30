/*
 * Copyright 2007 ETH Zuerich, CISD
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ch.systemsx.cisd.dbmigration;

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;

import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.utilities.FileUtilities;

/**
 * Class for creating and migrating a database.
 * 
 * @author Franz-Josef Elmer
 */
public class DBMigrationEngine
{
    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION, DBMigrationEngine.class);

    private static final String INSERT_DB_VERSION = "INSERT INTO DATABASE_VERSION VALUES (1, ?)";

    private static final String CREATE_DB_VERSION_TABLE =
            "CREATE TABLE DATABASE_VERSION (DB_VERSION SMALLINT NOT NULL," + "DB_INSTALLATION_DATE DATE)";

    private static final class DatabaseVersion
    {
        private final int version;

        private final Date installationDate;

        public DatabaseVersion(int version, Date installationDate)
        {
            this.version = version;
            this.installationDate = installationDate;
        }

        Date getInstallationDate()
        {
            return installationDate;
        }

        int getVersion()
        {
            return version;
        }
    }

    private final DataSource metaDataSource;

    private final DataSource dataSource;

    private final File scriptFolder;

    private final String initialDataScriptFile;

    private final String owner;

    private final String databaseName;

    public DBMigrationEngine(DataSource metaDataSource, DataSource dataSource, String scriptFolder,
            String initialDataScript, String owner, String databaseName)
    {
        this.metaDataSource = metaDataSource;
        this.dataSource = dataSource;
        this.initialDataScriptFile = initialDataScript;
        this.owner = owner;
        this.databaseName = databaseName;
        this.scriptFolder = new File(scriptFolder);
    }
    
    public void createDatabase()
    {
        dropDatabase();
        setupDatabase();
    }

    public void migrateTo(int version)
    {
        if (databaseExists() == false)
        {
            setupDatabase();
            return;
        }
        SimpleJdbcTemplate template = new SimpleJdbcTemplate(dataSource);
        List<DatabaseVersion> list =
                template.query("SELECT * FROM DATABASE_VERSION", new ParameterizedRowMapper<DatabaseVersion>()
                    {
                        public DatabaseVersion mapRow(ResultSet rs, int rowNum) throws SQLException
                        {
                            int dbVersion = rs.getInt("DB_VERSION");
                            java.sql.Date date = rs.getDate("DB_INSTALLATION_DATE");
                            return new DatabaseVersion(dbVersion, date);
                        }
                    });
        int size = list.size();
        if (size == 0)
        {
            throw new EnvironmentFailureException("Incompletely initialized database.");
        } else if (size > 1)
        {
            throw new EnvironmentFailureException("To many versions found in DATABASE_VERSION: " + size);
        } else
        {
            DatabaseVersion databaseVersion = list.get(0);
            int dbVersion = databaseVersion.getVersion();
            if (version == dbVersion)
            {
                return; // no migrate needed
            }
            if (version > dbVersion)
            {
                if (operationLog.isInfoEnabled())
                {
                    operationLog.info("Migrating database from version '" + dbVersion + "' to '" + version + "'.");
                }
                // TODO implementation of migration
            } else
            {
                throw new EnvironmentFailureException("Couldn't revert from version " + dbVersion
                        + " to previous version " + version + ".");
            }
        }
    }

    private void dropDatabase()
    {
        String dropDatabaseSQL = createScript("dropDatabase.sql", owner, databaseName);
        JdbcTemplate template = new JdbcTemplate(metaDataSource);
        template.execute(dropDatabaseSQL);
    }
    
    private void setupDatabase()
    {
        createUser();
        createEmptyDatabase();
        fillWithInitialData();
        if (operationLog.isInfoEnabled())
        {
            operationLog.info("Database '" + databaseName + "' has been successfully created.");
        }
    }

    private void createUser()
    {
        String createUserSQL = createScript("createUser.sql", owner, databaseName);
        JdbcTemplate template = new JdbcTemplate(metaDataSource);
        try
        {
            // If the user already exists, the database will throw an exception.
            // But, the exception thrown could have another reason. Right now, there is no possibility
            // to differentiate one case from another.
            template.execute(createUserSQL);
        } catch (BadSqlGrammarException ex)
        {
            operationLog.error("Executing following script '" + createUserSQL + "' threw an exception.", ex);
        }
    }

    private void createEmptyDatabase()
    {
        JdbcTemplate template = new JdbcTemplate(metaDataSource);
        String createDatabaseSQL = createScript("createDatabase.sql", owner, databaseName);
        template.execute(createDatabaseSQL);
        
        template = new JdbcTemplate(dataSource);
        template.execute(CREATE_DB_VERSION_TABLE);
        Object[] args = new Object[1];
        args[0] = new Date();
        template.update(INSERT_DB_VERSION, args);
        
        String createScript = loadScript("initial.sql");
        template.execute(createScript);
    }

    private void fillWithInitialData()
    {
        String initialDataScript = null;
        if (initialDataScriptFile != null)
        {
            initialDataScript = FileUtilities.loadStringResource(getClass(), "/" + initialDataScriptFile);
            if (initialDataScript == null)
            {
                File file = new File(initialDataScriptFile);
                if (file.exists())
                {
                    initialDataScript = FileUtilities.loadText(file);
                }
            }
        }
        if (initialDataScript != null)
        {
            JdbcTemplate template = new JdbcTemplate(dataSource);
            template.execute(initialDataScript);
        }
    }

    private String createScript(String scriptTemplateFile, String user, String database)
    {
        String script = loadScript(scriptTemplateFile);
        return script.replace("$USER", user).replace("$DATABASE", database);
    }
    
    /** Loads given script name. */
    private String loadScript(String scriptName)
    {   
        String resource = "/" + scriptFolder + "/" + scriptName;
        String script = FileUtilities.loadStringResource(getClass(), resource);
        if (script == null)
        {
            File file = new File(scriptFolder, scriptName);
            if (operationLog.isDebugEnabled())
            {
                operationLog.debug("Resource '" + resource + "' could not be found. Trying '" + file.getPath() + "'.");
            }
            script = FileUtilities.loadText(file);
        }
        return script;
    }

    private boolean databaseExists()
    {
        try
        {
            Connection connection = dataSource.getConnection();
            connection.close();
            return true;
        } catch (SQLException ex)
        {
            Throwable cause = ex.getCause();
            if (cause instanceof SQLException)
            {
                ex = (SQLException) cause;
            }
            if (isDBNotExistException(ex))
            {
                return false;
            }
            throw new EnvironmentFailureException("Couldn't connect database server.", ex);
        }
    }

    protected boolean isDBNotExistException(SQLException exception)
    {
        String message = exception.getMessage();
        return message.startsWith("FATAL: database") || message.startsWith("FATAL: password");
    }
}
