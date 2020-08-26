/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.server.util;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.IndexCreationUtil;
import org.apache.log4j.Logger;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.logging.LogInitializer;
import ch.systemsx.cisd.common.process.ProcessExecutionHelper;
import ch.systemsx.cisd.dbmigration.postgresql.DumpPreparator;

/**
 * @author Franz-Josef Elmer
 */
public class TestInitializer
{
    static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            TestInitializer.class);

    private static final String LUCENE_INDEX_TEMPLATE_PATH = "targets/tempLuceneIndices";

    private static final String LUCENE_INDEX_PATH = "targets/lucene/indices";

    private static String dbKind = "test";

    private static String dbKindForIndexing;

    private static boolean createDBFromScratch = true;

    private static String scriptFolderForTestDB = "../openbis/sourceTest";

    private static String scriptFolderForEmptyDB = "../openbis/source";

    public static void init()
    {
        initWithoutIndex();
    }

    public static void initWithoutIndex()
    {
        init(getScriptFolderTestDB());
    }

    public static void initWithIndex()
    {
        initWithoutIndex();
    }

    public static void initEmptyDbNoIndex()
    {
        init(getScriptFolderEmptyDB());
    }

    public static void initEmptyDbWithIndex()
    {
        initEmptyDbNoIndex();
    }

    private static boolean firstTry = true;

    private static void init(String scriptFolder)
    {
        LogInitializer.init();

        if (firstTry && System.getProperty("rebuild-index", "true").equals("true"))
        {
            String databaseKind = null;

            if (getDBKindForIndexing() != null)
            {
                databaseKind = getDBKindForIndexing();
            } else
            {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmssS");
                String timestamp = dateFormat.format(new Date());
                databaseKind = "indexing_" + timestamp;
            }

            try
            {
                File temporaryFile = new File(LUCENE_INDEX_TEMPLATE_PATH);
                FileUtilities.deleteRecursively(temporaryFile);
                temporaryFile.mkdirs();

                System.setProperty("script-folder", scriptFolder);

                IndexCreationUtil.main(databaseKind, temporaryFile.getAbsolutePath(), String.valueOf(getCreateDBFromScratch()));

                operationLog.info("Created Lucene index in '" + temporaryFile.getAbsolutePath() + "'. The index is based on data from '"
                        + scriptFolder + "' script folder.");

            } catch (Exception ex)
            {
                operationLog.error("Couldn't create Lucene index", ex);
                throw CheckedExceptionTunnel.wrapIfNecessary(ex);
            } finally
            {
                String psql = DumpPreparator.getPSQLExecutable();
                final String databaseName = "openbis_" + databaseKind;
                String sql = "drop database if exists " + databaseName;
                final List<String> cmd = Arrays.asList(psql, "-U", "postgres", "-c", sql);
                boolean result = ProcessExecutionHelper.runAndLog(cmd, operationLog, operationLog);
                if (result == false)
                {
                    operationLog.error("Couldn't drop database created for indexing: "
                            + databaseName);
                    Runtime.getRuntime().addShutdownHook(new Thread(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                operationLog.info("Try to drop indexing database " + databaseName);
                                boolean ok =
                                        ProcessExecutionHelper.runAndLog(cmd, operationLog,
                                                operationLog);
                                operationLog.info("Dropping indexing database " + databaseName
                                        + (ok ? "was" : "wasn't") + " successful.");
                            }
                        }, "dropping-indexing-database-shutdown"));
                }
            }
            firstTry = false;
        }

        System.setProperty("database.create-from-scratch", String.valueOf(getCreateDBFromScratch()));
        System.setProperty("database.kind", getDBKind());
        System.setProperty("script-folder", scriptFolder);
    }

    public static boolean getCreateDBFromScratch()
    {
        return createDBFromScratch;
    }

    public static void setCreateDBFromScratch(boolean createDBFromScratch)
    {
        TestInitializer.createDBFromScratch = createDBFromScratch;
    }

    public static String getDBKind()
    {
        return dbKind;
    }

    public static void setDBKind(String dbKind)
    {
        TestInitializer.dbKind = dbKind;
    }

    public static String getDBKindForIndexing()
    {
        return dbKindForIndexing;
    }

    public static void setDBKindForIndexing(String dbKindForIndexing)
    {
        TestInitializer.dbKindForIndexing = dbKindForIndexing;
    }

    public static String getScriptFolderTestDB()
    {
        return scriptFolderForTestDB;
    }

    public static void setScriptFolderForTestDB(String scriptFolderForTestDB)
    {
        TestInitializer.scriptFolderForTestDB = scriptFolderForTestDB;
    }

    public static String getScriptFolderEmptyDB()
    {
        return scriptFolderForEmptyDB;
    }

    public static void setScriptFolderForEmptyDB(String scriptFolderForEmptyDB)
    {
        TestInitializer.scriptFolderForEmptyDB = scriptFolderForEmptyDB;
    }

}
