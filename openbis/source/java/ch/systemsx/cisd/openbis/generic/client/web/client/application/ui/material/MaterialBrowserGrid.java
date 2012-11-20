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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.material;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.button.Button;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DisplayTypeIDGenerator;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.BaseEntityModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.renderer.PersonRenderer;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.DisplayedAndSelectedEntities;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.IChosenEntitiesListener;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.IChosenEntitiesProvider;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.MetaprojectChooserButton;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.AbstractEntityGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.ColumnDefsAndConfigs;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.DisposableEntityChooser;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.GridUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IBrowserGridActionInvoker;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.ICriteriaProvider;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.IDataRefreshCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IDelegatedAction;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IDelegatedActionWithResult;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ColumnDistinctValues;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DefaultResultSetConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.GridCustomColumnInfo;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.GridRowModels;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ListMaterialDisplayCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.MaterialGridColumnIDs;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ResultSet;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TableExportCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TypedTableResultSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.GridRowModel;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind.ObjectKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Material;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Metaproject;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MetaprojectCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelColumnHeader;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelRowWithObject;

/**
 * A {@link LayoutContainer} which contains the grid where the materials are displayed.
 * 
 * @author Izabela Adamczyk
 */
public class MaterialBrowserGrid extends AbstractEntityGrid<Material>
{
    private static final String PREFIX = "material-browser";

    public static final String BROWSER_ID = GenericConstants.ID_PREFIX + PREFIX;

    public static final String ADD_METAPROJECTS_BUTTON_ID = BROWSER_ID + "_add-metaprojects";

    public static final String REMOVE_METAPROJECTS_BUTTON_ID = BROWSER_ID + "_remove-metaprojects";

    public static final String GRID_ID = BROWSER_ID + "_grid";

    /**
     * Creates a browser with a toolbar which allows to choose the material type. Allows to show or
     * edit material details.
     * 
     * @param initialMaterialTypeOrNull
     */
    public static DisposableEntityChooser<TableModelRowWithObject<Material>> createWithTypeChooser(
            final IViewContext<ICommonClientServiceAsync> viewContext,
            String initialMaterialTypeOrNull)
    {
        return createWithTypeChooser(viewContext, true, initialMaterialTypeOrNull);
    }

    private static DisposableEntityChooser<TableModelRowWithObject<Material>> createWithTypeChooser(
            final IViewContext<ICommonClientServiceAsync> viewContext, boolean detailsAvailable,
            String initialMaterialTypeOrNull)
    {
        final MaterialBrowserToolbar toolbar =
                new MaterialBrowserToolbar(viewContext, initialMaterialTypeOrNull, null);
        final ICriteriaProvider<ListMaterialDisplayCriteria> criteriaProvider = toolbar;
        final MaterialBrowserGrid browserGrid =
                createBrowserGrid(viewContext, criteriaProvider, detailsAvailable);
        browserGrid.addGridRefreshListener(toolbar);
        browserGrid.extendBottomToolbar(detailsAvailable);
        return browserGrid.asDisposableWithToolbar(toolbar);
    }

    public static DisposableEntityChooser<TableModelRowWithObject<Material>> createForMetaproject(
            final IViewContext<?> viewContext, TechId metaprojectId)
    {
        final ListMaterialDisplayCriteria criteria =
                ListMaterialDisplayCriteria.createForMetaproject(new MetaprojectCriteria(
                        metaprojectId.getId()));
        final ICriteriaProvider<ListMaterialDisplayCriteria> criteriaProvider =
                new ICriteriaProvider<ListMaterialDisplayCriteria>()
                    {
                        @Override
                        public ListMaterialDisplayCriteria tryGetCriteria()
                        {
                            return criteria;
                        }

                        @Override
                        public DatabaseModificationKind[] getRelevantModifications()
                        {
                            return new DatabaseModificationKind[0];
                        }

                        @Override
                        public void update(Set<DatabaseModificationKind> observedModifications,
                                IDataRefreshCallback postRefreshCallback)
                        {
                            postRefreshCallback.postRefresh(true);
                        }
                    };
        final MaterialBrowserGrid browserGrid =
                createBrowserGrid(viewContext.getCommonViewContext(), criteriaProvider, true);
        browserGrid.addEntityOperationsLabel();
        browserGrid.addTaggingButtons();
        browserGrid.addEntityOperationsSeparator();
        browserGrid.allowMultipleSelection();
        return browserGrid.asDisposableWithoutToolbar();
    }

