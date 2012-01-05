/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.shared.api.v1;

import static ch.systemsx.cisd.common.collections.CollectionUtils.nullSafe;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;

import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.ControlledVocabularyPropertyType;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.ControlledVocabularyPropertyType.ControlledVocabularyPropertyTypeInitializer;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSet;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSet.Connections;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSet.DataSetInitializer;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSetType.DataSetTypeInitializer;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.EntityRegistrationDetails;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Experiment.ExperimentInitializer;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.PropertyType.PropertyTypeInitializer;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.PropertyTypeGroup;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.PropertyTypeGroup.PropertyTypeGroupInitializer;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Role;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Sample.SampleInitializer;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Vocabulary;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Vocabulary.VocabularyInitializer;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.VocabularyTerm;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ContainerDataSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataTypeCode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Person;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy.RoleCode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy.RoleLevel;

/**
 * @author Franz-Josef Elmer
 */
public class Translator
{
    public static Role translate(
            ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy role)
    {
        return translate(role.getRoleCode(), role.getRoleLevel().equals(RoleLevel.SPACE));
    }

    public static Role translate(RoleCode roleCode, boolean spaceLevel)
    {
        return new Role(roleCode.name(), spaceLevel);
    }

    public static List<Project> translateProjects(
            List<ch.systemsx.cisd.openbis.generic.shared.basic.dto.Project> projects)
    {
        ArrayList<Project> translated = new ArrayList<Project>();

        for (ch.systemsx.cisd.openbis.generic.shared.basic.dto.Project project : projects)
        {
            translated.add(translate(project));
        }

        return translated;
    }

    public static Project translate(
            ch.systemsx.cisd.openbis.generic.shared.basic.dto.Project project)
    {
        EntityRegistrationDetails registrationDetails = translateRegistrationDetails(project);
        return new Project(project.getSpace().getCode(), project.getCode(), registrationDetails);
    }

    public static List<Sample> translateSamples(
            Collection<ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample> privateSamples)
    {
        ArrayList<Sample> samples = new ArrayList<Sample>();
        for (ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample privateSample : privateSamples)
        {
            samples.add(Translator.translate(privateSample));
        }
        return samples;
    }

    public static Sample translate(
            ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample privateSample)
    {
        SampleInitializer initializer = new SampleInitializer();
        initializer.setId(privateSample.getId());
        initializer.setSpaceCode(privateSample.getSpace() != null ? privateSample.getSpace()
                .getCode() : null);
        initializer.setPermId(privateSample.getPermId());
        initializer.setCode(privateSample.getCode());
        initializer.setIdentifier(privateSample.getIdentifier());
        initializer.setSampleTypeId(privateSample.getSampleType().getId());
        initializer.setSampleTypeCode(privateSample.getSampleType().getCode());
        List<IEntityProperty> properties = privateSample.getProperties();
        for (IEntityProperty prop : properties)
        {
            initializer.putProperty(prop.getPropertyType().getCode(), prop.tryGetAsString());
        }

        ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment experimentOrNull =
                privateSample.getExperiment();
        if (null != experimentOrNull)
        {
            initializer.setExperimentIdentifierOrNull(experimentOrNull.getIdentifier());
        }

        EntityRegistrationDetails registrationDetails = translateRegistrationDetails(privateSample);
        initializer.setRegistrationDetails(registrationDetails);

        return new Sample(initializer);
    }

    public static List<Experiment> translateExperiments(
            Collection<ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment> privateExperiments)
    {
        ArrayList<Experiment> experiments = new ArrayList<Experiment>();
        for (ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment privateExpeiment : privateExperiments)
        {
            experiments.add(Translator.translate(privateExpeiment));
        }
        return experiments;
    }

    public static Experiment translate(
            ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment privateExperiment)
    {
        ExperimentInitializer initializer = new ExperimentInitializer();
        initializer.setId(privateExperiment.getId());
        initializer.setPermId(privateExperiment.getPermId());
        initializer.setCode(privateExperiment.getCode());
        initializer.setIdentifier(privateExperiment.getIdentifier());
        initializer.setExperimentTypeCode(privateExperiment.getExperimentType().getCode());
        List<IEntityProperty> properties = privateExperiment.getProperties();
        for (IEntityProperty prop : properties)
        {
            initializer.putProperty(prop.getPropertyType().getCode(), prop.tryGetAsString());
        }

        EntityRegistrationDetails registrationDetails =
                translateRegistrationDetails(privateExperiment);
        initializer.setRegistrationDetails(registrationDetails);

        return new Experiment(initializer);
    }

