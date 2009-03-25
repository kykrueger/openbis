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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.specific.data;

import java.util.List;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.framework.AbstractPropertyColDef;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;

public final class DataSetPropertyColDef extends AbstractPropertyColDef<ExternalData>
{
    private static final String ID_PREFIX = "ds";

    // GWT only
    public DataSetPropertyColDef()
    {
    }

    public DataSetPropertyColDef(PropertyType propertyType, boolean isDisplayedByDefault,
            int width, String propertyTypeLabel)
    {
        super(propertyType, isDisplayedByDefault, width, propertyTypeLabel, ID_PREFIX);
    }

    @Override
    protected List<? extends EntityProperty<?, ?>> getProperties(ExternalData entity)
    {
        return getDataSetProperties(entity);
    }

    public static List<DataSetProperty> getDataSetProperties(ExternalData entity)
    {
        return entity.getDataSetProperties();
    }

}