    /**
     * If the material type is given, does not show the toolbar with material type selection and
     * refreshes the grid automatically.<br>
     * Does not allow to show or edit the material details.
     */
    public static DisposableEntityChooser<TableModelRowWithObject<Material>> create(
            final IViewContext<ICommonClientServiceAsync> viewContext,
            final MaterialType initValueOrNull)
    {
        if (initValueOrNull == null)
        {
            return createWithTypeChooser(viewContext, false, null);
        } else
        {
            return createWithoutTypeChooser(viewContext, initValueOrNull);
        }
    }

    private static DisposableEntityChooser<TableModelRowWithObject<Material>> createWithoutTypeChooser(
            final IViewContext<ICommonClientServiceAsync> viewContext, final MaterialType initValue)
    {
        final ListMaterialDisplayCriteria criteria =
                ListMaterialDisplayCriteria.createForMaterialType(initValue);
        final ICriteriaProvider<ListMaterialDisplayCriteria> criteriaProvider =
                new ICriteriaProvider<ListMaterialDisplayCriteria>()
                    {
                        @Override
                        public ListMaterialDisplayCriteria tryGetCriteria()
                        {
                            return criteria;
                        }

                        @Override
                        public DatabaseModificationKind[] getRelevantModifications()
                        {
                            return new DatabaseModificationKind[0];
                        }

                        @Override
                        public void update(Set<DatabaseModificationKind> observedModifications,
                                IDataRefreshCallback postRefreshCallback)
                        {
                            postRefreshCallback.postRefresh(true);
                        }
                    };
        boolean detailsAvailable = false;
        final MaterialBrowserGrid browserGrid =
                createBrowserGrid(viewContext, criteriaProvider, detailsAvailable);
        return browserGrid.asDisposableWithoutToolbar();
    }

    private static MaterialBrowserGrid createBrowserGrid(
            final IViewContext<ICommonClientServiceAsync> viewContext,
            final ICriteriaProvider<ListMaterialDisplayCriteria> criteriaProvider,
            boolean detailsAvailable)
    {
        if (detailsAvailable)
        {
            return new MaterialBrowserGrid(viewContext, true, criteriaProvider);
        } else
        {
            return new MaterialBrowserGrid(viewContext, true, criteriaProvider)
                {
                    @Override
                    protected void showEntityViewer(Material material, boolean editMode,
                            boolean active)
                    {
                        // do nothing - avoid showing the details after double click
                    }
                };
        }

    }

    protected final ICriteriaProvider<ListMaterialDisplayCriteria> criteriaProvider;

    protected MaterialBrowserGrid(final IViewContext<ICommonClientServiceAsync> viewContext,
            boolean refreshAutomatically,
            ICriteriaProvider<ListMaterialDisplayCriteria> criteriaProvider)
    {
        super(viewContext, GRID_ID, refreshAutomatically,
                DisplayTypeIDGenerator.ENTITY_BROWSER_GRID);
        this.criteriaProvider = criteriaProvider;
        registerLinkClickListenerFor(MaterialGridColumnIDs.CODE, showEntityViewerLinkClickListener);
        setId(BROWSER_ID);
    }

    @Override
    protected String translateColumnIdToDictionaryKey(String columnID)
    {
        return columnID.toLowerCase();
    }

    protected void extendBottomToolbar(boolean detailsAvailable)
    {
        if (detailsAvailable && viewContext.isSimpleOrEmbeddedMode() == false)
        {
            addEntityOperationsLabel();
            addEntityOperationButtons();
            addEntityOperationsSeparator();
        }
    }

