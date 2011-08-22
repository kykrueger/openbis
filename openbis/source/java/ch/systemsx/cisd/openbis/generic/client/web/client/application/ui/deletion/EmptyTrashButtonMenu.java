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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.deletion;

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.MenuEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.button.SplitButton;
import com.extjs.gxt.ui.client.widget.menu.CheckMenuItem;
import com.extjs.gxt.ui.client.widget.menu.Menu;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.GWTUtils;

/**
 * @author Pawel Glyzewski
 */
public class EmptyTrashButtonMenu extends SplitButton
{
    private final IViewContext<ICommonClientServiceAsync> viewContext;

    private final CheckMenuItem emptyTrash;

    private final CheckMenuItem forceEmptyTrash;

    public EmptyTrashButtonMenu(final IViewContext<ICommonClientServiceAsync> viewContext,
            final AbstractAsyncCallback<Void> callback)
    {
        super(viewContext.getMessage(Dict.BUTTON_EMPTY_TRASH));
        this.viewContext = viewContext;

        final Menu exportMenu = new Menu();
        emptyTrash = new CheckMenuItem(viewContext.getMessage(Dict.BUTTON_EMPTY_TRASH));
        forceEmptyTrash = new CheckMenuItem(viewContext.getMessage(Dict.BUTTON_FORCE_EMPTY_TRASH));

        emptyTrash.setToolTip(viewContext.getMessage(Dict.TOOLTIP_EMPTY_TRASH));
        forceEmptyTrash.setToolTip(viewContext.getMessage(Dict.TOOLTIP_FORCE_EMPTY_TRASH));

        emptyTrash.setGroup("deletionType");
        forceEmptyTrash.setGroup("deletionType");

        exportMenu.add(emptyTrash);
        exportMenu.add(forceEmptyTrash);

        setMenu(exportMenu);

        addSelectionListener(new SelectionListener<ButtonEvent>()
            {
                @Override
                public void componentSelected(ButtonEvent be)
                {
                    invokeAction(callback);
                }
            });

        SelectionListener<MenuEvent> menuEventListener = new SelectionListener<MenuEvent>()
            {
                @Override
                public void componentSelected(MenuEvent ce)
                {
                    boolean isExportAllColumns = isForceEmptyTrash();
                    invokeAction(callback);
                    setText(isExportAllColumns ? viewContext
                            .getMessage(Dict.BUTTON_FORCE_EMPTY_TRASH) : viewContext
                            .getMessage(Dict.BUTTON_EMPTY_TRASH));
                    updateTooltip();
                }

            };
        emptyTrash.addSelectionListener(menuEventListener);
        forceEmptyTrash.addSelectionListener(menuEventListener);

        // select export visible columns by default
        emptyTrash.setChecked(true);
    }

    private void invokeAction(final AbstractAsyncCallback<Void> callback)
    {
        new EmptyTrashConfirmationDialog(viewContext, isForceEmptyTrash(), callback).show();
    }

    private boolean isForceEmptyTrash()
    {
        return forceEmptyTrash.isChecked();
    }

    private void updateTooltip()
    {
        String enabledButtonMessageKey =
                isForceEmptyTrash() ? Dict.TOOLTIP_FORCE_EMPTY_TRASH : Dict.TOOLTIP_EMPTY_TRASH;
        String title = viewContext.getMessage(enabledButtonMessageKey);
        GWTUtils.setToolTip(this, title);
    }

    @Override
    public void enable()
    {
        super.enable();
        updateTooltip();
    }
}
