/*
 * Copyright 2014 ETH Zuerich, CISD
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

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.server.api.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.api.v3.executor.entity.AbstractCreateEntityExecutor;
import ch.ethz.sis.openbis.generic.server.api.v3.executor.entity.IMapEntityTypeByIdExecutor;
import ch.ethz.sis.openbis.generic.server.api.v3.executor.property.IUpdateEntityPropertyExecutor;
import ch.ethz.sis.openbis.generic.server.api.v3.executor.tag.IAddTagToEntityExecutor;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.dataset.Complete;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.dataset.DataSetCreation;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.dataset.DataSetKind;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.dataset.ExternalDataCreation;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.dataset.DataSetPermId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.entitytype.IEntityTypeId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.tag.ITagId;
import ch.ethz.sis.openbis.generic.shared.api.v3.exceptions.ObjectNotFoundException;
import ch.ethz.sis.openbis.generic.shared.api.v3.exceptions.UnauthorizedObjectAccessException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.common.types.BooleanOrUnknown;
import ch.systemsx.cisd.openbis.generic.server.authorization.validator.DataSetPEByExperimentOrSampleIdentifierValidator;
import ch.systemsx.cisd.openbis.generic.server.business.bo.DataAccessExceptionTranslator;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExternalDataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.IEntityPropertiesHolder;
import ch.systemsx.cisd.openbis.generic.shared.dto.IEntityWithMetaprojects;
import ch.systemsx.cisd.openbis.generic.shared.dto.LinkDataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.util.RelationshipUtils;

/**
 * @author pkupczyk
 */
@Component
public class CreateDataSetExecutor extends AbstractCreateEntityExecutor<DataSetCreation, DataPE, DataSetPermId> implements ICreateDataSetExecutor
{

    @Autowired
    private IDAOFactory daoFactory;

    @Autowired
    private IMapEntityTypeByIdExecutor mapEntityTypeByIdExecutor;

    @Autowired
    private ISetDataSetStorageFormatExecutor setDataSetStorageFormatExecutor;

    @Autowired
    private ISetDataSetLocatorTypeExecutor setDataSetLocatorTypeExecutor;

    @Autowired
    private ISetDataSetDataStoreExecutor setDataSetDataStoreExecutor;

    @Autowired
    private ISetDataSetExternalSystemExecutor setDataSetExternalSystemExecutor;

    @Autowired
    private ISetDataSetExperimentExecutor setDataSetExperimentExecutor;

    @Autowired
    private ISetDataSetSampleExecutor setDataSetSampleExecutor;

    @Autowired
    private ISetDataSetRelatedDataSetsExecutor setDataSetRelatedDataSetsExecutor;

    @Autowired
    private IUpdateEntityPropertyExecutor updateEntityPropertyExecutor;

    @Autowired
    private IAddTagToEntityExecutor addTagToEntityExecutor;

    @Autowired
    private IVerifyDataSetExecutor verifyDataSetExecutor;

    @Override
    protected void checkData(IOperationContext context, DataSetCreation creation)
    {
        if (StringUtils.isEmpty(creation.getCode()))
        {
            throw new UserFailureException("Code cannot be empty.");
        }
        if (creation.getTypeId() == null)
        {
            throw new UserFailureException("Type id cannot be null.");
        }
    }

    @Override
    protected List<DataPE> createEntities(IOperationContext context, Collection<DataSetCreation> creations)
    {
        Collection<IEntityTypeId> typeIds = new HashSet<IEntityTypeId>();

        for (DataSetCreation creation : creations)
        {
            typeIds.add(creation.getTypeId());
        }

        Map<IEntityTypeId, EntityTypePE> types = mapEntityTypeByIdExecutor.map(context, EntityKind.DATA_SET, typeIds);

        List<DataPE> dataSets = new LinkedList<DataPE>();
        for (DataSetCreation creation : creations)
        {
            DataSetTypePE type = (DataSetTypePE) types.get(creation.getTypeId());

            if (type == null)
            {
                throw new ObjectNotFoundException(creation.getTypeId());
            }

            DataSetKind kind = DataSetKind.valueOf(type.getDataSetKind());
            DataPE dataSet = null;

            if (DataSetKind.PHYSICAL.equals(kind))
            {
                dataSet = createPhysicalDataSet(context, creation);
            } else if (DataSetKind.CONTAINER.equals(kind))
            {
                dataSet = createContainerDataSet(context, creation);
            } else if (DataSetKind.LINK.equals(kind))
            {
                dataSet = createLinkDataSet(creation);
            } else
            {
                throw new IllegalArgumentException("Unsupported data set kind: " + kind);
            }

            dataSet.setCode(creation.getCode());
            dataSet.setDataSetType(type);
            dataSet.setDerived(false == creation.isMeasured());
            dataSet.setRegistrator(context.getSession().tryGetPerson());
            RelationshipUtils.updateModificationDateAndModifier(dataSet, context.getSession().tryGetPerson());

            dataSets.add(dataSet);
        }

        return dataSets;
    }