    protected void addTaggingButtons()
    {
        final MetaprojectChooserButton tagButton =
                new MetaprojectChooserButton(viewContext, getId(),
                        new IChosenEntitiesProvider<String>()
                            {
                                @Override
                                public List<String> getEntities()
                                {
                                    return getMetaProjectsReferencedyByEachOf(taggables(getSelectedItems()));
                                }

                                @Override
                                public boolean isBlackList()
                                {
                                    return true;
                                }
                            });

        tagButton
                .addChosenEntityListener(new IChosenEntitiesListener<TableModelRowWithObject<Metaproject>>()
                    {
                        @Override
                        public void entitiesChosen(
                                List<TableModelRowWithObject<Metaproject>> entities)
                        {
                            List<Long> materialIds = new ArrayList<Long>();
                            for (BaseEntityModel<TableModelRowWithObject<Material>> item : getSelectedItems())
                            {
                                materialIds.add(item.getBaseObject().getObjectOrNull().getId());
                            }

                            List<Long> metaProjectIds = new ArrayList<Long>();
                            for (TableModelRowWithObject<Metaproject> row : entities)
                            {
                                metaProjectIds.add(row.getObjectOrNull().getId());
                            }
                            viewContext.getCommonService().assignMaterialsToMetaProjects(
                                    metaProjectIds, materialIds,
                                    createRefreshCallback(asActionInvoker()));

                        }
                    });

        tagButton.setId(ADD_METAPROJECTS_BUTTON_ID);
        tagButton.setText(viewContext.getMessage(Dict.BUTTON_TAG));
        enableButtonOnSelectedItems(tagButton);
        addButton(tagButton);

        final MetaprojectChooserButton untagButton =
                new MetaprojectChooserButton(viewContext, getId(),
                        new IChosenEntitiesProvider<String>()
                            {
                                @Override
                                public List<String> getEntities()
                                {
                                    return getMetaProjectsReferencedByAtLeastOneOf(taggables(getSelectedItems()));
                                }

                                @Override
                                public boolean isBlackList()
                                {
                                    return false;
                                }
                            });

        untagButton
                .addChosenEntityListener(new IChosenEntitiesListener<TableModelRowWithObject<Metaproject>>()
                    {
                        @Override
                        public void entitiesChosen(
                                List<TableModelRowWithObject<Metaproject>> entities)
                        {
                            List<Long> materialIds = new ArrayList<Long>();
                            for (BaseEntityModel<TableModelRowWithObject<Material>> item : getSelectedItems())
                            {
                                materialIds.add(item.getBaseObject().getObjectOrNull().getId());
                            }

                            List<Long> metaProjectIds = new ArrayList<Long>();
                            for (TableModelRowWithObject<Metaproject> row : entities)
                            {
                                metaProjectIds.add(row.getObjectOrNull().getId());
                            }
                            viewContext.getCommonService()
                                    .removeMaterialsFromMetaProjects(
                                            metaProjectIds, materialIds,
                                            createRefreshCallback(asActionInvoker()));

                        }
                    });

        untagButton.setId(REMOVE_METAPROJECTS_BUTTON_ID);
        untagButton.setText(viewContext.getMessage(Dict.BUTTON_UNTAG));
        enableButtonOnSelectedItems(untagButton);
        addButton(untagButton);
    }

    private void addEntityOperationButtons()
    {
        String showDetailsTitle = viewContext.getMessage(Dict.BUTTON_SHOW_DETAILS);
        Button showDetailsButton =
                createSelectedItemButton(showDetailsTitle, asShowEntityInvoker(false));
        addButton(showDetailsButton);

        String editTitle = viewContext.getMessage(Dict.BUTTON_EDIT);
        Button editButton = createSelectedItemButton(editTitle, asShowEntityInvoker(true));
        addButton(editButton);

        addTaggingButtons();

        final String deleteTitle = viewContext.getMessage(Dict.BUTTON_DELETE);
        final String deleteAllTitle = deleteTitle + " All";
        final Button deleteButton = new Button(deleteAllTitle, new AbstractCreateDialogListener()
            {
                @Override
                protected Dialog createDialog(List<TableModelRowWithObject<Material>> materials,
                        IBrowserGridActionInvoker invoker)
                {
                    return new MaterialListDeletionConfirmationDialog(viewContext,
                            createRefreshCallback(invoker), getDisplayedAndSelectedItemsAction()
                                    .execute());
                }
            });
        changeButtonTitleOnSelectedItems(deleteButton, deleteAllTitle, deleteTitle);
        addButton(deleteButton);
        allowMultipleSelection(); // we allow deletion of multiple materials
    }

    protected void addGridRefreshListener(MaterialBrowserToolbar toolbar)
    {
        toolbar.setCriteriaChangedListeners(createGridRefreshDelegatedAction());
    }

