/*
 * Copyright 2017 ETH Zuerich, SIS
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

package ch.ethz.sis.openbis.generic.dss.systemtest.api.v3;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.MultiPartContentProvider;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSet;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.fetchoptions.DataSetFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.DataSetPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.IDataSetId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.EntityTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentIdentifier;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SampleIdentifier;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SamplePermId;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.dataset.create.UploadedDataSetCreation;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.DataSetFile;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.download.DataSetFileDownload;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.download.DataSetFileDownloadOptions;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.download.DataSetFileDownloadReader;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.id.DataSetFilePermId;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.id.IDataSetFileId;
import ch.ethz.sis.openbis.generic.server.dssapi.v3.upload.StoreShareFileUploadServlet;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.http.JettyHttpClientFactory;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;
import ch.systemsx.cisd.openbis.generic.shared.util.TestInstanceHostUtils;
import ch.systemsx.cisd.openbis.systemtest.authorization.ProjectAuthorizationUser;

/**
 * @author Ganime Betul Akin
 */
public class CreateUploadedDataSetsTest extends AbstractFileTest
{
    private static final String SERVICE_URL = TestInstanceHostUtils.getDSSUrl() + "/datastore_server/store_share_file_upload";

    private IApplicationServerApi as;

    @Override
    @BeforeClass
    protected void beforeClass() throws Exception
    {
        super.beforeClass();
        as = ServiceProvider.getV3ApplicationService();
    }

    @Test
    public void testUploadWithInvalidSession() throws Exception
    {
        ContentResponse response =
                uploadFiles("admin-180211214633760xF769DD44CAFFAF7B50FBEADF00DBEE1F", UUID.randomUUID().toString(), "UNKNOWN", true,
                        new FileToUpload());
        assertResponseError(response);
        assertTrue(response.getContentAsString(), response.getContentAsString().contains("user is not logged in"));
    }

    @Test
    public void testUploadWithoutSession() throws Exception
    {
        ContentResponse response = uploadFiles(null, UUID.randomUUID().toString(), "UNKNOWN", true, new FileToUpload());
        assertResponseError(response);
        assertTrue(response.getContentAsString(), response.getContentAsString().contains("Session token cannot be null or empty"));
    }

    @Test
    public void testUploadWithoutType() throws Exception
    {
        String sessionToken = as.login(TEST_USER, PASSWORD);

        ContentResponse response = uploadFiles(sessionToken, UUID.randomUUID().toString(), null, true, new FileToUpload());
        assertResponseError(response);
        assertTrue(response.getContentAsString(), response.getContentAsString().contains("Data set type cannot be null or empty"));
    }

    @Test
    public void testUploadWithoutUploadId() throws Exception
    {
        String sessionToken = as.login(TEST_USER, PASSWORD);

        ContentResponse response = uploadFiles(sessionToken, null, "UNKNOWN", true, new FileToUpload());
        assertResponseError(response);
        assertTrue(response.getContentAsString(), response.getContentAsString().contains("Upload id cannot be null or empty"));
    }

    @Test
    public void testUploadWithUploadIdThatContainsSlash() throws Exception
    {
        String sessionToken = as.login(TEST_USER, PASSWORD);

        ContentResponse response = uploadFiles(sessionToken, "iam/incorrect", "UNKNOWN", true, new FileToUpload());
        assertResponseError(response);
        assertTrue(response.getContentAsString(), response.getContentAsString().contains("Upload id must not contain &apos;/&apos;"));
    }

    @Test
    public void testUploadWithoutFile() throws Exception
    {
        String sessionToken = as.login(TEST_USER, PASSWORD);

        ContentResponse response = uploadFiles(sessionToken, UUID.randomUUID().toString(), "UNKNOWN", true);
        assertResponseError(response);
        assertTrue(response.getContentAsString(), response.getContentAsString().contains("Please upload at least one file"));
    }

    @Test
    public void testUploadWithFileWithFolderUpPath() throws Exception
    {
        String sessionToken = as.login(TEST_USER, PASSWORD);

        ContentResponse response =
                uploadFiles(sessionToken, UUID.randomUUID().toString(), "UNKNOWN", false, new FileToUpload("name", "iam/../incorrect", "content"));
        assertResponseError(response);
        assertTrue(response.getContentAsString(), response.getContentAsString().contains("File path must not contain &apos;../&apos;"));
    }

