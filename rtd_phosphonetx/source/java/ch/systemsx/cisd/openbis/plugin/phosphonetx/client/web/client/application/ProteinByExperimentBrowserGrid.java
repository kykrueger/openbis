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
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.framework.IColumnDefinitionKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.AbstractSimpleBrowserGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IDisposableComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DefaultResultSetConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.IColumnDefinition;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ResultSet;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TableExportCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind.ObjectKind;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.client.web.client.IPhosphoNetXClientServiceAsync;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.client.web.client.dto.ListProteinByExperimentCriteria;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.basic.dto.Protein;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
class ProteinByExperimentBrowserGrid extends AbstractSimpleBrowserGrid<Protein>
{
    private static final String PREFIX = GenericConstants.ID_PREFIX + "protein-by-experiment-browser";

    // browser consists of the grid and additional toolbars (paging, filtering)
    public static final String BROWSER_ID = PREFIX + "_main";

    public static final String GRID_ID = PREFIX + "_grid";

    private final IViewContext<IPhosphoNetXClientServiceAsync> specificViewContext;

    private ListProteinByExperimentCriteria criteria;
    
    static IDisposableComponent create(
            final IViewContext<IPhosphoNetXClientServiceAsync> viewContext)
    {
        final ProteinByExperimentBrowerToolBar toolbar =
                new ProteinByExperimentBrowerToolBar(viewContext);
        final ProteinByExperimentBrowserGrid browserGrid =
                new ProteinByExperimentBrowserGrid(viewContext);
        toolbar.setBrowserGrid(browserGrid);
        return browserGrid.asDisposableWithToolbar(toolbar);
    }
    
    private ProteinByExperimentBrowserGrid(IViewContext<IPhosphoNetXClientServiceAsync> viewContext)
    {
        super(viewContext.getCommonViewContext(), BROWSER_ID, GRID_ID);
        specificViewContext = viewContext;
        setDisplayTypeIDGenerator(PhosphoNetXDisplayTypeIDGenerator.PROTEIN_BY_EXPERIMENT_BROWSER_GRID);
    }
    
    void update(ExperimentIdentifier identifier)
    {
        criteria = new ListProteinByExperimentCriteria();
        criteria.setExperimentID(identifier.getTechID());
        refresh();
    }

    @Override
    protected IColumnDefinitionKind<Protein>[] getStaticColumnsDefinition()
    {
        return ProteinColDefKind.values();
    }

    @Override
    protected List<IColumnDefinition<Protein>> getInitialFilters()
    {
        return asColumnFilters(new ProteinColDefKind[] {ProteinColDefKind.DESCRIPTION});
    }
    
    @Override
    protected void listEntities(DefaultResultSetConfig<String, Protein> resultSetConfig,
            AbstractAsyncCallback<ResultSet<Protein>> callback)
    {
        if (criteria != null)
        {
            specificViewContext.getService().listProteinsByExperiment(criteria, callback);
        }
    }

    @Override
    protected void prepareExportEntities(TableExportCriteria<Protein> exportCriteria,
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
