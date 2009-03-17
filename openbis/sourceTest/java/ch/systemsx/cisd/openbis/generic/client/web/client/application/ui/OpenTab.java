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

import com.google.gwt.user.client.rpc.AsyncCallback;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.SessionContextCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.menu.ActionMenu;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.menu.TopMenu.ActionMenuKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.AbstractDefaultTestCommand;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.GWTTestUtil;

/**
 * A {@link AbstractDefaultTestCommand} extension for opening a tab by selecting an
 * {@link ActionMenu}.
 * 
 * @author Izabela Adamczyk
 * @author Piotr Buczek
 */
public final class OpenTab extends AbstractDefaultTestCommand
{
    private final ActionMenuKind action;

    public OpenTab(final ActionMenuKind action,
            final Class<? extends AsyncCallback<?>> callbackClass)
    {
        if (callbackClass == null)
        {
            addCallbackClass(SessionContextCallback.class);
        } else
        {
            addCallbackClass(callbackClass);
        }
        this.action = action;
    }

    public OpenTab(final ActionMenuKind action)
    {
        this(action, null);
    }

    //
    // AbstractDefaultTestCommand
    //

    public final void execute()
    {
        GWTTestUtil.selectTopActionMenu(action);
    }
}
