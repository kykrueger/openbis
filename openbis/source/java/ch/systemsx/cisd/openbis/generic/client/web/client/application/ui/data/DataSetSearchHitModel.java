/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.data;

import java.util.ArrayList;
import java.util.List;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.BaseEntityModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.AbstractPropertyColDef;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.ColumnDefsAndConfigs;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IColumnDefinitionUI;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IMessageProvider;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DataSetSearchHit;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentTypePropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;

/**
 * @author Tomasz Pylak
 */
public class DataSetSearchHitModel extends BaseEntityModel<DataSetSearchHit>
{
    private static final long serialVersionUID = 1L;

    private static final String LABEL_EXPERIMENT_PROPERTY_PREFIX = "Exp. ";

    public DataSetSearchHitModel(final DataSetSearchHit entity)
    {
        super(entity, createColumnsSchema(entity));
    }

    // here we create the columns definition having just one table row. We need them only to render
    // column values (headers have been already created), so no message provider is needed.
    private static List<IColumnDefinitionUI<DataSetSearchHit>> createColumnsSchema(
            DataSetSearchHit entity)
    {
        List<IColumnDefinitionUI<DataSetSearchHit>> list = createCommonColumnsSchema(null);
        for (ExperimentProperty prop : getExperimentProperties(entity))
        {
            ExperimentTypePropertyType etpt = prop.getEntityTypePropertyType();
            list.add(createExperimentPropertyTypeColDef(etpt.getPropertyType()));
        }
        return list;
    }

    private static IColumnDefinitionUI<DataSetSearchHit> createExperimentPropertyTypeColDef(
            PropertyType propertyType)
    {
        String label = LABEL_EXPERIMENT_PROPERTY_PREFIX + propertyType.getLabel();
        return new AbstractPropertyColDef<DataSetSearchHit>(propertyType, true, label)
            {
                @Override
                protected List<? extends EntityProperty<?, ?>> getProperties(DataSetSearchHit entity)
                {
                    return getExperimentProperties(entity);
                }
            };
    }

    private static List<ExperimentProperty> getExperimentProperties(DataSetSearchHit entity)
    {
        return entity.getDataSet().getProcedure().getExperiment().getProperties();
    }

    public static ColumnDefsAndConfigs<DataSetSearchHit> createColumnsSchema(
            IMessageProvider messageProvider, List<PropertyType> propertyTypes)
    {
        List<IColumnDefinitionUI<DataSetSearchHit>> commonColumnsSchema =
                createCommonColumnsSchema(messageProvider);
        ColumnDefsAndConfigs<DataSetSearchHit> columns =
                ColumnDefsAndConfigs.create(commonColumnsSchema);

        List<IColumnDefinitionUI<DataSetSearchHit>> experimentPropertyColumnsSchema =
                createExperimentsPropertyColumnsSchema(propertyTypes);
        columns.addColumns(experimentPropertyColumnsSchema);

        return columns;
    }

    private static List<IColumnDefinitionUI<DataSetSearchHit>> createExperimentsPropertyColumnsSchema(
            List<PropertyType> propertyTypes)
    {
        List<PropertyType> experimentPropertyTypes = filterExperimentPropertyTypes(propertyTypes);
        List<IColumnDefinitionUI<DataSetSearchHit>> list = createEmptyColDefList();
        for (PropertyType prop : experimentPropertyTypes)
        {
            list.add(createExperimentPropertyTypeColDef(prop));
        }
        return list;
    }

    private static List<IColumnDefinitionUI<DataSetSearchHit>> createCommonColumnsSchema(
            IMessageProvider msgProviderOrNull)
    {
        return createColumnsDefinition(DataSetSearchHitColDefKind.values(), msgProviderOrNull);
    }

    private static ArrayList<IColumnDefinitionUI<DataSetSearchHit>> createEmptyColDefList()
    {
        return new ArrayList<IColumnDefinitionUI<DataSetSearchHit>>();
    }

    // returns property types which are assigned to at least one sample type
    public static List<PropertyType> filterSamplePropertyTypes(List<PropertyType> propertyTypes)
    {
        List<PropertyType> result = new ArrayList<PropertyType>();
        for (final PropertyType st : propertyTypes)
        {
            if (st.getSampleTypePropertyTypes().size() > 0)
            {
                result.add(st);
            }
        }
        return result;
    }

    // returns property types which are assigned to at least one experiment type
    public static List<PropertyType> filterExperimentPropertyTypes(List<PropertyType> propertyTypes)
    {
        List<PropertyType> result = new ArrayList<PropertyType>();
        for (final PropertyType st : propertyTypes)
        {
            if (st.getExperimentTypePropertyTypes().size() > 0)
            {
                result.add(st);
            }
        }
        return result;
    }
}
