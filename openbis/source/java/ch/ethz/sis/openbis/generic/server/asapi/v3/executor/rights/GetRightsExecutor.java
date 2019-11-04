/*
 * Copyright 2019 ETH Zuerich, SIS
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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.rights;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.id.IObjectId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.rights.Right;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.rights.Rights;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.rights.fetchoptions.RightsFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.ISampleId;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.sample.IMapSampleByIdExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.sample.ISampleAuthorizationExecutor;
import ch.systemsx.cisd.common.exceptions.AuthorizationFailureException;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;

/**
 * @author Franz-Josef Elmer
 *
 */
@Component
public class GetRightsExecutor implements IGetRightsExecutor
{
    @Autowired
    private IMapSampleByIdExecutor mapSampleByIdExecutor;
    
    @Autowired
    private ISampleAuthorizationExecutor sampleAuthorizationExecutor;

    @Override
    public Map<IObjectId, Rights> getRights(IOperationContext context, List<? extends IObjectId> objectIds, RightsFetchOptions fetchOptions)
    {
        Map<IObjectId, Rights> result = new HashMap<>();
        List<ISampleId> sampleIds = new ArrayList<>();
        for (IObjectId id : objectIds)
        {
            if (id instanceof ISampleId)
            {
                ISampleId sampleId = (ISampleId) id;
                sampleIds.add(sampleId);
            }
        }
        Map<ISampleId, SamplePE> map = mapSampleByIdExecutor.map(context, sampleIds);
        Set<Entry<ISampleId, SamplePE>> entrySet = map.entrySet();
        for (Entry<ISampleId, SamplePE> entry : entrySet)
        {
            Set<Right> rights = new HashSet<>();
            ISampleId id = entry.getKey();
            SamplePE object = entry.getValue();
            
            try
            {
                sampleAuthorizationExecutor.canUpdate(context, id, object);
                rights.add(Right.UPDATE);
            } catch (AuthorizationFailureException e)
            {
                // silently ignored
            }
            result.put(id, new Rights(rights));
        }
        
        return result;
    }
    
    private enum RightsDetector
    {
        SAMPLE;
        
        
    }
}
