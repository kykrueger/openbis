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

package ch.systemsx.cisd.openbis.generic.client.web.client.application;

import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.MatchingEntitiesPanelColumnIDs.ENTITY_TYPE;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.MatchingEntitiesPanelColumnIDs.IDENTIFIER;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.MatchingEntitiesPanelColumnIDs.MATCHING_FIELD;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.MatchingEntitiesPanelColumnIDs.REGISTRATOR;
import static ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind.createOrDelete;
import static ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind.edit;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import com.extjs.gxt.ui.client.core.XDOM;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.google.gwt.user.client.rpc.AsyncCallback;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.AbstractTabItemFactory;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DispatcherHelper;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DisplayTypeIDGenerator;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.MatchingEntityModel.MatchingEntityColumnKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.plugin.IClientPlugin;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.plugin.IClientPluginFactory;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.renderer.PersonRenderer;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.TypedTableGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.ColumnDefsAndConfigs;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.ICellListener;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IDisposableComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.IDataRefreshCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DefaultResultSetConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SearchableEntity;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TableExportCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TypedTableResultSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.IEntityInformationHolderWithPermId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.BasicEntityType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind.ObjectKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MatchingEntity;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelRowWithObject;

/**
 * A {@link LayoutContainer} extension which displays the matching entities.
 * 
 * @author Christian Ribeaud
 */
public final class MatchingEntitiesPanel extends TypedTableGrid<MatchingEntity>
{
    static final String PREFIX = GenericConstants.ID_PREFIX + "matching-entities-panel_";

    static final String GRID_ID = PREFIX + "grid";

    public static final String SHOW_RELATED_DATASETS_BUTTON_ID = GRID_ID
            + "_show-related-datasets-button";

    private final SearchableEntity searchableEntity;

    private final String queryText;

    private final boolean useWildcardSearchMode;

    public IDisposableComponent asDisposableComponent()
    {
        return asDisposableWithoutToolbar();
    }

    public MatchingEntitiesPanel(IViewContext<ICommonClientServiceAsync> viewContext,
            SearchableEntity searchableEntity, String queryText, boolean useWildcardSearchMode)
    {
        // NOTE: refreshAutomatically is false, refreshing should be called manually
        super(viewContext, GRID_ID, false, DisplayTypeIDGenerator.SEARCH_RESULT_GRID);
        this.searchableEntity = searchableEntity;
        this.queryText = queryText;
        this.useWildcardSearchMode = useWildcardSearchMode;
        setId(createId());

        updateDefaultRefreshButton();

        registerLinkClickListenerFor(MatchingEntityColumnKind.IDENTIFIER.id(),
                new ICellListener<TableModelRowWithObject<MatchingEntity>>()
                    {
                        public void handle(TableModelRowWithObject<MatchingEntity> rowItem, boolean keyPressed)
                        {
                            showEntityViewer(rowItem, false, keyPressed);
                        }
                    });
        extendBottomToolbar();
    }

    private void extendBottomToolbar()
    {
        addEntityOperationsLabel();

        String showRelatedDatasetsTitle = viewContext.getMessage(Dict.BUTTON_SHOW_RELATED_DATASETS);
        Button showRelatedDatasetsButton =
                new Button(showRelatedDatasetsTitle, new SelectionListener<ButtonEvent>()
                    {
                        @Override
                        public void componentSelected(ButtonEvent ce)
                        {
                            showRelatedDataSets(viewContext, MatchingEntitiesPanel.this);
                        }
                    });
        showRelatedDatasetsButton.setId(SHOW_RELATED_DATASETS_BUTTON_ID);
        addButton(showRelatedDatasetsButton);
        allowMultipleSelection();

        addEntityOperationsSeparator();
    }

    private static String createId()
    {
        return PREFIX + XDOM.getUniqueId();
    }

    @Override
    protected boolean isRefreshEnabled()
    {
        return true;
    }

    /** used to refresh the results of the previously executed query */
    @Override
    protected final void refresh()
    {
        super.refresh(false);
    }