    @Test
    public void testUploadWithFileWithoutPath() throws Exception
    {
        String sessionToken = as.login(TEST_USER, PASSWORD);

        ContentResponse response =
                uploadFiles(sessionToken, UUID.randomUUID().toString(), "UNKNOWN", false, new FileToUpload("name", null, "content"));
        assertResponseError(response);
        assertTrue(response.getContentAsString(), response.getContentAsString().contains("File path cannot be null or empty"));
    }

    @Test
    public void testCreateWithInvalidSession() throws Exception
    {
        try
        {
            getCreateAndGetDataSet("admin-180211214633760xF769DD44CAFFAF7B50FBEADF00DBEE1F", null);
        } catch (UserFailureException e)
        {
            assertTrue(e.getMessage(), e.getMessage().contains("user is not logged in"));
        }
    }

    @Test
    public void testCreateWithoutType() throws Exception
    {
        String sessionToken = as.login(TEST_USER, PASSWORD);

        String uploadId = UUID.randomUUID().toString();
        String dataSetType = "UNKNOWN";
        String sampleIdentifier = "/CISD/CP-TEST-1";

        UploadedDataSetCreation creation = new UploadedDataSetCreation();
        creation.setSampleId(new SampleIdentifier(sampleIdentifier));
        creation.setUploadId(uploadId);

        ContentResponse response = uploadFiles(sessionToken, uploadId, dataSetType, true, new FileToUpload());
        assertResponseOK(response);

        try
        {
            getCreateAndGetDataSet(sessionToken, creation);
            fail();
        } catch (UserFailureException e)
        {
            assertTrue(e.getMessage(), e.getMessage().contains("A dataset needs a type"));
        }
    }

    @Test
    public void testCreateWithTypeNonexistent() throws Exception
    {
        String sessionToken = as.login(TEST_USER, PASSWORD);

        String uploadId = UUID.randomUUID().toString();
        String dataSetType = "IDONTEXIST";
        String sampleIdentifier = "/CISD/CP-TEST-1";

        UploadedDataSetCreation creation = new UploadedDataSetCreation();
        creation.setTypeId(new EntityTypePermId(dataSetType));
        creation.setSampleId(new SampleIdentifier(sampleIdentifier));
        creation.setUploadId(uploadId);

        ContentResponse response = uploadFiles(sessionToken, uploadId, dataSetType, true, new FileToUpload());
        assertResponseOK(response);

        try
        {
            getCreateAndGetDataSet(sessionToken, creation);
            fail();
        } catch (UserFailureException e)
        {
            assertTrue(e.getMessage(), e.getMessage().contains("Object with EntityTypePermId = [IDONTEXIST, null] has not been found"));
        }
    }

    @Test
    public void testCreateWithoutOwner() throws Exception
    {
        String sessionToken = as.login(TEST_USER, PASSWORD);

        String uploadId = UUID.randomUUID().toString();
        String dataSetType = "UNKNOWN";

        UploadedDataSetCreation creation = new UploadedDataSetCreation();
        creation.setTypeId(new EntityTypePermId(dataSetType));
        creation.setUploadId(uploadId);

        ContentResponse response = uploadFiles(sessionToken, uploadId, dataSetType, true, new FileToUpload());
        assertResponseOK(response);

        try
        {
            getCreateAndGetDataSet(sessionToken, creation);
            fail();
        } catch (UserFailureException e)
        {
            assertTrue(e.getMessage(), e.getMessage().contains("A dataset needs either a sample or an experiment as an owner"));
        }
    }

    @Test
    public void testCreateWithSampleNonexistent() throws Exception
    {
        String sessionToken = as.login(TEST_USER, PASSWORD);

        String uploadId = UUID.randomUUID().toString();
        String dataSetType = "UNKNOWN";
        String sampleIdentifier = "/IDONTEXIST/IDONTEXIST";

        UploadedDataSetCreation creation = new UploadedDataSetCreation();
        creation.setTypeId(new EntityTypePermId(dataSetType));
        creation.setSampleId(new SampleIdentifier(sampleIdentifier));
        creation.setUploadId(uploadId);

        ContentResponse response = uploadFiles(sessionToken, uploadId, dataSetType, true, new FileToUpload());
        assertResponseOK(response);

        try
        {
            getCreateAndGetDataSet(sessionToken, creation);
            fail();
        } catch (UserFailureException e)
        {
            assertTrue(e.getMessage(), e.getMessage().contains("Object with SampleIdentifier = [/IDONTEXIST/IDONTEXIST] has not been found"));
        }
    }