    public static DataSetType translate(
            ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType privateDataSetType,
            HashMap<ch.systemsx.cisd.openbis.generic.shared.basic.dto.Vocabulary, List<ControlledVocabularyPropertyType.VocabularyTerm>> vocabTerms)
    {
        DataSetTypeInitializer initializer = new DataSetTypeInitializer();
        initializer.setCode(privateDataSetType.getCode());

        List<ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetTypePropertyType> dstpts =
                privateDataSetType.getAssignedPropertyTypes();
        Collections.sort(dstpts);

        String sectionName = null;
        PropertyTypeGroupInitializer groupInitializer = new PropertyTypeGroupInitializer();
        for (ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetTypePropertyType dstpt : dstpts)
        {
            // Skip Dynamic and Managed properties
            if (dstpt.isDynamic() || dstpt.isManaged())
            {
                continue;
            }

            String thisSectionName = dstpt.getSection();
            if (thisSectionName != null && false == thisSectionName.equals(sectionName))
            {
                // Start a new section
                initializer.addPropertyTypeGroup(new PropertyTypeGroup(groupInitializer));
                groupInitializer = new PropertyTypeGroupInitializer();
                sectionName = thisSectionName;
            }
            PropertyTypeInitializer ptInitializer;
            ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType propertyType =
                    dstpt.getPropertyType();

            boolean isControlledVocabulary =
                    propertyType.getDataType().getCode() == DataTypeCode.CONTROLLEDVOCABULARY;
            if (isControlledVocabulary)
            {
                ControlledVocabularyPropertyTypeInitializer cvptInitializer =
                        new ControlledVocabularyPropertyTypeInitializer();

                cvptInitializer.setVocabulary(propertyType.getVocabulary());
                cvptInitializer.setTerms(vocabTerms.get(propertyType.getVocabulary()));
                ptInitializer = cvptInitializer;
            } else
            {
                ptInitializer = new PropertyTypeInitializer();
            }

            ptInitializer.setDataType(propertyType.getDataType().getCode());
            ptInitializer.setCode(propertyType.getCode());
            ptInitializer.setLabel(propertyType.getLabel());
            ptInitializer.setDescription(propertyType.getDescription());
            ptInitializer.setMandatory(dstpt.isMandatory());

            if (isControlledVocabulary)
            {
                groupInitializer.addPropertyType(new ControlledVocabularyPropertyType(
                        (ControlledVocabularyPropertyTypeInitializer) ptInitializer));
            } else
            {
                groupInitializer.addPropertyType(new PropertyType(ptInitializer));
            }
        }
        // Finally set the group
        initializer.addPropertyTypeGroup(new PropertyTypeGroup(groupInitializer));

        return new DataSetType(initializer);
    }

    public static List<ControlledVocabularyPropertyType.VocabularyTerm> translatePropertyTypeTerms(
            Collection<ch.systemsx.cisd.openbis.generic.shared.basic.dto.VocabularyTerm> privateTerms)
    {
        ArrayList<ControlledVocabularyPropertyType.VocabularyTerm> terms =
                new ArrayList<ControlledVocabularyPropertyType.VocabularyTerm>();
        for (ch.systemsx.cisd.openbis.generic.shared.basic.dto.VocabularyTerm privateTerm : sortPrivateVocabularyTerms(privateTerms))
        {
            EntityRegistrationDetails registrationDetails =
                    translateRegistrationDetails(privateTerm);
            terms.add(new ControlledVocabularyPropertyType.VocabularyTerm(privateTerm.getCode(),
                    privateTerm.getCodeOrLabel(), privateTerm.getOrdinal(), privateTerm
                            .isOfficial(), registrationDetails));
        }

        return terms;
    }

    public static List<VocabularyTerm> translate(
            Collection<ch.systemsx.cisd.openbis.generic.shared.basic.dto.VocabularyTerm> privateTerms)
    {
        ArrayList<VocabularyTerm> terms = new ArrayList<VocabularyTerm>();
        for (ch.systemsx.cisd.openbis.generic.shared.basic.dto.VocabularyTerm privateTerm : sortPrivateVocabularyTerms(privateTerms))
        {
            EntityRegistrationDetails registrationDetails =
                    translateRegistrationDetails(privateTerm);
            terms.add(new VocabularyTerm(privateTerm.getCode(), privateTerm.getCodeOrLabel(),
                    privateTerm.getOrdinal(), privateTerm.isOfficial(), registrationDetails));
        }

        return terms;
    }

    private static ArrayList<ch.systemsx.cisd.openbis.generic.shared.basic.dto.VocabularyTerm> sortPrivateVocabularyTerms(
            Collection<ch.systemsx.cisd.openbis.generic.shared.basic.dto.VocabularyTerm> privateTerms)
    {
        ArrayList<ch.systemsx.cisd.openbis.generic.shared.basic.dto.VocabularyTerm> sortedTerms =
                new ArrayList<ch.systemsx.cisd.openbis.generic.shared.basic.dto.VocabularyTerm>(
                        privateTerms);
        Collections.sort(sortedTerms,
                new Comparator<ch.systemsx.cisd.openbis.generic.shared.basic.dto.VocabularyTerm>()
                    {
                        public int compare(
                                ch.systemsx.cisd.openbis.generic.shared.basic.dto.VocabularyTerm o1,
                                ch.systemsx.cisd.openbis.generic.shared.basic.dto.VocabularyTerm o2)
                        {
                            if (o1.isOfficial() != o2.isOfficial())
                            {
                                return o1.isOfficial() ? -1 : 1;
                            } else
                            {
                                return o1.getOrdinal().compareTo(o2.getOrdinal());
                            }
                        }
                    });
        return sortedTerms;
    }

