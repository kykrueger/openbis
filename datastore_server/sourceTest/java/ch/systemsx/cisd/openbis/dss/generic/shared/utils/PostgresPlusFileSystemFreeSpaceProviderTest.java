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

package ch.systemsx.cisd.openbis.dss.generic.shared.utils;

import static ch.systemsx.cisd.openbis.dss.generic.shared.utils.PostgresPlusFileSystemFreeSpaceProvider.DATA_SOURCE_KEY;

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Properties;

import javax.sql.DataSource;

import org.apache.commons.io.FileSystemUtils;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.springframework.beans.factory.BeanFactory;
import org.testng.AssertJUnit;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.filesystem.HostAwareFile;
import ch.systemsx.cisd.openbis.dss.generic.shared.IDataSourceProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProviderTestWrapper;

/**
 * @author Kaloyan Enimanev
 */
public class PostgresPlusFileSystemFreeSpaceProviderTest extends AssertJUnit
{

    private final static String DATA_SOURCE = "data-source";

    private PostgresPlusFileSystemFreeSpaceProvider provider;

    private Mockery context;
    private Connection connection;

    private BeanFactory mockApplicationContext;

    @BeforeMethod
    public void setUp() throws Exception
    {
        context = new Mockery();
        mockApplicationContext = context.mock(BeanFactory.class);
        connection = context.mock(Connection.class);

        ServiceProviderTestWrapper.setApplicationContext(mockApplicationContext);
        
        final IDataSourceProvider dsProvider =
                ServiceProviderTestWrapper.mock(context, IDataSourceProvider.class);
        final DataSource dataSource = context.mock(DataSource.class);

        context.checking(new Expectations()
            {
                {
                    allowing(dsProvider).getDataSource(DATA_SOURCE);
                    will(returnValue(dataSource));

                    one(dataSource).getConnection();
                    will(returnValue(connection));

                    one(connection).close();
                }
            });
    }

    @AfterMethod
    public void tearDown()
    {
        ServiceProviderTestWrapper.restoreApplicationContext();
    }

    private Properties createProperties(boolean executeVacuum) {
        Properties props = new Properties();
        props.put(DATA_SOURCE_KEY, DATA_SOURCE);
        if (executeVacuum)
        {
            props.put(PostgresPlusFileSystemFreeSpaceProvider.EXECUTE_VACUUM_KEY, "true");
        }
        return props;
    }

    @Test
    public void testNoVacuum() throws Exception
    {
        Properties props = createProperties(false);
        provider = new PostgresPlusFileSystemFreeSpaceProvider(props);

        final long postgresFreeSpace = 1000L;
        prepareFreeSpaceExpectations(postgresFreeSpace);
        
        File workDir = new File(".");
        HostAwareFile file = new HostAwareFile(workDir);
        long fsFreeSpace = FileSystemUtils.freeSpaceKb(workDir.getAbsolutePath());
        long totalFreeSpace = provider.freeSpaceKb(file);
        
        assertEquals(fsFreeSpace + postgresFreeSpace, totalFreeSpace);
    }

    @Test
    public void testWithVacuum() throws Exception
    {
        Properties props = createProperties(true);
        provider = new PostgresPlusFileSystemFreeSpaceProvider(props);

        final long postgresFreeSpace = 1000L;
        prepareVacuumExpectations();
        prepareFreeSpaceExpectations(postgresFreeSpace);

        File workDir = new File(".");
        HostAwareFile file = new HostAwareFile(workDir);
        long fsFreeSpace = FileSystemUtils.freeSpaceKb(workDir.getAbsolutePath());
        long totalFreeSpace = provider.freeSpaceKb(file);

        assertEquals(fsFreeSpace + postgresFreeSpace, totalFreeSpace);
    }

    private void prepareVacuumExpectations() throws Exception
    {
        context.checking(new Expectations()
            {
                {
                    Statement statement = context.mock(Statement.class, "vacuumStatement");
                    one(connection).createStatement();
                    will(returnValue(statement));

                    one(statement).execute("VACUUM;");
                }
            });
    }

    private void prepareFreeSpaceExpectations(final long freeSpace) throws Exception
    {
        context.checking(new Expectations()
            {
                {
                    Statement statement = context.mock(Statement.class, "freeSpaceStatement");
                    one(connection).createStatement();
                    will(returnValue(statement));

                    one(statement).executeQuery(with(any(String.class)));

                    ResultSet rs = context.mock(ResultSet.class);
                    will(returnValue(rs));

                    one(rs).getLong(1);
                    will(returnValue(freeSpace));
                }
            });
    }

}
