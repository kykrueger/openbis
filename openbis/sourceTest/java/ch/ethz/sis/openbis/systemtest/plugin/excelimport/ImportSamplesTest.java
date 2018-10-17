package ch.ethz.sis.openbis.systemtest.plugin.excelimport;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.Sample;
import ch.ethz.sis.openbis.generic.server.asapi.v3.IApplicationServerInternalApi;
import ch.systemsx.cisd.common.exceptions.UserFailureException;

@ContextConfiguration(locations = "classpath:applicationContext.xml")
@TransactionConfiguration(transactionManager = "transaction-manager")
public class ImportSamplesTest extends AbstractImportTest
{
    @Autowired
    private IApplicationServerInternalApi v3api;

    private static final String TEST_USER = "test";

    private static final String PASSWORD = "password";

    private static final String SAMPLES_XLS = "samples/all_in.xls";

    private static final String SAMPLES_SPACE_ELSEWHERE = "samples/space_elsewhere.xls";

    private static final String SAMPLES_SAMPLE_TYPE_ELSWHERE = "samples/sample_type_elsewhere.xls";

    private static final String SAMPLES_SPACE_PROJECT_EXPERIMENT_ELSEWHERE = "samples/space_project_experiment_elsewhere.xls";

    private static final String SPACE = "samples/space.xls";

    private static final String SAMPLE_TYPE = "samples/sample_type.xls";

    private static final String VOCABULARY_TYPE = "samples/vocab_type.xls";

    private static final String EXPERIMENT = "samples/experiment.xls";

    private static final String EXPERIMENT_TYPE = "samples/experiment_type.xls";

    private static final String PROJECT = "samples/project.xls";

    private static String FILES_DIR;

    private String sessionToken;

    @BeforeClass
    public void setupClass() throws IOException
    {
        String testDir = Paths.get(this.getClass().getResource(this.getClass().getSimpleName() + ".class").getPath())
                .getParent().toString();
        FILES_DIR = FilenameUtils.concat(testDir, "test_files");
    }

    @BeforeMethod
    public void beforeTest()
    {
        sessionToken = v3api.login(TEST_USER, PASSWORD);
    }

    @Test
    @DirtiesContext
    public void testSamplesAreCreated() throws IOException
    {
        // GIVEN
        TestUtils.createFrom(v3api, sessionToken, Paths.get(FilenameUtils.concat(FILES_DIR, SAMPLES_XLS)));
        // WHEN
        Sample sample = TestUtils.getSample(v3api, sessionToken, "AAA", "TEST_SPACE");
        // THEN
        assertEquals(sample.getCode(), "AAA");
        assertEquals(sample.getProject(), null);
        assertEquals(sample.getExperiment().getCode(), "TEST_EXPERIMENT2");
        assertEquals(sample.getSpace().getCode(), "TEST_SPACE");
    }

    @Test
    @DirtiesContext
    public void testSamplesAreCreatedSecondSample() throws IOException
    {
        // GIVEN
        TestUtils.createFrom(v3api, sessionToken, Paths.get(FilenameUtils.concat(FILES_DIR, SAMPLES_XLS)));
        // WHEN
        Sample sample = TestUtils.getSample(v3api, sessionToken, "VVV", "TEST_SPACE");
        // THEN
        assertEquals(sample.getCode(), "VVV");
        assertEquals(sample.getProject(), null);
        assertEquals(sample.getExperiment().getCode(), "TEST_EXPERIMENT");
        assertEquals(sample.getSpace().getCode(), "TEST_SPACE");
    }

    @Test
    @DirtiesContext
    public void testSamplesAreCreatedThirdSample() throws IOException
    {
        // GIVEN
        TestUtils.createFrom(v3api, sessionToken, Paths.get(FilenameUtils.concat(FILES_DIR, SAMPLES_XLS)));
        // WHEN
        Sample sample = TestUtils.getSample(v3api, sessionToken, "S1", "TEST_SPACE");
        // THEN
        assertEquals(sample.getCode(), "S1");
        assertEquals(sample.getProject(), null);
        assertEquals(sample.getExperiment().getCode(), "TEST_EXPERIMENT");
        assertEquals(sample.getSpace().getCode(), "TEST_SPACE");
    }

    @Test
    @DirtiesContext
    public void testSamplesAreCreatedWhenSpaceOnServer() throws IOException
    {
        // GIVEN
        TestUtils.createFrom(v3api, sessionToken, Paths.get(FilenameUtils.concat(FILES_DIR, SPACE)));
        TestUtils.createFrom(v3api, sessionToken, Paths.get(FilenameUtils.concat(FILES_DIR, SAMPLES_SPACE_ELSEWHERE)));
        // WHEN
        Sample sample = TestUtils.getSample(v3api, sessionToken, "VVV", "TEST_SPACE");
        // THEN
        assertNotNull(sample);
    }