    /**
     * Translates specified iterable collection of {@link ExternalData} into a list of
     * {@link DataSet} instance.
     * 
     * @param connectionsToGet Set of data set connections which should also be translated. This
     *            assumes that the {@link ExternalData} instances are populated with these
     *            connections.
     */
    public static List<DataSet> translate(Iterable<ExternalData> dataSets,
            EnumSet<Connections> connectionsToGet)
    {
        ArrayList<DataSet> translated = new ArrayList<DataSet>();
        for (ExternalData dataSet : dataSets)
        {
            translated.add(translate(dataSet, connectionsToGet));
        }
        return translated;
    }

    /**
     * Translates the specified {@link ExternalData} instance into a {@link DataSet} instance.
     * 
     * @param connectionsToGet Set of data set connections which should also be translated. This
     *            assumes that the {@link ExternalData} instance is populated with these
     *            connections.
     */
    public static DataSet translate(ExternalData externalDatum,
            EnumSet<Connections> connectionsToGet)
    {
        DataSetInitializer initializer = new DataSetInitializer();
        initializer.setCode(externalDatum.getCode());
        initializer.setExperimentIdentifier(externalDatum.getExperiment().getIdentifier());
        initializer.setSampleIdentifierOrNull(externalDatum.getSampleIdentifier());
        initializer.setDataSetTypeCode(externalDatum.getDataSetType().getCode());
        List<IEntityProperty> properties = externalDatum.getProperties();
        for (IEntityProperty prop : properties)
        {
            initializer.putProperty(prop.getPropertyType().getCode(), prop.tryGetAsString());
        }

        initializer.setContainerDataSet(externalDatum.isContainer());
        if (externalDatum.isContainer())
        {
            // Recursively translate any contained data sets
            ContainerDataSet containerDataSet = externalDatum.tryGetAsContainerDataSet();

            ArrayList<DataSet> containedDataSetCodes =
                    new ArrayList<DataSet>(containerDataSet.getContainedDataSets().size());
            for (ExternalData containedDataSet : containerDataSet.getContainedDataSets())
            {
                containedDataSetCodes.add(translate(containedDataSet, connectionsToGet));
            }
            initializer.setContainedDataSets(containedDataSetCodes);
        }

        initializer.setRetrievedConnections(connectionsToGet);
        for (Connections connection : connectionsToGet)
        {
            switch (connection)
            {
                case PARENTS:
                    Collection<ExternalData> parents = externalDatum.getParents();
                    ArrayList<String> parentCodes = new ArrayList<String>();
                    for (ExternalData parentDatum : nullSafe(parents))
                    {
                        parentCodes.add(parentDatum.getCode());
                    }
                    initializer.setParentCodes(parentCodes);
                    break;
                case CHILDREN:
                    Collection<ExternalData> children = externalDatum.getChildren();
                    ArrayList<String> childrenCodes = new ArrayList<String>();
                    for (ExternalData parentDatum : nullSafe(children))
                    {
                        childrenCodes.add(parentDatum.getCode());
                    }
                    initializer.setChildrenCodes(childrenCodes);
                    break;
            }
        }
        EntityRegistrationDetails registrationDetails = translateRegistrationDetails(externalDatum);
        initializer.setRegistrationDetails(registrationDetails);

        return new DataSet(initializer);
    }

    private static EntityRegistrationDetails translateRegistrationDetails(
            ch.systemsx.cisd.openbis.generic.shared.basic.dto.CodeWithRegistration<?> thingWithRegistrationDetails)
    {
        Person registrator = thingWithRegistrationDetails.getRegistrator();
        EntityRegistrationDetails.EntityRegistrationDetailsInitializer initializer =
                new EntityRegistrationDetails.EntityRegistrationDetailsInitializer();
        if (registrator != null)
        {
            initializer.setEmail(registrator.getEmail());
            initializer.setFirstName(registrator.getFirstName());
            initializer.setLastName(registrator.getLastName());
            initializer.setUserId(registrator.getUserId());
        }
        initializer.setRegistrationDate(thingWithRegistrationDetails.getRegistrationDate());
        return new EntityRegistrationDetails(initializer);
    }

    private Translator()
    {
    }

    public static Vocabulary translate(
            ch.systemsx.cisd.openbis.generic.shared.basic.dto.Vocabulary privateVocabulary)
    {
        VocabularyInitializer initializer = new VocabularyInitializer();
        initializer.setId(privateVocabulary.getId());
        initializer.setCode(privateVocabulary.getCode());
        initializer.setDescription(privateVocabulary.getDescription());
        initializer.setChosenFromList(privateVocabulary.isChosenFromList());
        initializer.setInternalNamespace(privateVocabulary.isInternalNamespace());
        initializer.setManagedInternally(privateVocabulary.isManagedInternally());
        initializer.setUrlTemplate(privateVocabulary.getURLTemplate());
        initializer.setTerms(translate(privateVocabulary.getTerms()));
        return new Vocabulary(initializer);
    }
}
