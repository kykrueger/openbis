/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;

import ch.ethz.sis.openbis.generic.server.asapi.v3.search.sql.ISQLExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.sql.JDBCSQLExecutor;

public class SamplesDBTestHelper
{

    public static final String ID_DELIMITER = "/";

    public static final String CONTAINER_DELIMITER = ":";

    public static final String STRING_DATA_TYPE_CODE = "VARCHAR";

    public static final String INTEGER_DATA_TYPE_CODE = "INTEGER";

    public static final String DOUBLE_DATA_TYPE_CODE = "REAL";

    public static final String DATE_DATA_TYPE_CODE = "TIMESTAMP";

    public static final long ADMIN_USER_TECH_ID = 2L;

    public static final String ADMIN_USER_ID = "etlserver";

    public static final long REGISTRATOR_ID = 101L;

    public static final String REGISTRATOR_USER_ID = "jbrown";

    public static final String REGISTRATOR_FIRST_NAME = "John";

    public static final String REGISTRATOR_LAST_NAME = "Brown";

    public static final String REGISTRATOR_EMAIL = "jbrown@example.com";

    public static final String MODIFIER_USER_ID = "jblack";

    public static final String MODIFIER_FIRST_NAME = "Jimm";

    public static final String MODIFIER_LAST_NAME = "Black";

    public static final String MODIFIER_EMAIL = "jblack@example.com";

    public static final long MODIFIER_ID = 102L;

    public static final long SAMPLE_ID_1 = 1001L;

    public static final long SAMPLE_ID_2 = 1002L;

    public static final long SAMPLE_ID_3 = 1003L;

    public static final long SPACE_ID_1 = 10000L;

    public static final long SPACE_ID_2 = 10001L;

    public static final long PROJECT_ID = 10002L;

    public static final long EXPERIMENT_ID = 10003L;

    public static final long EXPERIMENT_TYPE_ID = 10004L;

    public static final long SAMPLE_PROPERTY_ID_1 = 1001L;

    public static final long SAMPLE_PROPERTY_ID_2 = 1002L;

    public static final long SAMPLE_PROPERTY_ID_3 = 1003L;

    public static final long SAMPLE_PROPERTY_ID_4 = 1004L;

    public static final long SAMPLE_PROPERTY_ID_5 = 1005L;

    public static final long SAMPLE_TYPE_PROPERTY_TYPE_ID_1 = 2001L;

    public static final long SAMPLE_TYPE_PROPERTY_TYPE_ID_2 = 2002L;

    public static final long SAMPLE_TYPE_PROPERTY_TYPE_ID_3 = 2003L;

    public static final long SAMPLE_TYPE_PROPERTY_TYPE_ID_4 = 2004L;

    public static final long SAMPLE_TYPE_PROPERTY_TYPE_ID_5 = 2005L;

    public static final long SAMPLE_TYPE_PROPERTY_TYPE_ORDINAL_2 = 2L;

    public static final long SAMPLE_TYPE_ID_1 = 3001L;

    public static final long SAMPLE_TYPE_ID_2 = 3002L;

    public static final long SAMPLE_TYPE_ID_3 = 3003L;

    public static final long SAMPLE_TYPE_ID_4 = 3004L;

    public static final long SAMPLE_TYPE_ID_5 = 3005L;

    public static final long LISTABLE_SAMPLE_TYPE_ID = 3101L;

    public static final long NOT_LISTABLE_SAMPLE_TYPE_ID = 3102L;

    public static final long SAMPLE_PROPERTY_1_NUMBER_VALUE = 101L;

    public static final String SAMPLE_PROPERTY_1_INTERNAL_STRING_VALUE = "Internal value";
    
    public static final String SAMPLE_PROPERTY_2_STRING_VALUE = "Test property value";

    public static final double SAMPLE_PROPERTY_3_NUMBER_VALUE = 90.25;

    public static final Date SAMPLE_PROPERTY_2_DATE_VALUE = new Date(119, Calendar.SEPTEMBER, 17, 15, 58, 0);

    public static final Date SAMPLE_PROPERTY_2_EARLIER_DATE_VALUE = new Date(119, Calendar.SEPTEMBER, 17, 15, 57, 0);

    public static final Date SAMPLE_PROPERTY_2_LATER_DATE_VALUE = new Date(119, Calendar.SEPTEMBER, 17, 15, 59, 0);

    public static final String SAMPLE_PROPERTY_2_DATE_STRING_VALUE = "2019-09-17 15:58:00 +0200";

    public static final String SAMPLE_PROPERTY_2_EARLIER_DATE_STRING_VALUE = "2019-09-17 15:57:00 +0200";

    public static final String SAMPLE_PROPERTY_2_LATER_DATE_STRING_VALUE = "2019-09-17 15:59:00 +0200";

    public static final long SAMPLE_PROPERTY_TYPE_ID_STRING = 4001L;

    public static final long SAMPLE_PROPERTY_TYPE_ID_LONG = 4002L;

    public static final long SAMPLE_PROPERTY_TYPE_ID_DOUBLE = 4003L;

    public static final long SAMPLE_PROPERTY_TYPE_ID_DATE = 4004L;

    public static final long SAMPLE_PROPERTY_TYPE_ID_STRING_INTERNAL = 4005L;

    public static final String INTERNAL_SAMPLE_PROPERTY_CODE_STRING = "$TEST.STRING";

    public static final String SAMPLE_PROPERTY_CODE_STRING = "TEST.STRING";

