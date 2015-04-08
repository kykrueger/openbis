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

package ch.ethz.sis.openbis.generic.server.api.v3.executor.method;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.server.api.v3.executor.dataset.IDeleteDataSetExecutor;
import ch.ethz.sis.openbis.generic.server.api.v3.executor.entity.IDeleteEntityExecutor;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.deletion.dataset.DataSetDeletionOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.dataset.IDataSetId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.deletion.IDeletionId;

/**
 * @author pkupczyk
 */
@Component
public class DeleteDataSetMethodExecutor extends AbstractDeleteMethodExecutor<IDeletionId, IDataSetId, DataSetDeletionOptions> implements
        IDeleteDataSetMethodExecutor
{

    @Autowired
    private IDeleteDataSetExecutor deleteExecutor;

    @Override
    protected IDeleteEntityExecutor<IDeletionId, IDataSetId, DataSetDeletionOptions> getDeleteExecutor()
    {
        return deleteExecutor;
    }

}
