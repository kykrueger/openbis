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

package ch.systemsx.cisd.openbis.uitest.page;

import java.util.List;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.FindBys;

public class SampleTypeBrowser extends BrowserPage
{

    @FindBy(id = "add-entity-type")
    private WebElement addSampleTypeButton;

    @FindBys(
        {
                @FindBy(id = "openbis_sample-type-browser-grid"),
                @FindBy(xpath = "//*[contains(@class, \"x-grid\") and contains(@class, \"-header \")]") })
    private List<WebElement> columns;

    @FindBys(
        {
                @FindBy(id = "openbis_sample-type-browser-grid"),
                @FindBy(xpath = "//*[contains(@class, \"x-grid\") and contains(@class, \"-col \")]") })
    private List<WebElement> data;

    public AddSampleTypeDialog add()
    {
        addSampleTypeButton.click();
        return get(AddSampleTypeDialog.class);
    }

    @Override
    protected List<WebElement> getColumns()
    {
        return this.columns;
    }

    @Override
    protected List<WebElement> getData()
    {
        return this.data;
    }

}