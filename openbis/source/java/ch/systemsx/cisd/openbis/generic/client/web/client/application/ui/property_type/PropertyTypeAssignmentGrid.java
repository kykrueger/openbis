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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.property_type;

import java.util.List;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.BaseEntityModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.AbstractSimpleBrowserGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.DisposableComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IColumnDefinitionKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DefaultResultSetConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.IColumnDefinition;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ResultSet;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TableExportCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityTypePropertyType;

/**
 * Grid with 'entity type' - 'property type' assignments.
 * 
 * @author Izabela Adamczyk
 */
public class PropertyTypeAssignmentGrid
        extends
        AbstractSimpleBrowserGrid<EntityTypePropertyType<?>, BaseEntityModel<EntityTypePropertyType<?>>>
{
    // browser consists of the grid and the paging toolbar
    public static final String BROWSER_ID =
            GenericConstants.ID_PREFIX + "property-type-assignment-browser";

    public static final String GRID_ID = BROWSER_ID + "_grid";

    public static DisposableComponent create(
            final IViewContext<ICommonClientServiceAsync> viewContext)
    {
        return new PropertyTypeAssignmentGrid(viewContext).asDisposableWithoutToolbar();
    }

    private PropertyTypeAssignmentGrid(IViewContext<ICommonClientServiceAsync> viewContext)
    {
        super(viewContext, BROWSER_ID, GRID_ID);
    }

    @Override
    protected IColumnDefinitionKind<EntityTypePropertyType<?>>[] getStaticColumnsDefinition()
    {
        return PropertyTypeAssignmentColDefKind.values();
    }

    @Override
    protected List<IColumnDefinition<EntityTypePropertyType<?>>> getAvailableFilters()
    {
        return asColumnFilters(new PropertyTypeAssignmentColDefKind[]
            { PropertyTypeAssignmentColDefKind.PROPERTY_TYPE_CODE,
                    PropertyTypeAssignmentColDefKind.ENTITY_TYPE_CODE,
                    PropertyTypeAssignmentColDefKind.ENTITY_KIND });
    }

    @Override
    protected BaseEntityModel<EntityTypePropertyType<?>> createModel(
            EntityTypePropertyType<?> entity)
    {
        return new BaseEntityModel<EntityTypePropertyType<?>>(entity, getStaticColumnsDefinition());
    }

    @Override
    protected void listEntities(
            DefaultResultSetConfig<String, EntityTypePropertyType<?>> resultSetConfig,
            AbstractAsyncCallback<ResultSet<EntityTypePropertyType<?>>> callback)
    {
        viewContext.getService().listPropertyTypeAssignments(resultSetConfig, callback);
    }

    @Override
    protected void prepareExportEntities(
            TableExportCriteria<EntityTypePropertyType<?>> exportCriteria,
            AbstractAsyncCallback<String> callback)
    {
        viewContext.getService().prepareExportPropertyTypeAssignments(exportCriteria, callback);
    }
}