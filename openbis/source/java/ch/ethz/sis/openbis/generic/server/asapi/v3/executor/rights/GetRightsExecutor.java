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
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.id.IObjectId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.DataSetPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.IDataSetId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentIdentifier;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.IExperimentId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.ProjectIdentifier;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.rights.Right;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.rights.Rights;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.rights.fetchoptions.RightsFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.ISampleId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SampleIdentifier;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SamplePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.SpacePermId;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.dataset.IDataSetAuthorizationExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.dataset.IMapDataSetByIdExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.experiment.IExperimentAuthorizationExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.experiment.IMapExperimentByIdExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.project.IMapProjectByIdExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.sample.IMapSampleByIdExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.sample.ISampleAuthorizationExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.space.IMapSpaceByIdExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.sample.FullSampleIdentifier;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.sample.SampleIdentifierParts;
import ch.systemsx.cisd.common.exceptions.AuthorizationFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifierFactory;

/**
 * @author Franz-Josef Elmer
 */
@Component
public class GetRightsExecutor implements IGetRightsExecutor
{
    @Autowired
    private IMapSpaceByIdExecutor mapSpaceByIdExecutor;

    @Autowired
    private IMapProjectByIdExecutor mapProjectByIdExecutor;

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

    @Override
    public Map<IObjectId, Rights> getRights(IOperationContext context, List<? extends IObjectId> objectIds, RightsFetchOptions fetchOptions)
    {
        Map<IObjectId, Rights> result = new HashMap<>();
        Map<Class<? extends IObjectId>, IHandler> handlersByObjectIdClass = getHandlersByObjectIdClassMap();
        for (IObjectId id : objectIds)
        {
            if (id != null)
            {
                IHandler handler = handlersByObjectIdClass.get(id.getClass());
                if (handler != null)
                {
                    handler.handle(id);
                }
            }
        }
        Set<IHandler> handlers = new LinkedHashSet<>(handlersByObjectIdClass.values());
        for (IHandler handler : handlers)
        {
            handler.addRights(context, result);
        }
        return result;
    }

    private Map<Class<? extends IObjectId>, IHandler> getHandlersByObjectIdClassMap()
    {
        Map<Class<? extends IObjectId>, IHandler> map = new LinkedHashMap<>();

        IHandler sampleHandler = new SampleHandler();
        map.put(SampleIdentifier.class, sampleHandler);
        map.put(SamplePermId.class, sampleHandler);

        IHandler experimentHandler = new ExperimentHandler();
        map.put(ExperimentIdentifier.class, experimentHandler);
        map.put(ExperimentPermId.class, experimentHandler);

        IHandler dataSetHandler = new DataSetHandler();
        map.put(DataSetPermId.class, dataSetHandler);
        return map;
    }

    private static interface IHandler
    {
        void handle(IObjectId id);

        void addRights(IOperationContext context, Map<IObjectId, Rights> rightsByIds);
    }

    private static abstract class AbstractHandler<ID extends IObjectId, ENTITY> implements IHandler
    {
        private Class<ID> idClass;

        private List<ID> ids = new ArrayList<>();

        AbstractHandler(Class<ID> idClass)
        {
            this.idClass = idClass;
        }

        @SuppressWarnings("unchecked")
        @Override
        public void handle(IObjectId id)
        {
            if (idClass.isAssignableFrom(id.getClass()))
            {
                ids.add((ID) id);
            }
        }

        @Override
        public void addRights(IOperationContext context, Map<IObjectId, Rights> rightsByIds)
        {
            Map<ID, ENTITY> entitiesByIds = getEntitiesByIds(context, ids);
            Set<ID> unknownIds = new HashSet<>(ids);
            for (Entry<ID, ENTITY> entry : entitiesByIds.entrySet())
            {
                Set<Right> rights = new HashSet<>();
                ID id = entry.getKey();
                ENTITY entity = entry.getValue();
                try
                {
                    canUpdate(context, id, entity);
                    rights.add(Right.UPDATE);
                } catch (AuthorizationFailureException e)
                {
                    // silently ignored
                }
                rightsByIds.put(id, new Rights(rights));
                unknownIds.remove(id);
            }
            for (ID id : unknownIds)
            {
                Set<Right> rights = new HashSet<>();
                try
                {
                    ENTITY entity = createDummyEntity(context, id);
                    canCreate(context, entity);
                    rights.add(Right.CREATE);
                } catch (AuthorizationFailureException e)
                {
                    // silently ignored
                }
                rightsByIds.put(id, new Rights(rights));
            }
        }

        abstract Map<ID, ENTITY> getEntitiesByIds(IOperationContext context, Collection<ID> ids);

        abstract void canUpdate(IOperationContext context, ID id, ENTITY entity);

        abstract ENTITY createDummyEntity(IOperationContext context, ID id);

