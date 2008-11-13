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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui;

import java.util.ArrayList;
import java.util.List;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.SampleModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SampleTypePropertyType;

/**
 * Defines the sample properties columns for sample grid/table.
 * 
 * @author Izabela Adamczyk
 */
public final class PropertyColumns
{
    private final List<LoadableColumnConfig> columns;

    public PropertyColumns()
    {
        columns = new ArrayList<LoadableColumnConfig>();
    }

    public final void define(final SampleType sampleType)
    {
        columns.clear();
        for (final SampleTypePropertyType sampleTypePropertyType : sampleType.getSampleTypePropertyTypes())
        {
            columns.add(createPropertyColumn(sampleTypePropertyType));
        }
    }

    public final void resetLoaded()
    {
        for (final LoadableColumnConfig cc : columns)
        {
            cc.setLoaded(false);
        }

    }

    public List<LoadableColumnConfig> getColumns()
    {
        return columns;
    }

    private final LoadableColumnConfig createPropertyColumn(
            final SampleTypePropertyType sampleTypePropertyType)
    {
        final LoadableColumnConfig columnConfig = new LoadableColumnConfig();
        columnConfig.setMenuDisabled(true);
        final PropertyType propertyType = sampleTypePropertyType.getPropertyType();
        columnConfig.setId(SampleModel.createID(propertyType));
        columnConfig.setHeader(propertyType.getLabel());
        columnConfig.setWidth(80);
        columnConfig.setHidden(sampleTypePropertyType.isDisplayed() == false);
        columnConfig.setPropertyType(propertyType);
        return columnConfig;
    }

    public boolean isDirty()
    {
        for (final LoadableColumnConfig cc : columns)
        {
            if (cc.isDirty())
            {
                return true;
            }
        }
        return false;
    }

    public List<PropertyType> getDirtyColumns()
    {
        final ArrayList<PropertyType> result = new ArrayList<PropertyType>();
        for (final LoadableColumnConfig cc : columns)
        {
            if (cc.isDirty())
            {
                result.add(cc.getPropertyType());
            }
        }
        return result;
    }
}