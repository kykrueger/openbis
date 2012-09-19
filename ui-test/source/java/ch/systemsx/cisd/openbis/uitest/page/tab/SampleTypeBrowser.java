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

import java.util.List;

import org.openqa.selenium.WebElement;

import ch.systemsx.cisd.openbis.uitest.infra.webdriver.Locate;
import ch.systemsx.cisd.openbis.uitest.page.common.BrowserPage;
import ch.systemsx.cisd.openbis.uitest.page.dialog.AddSampleTypeDialog;
import ch.systemsx.cisd.openbis.uitest.page.dialog.EditSampleTypeDialog;
import ch.systemsx.cisd.openbis.uitest.type.SampleType;
import ch.systemsx.cisd.openbis.uitest.widget.Button;
import ch.systemsx.cisd.openbis.uitest.widget.Grid;

public class SampleTypeBrowser extends BrowserPage
{
    @Locate("add-entity-type-SAMPLE")
    private Button add;

    @Locate("edit-entity-type-SAMPLE")
    private Button edit;

    @Locate("delete-entity-type-SAMPLE")
    private Button delete;

    @Locate("openbis_sample-type-browser-grid")
    private Grid grid;

    public AddSampleTypeDialog add()
    {
        add.click();
        return get(AddSampleTypeDialog.class);
    }

    @Override
    protected List<WebElement> getColumns()
    {
        return grid.getColumns();
    }

    @Override
    protected List<WebElement> getData()
    {
        return grid.getCells();
    }

    public EditSampleTypeDialog editSampleType(SampleType type)
    {
        grid.select(type.getCode());
        edit.click();
        return get(EditSampleTypeDialog.class);
    }

    @Override
    protected WebElement getDeleteButton()
    {
        return delete.getContext();
    }
}