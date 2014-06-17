/*
 * Copyright 2013 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.generic.server;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import ch.systemsx.cisd.openbis.common.types.BooleanOrUnknown;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ContainerDataSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Person;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Space;

/**
 * Helper class to render a data set with properties as multi-line text.
 * 
 *
 * @author Franz-Josef Elmer
 */
public class MetaDataBuilder
{
    private static final String CONTAINER = "container";
    
    private static final String DATA_SET = "data_set";

    private static final String SAMPLE = "sample";

    private static final String EXPERIMENT = "experiment";

    private static final char DELIM = '\t';

    private static final DateFormat DATE_FORMAT_PATTERN = new SimpleDateFormat(
            "yyyy-MM-dd HH:mm:ss Z");

    private static final Comparator<AbstractExternalData> DATA_SET_COMPARATOR = new Comparator<AbstractExternalData>()
        {
            @Override
            public int compare(AbstractExternalData d1, AbstractExternalData d2)
            {
                return d1.getCode().compareTo(d2.getCode());
            }
        };

    private static final Comparator<IEntityProperty> PROPERTIES_COMPARATOR = new Comparator<IEntityProperty>()
        {
            @Override
            public int compare(IEntityProperty p1, IEntityProperty p2)
            {
                return p1.getPropertyType().getCode().compareTo(p2.getPropertyType().getCode());
            }
        };

    public static String createMetaData(AbstractExternalData dataSet)
    {
        MetaDataBuilder builder = new MetaDataBuilder();
        builder.dataSet("code", dataSet.getCode());
        builder.dataSet("production_timestamp", dataSet.getProductionDate());
        builder.dataSet("producer_code", dataSet.getDataProducerCode());
        builder.dataSet("data_set_type", dataSet.getDataSetType().getCode());
        builder.dataSet("is_measured", dataSet.isDerived() == false);
        if (dataSet.tryGetAsDataSet() != null)
        {
            final Boolean completeFlag = dataSet.tryGetAsDataSet().getComplete();
            builder.dataSet("is_complete", BooleanOrUnknown.T.equals(completeFlag));
        }
        builder.dataSetProperties(dataSet.getProperties());
    
        StringBuilder stringBuilder = new StringBuilder();
        List<AbstractExternalData> parents = getParents(dataSet);
        if (parents.isEmpty() == false)
        {
            Collections.sort(parents, DATA_SET_COMPARATOR);
            for (AbstractExternalData parent : parents)
            {
                if (stringBuilder.length() > 0)
                {
                    stringBuilder.append(',');
                }
                stringBuilder.append(parent.getCode());
            }
        }
        builder.dataSet("parent_codes", stringBuilder.toString());
        Sample sample = dataSet.getSample();
        if (sample != null)
        {
            builder.sample("type_code", sample.getSampleType().getCode());
            builder.sample("code", sample.getCode());
            Space space = sample.getSpace();
            builder.sample("space_code", space == null ? "(shared)" : space.getCode());
            builder.sample("registration_timestamp", sample.getRegistrationDate());
            builder.sample("registrator", sample.getRegistrator());
            builder.sampleProperties(sample.getProperties());
        }
        Experiment experiment = dataSet.getExperiment();
        Project project = experiment.getProject();
        builder.experiment("space_code", project.getSpace().getCode());
        builder.experiment("project_code", project.getCode());
        builder.experiment("experiment_code", experiment.getCode());
        builder.experiment("experiment_type_code", experiment.getExperimentType().getCode());
        builder.experiment("registration_timestamp", experiment.getRegistrationDate());
        builder.experiment("registrator", experiment.getRegistrator());
        builder.experimentProperties(experiment.getProperties());
        
        if(dataSet.getContainerDataSets() != null) {
            for(int i = 0; i < dataSet.getContainerDataSets().size(); i++) {
                //Container
                ContainerDataSet container = dataSet.getContainerDataSets().get(i);
                builder.container("[" + i + "].code", container.getCode());
                builder.container("[" + i + "].permId", container.getPermId());
                builder.container("[" + i + "].identifier", container.getIdentifier());
                if(container.getDataSetType() != null) {
                    builder.container("[" + i + "].type", container.getDataSetType().getCode());
                }
                if(container.getRegistrator() != null) {
                    builder.container("[" + i + "].registrator", container.getRegistrator().getUserId());
                }
                if(container.getRegistrationDate() != null) {
                    builder.container("[" + i + "].registration_date", container.getRegistrationDate().toString());
                }
                if(container.getModificationDate() != null) {
                    builder.container("[" + i + "].modification_date", container.getModificationDate().toString());
                }
                //Container Experiment
                if(container.getExperiment() != null) {
                    builder.container("[" + i + "].experiment_code", container.getExperiment().getCode());
                    builder.container("[" + i + "].experiment_permId", container.getExperiment().getPermId());
                    builder.container("[" + i + "].experiment_identifier", container.getExperiment().getIdentifier());
                    if(container.getExperiment().getExperimentType() != null) {
                        builder.container("[" + i + "].experiment_type", container.getExperiment().getExperimentType().getCode());
                    }
                    if(container.getExperiment().getRegistrator() != null) {
                        builder.container("[" + i + "].experiment_registrator", container.getExperiment().getRegistrator().getUserId());
                    }
                    if(container.getExperiment().getRegistrationDate() != null) {
                        builder.container("[" + i + "].experiment_registration_date", container.getExperiment().getRegistrationDate().toString());
                    }
                    if(container.getExperiment().getModificationDate() != null) {
                        builder.container("[" + i + "].experiment_modification_date", container.getExperiment().getModificationDate().toString());
                    }
                }
                //Container Sample
                if(container.getSample() != null) {
                    builder.container("[" + i + "].sample_code", container.getSample().getCode());
                    builder.container("[" + i + "].sample_permId", container.getSample().getPermId());
                    builder.container("[" + i + "].sample_identifier", container.getSample().getIdentifier());
                    if(container.getSample().getSampleType() != null) {
                        builder.container("[" + i + "].sample_type", container.getSample().getSampleType().getCode());
                    }
                    if(container.getSample().getRegistrator() != null) {
                        builder.container("[" + i + "].sample_registrator", container.getSample().getRegistrator().getUserId());
                    }
                    if(container.getSample().getRegistrationDate() != null) {
                        builder.container("[" + i + "].sample_registration_date", container.getSample().getRegistrationDate().toString());
                    }
                    if(container.getSample().getModificationDate() != null) {
                        builder.container("[" + i + "].sample_modification_date", container.getSample().getModificationDate().toString());
                    }
                }
            }
        }
        
        return builder.getRenderedMetaData();
    }

