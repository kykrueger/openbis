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

import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.ModelDataPropertyNames;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IMessageProvider;

/**
 * A <i>static</i> class for creating {@link ColumnConfig}.
 * 
 * @author Christian Ribeaud
 */
public final class ColumnConfigFactory
{

    private ColumnConfigFactory()
    {
        // Can not be instantiated.
    }

    public final static ColumnConfig createMenuDisableColumnConfig()
    {
        final ColumnConfig columnConfig = new ColumnConfig();
        columnConfig.setMenuDisabled(true);
        return columnConfig;
    }

    public final static ColumnConfig createRegistratorColumnConfig(
            final IMessageProvider messageProvider)
    {
        return createDefaultConfig(messageProvider, ModelDataPropertyNames.REGISTRATOR);
    }

    public final static ColumnConfig createCodeColumnConfig(final IMessageProvider messageProvider)
    {
        return createDefaultConfig(messageProvider, ModelDataPropertyNames.CODE);
    }

    public final static ColumnConfig createRegistrationDateColumnConfig(
            final IMessageProvider messageProvider)
    {
        return createDefaultConfig(messageProvider, ModelDataPropertyNames.REGISTRATION_DATE,
                "registration_date");
    }

    public final static ColumnConfig createDefaultConfig(final IMessageProvider messageProvider,
            final String id)
    {
        return createDefaultConfig(messageProvider, id, id);
    }

    public final static ColumnConfig createDefaultConfig(final IMessageProvider messageProvider,
            final String id, final String headerKey)
    {
        final ColumnConfig columnConfig = createMenuDisableColumnConfig();
        columnConfig.setId(id);
        columnConfig.setHeader(messageProvider.getMessage(headerKey));
        columnConfig.setWidth(100);
        return columnConfig;
    }

}
