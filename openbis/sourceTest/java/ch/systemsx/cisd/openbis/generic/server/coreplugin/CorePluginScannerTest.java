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

package ch.systemsx.cisd.openbis.generic.server.coreplugin;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.testng.annotations.Test;

import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.logging.MockLogger;
import ch.systemsx.cisd.openbis.generic.server.coreplugin.CorePluginScanner.ScannerType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.CorePlugin;

/**
 * @author Kaloyan Enimanev
 */
public class CorePluginScannerTest extends AbstractFileSystemTestCase
{
    private final String CORE_PLUGINS_DIR = "../openbis/resource/test-data/core-plugins";

    @Test
    public void testWithRealFolder() throws IOException
    {
        MockLogger logger = new MockLogger();
        File pluginsDir = preparePluginsDirectory(CORE_PLUGINS_DIR);
        CorePluginScanner scanner =
                new CorePluginScanner(pluginsDir.getAbsolutePath(), ScannerType.AS, logger);

        List<CorePlugin> plugins = scanner.scanForPlugins();
        Collections.sort(plugins);

        assertEquals(2, plugins.size());

        CorePlugin plugin = plugins.get(0);
        assertEquals("plugin-X", plugin.getName());
        assertEquals(15, plugin.getVersion());
        assertEquals("TEST-SCRIPT", getMasterDataScript(plugin, scanner).trim());

        plugin = plugins.get(1);
        assertEquals("plugin-Y", plugin.getName());
        assertEquals(3, plugin.getVersion());
        assertEquals(null, getMasterDataScript(plugin, scanner));

        String output =
                String.format(
                        "WARN: No valid versions have been detected for plugin '%s/invalid-folder'.\n"
                                + "WARN: Invalid version 'NaN-version' for plugin '%s/plugin-X'. Plugin version must be non-negative integer numbers.\n",
                        pluginsDir.getAbsolutePath(), pluginsDir.getAbsolutePath());
        assertEquals(output, logger.toString());
    }

    private File preparePluginsDirectory(String originalPath) throws IOException
    {
        File originalDir = new File(originalPath);
        FileUtils.copyDirectory(originalDir, workingDirectory, new FileFilter()
            {
                public boolean accept(File pathname)
                {
                    return false == pathname.getName().equals(".svn");
                }
            });
        return workingDirectory;
    }

    private String getMasterDataScript(CorePlugin plugin, CorePluginScanner scanner)
    {
        return scanner.tryLoadToString(plugin, AsCorePluginPaths.INIT_MASTER_DATA_SCRIPT);
    }

    @Test(expectedExceptions = ConfigurationFailureException.class)
    public void testWithInvalidFolder()
    {
        new CorePluginScanner("/invalid-folder", ScannerType.AS);
    }

}
