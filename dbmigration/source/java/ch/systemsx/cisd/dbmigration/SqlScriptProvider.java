/*
 * Copyright 2007 ETH Zuerich, CISD
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

package ch.systemsx.cisd.dbmigration;

import java.io.File;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.utilities.FileUtilities;

/**
 * Implementation of {@link ISqlScriptProvider} based on files in classpath or working directory. 
 *
 * @author Franz-Josef Elmer
 */
public class SqlScriptProvider implements ISqlScriptProvider
{
    private static final String SQL_FILE_TYPE = ".sql";

    private static final Logger operationLog 
                = LogFactory.getLogger(LogCategory.OPERATION, SqlScriptProvider.class);
    
    private final String schemaScriptFolder;
    private final String dataScriptFolder;
    
    public SqlScriptProvider(String schemaScriptFolder, String dataScriptFolder)
    {
        this.schemaScriptFolder = schemaScriptFolder;
        this.dataScriptFolder = dataScriptFolder;
    }

    public Script getDataScript(String version)
    {
        return loadScript(dataScriptFolder + "/" + version, "data-" + version + SQL_FILE_TYPE);
    }

    public Script getMigrationScript(String fromVersion, String toVersion)
    {
        String scriptName = "migration-" + fromVersion + "-" + toVersion + SQL_FILE_TYPE;
        return loadScript(schemaScriptFolder + "/migration", scriptName);
    }

    public Script getSchemaScript(String version)
    {
        return loadScript(schemaScriptFolder + "/" + version, "schema-" + version + SQL_FILE_TYPE);
    }

    private Script loadScript(String folder, String scriptName)
    {
        String fullScriptName = folder + "/" + scriptName;
        String resource = "/" + fullScriptName;
        String script = FileUtilities.loadToString(getClass(), resource);
        if (script == null)
        {
            File file = new File(folder, scriptName);
            if (operationLog.isDebugEnabled())
            {
                operationLog.debug("Resource '" + resource + "' could not be found. Trying '" + file.getPath() + "'.");
            }
            if (file.exists() == false)
            {
                if (operationLog.isDebugEnabled())
                {
                    operationLog.debug("File '" + file.getPath() + "' does not exist.");
                }
                return null;
            }
            script = FileUtilities.loadToString(file);
        }
        return new Script(fullScriptName, script);
    }


}
