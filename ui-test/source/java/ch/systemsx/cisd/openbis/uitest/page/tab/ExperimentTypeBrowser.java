/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.uitest.page.tab;

import ch.systemsx.cisd.openbis.uitest.infra.Browser;
import ch.systemsx.cisd.openbis.uitest.infra.Cell;
import ch.systemsx.cisd.openbis.uitest.infra.Row;
import ch.systemsx.cisd.openbis.uitest.infra.webdriver.Lazy;
import ch.systemsx.cisd.openbis.uitest.infra.webdriver.Locate;
import ch.systemsx.cisd.openbis.uitest.infra.webdriver.WaitForRefreshOf;
import ch.systemsx.cisd.openbis.uitest.type.ExperimentType;
import ch.systemsx.cisd.openbis.uitest.widget.Button;
import ch.systemsx.cisd.openbis.uitest.widget.FilterToolBar;
import ch.systemsx.cisd.openbis.uitest.widget.Grid;
import ch.systemsx.cisd.openbis.uitest.widget.PagingToolBar;

public class ExperimentTypeBrowser implements Browser<ExperimentType>
{

    @Locate("add-entity-type-EXPERIMENT")
    private Button add;

    @SuppressWarnings("unused")
    @Locate("edit-entity-type-EXPERIMENT")
    private Button edit;

    @SuppressWarnings("unused")
    @Locate("delete-entity-type-EXPERIMENT")
    private Button delete;

    @Locate("openbis_experiment-type-browser-grid")
    private Grid grid;

    @Locate("openbis_experiment-type-browser-grid-paging-toolbar")
    private PagingToolBar paging;

    @Lazy
    @Locate("openbis_experiment-type-browser-grid-filter-toolbar")
    private FilterToolBar filters;

    public void add()
    {
        add.click();
    }

    @Override
    public Row select(ExperimentType experimentType)
    {
        return grid.select("Code", experimentType.getCode());
    }

    @Override
    public Cell cell(ExperimentType experimentType, String column)
    {
        return select(experimentType).get(column);
    }

    @Override
    public void filter(ExperimentType type)
    {
        paging.filters();
        filters.setCode(type.getCode());
        new WaitForRefreshOf(grid).withTimeoutOf(10);
    }

    @Override
    public void resetFilters()
    {
        paging.filters();
        filters.reset();
    }

    @Override
    public String toString()
    {
        String s = "ExperimentTypeBrowser\n==========\n";
        return s + grid.toString();
    }

}