    public static final String SAMPLE_PROPERTY_CODE_LONG = "TEST.LONG";

    public static final String SAMPLE_PROPERTY_CODE_DOUBLE = "TEST.DOUBLE";

    public static final String SAMPLE_PROPERTY_CODE_DATE = "TEST.DATE";

    public static final String DEFAULT_PERM_ID = "20190612105000000-0";

    public static final String PERM_ID_1 = "20190612105000000-1001";

    public static final String PERM_ID_2 = "20190612105000001-1001";

    public static final String PERM_ID_3 = "20190612105000000-1003";

    public static final String DEFAULT_CODE = "DEFAULT_UNIQUE_CODE";

    public static final String PROJECT_CODE = "PROJECT_CODE";

    public static final String SPACE_CODE_1 = "MY_SPACE_UNIQUE_CODE1";

    public static final String SPACE_CODE_2 = "MY_SPACE_UNIQUE_CODE2";

    public static final String CODE_1 = "MY_UNIQUE_CODE1";

    public static final String CODE_2 = "ANOTHER_UNIQUE_CODE2";

    public static final String CODE_3 = "ANOTHER_UNIQUE_CODE3";

    public static final int DEFAULT_VERSION = 10;

    public static final int VERSION_1 = 101;

    public static final int VERSION_2 = 102;

    public static final int VERSION_3 = 103;

    public static final Date REGISTRATION_DATE_1 = new Date(119, Calendar.JUNE, 11, 10, 50, 0);

    public static final Date REGISTRATION_DATE_2 = new Date(119, Calendar.JUNE, 12, 10, 50, 0);

    public static final String REGISTRATION_DATE_STRING_1 = "2019-06-11 10:50:00 +0200";

    public static final String REGISTRATION_DATE_STRING_2 = "2019-06-12 10:50:00 +0200";

    public static final Date MODIFICATION_DATE_1 = new Date(118, Calendar.JUNE, 11, 10, 50, 0);

    public static final Date MODIFICATION_DATE_2 = new Date(118, Calendar.JUNE, 12, 10, 50, 0);

    public static final Date MODIFICATION_DATE_3 = new Date(118, Calendar.JUNE, 13, 10, 50, 0);

    public static final String MODIFICATION_DATE_STRING_2 = "2018-06-12 10:50:00 +0200";

    public static final Date DEFAULT_DATE = new Date(119, Calendar.JUNE, 10, 10, 50, 0);

    public static final String SAMPLE_TYPE_CODE_1 = "SAMPLE.TYPE.1";

    public static final String SAMPLE_TYPE_CODE_2 = "SAMPLE.TYPE.2";

    public static final String SAMPLE_TYPE_CODE_3 = "SAMPLE.TYPE.3";

    public static final String SAMPLE_TYPE_CODE_4 = "SAMPLE.TYPE.4";

    public static final String SAMPLE_TYPE_CODE_5 = "SAMPLE.TYPE.5";

    public static final String LISTABLE_SAMPLE_TYPE_CODE = "MY.SAMPLE.TYPE.LISTABLE";

    public static final String NOT_LISTABLE_SAMPLE_TYPE_CODE = "MY.SAMPLE.TYPE.NOT.LISTABLE";

    /** Indicator that the property is internal. */
    private static final String INTERNAL_PROPERTY_PREFIX = "$";

    private JDBCSQLExecutor sqlExecutor;

    private Connection connection;

    private boolean autocommit = false;

    public ISQLExecutor getSqlExecutor()
    {
        return sqlExecutor;
    }

    public void setAutocommit(final boolean autocommit)
    {
        this.autocommit = autocommit;
    }

    public void setUp() throws Exception
    {
        initConnection(false);
        populateDB();
    }

    private void executeUpdateSqlFromResourceFile(final String fileName)
    {
        final URL sqlFile = getFileURLFromResources(fileName);
        final String content;
        try
        {
            content = new String(Files.readAllBytes(Paths.get(sqlFile.toURI())));
        } catch (IOException | URISyntaxException e)
        {
            throw new RuntimeException("URL conversion error.");
        }

        sqlExecutor.executeUpdate(content, Collections.emptyList());
    }

    private URL getFileURLFromResources(final String fileName)
    {
        return getClass().getClassLoader().getResource(fileName);
    }

    /**
     * Initializes {@link #connection} and {@link #sqlExecutor}.
     *
     * @throws SQLException if an SQL exception occurs.
     */
    private void initConnection(final boolean autocommit) throws SQLException
    {
        connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/openbis_dev", "postgres", "");
        connection.setAutoCommit(autocommit);

        if (sqlExecutor == null)
        {
            sqlExecutor = new JDBCSQLExecutor();
        }
        sqlExecutor.setConnection(connection);
    }

    private void populateDB() throws SQLException
    {
        try
        {
            executeUpdateSqlFromResourceFile("sample-search-init.sql");
            connection.commit();
        } catch (final Exception e)
        {
            connection.rollback();
            throw e;
        }
    }

    public void tearDown() throws Exception
    {
        try
        {
            connection.setAutoCommit(true);
            executeUpdateSqlFromResourceFile("sample-search-cleanup.sql");
        } finally
        {
            closeConnection();
        }
    }

    public void resetConnection() throws SQLException
    {
        closeConnection();

        // A different connection should be used to be able to clean up even when the transaction is set for rollback.
        initConnection(autocommit);
    }

    private void closeConnection() throws SQLException
    {
        if (connection != null)
        {
            connection.close();
        }
    }

}
