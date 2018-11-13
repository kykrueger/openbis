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

import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSetType;
import ch.ethz.sis.openbis.generic.server.asapi.v3.IApplicationServerInternalApi;
import ch.systemsx.cisd.common.exceptions.UserFailureException;

@ContextConfiguration(locations = "classpath:applicationContext.xml")
@Transactional(transactionManager = "transaction-manager")
@Rollback
public class ImportDatasetTypesTest extends AbstractImportTest
{

    @Autowired
    private IApplicationServerInternalApi v3api;

    private static final String TEST_USER = "test";

    private static final String PASSWORD = "password";

    private static final String DATASET_TYPES_XLS = "dataset_types/normal_dataset.xls";

    private static final String DATASET_NO_CODE = "dataset_types/no_code.xls";

    private static final String DATASET_WITH_VALIDATION_SCRIPT = "dataset_types/with_validation.xls";

    private static final String DATASET_WITHOUT_PROPERTIES = "dataset_types/no_properties.xls";

    private static String FILES_DIR;

    private String sessionToken;

    @BeforeClass
    public void setupClass() throws IOException
    {
        String f = ImportDatasetTypesTest.class.getName().replace(".", "/");
        FILES_DIR = f.substring(0, f.length() - ImportDatasetTypesTest.class.getSimpleName().length()) + "/test_files/";
        System.out.println(FILES_DIR);
        System.out.println("SEE ME MY DEAR FRIEND");
    }

    @BeforeMethod
    public void beforeTest()
    {
        sessionToken = v3api.login(TEST_USER, PASSWORD);
    }

    @Test
    @DirtiesContext
    public void testNormalDatasetsTypesAreCreated() throws IOException
    {
        // GIVEN
        TestUtils.createFrom(v3api, sessionToken, Paths.get(FilenameUtils.concat(FILES_DIR, DATASET_TYPES_XLS)));
        // WHEN
        DataSetType rawData = TestUtils.getDatasetType(v3api, sessionToken, "RAW_DATA");
        // THEN
        assertEquals(rawData.getCode(), "RAW_DATA");
        assertEquals(rawData.getPropertyAssignments().size(), 2);
    }

    @Test
    @DirtiesContext
    public void testDatasetsWithoutPropertiesTypesAreCreated() throws IOException
    {
        // GIVEN
        TestUtils.createFrom(v3api, sessionToken, Paths.get(FilenameUtils.concat(FILES_DIR, DATASET_WITHOUT_PROPERTIES)));
        // WHEN
        DataSetType rawData = TestUtils.getDatasetType(v3api, sessionToken, "RAW_DATA");
        // THEN
        assertEquals(rawData.getCode(), "RAW_DATA");
        assertEquals(rawData.getPropertyAssignments().size(), 0);
    }

    @Test
    @DirtiesContext
    public void testSampleTypesWithValidationScript() throws IOException
    {
        // GIVEN
        TestUtils.createFrom(v3api, sessionToken, TestUtils.getValidationPluginMap(),
                Paths.get(FilenameUtils.concat(FILES_DIR, DATASET_WITH_VALIDATION_SCRIPT)));
        // WHEN
        DataSetType collection = TestUtils.getDatasetType(v3api, sessionToken, "RAW_DATA");
        // THEN
        assertEquals(collection.getValidationPlugin().getName().toUpperCase(), "RAW_DATA.VALID");
    }

    @Test(expectedExceptions = UserFailureException.class)
    public void shouldThrowExceptionIfNoSampleCode() throws IOException
    {
        TestUtils.createFrom(v3api, sessionToken, Paths.get(FilenameUtils.concat(FILES_DIR, DATASET_NO_CODE)));
    }

}
