/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.dataset;

import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.CheckBox;
import com.google.gwt.user.client.ui.Frame;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.SingleSectionPanel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.DataSetUtils;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;

/**
 * Section panel presenting data from Data Store Server.
 * 
 * @author Izabela Adamczyk
 */
public class DataViewSection extends SingleSectionPanel
{

    public DataViewSection(final IViewContext<?> viewContext,
            final ExternalData dataset)
    {
        super(viewContext.getMessage(Dict.DATA_VIEW));

        Button refreshButton = new Button("Refresh");
        final CheckBox autoResolveCheckbox = new CheckBox();
        autoResolveCheckbox.setBoxLabel("Auto Resolve");
        autoResolveCheckbox.setValue(true);

        final Frame iFrame = new Frame(createUrl(viewContext, dataset, autoResolveCheckbox));

        autoResolveCheckbox.addListener(Events.Change, new Listener<BaseEvent>()
            {
                public void handleEvent(BaseEvent be)
                {
                    iFrame.setUrl(createUrl(viewContext, dataset, autoResolveCheckbox));
                }
            });
        refreshButton.addSelectionListener(new SelectionListener<ButtonEvent>()
            {
                @Override
                public void componentSelected(ButtonEvent ce)
                {
                    iFrame.setUrl(createUrl(viewContext, dataset, autoResolveCheckbox));
                }
            });

        getHeader().addTool(autoResolveCheckbox);
        getHeader().addTool(refreshButton);

        add(iFrame);
    }

    private String createUrl(final IViewContext<?> viewContext,
            final ExternalData dataset, CheckBox autoResolve)
    {
        return DataSetUtils.createDataViewUrl(dataset, viewContext.getModel(), "simpleHtml",
                autoResolve.getValue());
    }

}
