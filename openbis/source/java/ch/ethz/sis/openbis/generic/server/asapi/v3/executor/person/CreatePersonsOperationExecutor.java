/*
 * Copyright 2017 ETH Zuerich, SIS
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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.person;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.create.CreateObjectsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.create.CreateObjectsOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.create.CreatePersonsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.create.CreatePersonsOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.create.PersonCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.id.PersonPermId;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.create.CreateObjectsOperationExecutor;

/**
 * @author Franz-Josef Elmer
 */
@Component
public class CreatePersonsOperationExecutor
        extends CreateObjectsOperationExecutor<PersonCreation, PersonPermId>
        implements ICreatePersonsOperationExecutor
{
    @Autowired
    private ICreatePersonExecutor executor;

    @Override
    protected Class<? extends CreateObjectsOperation<PersonCreation>> getOperationClass()
    {
        return CreatePersonsOperation.class;
    }

    @Override
    protected CreateObjectsOperationResult<PersonPermId> doExecute(IOperationContext context, CreateObjectsOperation<PersonCreation> operation)
    {
        return new CreatePersonsOperationResult(executor.create(context, operation.getCreations()));
    }

}