    @Test
    @DirtiesContext
    public void testSamplesAreCreatedWhenSpaceInSeparateXls() throws IOException
    {
        // GIVEN
        TestUtils.createFrom(v3api, sessionToken,
                Paths.get(FilenameUtils.concat(FILES_DIR, SAMPLES_SPACE_ELSEWHERE)),
                Paths.get(FilenameUtils.concat(FILES_DIR, SPACE)));
        // WHEN
        Sample sample = TestUtils.getSample(v3api, sessionToken, "VVV", "TEST_SPACE");
        // THEN
        assertNotNull(sample);
    }

    @Test(expectedExceptions = UserFailureException.class)
    public void shouldThrowExceptionIfSpaceDoesntExist() throws IOException
    {
        TestUtils.createFrom(v3api, sessionToken, Paths.get(FilenameUtils.concat(FILES_DIR, SAMPLES_SPACE_ELSEWHERE)));
    }

    @Test
    @DirtiesContext
    public void testSamplesAreCreatedWhenSampleTypeOnServer() throws IOException
    {
        // GIVEN
        TestUtils.createFrom(v3api, sessionToken,
                Paths.get(FilenameUtils.concat(FILES_DIR, VOCABULARY_TYPE)),
                Paths.get(FilenameUtils.concat(FILES_DIR, SAMPLE_TYPE)));
        TestUtils.createFrom(v3api, sessionToken, Paths.get(FilenameUtils.concat(FILES_DIR, SAMPLES_SAMPLE_TYPE_ELSWHERE)));
        // WHEN
        Sample sample = TestUtils.getSample(v3api, sessionToken, "VVV", "TEST_SPACE");
        // THEN
        assertNotNull(sample);
    }

    @Test
    @DirtiesContext
    public void testSamplesAreCreatedWhenSampleTypeInSeparateXls() throws IOException
    {
        // GIVEN
        TestUtils.createFrom(v3api, sessionToken,
                Paths.get(FilenameUtils.concat(FILES_DIR, SAMPLES_SAMPLE_TYPE_ELSWHERE)),
                Paths.get(FilenameUtils.concat(FILES_DIR, SAMPLE_TYPE)));
        // WHEN
        Sample sample = TestUtils.getSample(v3api, sessionToken, "VVV", "TEST_SPACE");
        // THEN
        assertNotNull(sample);
    }

    @Test(expectedExceptions = UserFailureException.class)
    public void shouldThrowExceptionIfSamplesSpaceProjectDoesntExist() throws IOException
    {
        TestUtils.createFrom(v3api, sessionToken, Paths.get(FilenameUtils.concat(FILES_DIR, SAMPLES_SPACE_PROJECT_EXPERIMENT_ELSEWHERE)));
    }

    @Test
    @DirtiesContext
    public void testSamplesAreCreatedWhenSamplesSpaceProjectTypeOnServer() throws IOException
    {
        // GIVEN
        TestUtils.createFrom(v3api, sessionToken, Paths.get(FilenameUtils.concat(FILES_DIR, SPACE)));
        TestUtils.createFrom(v3api, sessionToken, Paths.get(FilenameUtils.concat(FILES_DIR, PROJECT)));
        TestUtils.createFrom(v3api, sessionToken, Paths.get(FilenameUtils.concat(FILES_DIR, EXPERIMENT_TYPE)));
        TestUtils.createFrom(v3api, sessionToken, Paths.get(FilenameUtils.concat(FILES_DIR, EXPERIMENT)));
        try
        {
            Thread.sleep(2000);
        } catch (InterruptedException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        TestUtils.createFrom(v3api, sessionToken, Paths.get(FilenameUtils.concat(FILES_DIR, SAMPLES_SPACE_PROJECT_EXPERIMENT_ELSEWHERE)));
        // WHEN
        Sample sample = TestUtils.getSample(v3api, sessionToken, "VVV", "TEST_SPACE");
        // THEN
        assertNotNull(sample);
    }

    @Test
    @DirtiesContext
    public void testSamplesAreCreatedWhenSamplesSpaceProjectInSeparateXls() throws IOException
    {
        // GIVEN
        Path space = Paths.get(FilenameUtils.concat(FILES_DIR, SPACE));
        Path project = Paths.get(FilenameUtils.concat(FILES_DIR, PROJECT));
        Path experiment = Paths.get(FilenameUtils.concat(FILES_DIR, EXPERIMENT));
        Path samples = Paths.get(FilenameUtils.concat(FILES_DIR, SAMPLES_SPACE_PROJECT_EXPERIMENT_ELSEWHERE));
        TestUtils.createFrom(v3api, sessionToken, space, experiment, samples, project);
        // WHEN
        Sample sample = TestUtils.getSample(v3api, sessionToken, "VVV", "TEST_SPACE");
        // THEN
        assertNotNull(sample);
    }

}
