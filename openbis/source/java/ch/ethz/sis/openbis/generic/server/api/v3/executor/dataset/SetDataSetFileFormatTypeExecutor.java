/*
 * Copyright 2015 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.server.api.v3.executor.dataset;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.server.api.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.api.v3.executor.entity.AbstractSetEntityToOneRelationExecutor;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.dataset.DataSetCreation;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.dataset.IFileFormatTypeId;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExternalDataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.FileFormatTypePE;

/**
 * @author pkupczyk
 */
@Component
public class SetDataSetFileFormatTypeExecutor extends
        AbstractSetEntityToOneRelationExecutor<DataSetCreation, DataPE, IFileFormatTypeId, FileFormatTypePE>
        implements ISetDataSetFileFormatTypeExecutor
{

    @Autowired
    private IMapFileFormatTypeByIdExecutor mapFileFormatTypeByIdExecutor;

    @Override
    protected IFileFormatTypeId getRelatedId(DataSetCreation creation)
    {
        return creation.getPhysicalData() != null ? creation.getPhysicalData().getFileFormatTypeId() : null;
    }

    @Override
    protected Map<IFileFormatTypeId, FileFormatTypePE> map(IOperationContext context, List<IFileFormatTypeId> relatedIds)
    {
        return mapFileFormatTypeByIdExecutor.map(context, relatedIds);
    }

    @Override
    protected void check(IOperationContext context, DataPE entity, IFileFormatTypeId relatedId, FileFormatTypePE related)
    {
        if (entity instanceof ExternalDataPE && relatedId == null)
        {
            throw new UserFailureException("File format type id cannot be null for a physical data set.");
        }
    }

    @Override
    protected void set(IOperationContext context, DataPE entity, FileFormatTypePE related)
    {
        if (entity instanceof ExternalDataPE)
        {
            ((ExternalDataPE) entity).setFileFormatType(related);
        }
    }

}