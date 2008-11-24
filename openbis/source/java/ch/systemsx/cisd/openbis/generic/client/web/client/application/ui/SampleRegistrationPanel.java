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

import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.toolbar.AdapterToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.LabelToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.user.client.Element;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.sample_browser.SampleTypeSelectionWidget;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SampleType;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.sample.GenericSampleRegistrationForm;

/**
 * @author Izabela Adamczyk
 */
public class SampleRegistrationPanel extends ContentPanel
{

    private final SampleTypeSelectionWidget sampleTypeSelection;

    public SampleRegistrationPanel(final GenericViewContext viewContext)
    {
        setHeading("Sample registration");
        setHeaderVisible(false);
        setBodyBorder(false);
        setScrollMode(Scroll.AUTO);
        sampleTypeSelection = new SampleTypeSelectionWidget(viewContext, true);
        sampleTypeSelection.addSelectionChangedListener(new SelectionChangedListener<ModelData>()
            {
                @Override
                public void selectionChanged(final SelectionChangedEvent<ModelData> se)
                {
                    final SampleType selectedType = sampleTypeSelection.tryGetSelected();
                    if (selectedType != null)
                    {
                        removeAll();
                        add(new GenericSampleRegistrationForm(viewContext, selectedType));
                        layout();
                    }
                }
            });
        final ToolBar toolBar = new ToolBar();
        toolBar.add(new LabelToolItem("Sample type:"));
        toolBar.add(new AdapterToolItem(sampleTypeSelection));
        setTopComponent(toolBar);
    }

    @Override
    protected void onRender(final Element parent, final int pos)
    {
        super.onRender(parent, pos);
        createUI();
    }

    void createUI()
    {

    }
}