    @Test
    public void testCreateWithSampleIdentifier() throws Exception
    {
        String sessionToken = as.login(TEST_USER, PASSWORD);

        String uploadId = UUID.randomUUID().toString();
        String dataSetType = "UNKNOWN";
        String sampleIdentifier = "/CISD/CP-TEST-1";

        UploadedDataSetCreation creation = new UploadedDataSetCreation();
        creation.setTypeId(new EntityTypePermId(dataSetType));
        creation.setSampleId(new SampleIdentifier(sampleIdentifier));
        creation.setUploadId(uploadId);

        ContentResponse response = uploadFiles(sessionToken, uploadId, dataSetType, true, new FileToUpload());
        assertResponseOK(response);

        DataSet dataSet = getCreateAndGetDataSet(sessionToken, creation);

        assertEquals(dataSetType, dataSet.getType().getCode());
        assertEquals(sampleIdentifier, dataSet.getSample().getIdentifier().getIdentifier());
    }

    @Test
    public void testCreateWithSamplePermId() throws Exception
    {
        String sessionToken = as.login(TEST_USER, PASSWORD);

        String uploadId = UUID.randomUUID().toString();
        String dataSetType = "UNKNOWN";
        String samplePermId = "200902091219327-1025";

        UploadedDataSetCreation creation = new UploadedDataSetCreation();
        creation.setTypeId(new EntityTypePermId(dataSetType));
        creation.setSampleId(new SamplePermId(samplePermId));
        creation.setUploadId(uploadId);

        ContentResponse response = uploadFiles(sessionToken, uploadId, dataSetType, true, new FileToUpload());
        assertResponseOK(response);

        DataSet dataSet = getCreateAndGetDataSet(sessionToken, creation);

        assertEquals(dataSetType, dataSet.getType().getCode());
        assertEquals(samplePermId, dataSet.getSample().getPermId().getPermId());
    }

    @Test
    public void testCreateWithExperimentNonexistent() throws Exception
    {
        String sessionToken = as.login(TEST_USER, PASSWORD);

        String uploadId = UUID.randomUUID().toString();
        String dataSetType = "UNKNOWN";
        String experimentIdentifier = "/IDONTEXIST/IDONTEXIST/IDONTEXIST";

        UploadedDataSetCreation creation = new UploadedDataSetCreation();
        creation.setTypeId(new EntityTypePermId(dataSetType));
        creation.setExperimentId(new ExperimentIdentifier(experimentIdentifier));
        creation.setUploadId(uploadId);

        ContentResponse response = uploadFiles(sessionToken, uploadId, dataSetType, true, new FileToUpload());
        assertResponseOK(response);

        try
        {
            getCreateAndGetDataSet(sessionToken, creation);
            fail();
        } catch (UserFailureException e)
        {
            assertTrue(e.getMessage(),
                    e.getMessage().contains("Object with ExperimentIdentifier = [/IDONTEXIST/IDONTEXIST/IDONTEXIST] has not been found"));
        }
    }

    @Test
    public void testCreateWithExperimentIdentifier() throws Exception
    {
        String sessionToken = as.login(TEST_USER, PASSWORD);

        String uploadId = UUID.randomUUID().toString();
        String dataSetType = "UNKNOWN";
        String experimentIdentifier = "/CISD/NEMO/EXP1";

        UploadedDataSetCreation creation = new UploadedDataSetCreation();
        creation.setTypeId(new EntityTypePermId(dataSetType));
        creation.setExperimentId(new ExperimentIdentifier(experimentIdentifier));
        creation.setUploadId(uploadId);

        ContentResponse response = uploadFiles(sessionToken, uploadId, dataSetType, true, new FileToUpload());
        assertResponseOK(response);

        DataSet dataSet = getCreateAndGetDataSet(sessionToken, creation);

        assertEquals(dataSetType, dataSet.getType().getCode());
        assertEquals(experimentIdentifier, dataSet.getExperiment().getIdentifier().getIdentifier());
    }

