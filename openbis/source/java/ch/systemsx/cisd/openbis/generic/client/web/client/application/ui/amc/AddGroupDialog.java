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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.amc;

import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.TextField;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.CommonViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GroupsView;

/**
 * {@link Window} containing group registration form.
 * 
 * @author Franz-Josef Elmer
 * @author Izabela Adamczyk
 */
public class AddGroupDialog extends Window
{
    final class RegisterGroupCallback extends AbstractAsyncCallback<Void>
    {
        private RegisterGroupCallback(final CommonViewContext viewContext)
        {
            super(viewContext);
        }

        @Override
        public void process(final Void result)
        {
            hide();
            groupList.refresh();
        }
    }

    private static final String PREFIX = "add-group_";

    static final String CODE_FIELD_ID = GenericConstants.ID_PREFIX + PREFIX + "code-field";

    static final String SAVE_BUTTON_ID = GenericConstants.ID_PREFIX + PREFIX + "save-button";

    private final GroupsView groupList;

    public AddGroupDialog(final CommonViewContext viewContext, final GroupsView g)
    {
        this.groupList = g;
        setHeading("Add a new group");
        setModal(true);
        setWidth(400);
        final FormPanel form = new FormPanel();
        form.setHeaderVisible(false);
        form.setBorders(false);
        form.setBodyBorder(false);
        add(form);
        final TextField<String> codeField = new TextField<String>();
        codeField.setWidth(100);
        codeField.setFieldLabel("Code");
        codeField.setAllowBlank(false);
        codeField.setValidateOnBlur(true);
        codeField.setId(CODE_FIELD_ID);
        form.add(codeField);

        final TextField<String> descriptionField = new TextField<String>();
        descriptionField.setFieldLabel("Description");
        form.add(descriptionField);

        final Button saveButton = new Button("Save", new SelectionListener<ComponentEvent>()
            {
                @Override
                public void componentSelected(ComponentEvent ce)
                {
                    viewContext.getService().registerGroup(codeField.getValue(),
                            descriptionField.getValue(), null,
                            new RegisterGroupCallback(viewContext));
                }
            });
        saveButton.setId(SAVE_BUTTON_ID);
        addButton(saveButton);
        addButton(new Button("Cancel", new SelectionListener<ComponentEvent>()
            {
                @Override
                public void componentSelected(final ComponentEvent ce)
                {
                    hide();
                }
            }));
    }

}
