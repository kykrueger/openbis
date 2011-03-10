/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.plugin.phosphonetx.client.web.client.application.wizard;

import java.util.Arrays;
import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.TypedTableGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DefaultResultSetConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TableExportCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TypedTableResultSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelRowWithObject;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.client.web.client.IPhosphoNetXClientServiceAsync;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.client.web.client.application.PhosphoNetXDisplayTypeIDGenerator;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.client.web.client.dto.BiologicalSampleGridColumnIDs;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class BiologicalSampleGrid extends TypedTableGrid<Sample>
{
    private static final String PREFIX = GenericConstants.ID_PREFIX
            + "biological_sample";

    public static final String BROWSER_ID = PREFIX + "_main";

    public static final String GRID_ID = PREFIX + TypedTableGrid.GRID_POSTFIX;

    private final IViewContext<IPhosphoNetXClientServiceAsync> specificViewContext;

    public BiologicalSampleGrid(IViewContext<IPhosphoNetXClientServiceAsync> viewContext)
    {
        super(viewContext.getCommonViewContext(), BROWSER_ID, true,
                PhosphoNetXDisplayTypeIDGenerator.BIOLOGICAL_SAMPLE_BROWSER_GRID);
        specificViewContext = viewContext;
        removeConfigAndExportButtons();
        removeFiltersButtons();
        showFiltersBar();
        setBorders(true);
    }

    @Override
    protected void listTableRows(
            DefaultResultSetConfig<String, TableModelRowWithObject<Sample>> resultSetConfig,
            AsyncCallback<TypedTableResultSet<Sample>> callback)
    {
        specificViewContext.getService().listBiologicalSamples(resultSetConfig, callback);
    }

    @Override
    protected List<String> getColumnIdsOfFilters()
    {
        return Arrays.asList(BiologicalSampleGridColumnIDs.IDENTIFIER,
                BiologicalSampleGridColumnIDs.REGISTRATION_DATE);
    }

    @Override
    protected void prepareExportEntities(
            TableExportCriteria<TableModelRowWithObject<Sample>> exportCriteria,
            AbstractAsyncCallback<String> callback)
    {
    }
    
    public void dispose()
    {
        asDisposableWithoutToolbar().dispose();
    }

}
