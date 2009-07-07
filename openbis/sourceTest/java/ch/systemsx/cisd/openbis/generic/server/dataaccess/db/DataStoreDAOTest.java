/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.server.dataaccess.db;

import java.util.HashSet;
import java.util.Set;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataStorePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataStoreServicePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataStoreServicePE.DataStoreServiceKind;
import ch.systemsx.cisd.openbis.generic.shared.dto.types.DataSetTypeCode;

/**
 * Test cases for corresponding {@link DataStoreDAO} class.
 * 
 * @author Tomasz Pylak
 */
@Test(groups =
    { "db", "dataStore" })
public final class DataStoreDAOTest extends AbstractDAOTest
{
    @Test
    public void testCreate()
    {
        DataStorePE dataStore = new DataStorePE();
        dataStore.setDatabaseInstance(daoFactory.getHomeDatabaseInstance());
        String dataStoreCode = "xxx";
        dataStore.setCode(dataStoreCode);
        dataStore.setDownloadUrl(dataStoreCode);
        dataStore.setRemoteUrl(dataStoreCode);
        dataStore.setSessionToken(dataStoreCode);
        dataStore.setServices(createDataStoreServices(dataStoreCode));
        daoFactory.getDataStoreDAO().createOrUpdateDataStore(dataStore);
        AssertJUnit.assertNotNull(dataStore.getId());

        DataStorePE store = daoFactory.getDataStoreDAO().tryToFindDataStoreByCode(dataStoreCode);
        AssertJUnit.assertEquals(dataStore, store);
        // check if changing the registered services does not fail
        dataStore.setServices(createDataStoreServices("another"));
        daoFactory.getDataStoreDAO().createOrUpdateDataStore(dataStore);
    }

    private Set<DataStoreServicePE> createDataStoreServices(String prefix)
    {
        Set<DataStoreServicePE> services = new HashSet<DataStoreServicePE>();
        for (int i = 0; i < 3; i++)
        {
            DataStoreServicePE service = new DataStoreServicePE();
            service.setKey(prefix + "_key_" + i);
            service.setLabel("label");
            service.setKind(DataStoreServiceKind.PROCESSING);
            service.setDatasetTypes(getDataSetTypes());
            services.add(service);
        }
        return services;
    }

    private Set<DataSetTypePE> getDataSetTypes()
    {
        Set<DataSetTypePE> datasetTypes = new HashSet<DataSetTypePE>();
        DataSetTypePE type =
                daoFactory.getDataSetTypeDAO().tryToFindDataSetTypeByCode(
                        DataSetTypeCode.UNKNOWN.getCode());
        datasetTypes.add(type);
        return datasetTypes;
    }
}
