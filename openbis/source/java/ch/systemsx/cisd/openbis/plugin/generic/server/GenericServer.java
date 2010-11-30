/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.plugin.generic.server;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.authentication.ISessionManager;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.spring.IInvocationLoggerContext;
import ch.systemsx.cisd.openbis.generic.server.AbstractServer;
import ch.systemsx.cisd.openbis.generic.server.batch.BatchOperationExecutor;
import ch.systemsx.cisd.openbis.generic.server.batch.IBatchOperation;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IExperimentBO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IExternalDataBO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IMaterialBO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IMaterialTable;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IProjectBO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.ISampleBO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.MaterialUpdateDTO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.samplelister.ISampleLister;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.DynamicPropertyEvaluationOperation;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDynamicPropertyEvaluationScheduler;
import ch.systemsx.cisd.openbis.generic.server.plugin.IDataSetTypeSlaveServerPlugin;
import ch.systemsx.cisd.openbis.generic.server.plugin.ISampleTypeSlaveServerPlugin;
import ch.systemsx.cisd.openbis.generic.shared.ICommonServer;
import ch.systemsx.cisd.openbis.generic.shared.basic.IdentifierExtractor;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AttachmentWithContent;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Code;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetUpdateResult;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentUpdateResult;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ListMaterialCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ListOrSearchSampleCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Material;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewAttachment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewBasicExperiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewDataSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewDataSetsWithTypes;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewExperiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewExperimentsWithType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewMaterial;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSamplesWithTypes;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleBatchUpdateDetails;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleParentWithDerived;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleUpdateResult;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.UpdatedBasicExperiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.UpdatedExperimentsWithType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.UpdatedSample;
import ch.systemsx.cisd.openbis.generic.shared.dto.CodeConverter;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentBatchUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExternalDataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.IEntityInformationWithPropertiesHolder;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleBatchUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifierFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.GroupIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.IdentifierHelper;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifierFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifierFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.translator.AttachmentTranslator;
import ch.systemsx.cisd.openbis.generic.shared.translator.ExperimentTranslator;
import ch.systemsx.cisd.openbis.generic.shared.translator.MaterialTranslator;
import ch.systemsx.cisd.openbis.generic.shared.translator.MaterialTypeTranslator;
import ch.systemsx.cisd.openbis.generic.shared.translator.SampleTranslator;
import ch.systemsx.cisd.openbis.generic.shared.util.ServerUtils;
import ch.systemsx.cisd.openbis.plugin.generic.shared.IGenericServer;
import ch.systemsx.cisd.openbis.plugin.generic.shared.ResourceNames;

/**
 * Implementation of client-server interface.
 * 
 * @author Franz-Josef Elmer
 */
