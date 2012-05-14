/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.server.task;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.test.RecordingMatcher;
import ch.systemsx.cisd.dbmigration.DatabaseConfigurationContext;
import ch.systemsx.cisd.openbis.generic.server.ICommonServerForInternalUse;
import ch.systemsx.cisd.openbis.generic.server.task.MaterialReportingTask.MappingInfo;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DetailedSearchCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Material;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.builders.MaterialBuilder;
import ch.systemsx.cisd.openbis.generic.shared.dto.SessionContextDTO;

/**
 * @author Franz-Josef Elmer
 */
@Friend(toClasses =
    { MaterialReportingTask.class, MappingInfo.class })
public class MaterialReportingTaskTest extends AbstractFileSystemTestCase
{
    private static final String SESSION_TOKEN = "session-token";

    private Mockery context;

    private ICommonServerForInternalUse server;

    private DatabaseConfigurationContext dbConfigContext;

    private MaterialReportingTask materialReportingTask;

    private String databaseName;

    private File mappingFile;

    @BeforeMethod
    public void setUpMocks() throws Exception
    {
        context = new Mockery();
        server = context.mock(ICommonServerForInternalUse.class);
        materialReportingTask = new MaterialReportingTask(server);

        dbConfigContext = new DatabaseConfigurationContext();
        dbConfigContext.setDatabaseEngineCode("postgresql");
        dbConfigContext.setBasicDatabaseName("material_reporting_task");
        dbConfigContext.setDatabaseKind("test");
        dbConfigContext.setOwner(null);
        databaseName = dbConfigContext.getDatabaseName();
        dropTestDatabase();
        createTestDatabase();
        createTables("create table report1 (code varchar(20), description varchar(200))",
                "create table report2 (code varchar(20), prop1 varchar(200), prop2 varchar(200))");
        dbConfigContext.closeConnections();
        mappingFile = new File(workingDirectory, "mapping-file.txt");
    }

    @AfterMethod
    public void tearDown() throws Exception
    {
        context.assertIsSatisfied();
        materialReportingTask.closeDatabaseConnections();
        dbConfigContext.closeConnections();
    }

    @Test
    public void testReadValidMappingFile() throws SQLException
    {
        FileUtilities.writeToFile(mappingFile, "# my mapping\n[T1:TABLE1,CODE]\n\n"
                + "[T2: TABLE2, code]\nP2: prop2\nP1:prop1");

        Map<String, MappingInfo> mapping =
                MaterialReportingTask.readMappingFile(mappingFile.getPath());

        MappingInfo mappingInfo1 = mapping.get("T1");
        assertEquals("insert into TABLE1 (CODE) values(?)", mappingInfo1.createInsertStatement());
        MappingInfo mappingInfo2 = mapping.get("T2");
        assertEquals("insert into TABLE2 (code, prop1, prop2) values(?, ?, ?)",
                mappingInfo2.createInsertStatement());
        assertEquals(2, mapping.size());
    }

    @Test
    public void testReadMappingFileWithMissingInitialTableDefinition()
    {
        FileUtilities.writeToFile(mappingFile, "P2: prop2\nP1:prop1");

        try
        {
            MaterialReportingTask.readMappingFile(mappingFile.getPath());
            fail("ConfigurationFailureException expected");
        } catch (ConfigurationFailureException ex)
        {
            assertEquals("Error in mapping file '" + mappingFile + "' at line 1 'P2: prop2': "
                    + "Missing first material type table definition of form "
                    + "'[<material type tode>: <table name>, <code column name>]'", ex.getMessage());
        }
    }

    @Test
    public void testReadMappingFileWithInvalidTableDefinitionMissingFinishingBracket()
    {
        FileUtilities.writeToFile(mappingFile, "[T1: TABLE, CODE\nP1: p1");

        try
        {
            MaterialReportingTask.readMappingFile(mappingFile.getPath());
            fail("ConfigurationFailureException expected");
        } catch (ConfigurationFailureException ex)
        {
            assertEquals("Error in mapping file '" + mappingFile + "' at line 1 "
                    + "'[T1: TABLE, CODE': Missing ']'", ex.getMessage());
        }
    }

    @Test
    public void testReadMappingFileWithInvalidTableDefinitionMissingColon()
    {
        FileUtilities.writeToFile(mappingFile, "[T1]\nP1: p1");

        try
        {
            MaterialReportingTask.readMappingFile(mappingFile.getPath());
            fail("ConfigurationFailureException expected");
        } catch (ConfigurationFailureException ex)
        {
            assertEquals("Error in mapping file '" + mappingFile + "' at line 1 "
                    + "'[T1]': 2 items separated by ':' expected.", ex.getMessage());
        }
    }

