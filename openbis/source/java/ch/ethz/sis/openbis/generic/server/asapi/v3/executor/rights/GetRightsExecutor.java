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
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.IDataSetId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.IExperimentId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.rights.Right;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.rights.Rights;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.rights.fetchoptions.RightsFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.ISampleId;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.dataset.IDataSetAuthorizationExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.dataset.IMapDataSetByIdExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.experiment.IExperimentAuthorizationExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.experiment.IMapExperimentByIdExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.sample.IMapSampleByIdExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.sample.ISampleAuthorizationExecutor;
import ch.systemsx.cisd.common.exceptions.AuthorizationFailureException;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;

/**
 * @author Franz-Josef Elmer
 */
@Component
public class GetRightsExecutor implements IGetRightsExecutor
{
    @Autowired
    private IMapSampleByIdExecutor mapSampleByIdExecutor;

    @Autowired
    private ISampleAuthorizationExecutor sampleAuthorizationExecutor;

    @Autowired
    private IMapExperimentByIdExecutor mapExperimentByIdExecutor;

    @Autowired
    private IExperimentAuthorizationExecutor experimentAuthorizationExecutor;

    @Autowired
    private IMapDataSetByIdExecutor mapDataSetByIdExecutor;

    @Autowired
    private IDataSetAuthorizationExecutor dataSetAuthorizationExecutor;

    private List<Handler> handlers;

    public GetRightsExecutor()
    {
        handlers = new ArrayList<>();
        handlers.add(new SampleHandler());
        handlers.add(new ExperimentHandler());
        handlers.add(new DataSetHandler());
    }

    @Override
    public Map<IObjectId, Rights> getRights(IOperationContext context, List<? extends IObjectId> objectIds, RightsFetchOptions fetchOptions)
    {
        Map<IObjectId, Rights> result = new HashMap<>();
        for (IObjectId id : objectIds)
        {
            for (Handler handler : handlers)
            {
                handler.handle(id);
            }
        }
        for (Handler handler : handlers)
        {
            handler.addRights(context, result);
        }
        return result;
    }

    private static interface Handler
    {
        void handle(IObjectId id);

        void addRights(IOperationContext context, Map<IObjectId, Rights> rightsByIds);
    }

    private class SampleHandler implements Handler
    {
        private List<ISampleId> sampleIds = new ArrayList<>();

        @Override
        public void handle(IObjectId id)
        {
            if (id instanceof ISampleId)
            {
                sampleIds.add((ISampleId) id);
            }
        }

        @Override
        public void addRights(IOperationContext context, Map<IObjectId, Rights> rightsByIds)
        {
            Map<ISampleId, SamplePE> map = mapSampleByIdExecutor.map(context, sampleIds);
            for (Entry<ISampleId, SamplePE> entry : map.entrySet())
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
                rightsByIds.put(id, new Rights(rights));
            }
        }
    }

    private class ExperimentHandler implements Handler
    {
        private List<IExperimentId> experimentIds = new ArrayList<>();

        @Override
        public void handle(IObjectId id)
        {
            if (id instanceof IExperimentId)
            {
                experimentIds.add((IExperimentId) id);
            }
        }

        @Override
        public void addRights(IOperationContext context, Map<IObjectId, Rights> rightsByIds)
        {
            Map<IExperimentId, ExperimentPE> map = mapExperimentByIdExecutor.map(context, experimentIds);
            for (Entry<IExperimentId, ExperimentPE> entry : map.entrySet())
            {
                Set<Right> rights = new HashSet<>();
                IExperimentId id = entry.getKey();
                ExperimentPE object = entry.getValue();

                try
                {
                    experimentAuthorizationExecutor.canUpdate(context, id, object);
                    rights.add(Right.UPDATE);
                } catch (AuthorizationFailureException e)
                {
                    // silently ignored
                }
                rightsByIds.put(id, new Rights(rights));
            }
        }
    }

    private class DataSetHandler implements Handler
    {
        private List<IDataSetId> dataSetIds = new ArrayList<>();

        @Override
        public void handle(IObjectId id)
        {
            if (id instanceof IDataSetId)
            {
                dataSetIds.add((IDataSetId) id);
            }
        }

        @Override
        public void addRights(IOperationContext context, Map<IObjectId, Rights> rightsByIds)
        {
            Map<IDataSetId, DataPE> map = mapDataSetByIdExecutor.map(context, dataSetIds);
            for (Entry<IDataSetId, DataPE> entry : map.entrySet())
            {
                Set<Right> rights = new HashSet<>();
                IDataSetId id = entry.getKey();
                DataPE object = entry.getValue();

                try
                {
                    dataSetAuthorizationExecutor.canUpdate(context, id, object);
                    rights.add(Right.UPDATE);
                } catch (AuthorizationFailureException e)
                {
                    // silently ignored
                }
                rightsByIds.put(id, new Rights(rights));
            }
        }
    }
}
