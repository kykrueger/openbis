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

import java.io.File;

import com.izforge.izpack.api.data.AutomatedInstallData;

import ch.systemsx.cisd.common.security.PasswordGenerator;

/**
 * @author Kaloyan Enimanev
 */
public class GlobalInstallationContext
{
    public static final String POSTGRES_BIN_VARNAME = "POSTGRES_BIN";

    public static final String ADMIN_PASSWORD_VARNAME = "ADMIN_PASSWORD";

    public static final String ETL_SERVER_PASSWORD_VARNAME = "ETLSERVER_PASSWORD";

    public static final String KEY_STORE_FILE_VARNAME = "KEY_STORE_FILE";

    public static final String KEY_STORE_PASSWORD_VARNAME = "KEY_STORE_PASSWORD";
    
    public static final String KEY_PASSWORD_VARNAME = "KEY_PASSWORD";
    
    public static final String DATA_DIR_VARNAME = "DSS_ROOT_DIR";

    public static final String BACKUP_FOLDER_VARNAME = "BACKUP_FOLDER";
    
    public static final String TECHNOLOGY_PROTEOMICS = "PROTEOMICS";
    
    public static final String TECHNOLOGY_SCREENING = "SCREENING";
    
    public static final String TECHNOLOGY_ILLUMINA_NGS = "ILLUMINA-NGS";
    
    public static final String[] TECHNOLOGIES =
        { TECHNOLOGY_PROTEOMICS, TECHNOLOGY_SCREENING, TECHNOLOGY_ILLUMINA_NGS };
    
    /**
     * set to true if the installation process is trying to update an existing openBIS installation.
     */
    public static boolean isUpdateInstallation = false;

    /**
     * set to true if this is the first openBIS installation on the machine.
     */
    public static boolean isFirstTimeInstallation = true;

    /**
     * this variable is set to true if 'psql' or 'pg_dump' are not on path.
     * <p>
     * used by the installation xml config, not meant to be used by Java code.
     */
    public static boolean noPsqlToolsOnPath = (false == PostgresInstallationDetectorUtils
            .areCommandLineToolsOnPath());

    public static boolean presentKeyStoreFile = false;
    
    public static File installDir;

    public static void initialize(AutomatedInstallData data)
    {
        String installPath = data.getInstallPath();
        installDir = new File(installPath);
        isFirstTimeInstallation = (installDir.exists() == false);
        isUpdateInstallation = installDir.exists();

        data.setVariable(POSTGRES_BIN_VARNAME, "");

        if (isFirstTimeInstallation)
        {
            populateFirstTimeInstallVariables(data);
        }
    }

    /**
     * Return the data directory chosen for this intallation.
     */
    public static String getDataDir(AutomatedInstallData data)
    {
        return data.getVariable(DATA_DIR_VARNAME);
    }

    /**
     * populates variables need for fist time installations.
     */
    private static void populateFirstTimeInstallVariables(AutomatedInstallData data)
    {
        data.setVariable(ETL_SERVER_PASSWORD_VARNAME,
                new PasswordGenerator(true).generatePassword());
    }

}
