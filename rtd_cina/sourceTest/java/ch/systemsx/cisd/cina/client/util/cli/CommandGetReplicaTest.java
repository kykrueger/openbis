/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.cina.client.util.cli;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.GregorianCalendar;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.cina.client.util.v1.ICinaUtilities;
import ch.systemsx.cisd.cina.shared.constants.CinaConstants;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.openbis.dss.client.api.cli.ICommand;
import ch.systemsx.cisd.openbis.dss.client.api.cli.ResultCode;
import ch.systemsx.cisd.openbis.dss.client.api.v1.IDataSetDss;
import ch.systemsx.cisd.openbis.dss.client.api.v1.IDssComponent;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.FileInfoDssBuilder;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.FileInfoDssDTO;
import ch.systemsx.cisd.openbis.generic.shared.IETLLIMSService;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.IGeneralInformationService;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSet;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSet.DataSetInitializer;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Sample.SampleInitializer;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria.MatchClause;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria.MatchClauseAttribute;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public class CommandGetReplicaTest extends AbstractFileSystemTestCase
{
    private final class MockCommandGetReplica extends CommandGetReplica
    {
        @Override
        protected ICinaUtilities login()
        {
            facade =
                    ch.systemsx.cisd.cina.client.util.v1.impl.CinaUtilitiesFacadeTest.createFacade(
                            service, openbisService, dssComponent, USER_ID, PASSWORD);
            return facade;
        }
    }

    private final static String USER_ID = "userid";

    private final static String PASSWORD = "password";

    private final static String SESSION_TOKEN = "sessionToken";

    private Mockery context;

    private ICinaUtilities facade;

    private IGeneralInformationService service;

    private IETLLIMSService openbisService;

    private IDssComponent dssComponent;

    private IDataSetDss dataSetDss;

    @Override
    @BeforeMethod
    public void setUp() throws IOException
    {
        super.setUp();
        context = new Mockery();
        service = context.mock(IGeneralInformationService.class);
        openbisService = context.mock(IETLLIMSService.class);
        dssComponent = context.mock(IDssComponent.class);
        dataSetDss = context.mock(IDataSetDss.class);
    }

    @AfterMethod
    public void tearDown()
    {
        // To following line of code should also be called at the end of each test method.
        // Otherwise one does not known which test failed.
        context.assertIsSatisfied();
    }

    private void setupAuthenticationExpectations()
    {
        context.checking(new Expectations()
            {
                {
                    one(service).tryToAuthenticateForAllServices(USER_ID, PASSWORD);
                    will(returnValue(SESSION_TOKEN));

                    one(service).getMinorVersion();
                    will(returnValue(1));

                    one(service).logout(SESSION_TOKEN);
                }
            });
    }

    private void setupListDataSetsExpectations(final String sampleCode)
    {
        context.checking(new Expectations()
            {
                {
                    SearchCriteria searchCriteria = new SearchCriteria();
                    searchCriteria.addMatchClause(MatchClause.createAttributeMatch(
                            MatchClauseAttribute.CODE, sampleCode));

                    ArrayList<Sample> samples = new ArrayList<Sample>();
                    SampleInitializer sampInitializer = new SampleInitializer();
                    sampInitializer.setCode(sampleCode);
                    sampInitializer.setId((long) 1);
                    sampInitializer.setIdentifier("SPACE/" + sampleCode);
                    sampInitializer.setPermId("PERM-ID");
                    sampInitializer.setSampleTypeCode("SAMPLE-TYPE");
                    sampInitializer.setSampleTypeId((long) 1);
                    samples.add(new Sample(sampInitializer));

                    one(service).searchForSamples(SESSION_TOKEN, searchCriteria);
                    will(returnValue(samples));

                    ArrayList<DataSet> dataSets = new ArrayList<DataSet>();
                    DataSetInitializer dsInitializer = new DataSetInitializer();
                    dsInitializer.setCode(sampleCode + "-RAW-IMAGES");
                    dsInitializer.setDataSetTypeCode(CinaConstants.RAW_IMAGES_DATA_SET_TYPE_CODE);
                    dsInitializer.setRegistrationDate(new GregorianCalendar(2010, 0, 1).getTime());
                    dataSets.add(new DataSet(dsInitializer));

                    dsInitializer = new DataSetInitializer();
                    dsInitializer.setCode(sampleCode + "-METADATA-OLD");
                    dsInitializer.setDataSetTypeCode(CinaConstants.METADATA_DATA_SET_TYPE_CODE);
                    dsInitializer.setRegistrationDate(new GregorianCalendar(2010, 0, 1).getTime());
                    dataSets.add(new DataSet(dsInitializer));

                    dsInitializer = new DataSetInitializer();
                    dsInitializer.setCode(sampleCode + "-METADATA-NEW");
                    dsInitializer.setDataSetTypeCode(CinaConstants.METADATA_DATA_SET_TYPE_CODE);
                    dsInitializer.setRegistrationDate(new GregorianCalendar(2010, 1, 1).getTime());
                    dataSets.add(new DataSet(dsInitializer));

                    one(service).listDataSets(SESSION_TOKEN, samples);
                    will(returnValue(dataSets));
                }
            });
    }

    private void setupDownloadDataSetExpectations(final String sampleCode) throws IOException
    {
        final File parent = new File("sourceTest/java/ch/systemsx/cisd/cina/client/util/cli/");
        ArrayList<FileInfoDssDTO> rawImagesInfos =
                getFileInfosForPath(new File(parent, "RawImages"));
        final FileInfoDssDTO[] rawImagesInfosArray =
                (rawImagesInfos.size() > 0) ? rawImagesInfos
                        .toArray(new FileInfoDssDTO[rawImagesInfos.size()]) : new FileInfoDssDTO[0];

        ArrayList<FileInfoDssDTO> metadataInfos = getFileInfosForPath(new File(parent, "Metadata"));

        final FileInfoDssDTO[] metadataInfosArray =
                (metadataInfos.size() > 0) ? metadataInfos.toArray(new FileInfoDssDTO[metadataInfos
                        .size()]) : new FileInfoDssDTO[0];

        context.checking(new Expectations()
            {
                {
                    one(dssComponent).getDataSet(sampleCode + "-RAW-IMAGES");
                    will(returnValue(dataSetDss));
                    one(dataSetDss).listFiles("/", true);
                    will(returnValue(rawImagesInfosArray));
                    one(dataSetDss).getFile("/ReplicaRawImages/Image.txt");
                    will(returnValue(new FileInputStream(new File(parent,
                            "RawImages/ReplicaRawImages/Image.txt"))));

                    one(dssComponent).getDataSet(sampleCode + "-METADATA-NEW");
                    will(returnValue(dataSetDss));
                    one(dataSetDss).listFiles("/", true);
                    will(returnValue(metadataInfosArray));
                    one(dataSetDss).getFile("/ReplicaMetadata/Metadata.txt");
                    will(returnValue(new FileInputStream(new File(parent,
                            "Metadata/ReplicaMetadata/Metadata.txt"))));
                }
            });
    }

    @SuppressWarnings("unchecked")
    private ArrayList<FileInfoDssDTO> getFileInfosForPath(File file) throws IOException
    {
        ArrayList<FileInfoDssDTO> fileInfos = new ArrayList<FileInfoDssDTO>();
        if (false == file.exists())
        {
            return fileInfos;
        }

        String path = file.getCanonicalPath();
        if (false == file.isDirectory())
        {
            path = file.getParentFile().getCanonicalPath();
        }

        FileInfoDssBuilder builder = new FileInfoDssBuilder(path, path);
        builder.appendFileInfosForFile(file, fileInfos, true);

        // Strip SVN stuff
        for (FileInfoDssDTO fileInfo : (ArrayList<FileInfoDssDTO>) fileInfos.clone())
        {
            if (fileInfo.getPathInDataSet().matches(".*/.svn/.*"))
                fileInfos.remove(fileInfo);
        }

        return fileInfos;
    }

    @Test
    public void testCodePath() throws IOException
    {
        setupAuthenticationExpectations();
        setupListDataSetsExpectations("REPLICA-ID");
        setupDownloadDataSetExpectations("REPLICA-ID");

        ICommand command = new MockCommandGetReplica();

        File outputFolder = new File(workingDirectory, "Foo.bundle/");

        ResultCode exitCode =
                command.execute(new String[]
                    { "-s", "url", "-u", USER_ID, "-p", PASSWORD, "-o", outputFolder.getPath(),
                            "REPLICA-ID" });

        assertEquals(ResultCode.OK, exitCode);

        String[] bundleContents = outputFolder.list();
        Arrays.sort(bundleContents);
        assertEquals(2, bundleContents.length);
        assertEquals("Annotations", bundleContents[0]);
        assertEquals("RawData", bundleContents[1]);

        context.assertIsSatisfied();
    }

    @Test
    public void testMultipleReplicas() throws IOException
    {
        setupAuthenticationExpectations();

        setupListDataSetsExpectations("REPLICA-ID1");
        setupListDataSetsExpectations("REPLICA-ID2");

        setupDownloadDataSetExpectations("REPLICA-ID1");
        setupDownloadDataSetExpectations("REPLICA-ID2");

        ICommand command = new MockCommandGetReplica();

        File outputFolder = new File(workingDirectory, "Foo.bundle/");

        ResultCode exitCode =
                command.execute(new String[]
                    { "-s", "url", "-u", USER_ID, "-p", PASSWORD, "-o", outputFolder.getPath(),
                            "REPLICA-ID1", "REPLICA-ID2" });

        assertEquals(ResultCode.OK, exitCode);
        context.assertIsSatisfied();
    }

    @Test
    public void testOldVersion()
    {
        context.checking(new Expectations()
            {
                {
                    one(service).tryToAuthenticateForAllServices(USER_ID, PASSWORD);
                    will(returnValue(SESSION_TOKEN));

                    // The service used wasn't available in version 0
                    one(service).getMinorVersion();
                    will(returnValue(0));

                    one(service).logout(SESSION_TOKEN);
                }
            });

        ICommand command = new MockCommandGetReplica();

        try
        {
            command.execute(new String[]
                { "-s", "url", "-u", USER_ID, "-p", PASSWORD, "REPLICA-ID" });
            fail("Command should throw an exception when run against an older version of the interface.");
        } catch (EnvironmentFailureException e)
        {
            assertEquals("Server does not support this feature.", e.getMessage());
        }
    }
}
