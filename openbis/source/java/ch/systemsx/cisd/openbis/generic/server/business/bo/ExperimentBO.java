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

package ch.systemsx.cisd.openbis.generic.server.business.bo;

import java.util.List;
import java.util.Set;

import org.springframework.dao.DataAccessException;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.client.shared.ExperimentProperty;
import ch.systemsx.cisd.openbis.generic.client.shared.NewExperiment;
import ch.systemsx.cisd.openbis.generic.client.shared.SampleProperty;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.AttachmentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPropertyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifierFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.util.HibernateUtils;

/**
 * The unique {@link IExperimentBO} implementation.
 * 
 * @author Izabela Adamczyk
 */
public final class ExperimentBO extends AbstractBusinessObject implements IExperimentBO
{
    @Private
    static final String ERR_PROJECT_NOT_FOUND =
            "No project for experiment '%s' could be found in the database.";

    @Private
    static final String ERR_EXPERIMENT_TYPE_NOT_FOUND =
            "No experiment type with code '%s' could be found in the database.";

    private final IEntityPropertiesConverter propertiesConverter;

    private ExperimentPE experiment;

    private boolean dataChanged;

    public ExperimentBO(final IDAOFactory daoFactory, final Session session)
    {
        this(daoFactory, session, new EntityPropertiesConverter(EntityKind.EXPERIMENT, daoFactory));
    }

    @Private
    ExperimentBO(final IDAOFactory daoFactory, final Session session,
            final IEntityPropertiesConverter entityPropertiesConverter)
    {
        super(daoFactory, session);
        propertiesConverter = entityPropertiesConverter;
    }

    @SuppressWarnings("unused")
    private final ExperimentTypePE getExperimentType(final String code) throws UserFailureException
    {
        final EntityTypePE experimentType =
                getEntityTypeDAO(EntityKind.EXPERIMENT).tryToFindEntityTypeByCode(code);
        if (experimentType == null)
        {
            throw UserFailureException.fromTemplate(ERR_EXPERIMENT_TYPE_NOT_FOUND, code);
        }
        return (ExperimentTypePE) experimentType;
    }

    @SuppressWarnings("unused")
    private final void defineSampleProperties(final SampleProperty[] experimentProperties)
    {
        final String experimentTypeCode = experiment.getExperimentType().getCode();
        final List<ExperimentPropertyPE> properties =
                propertiesConverter.convertProperties(experimentProperties, experimentTypeCode,
                        experiment.getRegistrator());
        for (final ExperimentPropertyPE property : properties)
        {
            experiment.addProperty(property);
        }
    }

    public final ExperimentPE getExperiment()
    {
        checkExperimentLoaded();
        return experiment;
    }

    private void checkExperimentLoaded()
    {
        if (experiment == null)
        {
            throw new IllegalStateException("Unloaded experiment.");
        }
    }

    public final void loadByExperimentIdentifier(final ExperimentIdentifier identifier)
    {
        experiment = getExperimentByIdentifier(identifier);
        if (experiment == null)
        {
            throw UserFailureException.fromTemplate(
                    "No sample could be found with given identifier '%s'.", identifier);
        }
        dataChanged = false;
    }

    private ExperimentPE getExperimentByIdentifier(final ExperimentIdentifier identifier)
    {
        assert identifier != null : "Experiment identifier unspecified.";
        final ProjectPE project =
                getProjectDAO().tryFindProject(identifier.getDatabaseInstanceCode(),
                        identifier.getGroupCode(), identifier.getProjectCode());
        final ExperimentPE exp =
                getExperimentDAO().tryFindByCodeAndProject(project, identifier.getExperimentCode());
        if (exp == null)
        {
            throw UserFailureException.fromTemplate(
                    "No experiment could be found for identifier '%s'.", identifier);
        }
        return exp;
    }

    public final void enrichWithProperties()
    {
        if (experiment != null)
        {
            HibernateUtils.initialize(experiment.getProperties());
        }
    }

    public final void enrichWithAttachments()
    {
        if (experiment != null)
        {
            experiment.ensureAttachmentsLoaded();
        }
    }

    public AttachmentPE getExperimentFileAttachment(final String filename, final int version)
    {
        checkExperimentLoaded();
        experiment.ensureAttachmentsLoaded();
        final Set<AttachmentPE> attachments = experiment.getAttachments();
        for (AttachmentPE att : attachments)
        {
            if (att.getFileName().equals(filename) && att.getVersion() == version)
            {
                HibernateUtils.initialize(att.getAttachmentContent());
                return att;
            }
        }
        throw new UserFailureException("Attachment '" + filename + "' (version '" + version
                + "') not found in experiment '" + experiment.getIdentifier() + "'.");
    }

    public void define(NewExperiment newExperiment)
    {
        assert newExperiment != null : "Unspecified new experiment.";

        final ExperimentIdentifier experimentIdentifier =
                new ExperimentIdentifierFactory(newExperiment.getIdentifier()).createIdentifier();
        experiment = new ExperimentPE();
        final PersonPE registrator = findRegistrator();
        experiment.setCode(experimentIdentifier.getExperimentCode());
        experiment.setRegistrator(registrator);
        defineExperimentProperties(newExperiment.getExperimentTypeCode(), newExperiment
                .getProperties(), registrator);
        defineExperimentType(newExperiment);
        defineExperimentProject(newExperiment, experimentIdentifier);
        dataChanged = true;
    }

    private void defineExperimentProject(NewExperiment newExperiment,
            final ExperimentIdentifier experimentIdentifier)
    {
        ProjectPE project =
                getProjectDAO().tryFindProject(experimentIdentifier.getDatabaseInstanceCode(),
                        experimentIdentifier.getGroupCode(), experimentIdentifier.getProjectCode());
        if (project == null)
        {
            throw UserFailureException.fromTemplate(ERR_PROJECT_NOT_FOUND, newExperiment);
        }
        experiment.setProject(project);
    }

    private void defineExperimentType(NewExperiment newExperiment)
    {
        final String experimentTypeCode = newExperiment.getExperimentTypeCode();
        final EntityTypePE experimentType =
                getEntityTypeDAO(EntityKind.EXPERIMENT).tryToFindEntityTypeByCode(
                        experimentTypeCode);
        if (experimentType == null)
        {
            throw UserFailureException.fromTemplate(ERR_EXPERIMENT_TYPE_NOT_FOUND,
                    experimentTypeCode);
        }
        experiment.setExperimentType((ExperimentTypePE) experimentType);
    }

    public void save() throws UserFailureException
    {
        if (dataChanged)
        {
            try
            {
                getExperimentDAO().createExperiment(experiment);
            } catch (final DataAccessException ex)
            {
                final String projectCode = experiment.getProject().getCode();
                final ExperimentIdentifier identifier =
                        new ExperimentIdentifier(projectCode, experiment.getCode());
                throwException(ex, String.format("Experiment '%s'", identifier));
            }
            dataChanged = false;
        }

    }

    private final void defineExperimentProperties(final String experimentTypeCode,
            final ExperimentProperty[] experimentProperties, PersonPE registrator)
    {
        final List<ExperimentPropertyPE> properties =
                propertiesConverter.convertProperties(experimentProperties, experimentTypeCode,
                        registrator);
        for (final ExperimentPropertyPE experimentProperty : properties)
        {
            experiment.addProperty(experimentProperty);
        }
    }

}
