package ch.ethz.sis.openbis.systemtest.plugin.excelimport;

import org.springframework.test.context.testng.AbstractTransactionalTestNGSpringContextTests;
import org.testng.annotations.BeforeSuite;

import ch.systemsx.cisd.openbis.generic.server.util.TestInitializer;
import ch.systemsx.cisd.openbis.generic.shared.Constants;
import ch.systemsx.cisd.openbis.generic.shared.coreplugin.CorePluginsUtils;

public class AbstractImportTest extends AbstractTransactionalTestNGSpringContextTests
{

    protected String FILES_DIR;

    @BeforeSuite
    public void setupSuite()
    {
        System.setProperty(CorePluginsUtils.CORE_PLUGINS_FOLDER_KEY, "dist/core-plugins");
        System.setProperty(Constants.ENABLED_MODULES_KEY, "xls-import");
        TestInitializer.initEmptyDbNoIndex();
    }

}