    @Test
    public void testReadMappingFileWithInvalidTableDefinitionMissingCodeColumnName()
    {
        FileUtilities.writeToFile(mappingFile, "[T1: table]\nP1: p1");

        try
        {
            MaterialReportingTask.readMappingFile(mappingFile.getPath());
            fail("ConfigurationFailureException expected");
        } catch (ConfigurationFailureException ex)
        {
            assertEquals("Error in mapping file '" + mappingFile + "' at line 1 "
                    + "'[T1: table]': 2 items separated by ',' expected.", ex.getMessage());
        }
    }

    @Test
    public void testReadMappingFileWithInvalidTableDefinitionEmptyMaterialTypeCode()
    {
        FileUtilities.writeToFile(mappingFile, "[ : table, code]\nP1: p1");

        try
        {
            MaterialReportingTask.readMappingFile(mappingFile.getPath());
            fail("ConfigurationFailureException expected");
        } catch (ConfigurationFailureException ex)
        {
            assertEquals("Error in mapping file '" + mappingFile + "' at line 1 "
                    + "'[T1: table]': 2 items separated by ',' expected.", ex.getMessage());
        }
    }

    @Test
    public void test() throws Exception
    {
        FileUtilities.writeToFile(mappingFile, "# my mapping\n[T1:REPORT1,CODE]\n\n"
                + "[T2: REPORT2, code]\nP2: prop2\nP1:prop1");
        Properties properties = new Properties();
        properties.setProperty("database-driver", "org.postgresql.Driver");
        properties.setProperty("database-url", "jdbc:postgresql://localhost/" + databaseName);
        properties.setProperty("database-username", "postgres");
        properties.setProperty(MaterialReportingTask.MAPPING_FILE_KEY, mappingFile.getPath());
        materialReportingTask.setUp("", properties);
        final Material m1 =
                new MaterialBuilder().code("M1").type("T1").property("P1", "42").getMaterial();
        final Material m2 =
                new MaterialBuilder().code("M2").type("T2").property("P1", "42").getMaterial();
        final Material m3 =
                new MaterialBuilder().code("M3").type("T2").property("P2", "137").getMaterial();
        final RecordingMatcher<DetailedSearchCriteria> criteriaRecorder =
                new RecordingMatcher<DetailedSearchCriteria>();
        context.checking(new Expectations()
            {
                {
                    one(server).tryToAuthenticateAsSystem();
                    SessionContextDTO session = new SessionContextDTO();
                    session.setSessionToken(SESSION_TOKEN);
                    will(returnValue(session));

                    one(server).searchForMaterials(with(SESSION_TOKEN), with(criteriaRecorder));
                    will(returnValue(Arrays.asList(m1, m2, m3)));
                }
            });

        materialReportingTask.execute();

        List<?> result =
                new JdbcTemplate(dbConfigContext.getDataSource()).query("select * from report1",
                        new ColumnMapRowMapper());
        assertEquals("[{code=M1, description=null}]", result.toString());
        result =
                new JdbcTemplate(dbConfigContext.getDataSource()).query(
                        "select * from report2 order by code", new ColumnMapRowMapper());
        assertEquals("[{code=M2, prop1=null, prop2=42}, {code=M3, prop1=137, prop2=null}]",
                result.toString());
        context.assertIsSatisfied();
    }

    private void createTables(String... creationStatements) throws Exception
    {
        Connection connection = null;
        Statement statement = null;
        try
        {
            connection = dbConfigContext.getDataSource().getConnection();
            connection.setAutoCommit(true);
            statement = connection.createStatement();
            for (String creationStatement : creationStatements)
            {
                statement.execute(creationStatement);
            }
        } finally
        {
            if (statement != null)
            {
                statement.close();
            }
            if (connection != null)
            {
                connection.close();
            }
        }
    }

    private void createTestDatabase() throws SQLException
    {
        Connection connection = null;
        Statement statement = null;
        try
        {
            connection = dbConfigContext.getAdminDataSource().getConnection();
            connection.setAutoCommit(true);
            statement = connection.createStatement();
            statement.execute("create database " + dbConfigContext.getDatabaseName()
                    + " with owner = \"" + dbConfigContext.getOwner() + "\"");
        } finally
        {
            if (statement != null)
            {
                statement.close();
            }
            if (connection != null)
            {
                connection.close();
            }
        }
    }

    private void dropTestDatabase() throws SQLException
    {
        Connection connection = null;
        Statement statement = null;
        try
        {
            connection = dbConfigContext.getAdminDataSource().getConnection();
            connection.setAutoCommit(true);
            statement = connection.createStatement();
            statement.execute("drop database " + dbConfigContext.getDatabaseName());
        } catch (SQLException ex)
        {
            if (ex.getMessage().indexOf("does not exist") < 0)
            {
                throw ex;
            }
        } finally
        {
            if (statement != null)
            {
                statement.close();
            }
            if (connection != null)
            {
                connection.close();
            }
        }
    }

}
