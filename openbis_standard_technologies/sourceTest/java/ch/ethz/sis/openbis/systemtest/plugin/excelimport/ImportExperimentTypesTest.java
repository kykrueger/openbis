package ch.ethz.sis.openbis.systemtest.plugin.excelimport;

import static org.testng.Assert.assertEquals;

import java.io.IOException;
import java.nio.file.Paths;

import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.ExperimentType;
import ch.ethz.sis.openbis.generic.server.asapi.v3.IApplicationServerInternalApi;
import ch.systemsx.cisd.common.exceptions.UserFailureException;

@ContextConfiguration(locations = "classpath:applicationContext.xml")
@Transactional(transactionManager = "transaction-manager")
@Rollback
public class ImportExperimentTypesTest extends AbstractImportTest
{
    @Autowired
    private IApplicationServerInternalApi v3api;

    private static final String TEST_USER = "test";

    private static final String PASSWORD = "password";

    private static final String EXPERIMENT_TYPES_XLS = "experiment_types/normal_experiment.xls";

    private static final String EXPERIMENT_NO_CODE = "experiment_types/no_code.xls";

    private static final String EXPERIMENT_WITH_VALIDATION_SCRIPT = "experiment_types/with_validation_script.xls";

    private static String FILES_DIR;

    private String sessionToken;

    @BeforeClass
    public void setupClass() throws IOException
    {
        String f = ImportExperimentTypesTest.class.getName().replace(".", "/");
        FILES_DIR = f.substring(0, f.length() - ImportExperimentTypesTest.class.getSimpleName().length()) + "/test_files/";
    }

    @BeforeMethod
    public void beforeTest()
    {
        sessionToken = v3api.login(TEST_USER, PASSWORD);
    }

    @Test
    @DirtiesContext
    public void testNormalExperimentTypesAreCreated() throws IOException
    {
        // GIVEN
        TestUtils.createFrom(v3api, sessionToken, Paths.get(FilenameUtils.concat(FILES_DIR, EXPERIMENT_TYPES_XLS)));
        // WHEN
        ExperimentType collection = TestUtils.getExperimentType(v3api, sessionToken, "COLLECTION");
        // THEN
        assertEquals(collection.getCode(), "COLLECTION");
    }

    @Test
    @DirtiesContext
    public void testExperimentTypesWithValidationScript() throws IOException
    {
        // GIVEN
        TestUtils.createFrom(v3api, sessionToken, TestUtils.getValidationPluginMap(),
                Paths.get(FilenameUtils.concat(FILES_DIR, EXPERIMENT_WITH_VALIDATION_SCRIPT)));
        // WHEN
        ExperimentType collection = TestUtils.getExperimentType(v3api, sessionToken, "COLLECTION");
        // THEN
        assertEquals(collection.getValidationPlugin().getName().toUpperCase(), "COLLECTION.VALID");
    }

    @Test(expectedExceptions = UserFailureException.class)
    public void shouldThrowExceptionIfNoSampleCode() throws IOException
    {
        TestUtils.createFrom(v3api, sessionToken, Paths.get(FilenameUtils.concat(FILES_DIR, EXPERIMENT_NO_CODE)));
    }

}