    /** used to make a first data refresh, but can be also called many times */
    public final void refresh(final IDataRefreshCallback newRefreshCallback)
    {
        super.refresh(newRefreshCallback, true);
    }

    @Override
    protected void showEntityViewer(TableModelRowWithObject<MatchingEntity> object, boolean editMode,
            boolean inBackground)
    {
        MatchingEntity matchingEntity = object.getObjectOrNull();
        final EntityKind entityKind = matchingEntity.getEntityKind();
        BasicEntityType entityType = matchingEntity.getEntityType();
        final IClientPluginFactory clientPluginFactory =
                viewContext.getClientPluginFactoryProvider().getClientPluginFactory(entityKind,
                        entityType);
        // NOTE: createEntityViewer is the only allowed operation for MatchingEntityPanel
        final IClientPlugin<BasicEntityType, IEntityInformationHolderWithPermId> clientPlugin =
                clientPluginFactory.createClientPlugin(entityKind);
        final AbstractTabItemFactory tabView =
                clientPlugin.createEntityViewer(matchingEntity.asIdentifiable());
        tabView.setInBackground(inBackground);
        DispatcherHelper.dispatchNaviEvent(tabView);
    }

    @Override
    protected ColumnDefsAndConfigs<TableModelRowWithObject<MatchingEntity>> createColumnsDefinition()
    {
        ColumnDefsAndConfigs<TableModelRowWithObject<MatchingEntity>> schema =
                super.createColumnsDefinition();
        schema.setGridCellRendererFor(REGISTRATOR, PersonRenderer.REGISTRATOR_RENDERER);
        schema.setGridCellRendererFor(IDENTIFIER, createInternalLinkCellRenderer());
        return schema;
    }

    @Override
    protected String translateColumnIdToDictionaryKey(String columnID)
    {
        return columnID.toLowerCase();
    }

    @Override
    protected void listTableRows(
            DefaultResultSetConfig<String, TableModelRowWithObject<MatchingEntity>> resultSetConfig,
            AsyncCallback<TypedTableResultSet<MatchingEntity>> callback)
    {
        ShowResultSetCutInfo<TypedTableResultSet<MatchingEntity>> info =
                new ShowResultSetCutInfo<TypedTableResultSet<MatchingEntity>>(viewContext);
        if (callback instanceof AbstractAsyncCallback)
        {
            ((AbstractAsyncCallback<TypedTableResultSet<MatchingEntity>>) callback)
                    .addOnSuccessAction(info);
        }
        viewContext.getService().listMatchingEntities(searchableEntity, queryText,
                useWildcardSearchMode, resultSetConfig, callback);
    }

    @Override
    protected void prepareExportEntities(TableExportCriteria<TableModelRowWithObject<MatchingEntity>> exportCriteria,
            AbstractAsyncCallback<String> callback)
    {
        viewContext.getService().prepareExportMatchingEntities(exportCriteria, callback);
    }

    @Override
    protected List<String> getColumnIdsOfFilters()
    {
        return Arrays.asList(ENTITY_TYPE, IDENTIFIER, MATCHING_FIELD);
    }

    @Override
    public DatabaseModificationKind[] getRelevantModifications()
    {
        return new DatabaseModificationKind[]
            { createOrDelete(ObjectKind.MATERIAL), edit(ObjectKind.MATERIAL),
                    createOrDelete(ObjectKind.SAMPLE), edit(ObjectKind.SAMPLE),
                    createOrDelete(ObjectKind.EXPERIMENT), edit(ObjectKind.EXPERIMENT),
                    createOrDelete(ObjectKind.PROPERTY_TYPE_ASSIGNMENT),
                    edit(ObjectKind.PROPERTY_TYPE_ASSIGNMENT),
                    createOrDelete(ObjectKind.VOCABULARY_TERM), edit(ObjectKind.VOCABULARY_TERM) };
    }

    @Override
    public void update(Set<DatabaseModificationKind> observedModifications)
    {
        refreshGridSilently();
    }

}