@Component(ResourceNames.GENERIC_PLUGIN_SERVER)
public final class GenericServer extends AbstractServer<IGenericServer> implements
        ch.systemsx.cisd.openbis.plugin.generic.shared.IGenericServer
{
    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            GenericServer.class);

    @Resource(name = ResourceNames.GENERIC_BUSINESS_OBJECT_FACTORY)
    private IGenericBusinessObjectFactory businessObjectFactory;

    @Resource(name = ch.systemsx.cisd.openbis.generic.shared.ResourceNames.COMMON_SERVER)
    protected ICommonServer commonServer;

    public GenericServer()
    {
    }

    @Private
    GenericServer(final ISessionManager<Session> sessionManager, final IDAOFactory daoFactory,
            final IGenericBusinessObjectFactory businessObjectFactory,
            final ISampleTypeSlaveServerPlugin sampleTypeSlaveServerPlugin,
            final IDataSetTypeSlaveServerPlugin dataSetTypeSlaveServerPlugin)
    {
        super(sessionManager, daoFactory, sampleTypeSlaveServerPlugin, dataSetTypeSlaveServerPlugin);
        this.businessObjectFactory = businessObjectFactory;
    }

    //
    // IInvocationLoggerFactory
    //

    /**
     * Creates a logger used to log invocations of objects of this class.
     */
    public IGenericServer createLogger(IInvocationLoggerContext context)
    {
        return new GenericServerLogger(getSessionManager(), context);
    }

    //
    // IGenericServer
    //

    public final SampleParentWithDerived getSampleInfo(final String sessionToken,
            final SampleIdentifier identifier)
    {
        assert sessionToken != null : "Unspecified session token.";
        assert identifier != null : "Unspecified sample identifier.";

        final Session session = getSession(sessionToken);
        final ISampleBO sampleBO = businessObjectFactory.createSampleBO(session);
        sampleBO.loadBySampleIdentifier(identifier);
        sampleBO.enrichWithAttachments();
        sampleBO.enrichWithPropertyTypes();
        final SamplePE sample = sampleBO.getSample();
        return SampleTranslator.translate(getSampleTypeSlaveServerPlugin(sample.getSampleType())
                .getSampleInfo(session, sample), session.getBaseIndexURL());
    }

    public final SampleParentWithDerived getSampleInfo(final String sessionToken,
            final TechId sampleId)
    {
        assert sessionToken != null : "Unspecified session token.";
        assert sampleId != null : "Unspecified sample techId.";

        final Session session = getSession(sessionToken);
        final ISampleBO sampleBO = businessObjectFactory.createSampleBO(session);
        sampleBO.loadDataByTechId(sampleId);
        sampleBO.enrichWithAttachments();
        sampleBO.enrichWithPropertyTypes();
        final SamplePE sample = sampleBO.getSample();
        return SampleTranslator.translate(getSampleTypeSlaveServerPlugin(sample.getSampleType())
                .getSampleInfo(session, sample), session.getBaseIndexURL());
    }

    public final void registerSample(final String sessionToken, final NewSample newSample,
            final Collection<NewAttachment> attachments)
    {
        assert sessionToken != null : "Unspecified session token.";
        assert newSample != null : "Unspecified new sample.";

        final Session session = getSession(sessionToken);
        final ISampleBO sampleBO = businessObjectFactory.createSampleBO(session);
        sampleBO.define(newSample);
        sampleBO.save();
        for (NewAttachment attachment : attachments)
        {
            sampleBO.addAttachment(AttachmentTranslator.translate(attachment));
        }
        sampleBO.save();
        scheduleDynamicPropertiesEvaluation(
                getDAOFactory().getDynamicPropertyEvaluationScheduler(), SamplePE.class,
                Arrays.asList(sampleBO.getSample()));
    }

    public Experiment getExperimentInfo(final String sessionToken,
            final ExperimentIdentifier identifier)
    {
        final Session session = getSession(sessionToken);
        final IExperimentBO experimentBO = businessObjectFactory.createExperimentBO(session);
        experimentBO.loadByExperimentIdentifier(identifier);
        experimentBO.enrichWithProperties();
        experimentBO.enrichWithAttachments();
        final ExperimentPE experiment = experimentBO.getExperiment();
        if (experiment == null)
        {
            throw UserFailureException.fromTemplate(
                    "No experiment could be found with given identifier '%s'.", identifier);
        }
        return ExperimentTranslator.translate(experiment, session.getBaseIndexURL(),
                ExperimentTranslator.LoadableFields.PROPERTIES,
                ExperimentTranslator.LoadableFields.ATTACHMENTS);
    }

    public Experiment getExperimentInfo(final String sessionToken, final TechId experimentId)
    {
        final Session session = getSession(sessionToken);
        final IExperimentBO experimentBO = businessObjectFactory.createExperimentBO(session);
        experimentBO.loadDataByTechId(experimentId);
        experimentBO.enrichWithProperties();
        experimentBO.enrichWithAttachments();
        final ExperimentPE experiment = experimentBO.getExperiment();
        return ExperimentTranslator.translate(experiment, session.getBaseIndexURL(),
                ExperimentTranslator.LoadableFields.PROPERTIES,
                ExperimentTranslator.LoadableFields.ATTACHMENTS);
    }

    public Material getMaterialInfo(final String sessionToken, final TechId materialId)
    {
        final Session session = getSession(sessionToken);
        final IMaterialBO materialBO = businessObjectFactory.createMaterialBO(session);
        materialBO.loadDataByTechId(materialId);
        materialBO.enrichWithProperties();
        final MaterialPE material = materialBO.getMaterial();
        return MaterialTranslator.translate(material, true);
    }

    public ExternalData getDataSetInfo(final String sessionToken, final TechId datasetId)
    {
        return commonServer.getDataSetInfo(sessionToken, datasetId);
    }

    public AttachmentWithContent getExperimentFileAttachment(final String sessionToken,
            final TechId experimentId, final String filename, final int version)
            throws UserFailureException
    {
        final Session session = getSession(sessionToken);
        final IExperimentBO experimentBO = businessObjectFactory.createExperimentBO(session);
        experimentBO.loadDataByTechId(experimentId);
        return AttachmentTranslator.translateWithContent(experimentBO.getExperimentFileAttachment(
                filename, version));
    }

    public final void registerOrUpdateSamples(final String sessionToken,
            final List<NewSamplesWithTypes> newSamplesWithType) throws UserFailureException
    {
        assert sessionToken != null : "Unspecified session token.";
        final Session session = getSession(sessionToken);
        for (NewSamplesWithTypes samples : newSamplesWithType)
        {
            if (samples.isAllowUpdateIfExist() == false)
            {
                registerSamples(session, samples, null);
            } else
            {
                BatchOperationExecutor.executeInBatches(new SampleBatchRegisterOrUpdate(
                        businessObjectFactory.createSampleLister(session), samples.getNewSamples(),
                        samples.getSampleType(), session));
            }
        }
    }

    private class SampleBatchRegisterOrUpdate implements IBatchOperation<NewSample>
    {

        private final List<NewSample> entities;

        private final SampleType sampleType;

        private final ISampleLister sampleLister;

        private final Session session;

        public SampleBatchRegisterOrUpdate(ISampleLister sampleLister, List<NewSample> entities,
                SampleType sampleType, Session session)
        {
            this.sampleLister = sampleLister;
            this.entities = entities;
            this.sampleType = sampleType;
            this.session = session;
        }

        public void execute(List<NewSample> newSamples)
        {
            List<Sample> existingSamples = new ArrayList<Sample>();
            List<String> codes = SampleRegisterOrUpdateUtil.extractCodes(newSamples, false);
            List<Sample> list =
                    sampleLister.list(SampleRegisterOrUpdateUtil
                            .createListSamplesByCodeCriteria(codes));
            existingSamples.addAll(list);
            codes = SampleRegisterOrUpdateUtil.extractCodes(newSamples, true);
            ListOrSearchSampleCriteria criteria =
                    SampleRegisterOrUpdateUtil.createListSamplesByCodeCriteria(codes);
            List<Sample> existingContainers = sampleLister.list(criteria);
            for (Sample s : existingContainers)
            {
                existingSamples.addAll(sampleLister.list(new ListOrSearchSampleCriteria(
                        ListOrSearchSampleCriteria.createForContainer(new TechId(s.getId())))));
            }
            List<NewSample> samplesToUpdate =
                    SampleRegisterOrUpdateUtil.getSamplesToUpdate(newSamples, existingSamples);
            List<NewSample> samplesToRegister = new ArrayList<NewSample>(newSamples);
            samplesToRegister.removeAll(samplesToUpdate);
            registerSamples(session, new NewSamplesWithTypes(sampleType, samplesToRegister), null);
            updateSamples(session, new NewSamplesWithTypes(sampleType, samplesToUpdate));
        }

        public List<NewSample> getAllEntities()
        {
            return entities;
        }

        public String getEntityName()
        {
            return "sample";
        }

        public String getOperationName()
        {
            return "update/register preprocessing";
        }

    }

    public final void registerSamples(final String sessionToken,
            final List<NewSamplesWithTypes> newSamplesWithType) throws UserFailureException
    {
        assert sessionToken != null : "Unspecified session token.";
        final Session session = getSession(sessionToken);
        for (NewSamplesWithTypes samples : newSamplesWithType)
        {
            registerSamples(session, samples, null);
        }
    }

    public void updateSamples(String sessionToken, List<NewSamplesWithTypes> newSamplesWithType)
            throws UserFailureException
    {
        assert sessionToken != null : "Unspecified session token.";
        final Session session = getSession(sessionToken);
        for (NewSamplesWithTypes samples : newSamplesWithType)
        {
            updateSamples(session, samples);
        }
    }

    public void updateDataSets(String sessionToken, NewDataSetsWithTypes dataSets)
            throws UserFailureException
    {
        assert sessionToken != null : "Unspecified session token.";
        final Session session = getSession(sessionToken);
        final List<NewDataSet> newDataSets = dataSets.getNewDataSets();
        // Does nothing if samples list is empty.
        if (newDataSets.size() == 0)
        {
            return;
        }
        ServerUtils.prevalidate(newDataSets, "data set");
        final DataSetTypePE dataSetType =
                getDAOFactory().getDataSetTypeDAO().tryToFindDataSetTypeByCode(
                        dataSets.getDataSetType().getCode());
        if (dataSetType == null)
        {
            throw UserFailureException.fromTemplate("Data set type with code '%s' does not exist.",
                    dataSets.getDataSetType());
        }
        getDataSetTypeSlaveServerPlugin(dataSetType).updateDataSets(session, newDataSets);
    }

    private void updateSamples(final Session session,
            final NewSamplesWithTypes updatedSamplesWithType)
    {
        final SampleType sampleType = updatedSamplesWithType.getSampleType();
        final List<NewSample> updatedSamples = updatedSamplesWithType.getNewSamples();
        assert sampleType != null : "Unspecified sample type.";
        assert updatedSamples != null : "Unspecified new samples.";

        // Does nothing if samples list is empty.
        if (updatedSamples.size() == 0)
        {
            return;
        }

        ServerUtils.prevalidate(updatedSamples, "sample");
        final String sampleTypeCode = sampleType.getCode();
        final SampleTypePE sampleTypePE =
                getDAOFactory().getSampleTypeDAO().tryFindSampleTypeByCode(sampleTypeCode);
        if (sampleTypePE == null)
        {
            throw UserFailureException.fromTemplate("Sample type with code '%s' does not exist.",
                    sampleTypeCode);
        }

        getSampleTypeSlaveServerPlugin(sampleTypePE).updateSamples(session,
                convertSamples(updatedSamples));
    }

    private List<SampleBatchUpdatesDTO> convertSamples(final List<NewSample> updatedSamples)
    {
        List<SampleBatchUpdatesDTO> samples = new ArrayList<SampleBatchUpdatesDTO>();

        for (NewSample updatedSample : updatedSamples)
        {
            final SampleIdentifier oldSampleIdentifier =
                    SampleIdentifierFactory.parse(updatedSample.getIdentifier());
            final List<IEntityProperty> properties = Arrays.asList(updatedSample.getProperties());
            final ExperimentIdentifier experimentIdentifierOrNull;
            final SampleIdentifier newSampleIdentifier;
            if (updatedSample.getExperimentIdentifier() != null)
            {
                // experiment is provided - new sample identifier takes experiment space
                experimentIdentifierOrNull =
                        new ExperimentIdentifierFactory(updatedSample.getExperimentIdentifier())
                                .createIdentifier();
                newSampleIdentifier =
                        new SampleIdentifier(new GroupIdentifier(
                                experimentIdentifierOrNull.getDatabaseInstanceCode(),
                                experimentIdentifierOrNull.getSpaceCode()),
                                oldSampleIdentifier.getSampleCode());
            } else
            {
                // no experiment - leave sample identifier unchanged
                experimentIdentifierOrNull = null;
                newSampleIdentifier = oldSampleIdentifier;
            }
            final String[] parentsOrNull = updatedSample.getParentsOrNull();
            final String containerIdentifierOrNull = updatedSample.getContainerIdentifier();
            final SampleBatchUpdateDetails batchUpdateDetails =
                    createBatchUpdateDetails(updatedSample);

            samples.add(new SampleBatchUpdatesDTO(oldSampleIdentifier, properties,
                    experimentIdentifierOrNull, newSampleIdentifier, containerIdentifierOrNull,
                    parentsOrNull, batchUpdateDetails));
        }
        return samples;
    }

    SampleBatchUpdateDetails createBatchUpdateDetails(NewSample sample)
    {
        if (sample instanceof UpdatedSample)
        {
            return ((UpdatedSample) sample).getBatchUpdateDetails();
        } else
        {
            IEntityProperty[] properties = sample.getProperties();
            Set<String> propertyCodes = new HashSet<String>();
            for (IEntityProperty p : properties)
            {
                propertyCodes.add(p.getPropertyType().getCode());
            }
            SampleBatchUpdateDetails result =
                    new SampleBatchUpdateDetails(false, false, false, propertyCodes);
            return result;
        }
    }

    public void registerExperiment(String sessionToken, NewExperiment newExperiment,
            final Collection<NewAttachment> attachments)
    {
        assert sessionToken != null : "Unspecified session token.";
        assert newExperiment != null : "Unspecified new experiment.";

        final Session session = getSession(sessionToken);

        if (newExperiment.isRegisterSamples())
        {
            registerSamples(sessionToken, newExperiment.getNewSamples());
        }
        final IExperimentBO experimentBO = businessObjectFactory.createExperimentBO(session);
        experimentBO.define(newExperiment);
        experimentBO.save();
        for (NewAttachment attachment : attachments)
        {
            experimentBO.addAttachment(AttachmentTranslator.translate(attachment));
        }
        experimentBO.save();

        if (newExperiment.getSamples() != null && newExperiment.getSamples().length > 0)
        {
            ExperimentPE experiment = experimentBO.getExperiment();
            List<SampleIdentifier> sampleIdentifiers = null;
            if (newExperiment.getNewSamples() == null)
            {
                sampleIdentifiers =
                        IdentifierHelper.extractSampleIdentifiers(newExperiment.getSamples());
            } else
            {
                sampleIdentifiers = IdentifierHelper.extractSampleIdentifiers(newExperiment);
            }
            for (SampleIdentifier si : sampleIdentifiers)
            {
                IdentifierHelper
                        .fillAndCheckGroup(si, experiment.getProject().getSpace().getCode());
            }
            for (SampleIdentifier si : sampleIdentifiers)
            {
                ISampleBO sampleBO = businessObjectFactory.createSampleBO(session);
                sampleBO.loadBySampleIdentifier(si);
                sampleBO.setExperiment(experiment);
            }
        }
        scheduleDynamicPropertiesEvaluation(
                getDAOFactory().getDynamicPropertyEvaluationScheduler(), ExperimentPE.class,
                Arrays.asList(experimentBO.getExperiment()));
    }

    public void registerMaterials(String sessionToken, String materialTypeCode,
            final List<NewMaterial> newMaterials)
    {
        assert sessionToken != null : "Unspecified session token.";
        assert materialTypeCode != null : "Unspecified material type.";
        assert newMaterials != null : "Unspecified new materials.";

        // Does nothing if material list is empty.
        if (newMaterials.size() == 0)
        {
            return;
        }
        ServerUtils.prevalidate(newMaterials, "material");
        final MaterialTypePE materialTypePE = findMaterialType(materialTypeCode);
        final Session session = getSession(sessionToken);
        IBatchOperation<NewMaterial> strategy = new IBatchOperation<NewMaterial>()
            {

                public void execute(List<NewMaterial> entities)
                {
                    final IMaterialTable materialTable =
                            businessObjectFactory.createMaterialTable(session);
                    materialTable.add(entities, materialTypePE);
                    materialTable.save();
                }

                public List<NewMaterial> getAllEntities()
                {
                    return newMaterials;
                }

                public String getEntityName()
                {
                    return "material";
                }

                public String getOperationName()
                {
                    return "register";
                }
            };
        BatchOperationExecutor.executeInBatches(strategy);
    }

    public int updateMaterials(String sessionToken, String materialTypeCode,
            final List<NewMaterial> newMaterials, final boolean ignoreUnregisteredMaterials)
            throws UserFailureException
    {
        assert sessionToken != null : "Unspecified session token.";
        assert materialTypeCode != null : "Unspecified material type.";
        assert newMaterials != null : "Unspecified new materials.";

        // Does nothing if material list is empty.
        if (newMaterials.size() == 0)
        {
            return 0;
        }
        class Counter
        {
            int count;
        }
        final Counter counter = new Counter();
        ServerUtils.prevalidate(newMaterials, "material");
        final Map<String/* code */, Material> existingMaterials =
                listMaterials(sessionToken, materialTypeCode);
        final Session session = getSession(sessionToken);
        IBatchOperation<NewMaterial> strategy = new IBatchOperation<NewMaterial>()
            {
                public void execute(List<NewMaterial> entities)
                {
                    List<MaterialUpdateDTO> materialUpdates = new ArrayList<MaterialUpdateDTO>();
                    for (NewMaterial material : entities)
                    {
                        Material existingMaterial =
                                existingMaterials.get(CodeConverter.tryToDatabase(material
                                        .getCode()));
                        if (existingMaterial != null)
                        {
                            materialUpdates.add(createMaterialUpdate(existingMaterial, material));
                            counter.count++;
                        } else if (ignoreUnregisteredMaterials == false)
                        {
                            throw new UserFailureException("Can not update unregistered material '"
                                    + material.getCode()
                                    + "'. Please use checkbox for ignoring unregistered materials.");
                        }
                    }
                    updateMaterials(session.getSessionToken(), materialUpdates);
                }

                public List<NewMaterial> getAllEntities()
                {
                    return newMaterials;
                }

                public String getEntityName()
                {
                    return "material";
                }

                public String getOperationName()
                {
                    return "update";
                }
            };
        BatchOperationExecutor.executeInBatches(strategy);
        return counter.count;
    }

    private MaterialTypePE findMaterialType(String materialTypeCode)
    {
        final MaterialTypePE materialTypePE =
                (MaterialTypePE) getDAOFactory().getEntityTypeDAO(EntityKind.MATERIAL)
                        .tryToFindEntityTypeByCode(materialTypeCode);
        if (materialTypePE == null)
        {
            throw UserFailureException.fromTemplate("Material type with code '%s' does not exist.",
                    materialTypeCode);
        }
        return materialTypePE;
    }

    public AttachmentWithContent getProjectFileAttachment(String sessionToken, TechId projectId,
            String fileName, int version)
    {
        final Session session = getSession(sessionToken);
        final IProjectBO bo = businessObjectFactory.createProjectBO(session);
        bo.loadDataByTechId(projectId);
        return AttachmentTranslator.translateWithContent(bo.getProjectFileAttachment(fileName,
                version));
    }

    public AttachmentWithContent getSampleFileAttachment(String sessionToken, TechId sampleId,
            String fileName, int version)
    {
        final Session session = getSession(sessionToken);
        final ISampleBO bo = businessObjectFactory.createSampleBO(session);
        bo.loadDataByTechId(sampleId);
        return AttachmentTranslator.translateWithContent(bo.getSampleFileAttachment(fileName,
                version));
    }

    public List<String> generateCodes(String sessionToken, String prefix, int number)
    {
        checkSession(sessionToken);
        ArrayList<String> result = new ArrayList<String>();
        for (int i = 0; i < number; i++)
        {
            result.add(prefix + getDAOFactory().getCodeSequenceDAO().getNextCodeSequenceId());
        }
        return result;
    }

    public ExperimentUpdateResult updateExperiment(String sessionToken, ExperimentUpdatesDTO updates)
    {
        final Session session = getSession(sessionToken);
        if (updates.isRegisterSamples())
        {
            registerSamples(sessionToken, updates.getNewSamples());
        }
        final IExperimentBO experimentBO = businessObjectFactory.createExperimentBO(session);
        experimentBO.update(updates);
        experimentBO.save();
        ExperimentUpdateResult result = new ExperimentUpdateResult();
        ExperimentPE experiment = experimentBO.getExperiment();
        result.setModificationDate(experiment.getModificationDate());
        result.setSamples(Code.extractCodes(experiment.getSamples()));
        scheduleDynamicPropertiesEvaluation(
                getDAOFactory().getDynamicPropertyEvaluationScheduler(), ExperimentPE.class,
                Arrays.asList(experimentBO.getExperiment()));
        return result;
    }

    public Date updateMaterial(String sessionToken, TechId materialId,
            List<IEntityProperty> properties, Date version)
    {
        final Session session = getSession(sessionToken);
        final IMaterialBO materialBO = businessObjectFactory.createMaterialBO(session);
        materialBO.update(new MaterialUpdateDTO(materialId, properties, version));
        materialBO.save();
        return materialBO.getMaterial().getModificationDate();
    }

    public SampleUpdateResult updateSample(String sessionToken, SampleUpdatesDTO updates)
    {
        final Session session = getSession(sessionToken);
        final ISampleBO sampleBO = businessObjectFactory.createSampleBO(session);
        sampleBO.update(updates);
        sampleBO.save();
        SampleUpdateResult result = new SampleUpdateResult();
        SamplePE sample = sampleBO.getSample();
        result.setModificationDate(sample.getModificationDate());
        List<String> parents = IdentifierExtractor.extract(sample.getParents());
        Collections.sort(parents);
        result.setParents(parents);
        scheduleDynamicPropertiesEvaluation(
                getDAOFactory().getDynamicPropertyEvaluationScheduler(), SamplePE.class,
                Arrays.asList(sampleBO.getSample()));
        return result;
    }

    public DataSetUpdateResult updateDataSet(String sessionToken, DataSetUpdatesDTO updates)
    {
        final Session session = getSession(sessionToken);
        final IExternalDataBO dataSetBO = businessObjectFactory.createExternalDataBO(session);
        dataSetBO.update(updates);
        DataSetUpdateResult result = new DataSetUpdateResult();
        ExternalDataPE externalData = dataSetBO.getExternalData();
        result.setModificationDate(externalData.getModificationDate());
        result.setParentCodes(Code.extractCodes(externalData.getParents()));
        return result;
    }

    public void registerOrUpdateMaterials(String sessionToken, String materialTypeCode,
            List<NewMaterial> materials)
    {
        Map<String/* code */, Material> existingMaterials =
                listMaterials(sessionToken, materialTypeCode);
        List<NewMaterial> materialsToRegister = new ArrayList<NewMaterial>();
        List<MaterialUpdateDTO> materialUpdates = new ArrayList<MaterialUpdateDTO>();
        for (NewMaterial material : materials)
        {
            Material existingMaterial =
                    existingMaterials.get(CodeConverter.tryToDatabase(material.getCode()));
            if (existingMaterial != null)
            {
                materialUpdates.add(createMaterialUpdate(existingMaterial, material));
            } else
            {
                materialsToRegister.add(material);
            }
        }
        registerMaterials(sessionToken, materialTypeCode, materialsToRegister);
        updateMaterials(sessionToken, materialUpdates);
        operationLog.info(String.format("Number of newly registered materials: %d, "
                + "number of existing materials which have been updated: %d",
                materialsToRegister.size(), materialUpdates.size()));

    }

    private static MaterialUpdateDTO createMaterialUpdate(Material existingMaterial,
            NewMaterial material)
    {
        return new MaterialUpdateDTO(new TechId(existingMaterial.getId()), Arrays.asList(material
                .getProperties()), existingMaterial.getModificationDate());
    }

    private void updateMaterials(String sessionToken, List<MaterialUpdateDTO> updates)
    {
        if (updates.size() == 0)
        {
            return;
        }
        final Session session = getSession(sessionToken);
        IMaterialTable materialTable = businessObjectFactory.createMaterialTable(session);
        materialTable.update(updates);
        materialTable.save();
    }

    private Map<String/* code */, Material> listMaterials(String sessionToken,
            String materialTypeCode)
    {
        checkSession(sessionToken);
        EntityTypePE entityTypePE =
                getDAOFactory().getEntityTypeDAO(EntityKind.MATERIAL).tryToFindEntityTypeByCode(
                        materialTypeCode);
        MaterialType materialType = MaterialTypeTranslator.translateSimple(entityTypePE);
        List<Material> materials =
                commonServer.listMaterials(sessionToken, new ListMaterialCriteria(materialType),
                        false);
        return asCodeToMaterialMap(materials);
    }

    private static Map<String, Material> asCodeToMaterialMap(List<Material> materials)
    {
        Map<String, Material> map = new HashMap<String, Material>();
        for (Material material : materials)
        {
            map.put(material.getCode(), material);
        }
        return map;
    }

    /**
     * Schedules evaluation of dynamic properties on specified entities. After evaluation is done
     * the entities will be indexed.
     */
    private static <T extends IEntityInformationWithPropertiesHolder> void scheduleDynamicPropertiesEvaluation(
            IDynamicPropertyEvaluationScheduler scheduler, Class<T> entityClass, List<T> entities)
    {
        List<Long> ids = new ArrayList<Long>();
        for (IEntityInformationWithPropertiesHolder entity : entities)
        {
            ids.add(entity.getId());
        }
        scheduler.scheduleUpdate(DynamicPropertyEvaluationOperation.evaluate(entityClass, ids));
    }

    public void registerExperiments(String sessionToken, NewExperimentsWithType experiments)
            throws UserFailureException
    {
        assert experiments != null : "Unspecified experiments.";
        assert sessionToken != null : "Unspecified session token.";
        assert experiments.getExperimentTypeCode() != null : "Experiments type not specified";
        assert experiments.getNewExperiments() != null : "Experiments collection not specified";

        final Session session = getSession(sessionToken);
        final List<NewBasicExperiment> newExperiments = experiments.getNewExperiments();
        if (newExperiments.size() == 0)
        {
            return;
        }
        ServerUtils.prevalidate(newExperiments, "experiment");
        final ExperimentTypePE experimentTypePE =
                (ExperimentTypePE) getDAOFactory().getEntityTypeDAO(EntityKind.EXPERIMENT)
                        .tryToFindEntityTypeByCode(experiments.getExperimentTypeCode());
        if (experimentTypePE == null)
        {
            throw UserFailureException.fromTemplate(
                    "Experiment type with code '%s' does not exist.", experimentTypePE);
        }
        BatchOperationExecutor.executeInBatches(new ExperimentBatchRegistration(
                businessObjectFactory.createExperimentTable(session), newExperiments,
                experimentTypePE));
    }

    /**
     * @param sessionToken The session token for the request
     * @param experiments Should be a NewExperimentsWithType where the newExperiments contains a
     *            collection of {@link UpdatedBasicExperiment} objects.
     */
    public void updateExperiments(String sessionToken, UpdatedExperimentsWithType experiments)
            throws UserFailureException
    {
        assert experiments != null : "Unspecified experiments.";
        assert sessionToken != null : "Unspecified session token.";
        assert experiments.getExperimentType() != null : "Experiments type not specified";
        assert experiments.getUpdatedExperiments() != null : "Experiments collection not specified";

        final Session session = getSession(sessionToken);
        final List<UpdatedBasicExperiment> newExperiments = experiments.getUpdatedExperiments();
        if (newExperiments.size() == 0)
        {
            return;
        }
        ServerUtils.prevalidate(newExperiments, "experiment");
        final ExperimentTypePE experimentTypePE =
                (ExperimentTypePE) getDAOFactory().getEntityTypeDAO(EntityKind.EXPERIMENT)
                        .tryToFindEntityTypeByCode(experiments.getExperimentType().getCode());
        if (experimentTypePE == null)
        {
            throw UserFailureException.fromTemplate(
                    "Experiment type with code '%s' does not exist.", experimentTypePE);
        }
        BatchOperationExecutor.executeInBatches(new ExperimentBatchUpdate(businessObjectFactory
                .createExperimentTable(session), convertExperiments(newExperiments),
                experimentTypePE));
    }

    /**
     * @param updatedExperiments The experiments should actually be instances of
     *            UpdatedBasicExperiment.
     */
    private List<ExperimentBatchUpdatesDTO> convertExperiments(
            final List<UpdatedBasicExperiment> updatedExperiments)
    {
        List<ExperimentBatchUpdatesDTO> experiments = new ArrayList<ExperimentBatchUpdatesDTO>();

        for (NewBasicExperiment experiment : updatedExperiments)
        {
            assert experiment instanceof UpdatedBasicExperiment;
            UpdatedBasicExperiment updatedExperiment = (UpdatedBasicExperiment) experiment;

            final ExperimentIdentifier oldExperimentIdentifier =
                    new ExperimentIdentifierFactory(updatedExperiment.getIdentifier().toUpperCase())
                            .createIdentifier();
            final List<IEntityProperty> properties =
                    Arrays.asList(updatedExperiment.getProperties());
            // If we allow changing projects, we will have to take new/old experiment identifiers
            // into account, but since this is currently not possible, there is nothing to do.
            final ExperimentIdentifier newExperimentIdentifier;
            final boolean isProjectUpdateRequested;
            String projectIdentifierOrNull = updatedExperiment.getNewProjectIdentifierOrNull();
            projectIdentifierOrNull =
                    (null != projectIdentifierOrNull) ? projectIdentifierOrNull.trim() : null;
            if (null != projectIdentifierOrNull && projectIdentifierOrNull.length() > 0)
            {
                // the new experiment identifier is derived form the project id
                ProjectIdentifier projectIdentifier =
                        new ProjectIdentifierFactory(projectIdentifierOrNull.toUpperCase())
                                .createIdentifier();
                newExperimentIdentifier =
                        new ExperimentIdentifier(projectIdentifier,
                                oldExperimentIdentifier.getExperimentCode());
                isProjectUpdateRequested = true;
            } else
            {
                newExperimentIdentifier = oldExperimentIdentifier;
                isProjectUpdateRequested = false;
            }

            experiments.add(new ExperimentBatchUpdatesDTO(oldExperimentIdentifier, properties,
                    newExperimentIdentifier, updatedExperiment.getBatchUpdateDetails(),
                    isProjectUpdateRequested));
        }
        return experiments;
    }

}
