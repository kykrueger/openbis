package ch.ethz.sis.openbis.systemtest.plugin.excelimport;

import java.io.File;

import org.springframework.test.context.testng.AbstractTransactionalTestNGSpringContextTests;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeSuite;

import ch.systemsx.cisd.openbis.generic.server.util.TestInitializer;
import ch.systemsx.cisd.openbis.generic.shared.Constants;
import ch.systemsx.cisd.openbis.generic.shared.coreplugin.CorePluginsUtils;

public class AbstractImportTest extends AbstractTransactionalTestNGSpringContextTests
{

    protected String FILES_DIR;

    private String XLS_VERSIONING_DIR = "xls-import.version-data-file";

    @BeforeSuite
    public void setupSuite()
    {
        System.setProperty(XLS_VERSIONING_DIR, "./versioning.bin");
        System.setProperty(CorePluginsUtils.CORE_PLUGINS_FOLDER_KEY, "dist/core-plugins");
        System.setProperty(Constants.ENABLED_MODULES_KEY, "xls-import");
        TestInitializer.initEmptyDbNoIndex();
    }

    @AfterMethod
    public void afterTest()
    {
        File f = new File("./versioning.bin");
        f.delete();
    }

}
