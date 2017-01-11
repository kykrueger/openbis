/*
 * Copyright 2016 ETH Zuerich, SIS
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

package ch.ethz.sis.openbis.generic.server.dss.plugins.sync.harvester.config;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import ch.systemsx.cisd.common.mail.EMailAddress;

/**
 * 
 *
 * @author Ganime Betul Akin
 */
public class SyncConfig
{
    private String dataSourceURI;

    public String getDataSourceURI()
    {
        return dataSourceURI;
    }

    public void setDataSourceURI(String dataSourceURI)
    {
        this.dataSourceURI = dataSourceURI;
    }

    public String getDataSourceOpenbisURL()
    {
        return dataSourceOpenbisURL;
    }

    public void setDataSourceOpenbisURL(String dataSourceOpenbisURL)
    {
        this.dataSourceOpenbisURL = dataSourceOpenbisURL;
    }

    public String getDataSourceDSSURL()
    {
        return dataSourceDSSURL;
    }

    public void setDataSourceDSSURL(String dataSourceDSSURL)
    {
        this.dataSourceDSSURL = dataSourceDSSURL;
    }

    public String getLastSyncTimestampFileName()
    {
        return lastSyncTimestampFileName;
    }

    public void setLastSyncTimestampFileName(String lastSyncTimestampFileName)
    {
        this.lastSyncTimestampFileName = lastSyncTimestampFileName;
    }

    public String getNotSyncedDataSetsFileName()
    {
        return notSyncedDataSetsFileName;
    }

    public void setNotSyncedDataSetsFileName(String notSyncedDataSetsFileName)
    {
        this.notSyncedDataSetsFileName = notSyncedDataSetsFileName;
    }

    public String getDataSourceAlias()
    {
        return dataSourceAlias;
    }

    public void setDataSourceAlias(String dataSourceAlias)
    {
        this.dataSourceAlias = dataSourceAlias;
    }

    public List<String> getDataSourceSpaces()
    {
        return dataSourceSpaces;
    }

    public void setDataSourceSpaces(String dataSourceSpaces)
    {
        if (dataSourceSpaces == null)
        {
            return;
        }
        for (String token : dataSourceSpaces.split(SEPARATOR))
        {
            this.dataSourceSpaces.add(token.trim());
        }
    }

    public List<String> getHarvesterSpaces()
    {
        return harvesterSpaces;
    }

    public void setHarvesterSpaces(String harvesterSpaces)
    {
        if (harvesterSpaces == null)
        {
            return;
        }
        for (String token : harvesterSpaces.split(SEPARATOR))
        {
            this.harvesterSpaces.add(token.trim());
        }
    }

    public String getHarvesterTempDir()
    {
        return harvesterTempDir;
    }

    public void setHarvesterTempDir(String harvesterTempDir)
    {
        this.harvesterTempDir = harvesterTempDir;
    }

    public void printConfig()
    {
        for (Field field : this.getClass().getDeclaredFields())
        {
            field.setAccessible(true);
            String name = field.getName();
            Object value;
            try
            {
                value = field.get(this);
                System.out.printf("%s : %s%n", name, value);
            } catch (IllegalArgumentException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IllegalAccessException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    private BasicAuthCredentials auth;

    private String dataSourceOpenbisURL;

    private String dataSourceDSSURL;

    private String lastSyncTimestampFileName;

    private String notSyncedDataSetsFileName;

    private String dataSourceAlias;

    private List<String> dataSourceSpaces = new ArrayList<>();

    private List<String> harvesterSpaces = new ArrayList<>();

    private String harvesterTempDir;

    private List<EMailAddress> emailAddresses = new ArrayList<>();

    private String logFilePath;

    private static final String SEPARATOR = ",";

    private HashMap<String, String> spaceMappings = new HashMap<String, String>();

    public HashMap<String, String> getSpaceMappings()
    {
        return spaceMappings;
    }

    public String getLogFilePath()
    {
        return logFilePath;
    }

    public void setLogFilePath(String logFilePath)
    {
        this.logFilePath = logFilePath;
    }

    public List<EMailAddress> getEmailAddresses()
    {
        return emailAddresses;
    }

    public void setEmailAddresses(String emailAddresses)
    {
        for (String token : emailAddresses.split(SEPARATOR))
        {
            this.emailAddresses.add(new EMailAddress(token.trim()));
        }
    }

    public void setAuthCredentials(String realm, String user, String pass)
    {
        this.auth = new BasicAuthCredentials(realm, user, pass);
    }

    public BasicAuthCredentials getAuthenticationCredentials()
    {
        return auth;
    }

    public String getUser()
    {
        return this.auth.getUser();
    }

    public String getPassword()
    {
        return this.auth.getPassword();
    }
}