        abstract void canCreate(IOperationContext context, ENTITY entity);

    }

    private class SampleHandler extends AbstractHandler<ISampleId, SamplePE>
    {
        SampleHandler()
        {
            super(ISampleId.class);
        }

        @Override
        Map<ISampleId, SamplePE> getEntitiesByIds(IOperationContext context, Collection<ISampleId> ids)
        {
            return mapSampleByIdExecutor.map(context, ids);
        }

        @Override
        void canUpdate(IOperationContext context, ISampleId id, SamplePE entity)
        {
            sampleAuthorizationExecutor.canUpdate(context, id, entity);
        }

        @Override
        SamplePE createDummyEntity(IOperationContext context, ISampleId sampleId)
        {
            if (sampleId instanceof SamplePermId)
            {
                throw new UserFailureException("Unknown sample with perm id " + sampleId + ".");
            }
            if (sampleId instanceof SampleIdentifier == false)
            {
                throw new UserFailureException("Sample identifier of unsupported type ("
                        + sampleId.getClass().getName() + "): " + sampleId);
            }
            SpacePE homeSpace = context.getSession().tryGetHomeGroup();
            FullSampleIdentifier sampleIdentifier = new FullSampleIdentifier(((SampleIdentifier) sampleId).getIdentifier(),
                    homeSpace == null ? null : homeSpace.getCode());
            SampleIdentifierParts parts = sampleIdentifier.getParts();
            SamplePE samplePE = new SamplePE();
            samplePE.setCode(sampleIdentifier.getSampleCode());
            String spaceCode = parts.getSpaceCodeOrNull();
            if (StringUtils.isNotBlank(spaceCode))
            {
                SpacePermId spacePermId = new SpacePermId(spaceCode);
                SpacePE spacePE = mapSpaceByIdExecutor.map(context, Arrays.asList(spacePermId)).get(spacePermId);
                if (spacePE == null)
                {
                    throw new UserFailureException("Unknown space in sample identifier '" + sampleId + "'.");
                }
                samplePE.setSpace(spacePE);
            }
            String projectCode = parts.getProjectCodeOrNull();
            if (StringUtils.isNotBlank(projectCode))
            {
                if (StringUtils.isBlank(spaceCode))
                {
                    throw new UserFailureException("Unknown space in sample identifier '" + sampleId + "'.");
                }
                ProjectIdentifier projectIdentifier = new ProjectIdentifier(spaceCode, projectCode);
                ProjectPE projectPE = mapProjectByIdExecutor.map(context, Arrays.asList(projectIdentifier)).get(projectIdentifier);
                if (projectPE == null)
                {
                    throw new UserFailureException("Unknown project in sample identifier '" + sampleId + "'.");
                }
                samplePE.setProject(projectPE);
            }
            return samplePE;
        }

        @Override
        void canCreate(IOperationContext context, SamplePE sample)
        {
            sampleAuthorizationExecutor.canCreate(context, sample);
        }
    }

    private class ExperimentHandler extends AbstractHandler<IExperimentId, ExperimentPE>
    {
        ExperimentHandler()
        {
            super(IExperimentId.class);
        }

        @Override
        Map<IExperimentId, ExperimentPE> getEntitiesByIds(IOperationContext context, Collection<IExperimentId> ids)
        {
            return mapExperimentByIdExecutor.map(context, ids);
        }

        @Override
        void canUpdate(IOperationContext context, IExperimentId id, ExperimentPE entity)
        {
            experimentAuthorizationExecutor.canUpdate(context, id, entity);
        }

        @Override
        ExperimentPE createDummyEntity(IOperationContext context, IExperimentId id)
        {
            if (id instanceof ExperimentPermId)
            {
                throw new UserFailureException("Unknown experiment with perm id " + id + ".");
            }
            if (id instanceof ExperimentIdentifier == false)
            {
                throw new UserFailureException("Experiment identifier of unsupported type ("
                        + id.getClass().getName() + "): " + id);
            }

            ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier expeId =
                    ExperimentIdentifierFactory.parse(((ExperimentIdentifier) id).getIdentifier());

            ProjectIdentifier projectIdentifier = new ProjectIdentifier(expeId.getSpaceCode(), expeId.getProjectCode());
            ProjectPE projectPE = mapProjectByIdExecutor.map(context, Arrays.asList(projectIdentifier)).get(projectIdentifier);
            if (projectPE == null)
            {
                throw new UserFailureException("Unknown project in experiment identifier '" + id + "'.");
            }
            ExperimentPE experimentPE = new ExperimentPE();
            experimentPE.setProject(projectPE);
            experimentPE.setCode(expeId.getExperimentCode());
            return experimentPE;
        }

        @Override
        void canCreate(IOperationContext context, ExperimentPE entity)
        {
            experimentAuthorizationExecutor.canCreate(context, entity);

        }
    }

    private class DataSetHandler implements IHandler
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