    private static List<AbstractExternalData> getParents(AbstractExternalData dataSet)
    {
        Collection<AbstractExternalData> parents = dataSet.getParents();
        return parents == null ? new ArrayList<AbstractExternalData>() : new ArrayList<AbstractExternalData>(parents);
    }

    private final StringBuilder builder = new StringBuilder();

    private void dataSetProperties(List<IEntityProperty> properties)
    {
        addProperties(DATA_SET, properties);
    }

    private void sampleProperties(List<IEntityProperty> properties)
    {
        addProperties(SAMPLE, properties);
    }

    private void experimentProperties(List<IEntityProperty> properties)
    {
        addProperties(EXPERIMENT, properties);
    }

    private void addProperties(String category, List<IEntityProperty> properties)
    {
        Collections.sort(properties, PROPERTIES_COMPARATOR);
        for (IEntityProperty property : properties)
        {
            addRow(category, property.getPropertyType().getCode(), property.tryGetAsString());
        }
    }

    private void container(String key, String value)
    {
        addRow(CONTAINER, key, value);
    }
    
    private void dataSet(String key, String value)
    {
        addRow(DATA_SET, key, value);
    }

    private void dataSet(String key, Date date)
    {
        addRow(DATA_SET, key, date);
    }

    private void dataSet(String key, boolean flag)
    {
        addRow(DATA_SET, key, flag);
    }

    private void sample(String key, String value)
    {
        addRow(SAMPLE, key, value);
    }

    private void sample(String key, Person person)
    {
        addRow(SAMPLE, key, person);
    }

    private void sample(String key, Date date)
    {
        addRow(SAMPLE, key, date);
    }

    private void experiment(String key, String value)
    {
        addRow(EXPERIMENT, key, value);
    }

    private void experiment(String key, Person person)
    {
        addRow(EXPERIMENT, key, person);
    }

    private void experiment(String key, Date date)
    {
        addRow(EXPERIMENT, key, date);
    }

    private void addRow(String category, String key, Person person)
    {
        StringBuilder stringBuilder = new StringBuilder();
        if (person != null)
        {
            String firstName = person.getFirstName();
            String lastName = person.getLastName();
            if (firstName != null && lastName != null)
            {
                stringBuilder.append(firstName).append(' ').append(lastName);
            } else
            {
                stringBuilder.append(person.getUserId());
            }
            String email = person.getEmail();
            if (email != null)
            {
                stringBuilder.append(" <").append(email).append(">");
            }
        }
        addRow(category, key, stringBuilder.toString());
    }

    private void addRow(String category, String key, Date date)
    {
        addRow(category, key, date == null ? null : DATE_FORMAT_PATTERN.format(date));
    }

    private void addRow(String category, String key, boolean flag)
    {
        addRow(category, key, Boolean.valueOf(flag).toString().toUpperCase());
    }

    private void addRow(String category, String key, String value)
    {
        builder.append(category).append(DELIM).append(key).append(DELIM);
        builder.append(value == null ? "" : value).append('\n');
    }

    private String getRenderedMetaData()
    {
        return builder.toString();
    }

}