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

package ch.systemsx.cisd.openbis.installer.izpack;

import static ch.systemsx.cisd.openbis.installer.izpack.GlobalInstallationContext.TECHNOLOGY_ELN_LIMS;

import java.util.HashMap;

import com.izforge.izpack.api.data.AutomatedInstallData;
import com.izforge.izpack.api.data.PanelActionConfiguration;

/**
 * Executes a script that removes eln master data python script, if required.
 */
public class ELNMasterDataAction extends AbstractScriptExecutor
{
    /**
     * a script that creates a installation backup.
     */
    private static final String CREATE_BACKUP_SCRIPT = "delete-eln-master-data-script.sh";

    @Override
    public synchronized void executeAction(AutomatedInstallData data)
    {
        String script = getAdminScript(data, CREATE_BACKUP_SCRIPT);

        String elnEnabled = data.getVariable(TECHNOLOGY_ELN_LIMS);
        String elnMasterDataEnabled = data.getVariable("ELN-LIMS-MASTER-DATA");
        if (elnMasterDataEnabled == null)
        {
            elnMasterDataEnabled = "true";
        }

        if ("true".equals(elnEnabled) && "false".equals(elnMasterDataEnabled))
        {
            executeAdminScript(new HashMap<String, String>(), script);
        }
    }

    @Override
    public void initialize(PanelActionConfiguration arg0)
    {
    }

}