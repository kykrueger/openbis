/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.generic.server.plugins.demo;

import java.io.File;
import java.util.Properties;

import ch.systemsx.cisd.openbis.generic.shared.dto.DatasetDescription;

/**
 * @author Tomasz Pylak
 */
abstract class AbstractDatastorePlugin
{
    private final File storeRoot;

    protected AbstractDatastorePlugin(Properties properties, File storeRoot)
    {
        assert storeRoot.exists() : "storeRoot does not exist " + storeRoot;

        this.storeRoot = storeRoot;
    }

    protected File getOriginalDir(DatasetDescription dataset)
    {
        return new File(getDatasetDir(dataset), "original");
    }

    protected File getDatasetDir(DatasetDescription dataset)
    {
        return new File(storeRoot, dataset.getDataSetLocation());
    }
}