    @Test
    public void testCreateWithExperimentPermId() throws Exception
    {
        String sessionToken = as.login(TEST_USER, PASSWORD);

        String uploadId = UUID.randomUUID().toString();
        String dataSetType = "UNKNOWN";
        String experimentPermId = "200811050951882-1028";

        UploadedDataSetCreation creation = new UploadedDataSetCreation();
        creation.setTypeId(new EntityTypePermId(dataSetType));
        creation.setExperimentId(new ExperimentPermId(experimentPermId));
        creation.setUploadId(uploadId);

        ContentResponse response = uploadFiles(sessionToken, uploadId, dataSetType, true, new FileToUpload());
        assertResponseOK(response);

        DataSet dataSet = getCreateAndGetDataSet(sessionToken, creation);

        assertEquals(dataSetType, dataSet.getType().getCode());
        assertEquals(experimentPermId, dataSet.getExperiment().getPermId().getPermId());
    }

    @Test
    public void testCreateWithProperties() throws Exception
    {
        String sessionToken = as.login(TEST_USER, PASSWORD);

        String uploadId = UUID.randomUUID().toString();
        String dataSetType = "HCS_IMAGE";
        String experimentIdentifier = "/CISD/NEMO/EXP1";

        Map<String, String> properties = new HashMap<String, String>();
        properties.put("COMMENT", "test comment");

        UploadedDataSetCreation creation = new UploadedDataSetCreation();
        creation.setTypeId(new EntityTypePermId(dataSetType));
        creation.setExperimentId(new ExperimentIdentifier(experimentIdentifier));
        creation.setUploadId(uploadId);
        creation.setProperties(properties);

        ContentResponse response = uploadFiles(sessionToken, uploadId, dataSetType, true, new FileToUpload());
        assertResponseOK(response);

        DataSet dataSet = getCreateAndGetDataSet(sessionToken, creation);

        assertEquals(dataSetType, dataSet.getType().getCode());
        assertEquals(experimentIdentifier, dataSet.getExperiment().getIdentifier().getIdentifier());
        assertEquals("test comment", dataSet.getProperty("COMMENT"));
    }

    @Test
    public void testCreateWithParentIds() throws Exception
    {
        String sessionToken = as.login(TEST_USER, PASSWORD);

        String uploadId = UUID.randomUUID().toString();
        String dataSetType = "UNKNOWN";
        String experimentIdentifier = "/CISD/NEMO/EXP1";
        IDataSetId parentId = new DataSetPermId("20081105092159111-1");

        UploadedDataSetCreation creation = new UploadedDataSetCreation();
        creation.setTypeId(new EntityTypePermId(dataSetType));
        creation.setExperimentId(new ExperimentIdentifier(experimentIdentifier));
        creation.setParentIds(Arrays.asList(parentId));
        creation.setUploadId(uploadId);

        ContentResponse response = uploadFiles(sessionToken, uploadId, dataSetType, true, new FileToUpload());
        assertResponseOK(response);

        DataSet dataSet = getCreateAndGetDataSet(sessionToken, creation);

        assertEquals(dataSetType, dataSet.getType().getCode());
        assertEquals(experimentIdentifier, dataSet.getExperiment().getIdentifier().getIdentifier());
        assertEquals(1, dataSet.getParents().size());
        assertEquals(parentId, dataSet.getParents().get(0).getPermId());
    }

    @Test
    public void testCreateWithoutUploadId() throws Exception
    {
        String sessionToken = as.login(TEST_USER, PASSWORD);

        String dataSetType = "UNKNOWN";
        String experimentIdentifier = "/CISD/NEMO/EXP1";

        UploadedDataSetCreation creation = new UploadedDataSetCreation();
        creation.setTypeId(new EntityTypePermId(dataSetType));
        creation.setExperimentId(new ExperimentIdentifier(experimentIdentifier));

        try
        {
            getCreateAndGetDataSet(sessionToken, creation);
            fail();
        } catch (UserFailureException e)
        {
            assertTrue(e.getMessage(), e.getMessage().contains("Upload id cannot be null"));
        }
    }

