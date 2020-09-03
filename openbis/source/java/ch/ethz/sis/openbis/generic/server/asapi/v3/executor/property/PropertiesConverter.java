/*
 * Copyright 2020 ETH Zuerich, SIS
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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.property;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.DataType;
import ch.ethz.sis.openbis.generic.server.asapi.v3.context.IProgress;
import ch.ethz.sis.openbis.generic.server.asapi.v3.context.Progress;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.common.batch.Batch;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.common.batch.CollectionBatch;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.common.batch.CollectionBatchProcessor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.property.IPropertyQuery;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.property.PropertyRecord;
import net.lemnik.eodsql.QueryTool;

/**
 * @author Franz-Josef Elmer
 */
@Component
public class PropertiesConverter implements IPropertiesConverter
{

    @Override
    public void convertProperties(IOperationContext context, String propertyTypeCode, DataType currentDataType, DataType newDataType)
    {
        IPropertyQuery query = QueryTool.getManagedQuery(IPropertyQuery.class);
        if (currentDataType == DataType.TIMESTAMP && newDataType == DataType.DATE)
        {
            AbstractPropertyUpdater experimentPlainPropertyUpdater = new ExperimentPlainPropertyUpdater(query);
            AbstractPropertyUpdater samplePlainPropertyUpdater = new SamplePlainPropertyUpdater(query);
            AbstractPropertyUpdater dataSetPlainPropertyUpdater = new DataSetPlainPropertyUpdater(query);
            AbstractPropertyConverter converter = new TimestampToDatePropertyConverter();
            List<PropertyRecord> experimentProperties = query.listPlainExperimentProperties(propertyTypeCode);
            convert(context, experimentProperties, experimentPlainPropertyUpdater, converter);
            List<PropertyRecord> sampleProperties = query.listPlainSampleProperties(propertyTypeCode);
            convert(context, sampleProperties, samplePlainPropertyUpdater, converter);
            List<PropertyRecord> dataSetProperties = query.listPlainDataSetProperties(propertyTypeCode);
            convert(context, dataSetProperties, dataSetPlainPropertyUpdater, converter);
        } else if (currentDataType == DataType.CONTROLLEDVOCABULARY 
                && EnumSet.of(DataType.VARCHAR, DataType.MULTILINE_VARCHAR).contains(newDataType))
        {
            // TODO
        }
    }

    private void convert(IOperationContext context, List<PropertyRecord> properties, AbstractPropertyUpdater updater,
            AbstractPropertyConverter converter)
    {
        for (CollectionBatch<PropertyRecord> batch : Batch.createBatches(properties))
        {
            List<PropertyRecord> convertedProperties = new ArrayList<>();
            new CollectionBatchProcessor<PropertyRecord>(context, batch)
                {
                    @Override
                    public void process(PropertyRecord property)
                    {
                        PropertyRecord convertedProperty = new PropertyRecord();
                        convertedProperty.objectId = property.objectId;
                        convertedProperty.propertyValue = property.propertyValue.split(" ")[0];
                        convertedProperties.add(converter.convertProperty(property));
                    }

                    @Override
                    public IProgress createProgress(PropertyRecord property, int objectIndex, int totalObjectCount)
                    {
                        return new Progress(String.valueOf(property.objectId), objectIndex, totalObjectCount);
                    }
                };
            updater.update(convertedProperties);
        }

    }

    private static abstract class AbstractPropertyUpdater
    {
        protected IPropertyQuery query;

        public AbstractPropertyUpdater(IPropertyQuery query)
        {
            this.query = query;
        }

        abstract void update(List<PropertyRecord> convertedProperties);
    }

    private static class ExperimentPlainPropertyUpdater extends AbstractPropertyUpdater
    {
        public ExperimentPlainPropertyUpdater(IPropertyQuery query)
        {
            super(query);
        }

        @Override
        void update(List<PropertyRecord> convertedProperties)
        {
            query.updatePlainExperimentProperties(convertedProperties);
        }
    }

    private static class SamplePlainPropertyUpdater extends AbstractPropertyUpdater
    {
        public SamplePlainPropertyUpdater(IPropertyQuery query)
        {
            super(query);
        }

        @Override
        void update(List<PropertyRecord> convertedProperties)
        {
            query.updatePlainSampleProperties(convertedProperties);
        }
    }

    private static class DataSetPlainPropertyUpdater extends AbstractPropertyUpdater
    {
        public DataSetPlainPropertyUpdater(IPropertyQuery query)
        {
            super(query);
        }

        @Override
        void update(List<PropertyRecord> convertedProperties)
        {
            query.updatePlainDataSetProperties(convertedProperties);
        }
    }

    private static abstract class AbstractPropertyConverter
    {
        PropertyRecord convertProperty(PropertyRecord property)
        {
            PropertyRecord convertedProperty = new PropertyRecord();
            convertedProperty.objectId = property.objectId;
            convert(convertedProperty, property);
            return convertedProperty;
        }

        abstract void convert(PropertyRecord convertedProperty, PropertyRecord property);
    }

    private static class TimestampToDatePropertyConverter extends AbstractPropertyConverter
    {
        @Override
        void convert(PropertyRecord convertedProperty, PropertyRecord property)
        {
            convertedProperty.propertyValue = property.propertyValue.split(" ")[0];
        }
    }
}
