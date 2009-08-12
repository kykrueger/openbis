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

package ch.systemsx.cisd.openbis.plugin.phosphonetx.client.web.client.application;

import java.util.List;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DispatcherHelper;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.renderer.LinkRenderer;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.framework.IColumnDefinitionKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.AbstractSimpleBrowserGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.ColumnDefsAndConfigs;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.ICellListener;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IDisposableComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DefaultResultSetConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.IColumnDefinition;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ResultSet;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TableExportCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind.ObjectKind;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.client.web.client.IPhosphoNetXClientServiceAsync;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.client.web.client.application.columns.ProteinColDefKind;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.client.web.client.dto.ListProteinByExperimentCriteria;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.client.web.client.dto.ProteinInfo;

/**
 * @author Franz-Josef Elmer
 */
class ProteinByExperimentBrowserGrid extends AbstractSimpleBrowserGrid<ProteinInfo>
{
    private static final String PREFIX =
            GenericConstants.ID_PREFIX + "protein-by-experiment-browser";

    // browser consists of the grid and additional toolbars (paging, filtering)
    public static final String BROWSER_ID = PREFIX + "_main";

    public static final String GRID_ID = PREFIX + "_grid";

    private final IViewContext<IPhosphoNetXClientServiceAsync> specificViewContext;

    private final ProteinByExperimentBrowerToolBar toolbar;

    private ListProteinByExperimentCriteria criteria;

    static IDisposableComponent create(
            final IViewContext<IPhosphoNetXClientServiceAsync> viewContext)
    {
        final ProteinByExperimentBrowserGrid browserGrid =
                new ProteinByExperimentBrowserGrid(viewContext);
        return browserGrid.asDisposableWithToolbar(browserGrid.toolbar);
    }

    private ProteinByExperimentBrowserGrid(
            final IViewContext<IPhosphoNetXClientServiceAsync> viewContext)
    {
        super(viewContext.getCommonViewContext(), BROWSER_ID, GRID_ID, false);
        specificViewContext = viewContext;
        toolbar = new ProteinByExperimentBrowerToolBar(viewContext);
        toolbar.setBrowserGrid(this);
        setDisplayTypeIDGenerator(PhosphoNetXDisplayTypeIDGenerator.PROTEIN_BY_EXPERIMENT_BROWSER_GRID);
        registerLinkClickListenerFor(ProteinColDefKind.UNIPROT_ID.id(),
                new ICellListener<ProteinInfo>()
                    {
                        public void handle(ProteinInfo rowItem)
                        {
                            DispatcherHelper.dispatchNaviEvent(ProteinViewer.createTabItemFactory(
                                    viewContext, toolbar.getExperimentOrNull(), rowItem));
                        }
                    });
    }

    void update(TechId experimentID, double falseDiscoveryRate)
    {
        criteria = new ListProteinByExperimentCriteria();
        criteria.setExperimentID(experimentID);
        criteria.setFalseDiscoveryRate(falseDiscoveryRate);
        refresh();
    }

    @Override
    protected IColumnDefinitionKind<ProteinInfo>[] getStaticColumnsDefinition()
    {
        return ProteinColDefKind.values();
    }

    @Override
    protected ColumnDefsAndConfigs<ProteinInfo> createColumnsDefinition()
    {
        ColumnDefsAndConfigs<ProteinInfo> definitions = super.createColumnsDefinition();
        definitions.setGridCellRendererFor(ProteinColDefKind.UNIPROT_ID.id(), LinkRenderer
                .createLinkRenderer());
        return definitions;
    }

    @Override
    protected List<IColumnDefinition<ProteinInfo>> getInitialFilters()
    {
        return asColumnFilters(new ProteinColDefKind[]
            { ProteinColDefKind.DESCRIPTION });
    }

    @Override
    protected void listEntities(DefaultResultSetConfig<String, ProteinInfo> resultSetConfig,
            AbstractAsyncCallback<ResultSet<ProteinInfo>> callback)
    {
        if (criteria != null)
        {
            criteria.copyPagingConfig(resultSetConfig);
            specificViewContext.getService().listProteinsByExperiment(criteria, callback);
        }
    }

    @Override
    protected void prepareExportEntities(TableExportCriteria<ProteinInfo> exportCriteria,
            AbstractAsyncCallback<String> callback)
    {
        specificViewContext.getService().prepareExportProteins(exportCriteria, callback);
    }

    public DatabaseModificationKind[] getRelevantModifications()
    {
        return new DatabaseModificationKind[]
            { DatabaseModificationKind.createOrDelete(ObjectKind.DATA_SET) };
    }

}
