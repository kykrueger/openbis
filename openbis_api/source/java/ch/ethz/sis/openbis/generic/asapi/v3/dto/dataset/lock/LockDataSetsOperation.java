/*
 * Copyright 2018 ETH Zuerich, SIS
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

package ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.lock;

import java.util.List;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.operation.IOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.IDataSetId;
import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * @author Franz-Josef Elmer
 *
 */
@JsonObject("as.dto.dataset.lock.LockDataSetsOperation")
public class LockDataSetsOperation implements IOperation
{

    private static final long serialVersionUID = 1L;

    private List<? extends IDataSetId> dataSetIds;

    private DataSetLockOptions options;

    @SuppressWarnings("unused")
    private LockDataSetsOperation()
    {
    }

    public LockDataSetsOperation(List<? extends IDataSetId> dataSetIds, DataSetLockOptions options)
    {
        this.dataSetIds = dataSetIds;
        this.options = options;
    }

    public List<? extends IDataSetId> getDataSetIds()
    {
        return dataSetIds;
    }

    public DataSetLockOptions getOptions()
    {
        return options;
    }

    @Override
    public String getMessage()
    {
        return toString();
    }

    @Override
    public String toString()
    {
        return getClass().getSimpleName() + (dataSetIds != null ? " " + dataSetIds.size() + " dataset(s)" : "");
    }

}
