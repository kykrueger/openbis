/*
 * Copyright 2016 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.operation.IOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.operation.IOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.AbstractCompositeSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.AbstractFieldSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.ISearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchObjectsOperation;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.systemsx.cisd.openbis.generic.server.ConcurrentOperation;
import ch.systemsx.cisd.openbis.generic.server.IConcurrentOperationLimiter;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author pkupczyk
 */
public abstract class OperationExecutor<OPERATION extends IOperation, RESULT extends IOperationResult> implements IOperationExecutor
{

    private static final String PREFIX_TO_CLEAN = "$";

    @Autowired
    protected IConcurrentOperationLimiter operationLimiter;

    @SuppressWarnings("unchecked")
    @Override
    public Map<IOperation, IOperationResult> execute(IOperationContext context, List<? extends IOperation> operations)
    {
        Map<OPERATION, RESULT> results = new HashMap<OPERATION, RESULT>();
        Class<? extends OPERATION> operationClass = getOperationClass();

        for (IOperation operation : operations)
        {
            if (operation != null && operationClass.isAssignableFrom(operation.getClass()))
            {
                OPERATION theOperation = (OPERATION) operation;
                RESULT result = null;

                if (context.isAsync())
                {
                    result = operationLimiter.executeLimitedWithTimeoutAsync(theOperation.getClass().getSimpleName(), new ConcurrentOperation<RESULT>()
                        {
                            @Override
                            public RESULT execute()
                            {
                                return cleanupAndExecute(context, theOperation);
                            }
                        });
                } else
                {
                    result = operationLimiter.executeLimitedWithTimeout(theOperation.getClass().getSimpleName(), new ConcurrentOperation<RESULT>()
                        {
                            @Override
                            public RESULT execute()
                            {
                                return cleanupAndExecute(context, theOperation);
                            }
                        });
                }

                results.put(theOperation, result);
            }
        }

        return (Map<IOperation, IOperationResult>) results;
    }

    private RESULT cleanupAndExecute(IOperationContext context, OPERATION operation)
    {
        cleanPrefixesInCriteria(operation);
        return doExecute(context, operation);
    }

    private void cleanPrefixesInCriteria(OPERATION operation)
    {
        if (operation instanceof SearchObjectsOperation)
        {
            final ISearchCriteria searchCriteria = ((SearchObjectsOperation) operation).getCriteria();

            if (searchCriteria instanceof AbstractCompositeSearchCriteria)
            {
                cleanPrefixesInCriteria((AbstractCompositeSearchCriteria) searchCriteria);
            }
        }
    }

    private void cleanPrefixesInCriteria(final AbstractCompositeSearchCriteria criterion)
    {
        criterion.getCriteria().forEach(subcriterion -> {
            if (subcriterion instanceof AbstractFieldSearchCriteria) {
                final AbstractFieldSearchCriteria fieldSearchSubcriterion = (AbstractFieldSearchCriteria) subcriterion;
                final String fieldName = fieldSearchSubcriterion.getFieldName();
                if (fieldName.startsWith(PREFIX_TO_CLEAN)) {
                    fieldSearchSubcriterion.setFieldName(fieldName.substring(PREFIX_TO_CLEAN.length()));
                }
            }
        });
    }

    protected abstract Class<? extends OPERATION> getOperationClass();

    protected abstract RESULT doExecute(IOperationContext context, OPERATION operation);

}