    protected final IDelegatedAction createGridRefreshDelegatedAction()
    {
        return new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    if (criteriaProvider.tryGetCriteria() != null)
                    {
                        refreshGridWithFilters();
                    }
                }
            };
    }

    @Override
    public String getGridDisplayTypeID()
    {
        ListMaterialDisplayCriteria criteria = criteriaProvider.tryGetCriteria();

        if (criteria == null || criteria.getListCriteria() != null)
        {
            String suffix =
                    createDisplayIdSuffix(EntityKind.MATERIAL, criteria == null ? null : criteria
                            .getListCriteria().tryGetMaterialType());
            return createGridDisplayTypeID(suffix);
        } else
        {
            return createGridDisplayTypeID(null);
        }
    }

    @Override
    protected void listTableRows(
            DefaultResultSetConfig<String, TableModelRowWithObject<Material>> resultSetConfig,
            AbstractAsyncCallback<TypedTableResultSet<Material>> callback)
    {
        ListMaterialDisplayCriteria criteria = criteriaProvider.tryGetCriteria();
        if (criteria == null)
        {
            satisfyCallbackWithEmptyResultSet(callback);
        } else
        {
            criteria.copyPagingConfig(resultSetConfig);
            viewContext.getService().listMaterials(criteria, callback);
        }
    }

    @SuppressWarnings("unchecked")
    protected void satisfyCallbackWithEmptyResultSet(
            AbstractAsyncCallback<TypedTableResultSet<Material>> callback)
    {
        ResultSet<TableModelRowWithObject<Material>> resultSet =
                new ResultSet<TableModelRowWithObject<Material>>();
        resultSet.setList(new GridRowModels<TableModelRowWithObject<Material>>(Arrays
                .<GridRowModel<TableModelRowWithObject<Material>>> asList(), Arrays
                .<TableModelColumnHeader> asList(), Arrays.<GridCustomColumnInfo> asList(), Arrays
                .<ColumnDistinctValues> asList()));
        resultSet.setTotalLength(0);
        callback.onSuccess(new TypedTableResultSet<Material>(resultSet));
    }

    @Override
    protected ColumnDefsAndConfigs<TableModelRowWithObject<Material>> createColumnsDefinition()
    {
        ColumnDefsAndConfigs<TableModelRowWithObject<Material>> schema =
                super.createColumnsDefinition();
        schema.setGridCellRendererFor(MaterialGridColumnIDs.REGISTRATOR,
                PersonRenderer.REGISTRATOR_RENDERER);
        return schema;
    }

    @Override
    protected void prepareExportEntities(
            TableExportCriteria<TableModelRowWithObject<Material>> exportCriteria,
            AbstractAsyncCallback<String> callback)
    {
        viewContext.getService().prepareExportMaterials(exportCriteria, callback);
    }

    @Override
    public DatabaseModificationKind[] getRelevantModifications()
    {
        return GridUtils.getRelevantModifications(ObjectKind.MATERIAL, criteriaProvider);
    }

    @Override
    protected List<String> getColumnIdsOfFilters()
    {
        return Arrays.asList(MaterialGridColumnIDs.CODE);
    }

    protected void showEntityViewer(Material material, boolean editMode, boolean inBackground)
    {
        showEntityInformationHolderViewer(material, editMode, inBackground);
    }

    @Override
    protected EntityKind getEntityKindOrNull()
    {
        return EntityKind.MATERIAL;
    }

    public final class DisplayedAndSelectedMaterials extends
            DisplayedAndSelectedEntities<TableModelRowWithObject<Material>>
    {

        public DisplayedAndSelectedMaterials(List<TableModelRowWithObject<Material>> selectedItems,
                TableExportCriteria<TableModelRowWithObject<Material>> displayedItemsConfig,
                int displayedItemsCount)
        {
            super(selectedItems, displayedItemsConfig, displayedItemsCount);
        }
    }

    protected final IDelegatedActionWithResult<DisplayedAndSelectedMaterials> getDisplayedAndSelectedItemsAction()
    {
        return new IDelegatedActionWithResult<DisplayedAndSelectedMaterials>()
            {
                @Override
                public DisplayedAndSelectedMaterials execute()
                {
                    return new DisplayedAndSelectedMaterials(getSelectedBaseObjects(),
                            createTableExportCriteria(), getTotalCount());
                }
            };
    }

    private List<Taggable> taggables(List<BaseEntityModel<TableModelRowWithObject<Material>>> data)
    {
        List<Taggable> list = new ArrayList<Taggable>();
        for (final BaseEntityModel<TableModelRowWithObject<Material>> item : data)
        {
            list.add(new Taggable()
                {
                    @Override
                    public Collection<Metaproject> getMetaprojects()
                    {
                        return item.getBaseObject().getObjectOrNull()
                                .getMetaprojects();
                    }
                });
        }
        return list;
    }

}