    private ExternalDataPE createPhysicalDataSet(IOperationContext context, DataSetCreation creation)
    {
        ExternalDataCreation externalCreation = creation.getExternalData();

        if (externalCreation == null)
        {
            throw new IllegalArgumentException("External data cannot be null for a physical data set.");
        }

        ExternalDataPE dataSet = new ExternalDataPE();
        dataSet.setShareId(externalCreation.getShareId());
        dataSet.setLocation(externalCreation.getLocation());
        dataSet.setSize(externalCreation.getSize());
        if (externalCreation.getSpeedHint() != null)
        {
            dataSet.setSpeedHint(externalCreation.getSpeedHint());
        }

        BooleanOrUnknown complete = BooleanOrUnknown.U;
        if (Complete.YES.equals(externalCreation.getComplete()))
        {
            complete = BooleanOrUnknown.T;
        } else if (Complete.NO.equals(externalCreation.getComplete()))
        {
            complete = BooleanOrUnknown.F;
        }
        dataSet.setComplete(complete);

        return dataSet;
    }

    private DataPE createContainerDataSet(IOperationContext context, DataSetCreation creation)
    {
        DataPE dataSet = new DataPE();
        return dataSet;
    }

    private LinkDataPE createLinkDataSet(DataSetCreation creation)
    {
        LinkDataPE dataSet = new LinkDataPE();

        // TODO
        // dataSet.setExternalCode(newData.getExternalCode());

        return dataSet;
    }

    @Override
    protected DataSetPermId createPermId(IOperationContext context, DataPE entity)
    {
        return new DataSetPermId(entity.getPermId());
    }

    @Override
    protected void checkAccess(IOperationContext context, DataPE entity)
    {
        if (false == new DataSetPEByExperimentOrSampleIdentifierValidator().doValidation(context.getSession().tryGetPerson(), entity))
        {
            throw new UnauthorizedObjectAccessException(new DataSetPermId(entity.getPermId()));
        }
    }

    @Override
    protected void checkBusinessRules(IOperationContext context, Collection<DataPE> entities)
    {
        verifyDataSetExecutor.verify(context, entities);
    }

    @Override
    protected void updateBatch(IOperationContext context, Map<DataSetCreation, DataPE> entitiesMap)
    {
        setDataSetStorageFormatExecutor.set(context, entitiesMap);
        setDataSetLocatorTypeExecutor.set(context, entitiesMap);
        setDataSetDataStoreExecutor.set(context, entitiesMap);
        setDataSetExternalSystemExecutor.set(context, entitiesMap);
        setDataSetExperimentExecutor.set(context, entitiesMap);
        setDataSetSampleExecutor.set(context, entitiesMap);

        Map<IEntityPropertiesHolder, Map<String, String>> propertyMap = new HashMap<IEntityPropertiesHolder, Map<String, String>>();
        for (Map.Entry<DataSetCreation, DataPE> entry : entitiesMap.entrySet())
        {
            propertyMap.put(entry.getValue(), entry.getKey().getProperties());
        }
        updateEntityPropertyExecutor.update(context, propertyMap);
    }

    @Override
    protected void updateAll(IOperationContext context, Map<DataSetCreation, DataPE> entitiesMap)
    {
        Map<IEntityWithMetaprojects, Collection<? extends ITagId>> tagMap = new HashMap<IEntityWithMetaprojects, Collection<? extends ITagId>>();

        for (Map.Entry<DataSetCreation, DataPE> entry : entitiesMap.entrySet())
        {
            DataSetCreation creation = entry.getKey();
            DataPE entity = entry.getValue();
            tagMap.put(entity, creation.getTagIds());
        }

        addTagToEntityExecutor.add(context, tagMap);
        setDataSetRelatedDataSetsExecutor.set(context, entitiesMap);
    }

    @Override
    protected List<DataPE> list(IOperationContext context, Collection<Long> ids)
    {
        return daoFactory.getDataDAO().listByIDs(ids);
    }

    @Override
    protected void save(IOperationContext context, List<DataPE> entities, boolean clearCache)
    {
        daoFactory.getDataDAO().createOrUpdateDataSets(entities, context.getSession().tryGetPerson());
    }

    @Override
    protected void handleException(DataAccessException e)
    {
        DataAccessExceptionTranslator.throwException(e, EntityKind.DATA_SET.getLabel(), EntityKind.DATA_SET);
    }

}
