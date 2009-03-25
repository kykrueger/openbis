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

import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.google.gwt.user.client.rpc.AsyncCallback;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.CommonViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.AbstractRegistrationDialog;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IDelegatedAction;

/**
 * {@link Window} containing person registration form.
 * 
 * @author Izabela Adamczyk
 */
public class AddPersonDialog extends AbstractRegistrationDialog
{
    private final CommonViewContext viewContext;

    private final TextField<String> codeField;

    public AddPersonDialog(final CommonViewContext viewContext,
            final IDelegatedAction postRegistrationCallback)
    {
        super(viewContext, "Add a new person", postRegistrationCallback);

        this.viewContext = viewContext;
        this.codeField = createCodeField();
        addField(codeField);
    }

    @Override
    protected void register(AsyncCallback<Void> registrationCallback)
    {
        viewContext.getService().registerPerson(codeField.getValue(), registrationCallback);
    }
}