    @Test
    public void testCreateWithUploadIdThatContainsSlash() throws Exception
    {
        String sessionToken = as.login(TEST_USER, PASSWORD);

        String dataSetType = "UNKNOWN";
        String experimentIdentifier = "/CISD/NEMO/EXP1";

        UploadedDataSetCreation creation = new UploadedDataSetCreation();
        creation.setTypeId(new EntityTypePermId(dataSetType));
        creation.setExperimentId(new ExperimentIdentifier(experimentIdentifier));
        creation.setUploadId("iam/incorrect");

        try
        {
            getCreateAndGetDataSet(sessionToken, creation);
            fail();
        } catch (UserFailureException e)
        {
            assertTrue(e.getMessage(), e.getMessage().contains("Upload id must not contain '/'"));
        }
    }

    @Test
    public void testCreateWithoutUpload() throws Exception
    {
        String sessionToken = as.login(TEST_USER, PASSWORD);

        String uploadId = UUID.randomUUID().toString();
        String dataSetType = "UNKNOWN";
        String experimentIdentifier = "/CISD/NEMO/EXP1";

        UploadedDataSetCreation creation = new UploadedDataSetCreation();
        creation.setTypeId(new EntityTypePermId(dataSetType));
        creation.setExperimentId(new ExperimentIdentifier(experimentIdentifier));
        creation.setUploadId(uploadId);

        try
        {
            getCreateAndGetDataSet(sessionToken, creation);
            fail();
        } catch (UserFailureException e)
        {
            assertTrue(e.getMessage(),
                    e.getMessage()
                            .contains("The folder for the upload id " + uploadId + " could not be found. Have you uploaded the file(s) first?"));
        }
    }

    @Test
    public void testCreateWithSingleFile() throws Exception
    {
        String sessionToken = as.login(TEST_USER, PASSWORD);

        String uploadId = UUID.randomUUID().toString();
        String dataSetType = "UNKNOWN";
        String experimentIdentifier = "/CISD/NEMO/EXP1";

        FileToUpload file = new FileToUpload("file", "test.txt", "test content");

        UploadedDataSetCreation creation = new UploadedDataSetCreation();
        creation.setTypeId(new EntityTypePermId(dataSetType));
        creation.setExperimentId(new ExperimentIdentifier(experimentIdentifier));
        creation.setUploadId(uploadId);

        ContentResponse response = uploadFiles(sessionToken, uploadId, dataSetType, false, file);
        assertResponseOK(response);

        DataSet dataSet = getCreateAndGetDataSet(sessionToken, creation);

        assertEquals(dataSetType, dataSet.getType().getCode());
        assertEquals(experimentIdentifier, dataSet.getExperiment().getIdentifier().getIdentifier());

        List<DownloadedFile> files = downloadFiles(sessionToken, dataSet.getPermId());
        assertEquals(3, files.size());

        assertDirectory(files.get(0), "");
        assertDirectory(files.get(1), "original");
        assertFile(files.get(2), "original/test.txt", file.content);
    }

    @Test
    public void testCreateWithMultipleFiles() throws Exception
    {
        String sessionToken = as.login(TEST_USER, PASSWORD);

        String uploadId = UUID.randomUUID().toString();
        String dataSetType = "UNKNOWN";
        String experimentIdentifier = "/CISD/NEMO/EXP1";

        FileToUpload file1 = new FileToUpload("file1", "test1.txt", "test1 content");
        FileToUpload file2 = new FileToUpload("file2", "test2.txt", "test2 content");
        FileToUpload file3 = new FileToUpload("file3", "folder/test3.txt", "test3 content");
        FileToUpload file4 = new FileToUpload("file4", "folder/test4.txt", "test4 content");

        UploadedDataSetCreation creation = new UploadedDataSetCreation();
        creation.setTypeId(new EntityTypePermId(dataSetType));
        creation.setExperimentId(new ExperimentIdentifier(experimentIdentifier));
        creation.setUploadId(uploadId);

        ContentResponse response = uploadFiles(sessionToken, uploadId, dataSetType, false, file1, file2, file3, file4);
        assertResponseOK(response);

        DataSet dataSet = getCreateAndGetDataSet(sessionToken, creation);

        assertEquals(dataSetType, dataSet.getType().getCode());
        assertEquals(experimentIdentifier, dataSet.getExperiment().getIdentifier().getIdentifier());

        List<DownloadedFile> files = downloadFiles(sessionToken, dataSet.getPermId());
        assertEquals(8, files.size());

        assertDirectory(files.get(0), "");
        assertDirectory(files.get(1), "original");
        assertDirectory(files.get(2), "original/upload");
        assertDirectory(files.get(3), "original/upload/folder");
        assertFile(files.get(4), "original/upload/folder/test3.txt", file3.content);
        assertFile(files.get(5), "original/upload/folder/test4.txt", file4.content);
        assertFile(files.get(6), "original/upload/test1.txt", file1.content);
        assertFile(files.get(7), "original/upload/test2.txt", file2.content);
    }

    @Test
    public void testCreateWithIgnoreFilePathDefault() throws Exception
    {
        String sessionToken = as.login(TEST_USER, PASSWORD);

        String uploadId = UUID.randomUUID().toString();
        String dataSetType = "UNKNOWN";
        String experimentIdentifier = "/CISD/NEMO/EXP1";

        FileToUpload file1 = new FileToUpload("file1", "test1.txt", "test1 content");
        FileToUpload file2 = new FileToUpload("file2", "folder/test2.txt", "test2 content");

        UploadedDataSetCreation creation = new UploadedDataSetCreation();
        creation.setTypeId(new EntityTypePermId(dataSetType));
        creation.setExperimentId(new ExperimentIdentifier(experimentIdentifier));
        creation.setUploadId(uploadId);

        ContentResponse response = uploadFiles(sessionToken, uploadId, dataSetType, null, file1, file2);
        assertResponseOK(response);

        DataSet dataSet = getCreateAndGetDataSet(sessionToken, creation);

        assertEquals(dataSetType, dataSet.getType().getCode());
        assertEquals(experimentIdentifier, dataSet.getExperiment().getIdentifier().getIdentifier());

        List<DownloadedFile> files = downloadFiles(sessionToken, dataSet.getPermId());
        assertEquals(5, files.size());

        assertDirectory(files.get(0), "");
        assertDirectory(files.get(1), "original");
        assertDirectory(files.get(2), "original/upload");
        assertFile(files.get(3), "original/upload/test1.txt", file1.content);
        assertFile(files.get(4), "original/upload/test2.txt", file2.content);
    }

    @Test
    public void testCreateWithIgnoreFilePathSetToFalse() throws Exception
    {
        String sessionToken = as.login(TEST_USER, PASSWORD);

        String uploadId = UUID.randomUUID().toString();
        String dataSetType = "UNKNOWN";
        String experimentIdentifier = "/CISD/NEMO/EXP1";

        FileToUpload file1 = new FileToUpload("file1", "test1.txt", "test1 content");
        FileToUpload file2 = new FileToUpload("file2", "folder/test2.txt", "test2 content");

        UploadedDataSetCreation creation = new UploadedDataSetCreation();
        creation.setTypeId(new EntityTypePermId(dataSetType));
        creation.setExperimentId(new ExperimentIdentifier(experimentIdentifier));
        creation.setUploadId(uploadId);

        ContentResponse response = uploadFiles(sessionToken, uploadId, dataSetType, false, file1, file2);
        assertResponseOK(response);

        DataSet dataSet = getCreateAndGetDataSet(sessionToken, creation);

        assertEquals(dataSetType, dataSet.getType().getCode());
        assertEquals(experimentIdentifier, dataSet.getExperiment().getIdentifier().getIdentifier());

        List<DownloadedFile> files = downloadFiles(sessionToken, dataSet.getPermId());
        assertEquals(6, files.size());

        assertDirectory(files.get(0), "");
        assertDirectory(files.get(1), "original");
        assertDirectory(files.get(2), "original/upload");
        assertDirectory(files.get(3), "original/upload/folder");
        assertFile(files.get(4), "original/upload/folder/test2.txt", file2.content);
        assertFile(files.get(5), "original/upload/test1.txt", file1.content);
    }

    @Test
    public void testCreateWithMultipleAttempts() throws Exception
    {
        String sessionToken = as.login(TEST_USER, PASSWORD);

        String uploadId = UUID.randomUUID().toString();
        String dataSetType = "UNKNOWN";
        String experimentIdentifier = "/CISD/NEMO/EXP1";

        Map<String, String> properties = new HashMap<String, String>();
        properties.put("IDONTEXIST", "test value");

        FileToUpload file = new FileToUpload("file", "test.txt", "test content");

        UploadedDataSetCreation creation = new UploadedDataSetCreation();
        creation.setTypeId(new EntityTypePermId(dataSetType));
        creation.setExperimentId(new ExperimentIdentifier(experimentIdentifier));
        creation.setProperties(properties);
        creation.setUploadId(uploadId);

        ContentResponse response = uploadFiles(sessionToken, uploadId, dataSetType, true, file);
        assertResponseOK(response);

        // first attempt
        try
        {
            getCreateAndGetDataSet(sessionToken, creation);
            fail("Creation should have failed because of nonexistent 'IDONTEXIST' property");
        } catch (Exception e)
        {
            String fullStackTrace = ExceptionUtils.getFullStackTrace(e);
            assertTrue(fullStackTrace, fullStackTrace.contains("Property type with code 'IDONTEXIST' does not exist!"));
        }

        // second attempt
        try
        {
            getCreateAndGetDataSet(sessionToken, creation);
            fail("Creation should have failed as uploaded file should have been removed during the first attempt");
        } catch (Exception e)
        {
            String fullStackTrace = ExceptionUtils.getFullStackTrace(e);
            assertTrue(fullStackTrace, fullStackTrace.contains("The folder for the upload id " + uploadId + " could not be found"));
        }
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER)
    public void testCreateWithProjectAuthorization(ProjectAuthorizationUser user) throws Exception
    {
        String sessionToken = as.login(user.getUserId(), PASSWORD);

        String uploadId = UUID.randomUUID().toString();
        String dataSetType = "UNKNOWN";
        String experimentIdentifier = "/TEST-SPACE/TEST-PROJECT/EXP-SPACE-TEST";

        FileToUpload file = new FileToUpload("file", "test.txt", "test content");

        UploadedDataSetCreation creation = new UploadedDataSetCreation();
        creation.setTypeId(new EntityTypePermId(dataSetType));
        creation.setExperimentId(new ExperimentIdentifier(experimentIdentifier));
        creation.setUploadId(uploadId);

        ContentResponse response = uploadFiles(sessionToken, uploadId, dataSetType, true, file);
        assertResponseOK(response);

        if (user.isDisabledProjectUser())
        {
            try
            {
                getCreateAndGetDataSet(sessionToken, creation);
            } catch (UserFailureException e)
            {
                assertTrue(e.getMessage(), e.getMessage().contains(
                        "None of method roles '[PROJECT_OBSERVER, PROJECT_USER, PROJECT_POWER_USER, PROJECT_ADMIN, SPACE_ADMIN, INSTANCE_ADMIN, SPACE_POWER_USER, SPACE_USER, SPACE_OBSERVER, INSTANCE_OBSERVER, SPACE_ETL_SERVER, INSTANCE_ETL_SERVER]' could be found in roles of user"));
            }
        } else if (user.isInstanceUserOrTestSpaceUserOrEnabledTestProjectUser())
        {
            DataSet dataSet = getCreateAndGetDataSet(sessionToken, creation);

            assertEquals(dataSetType, dataSet.getType().getCode());
            assertEquals(experimentIdentifier, dataSet.getExperiment().getIdentifier().getIdentifier());

            List<DownloadedFile> files = downloadFiles(sessionToken, dataSet.getPermId());
            assertEquals(3, files.size());

            assertDirectory(files.get(0), "");
            assertDirectory(files.get(1), "original");
            assertFile(files.get(2), "original/test.txt", file.content);
        } else
        {
            try
            {
                getCreateAndGetDataSet(sessionToken, creation);
            } catch (UserFailureException e)
            {
                assertTrue(e.getMessage(),
                        e.getMessage().contains("Object with ExperimentIdentifier = [/TEST-SPACE/TEST-PROJECT/EXP-SPACE-TEST] has not been found"));
            }
        }
    }

    private ContentResponse uploadFiles(String sessionToken, String uploadId, String dataSetType, Boolean ignoreFilePath,
            FileToUpload... filesToUpload)
            throws InterruptedException, TimeoutException, ExecutionException
    {
        HttpClient client = JettyHttpClientFactory.getHttpClient();
        MultiPartContentProvider multiPart = new MultiPartContentProvider();

        for (FileToUpload fileToUpload : filesToUpload)
        {
            multiPart.addFilePart(fileToUpload.fieldName, fileToUpload.fileName, new StringContentProvider(fileToUpload.content), null);
        }

        multiPart.close();

        Request request = client.newRequest(SERVICE_URL).method(HttpMethod.POST);

        if (sessionToken != null)
        {
            request.param(StoreShareFileUploadServlet.SESSION_ID_PARAM, sessionToken);
        }
        if (uploadId != null)
        {
            request.param(StoreShareFileUploadServlet.UPLOAD_ID_PARAM, uploadId);
        }
        if (ignoreFilePath != null)
        {
            request.param(StoreShareFileUploadServlet.IGNORE_FILE_PATH_PARAM, String.valueOf(ignoreFilePath));
        }
        if (dataSetType != null)
        {
            request.param(StoreShareFileUploadServlet.DATA_SET_TYPE_PARAM, dataSetType);
        }
        request.content(multiPart);
        return request.send();
    }

    private DataSet getCreateAndGetDataSet(String sessionToken, UploadedDataSetCreation creation)
    {
        DataSetPermId permId = dss.createUploadedDataSet(sessionToken, creation);
        assertNotNull(permId);

        DataSetFetchOptions fo = new DataSetFetchOptions();
        fo.withType();
        fo.withExperiment();
        fo.withSample();
        fo.withProperties();
        fo.withParents();

        Map<IDataSetId, DataSet> dataSets = as.getDataSets(sessionToken, Arrays.asList(permId), fo);
        DataSet dataSet = dataSets.get(permId);
        assertNotNull(dataSet);

        return dataSet;
    }

    private List<DownloadedFile> downloadFiles(String sessionToken, IDataSetId dataSetId) throws IOException
    {
        DataSetFileDownloadOptions options = new DataSetFileDownloadOptions();
        options.setRecursive(true);

        IDataSetFileId fileId = new DataSetFilePermId(dataSetId, "");

        InputStream stream = dss.downloadFiles(sessionToken, Arrays.asList(fileId), options);
        DataSetFileDownloadReader reader = new DataSetFileDownloadReader(stream);

        List<DownloadedFile> files = new ArrayList<DownloadedFile>();
        DataSetFileDownload download = null;

        while ((download = reader.read()) != null)
        {
            DownloadedFile file = new DownloadedFile(download.getDataSetFile(), IOUtils.toString(download.getInputStream()));
            files.add(file);
        }

        return files;
    }

    private void assertDirectory(DownloadedFile actual, String expectedPath)
    {
        assertEquals(expectedPath, actual.file.getPath());
        assertEquals(true, actual.file.isDirectory());
        assertEquals(0, actual.file.getFileLength());
        assertEquals("", actual.content);
    }

    private void assertFile(DownloadedFile actual, String expectedPath, String expectedFileContent)
    {
        assertEquals(expectedPath, actual.file.getPath());
        assertEquals(false, actual.file.isDirectory());
        assertEquals(expectedFileContent.length(), actual.file.getFileLength());
        assertEquals(expectedFileContent, actual.content);
    }

    private void assertResponseOK(ContentResponse response)
    {
        int statusCode = response.getStatus();
        if (statusCode != HttpStatus.Code.OK.getCode())
        {
            throw new RuntimeException("Status Code was " + statusCode + " instead of " + HttpStatus.Code.OK.getCode());
        }
    }

    private void assertResponseError(ContentResponse response)
    {
        int statusCode = response.getStatus();
        if (statusCode != HttpStatus.Code.INTERNAL_SERVER_ERROR.getCode())
        {
            throw new RuntimeException("Status Code was " + statusCode + " instead of " + HttpStatus.Code.INTERNAL_SERVER_ERROR.getCode());
        }
    }

    private class FileToUpload
    {
        private String fieldName;

        private String fileName;

        private String content;

        public FileToUpload()
        {
            this("testFieldName", "testFileName", "testContent");
        }

        public FileToUpload(String fieldName, String fileName, String content)
        {
            this.fieldName = fieldName;
            this.fileName = fileName;
            this.content = content;
        }
    }

    private class DownloadedFile
    {
        private DataSetFile file;

        private String content;

        public DownloadedFile(DataSetFile file, String content)
        {
            this.file = file;
            this.content = content;
        }

    }

}
