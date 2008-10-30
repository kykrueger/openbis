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

package ch.systemsx.cisd.openbis.generic.client.web.client.application;

import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Dispatcher;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Html;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.SimpleComboBox;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.layout.FlowLayout;
import com.extjs.gxt.ui.client.widget.toolbar.AdapterToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.FillToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.SeparatorToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.TextToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.AppEvents;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DummyComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SessionContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.User;

/**
 * Implements functionality of the top menu.
 * 
 * @author Franz-Josef Elmer
 * @author Izabela Adamczyk
 */
public class TopMenu extends LayoutContainer
{
    public static final String LOGOUT_BUTTON_ID = GenericConstants.ID_PREFIX + "logout-button";

    private final DummyComponent dummyComponent;

    private final ToolBar toolBar;

    private final GenericViewContext viewContext;

    public TopMenu(final GenericViewContext viewContext, final DummyComponent dummyComponent)
    {
        this.viewContext = viewContext;
        this.dummyComponent = dummyComponent;
        setLayout(new FlowLayout());
        setBorders(true);
        toolBar = new ToolBar();
        add(toolBar);
    }

    void refresh()
    {
        toolBar.removeAll();
        toolBar.add(new AdapterToolItem(createTitleHeader()));

        toolBar.add(new AdapterToolItem(new DomainChooser()));
        toolBar.add(new AdapterToolItem(createSearchQueryField()));
        toolBar.add(new AdapterToolItem(createSearchButton()));

        toolBar.add(new FillToolItem());
        toolBar.add(new AdapterToolItem(userInfo()));
        toolBar.add(new SeparatorToolItem());
        toolBar.add(new LogoutButton(viewContext));
    }

    @Override
    protected void onRender(final Element parent, final int pos)
    {
        super.onRender(parent, pos);
        refresh();
    }

    private Html userInfo()
    {
        final SessionContext sessionContext = viewContext.getModel().getSessionContext();
        final User user = sessionContext.getUser();
        final String userName = user.getUserName();
        final String homeGroup = user.getHomeGroupCode();
        String fullInfo;
        if (homeGroup == null)
        {
            fullInfo = viewContext.getMessage("header_userWithoutHomegroup", userName);
        } else
        {
            fullInfo = viewContext.getMessage("header_userWithHomegroup", userName, homeGroup);
        }
        final Element div = DOM.createDiv();
        div.setClassName("header-user-info");
        div.setInnerText(fullInfo);
        return new Html(div.getString());
    }

    private Html createTitleHeader()
    {
        final Element titleHeader = DOM.createDiv();
        titleHeader.setClassName("header-title");
        titleHeader.setInnerText("OpenBIS");
        return new Html(titleHeader.getString());
    }

    private Button createSearchButton()
    {
        final Button button = new Button("Search");
        button.addSelectionListener(new SelectionListener<ComponentEvent>()
            {

                @Override
                public void componentSelected(final ComponentEvent ce)
                {
                    final AppEvent<ContentPanel> event =
                            new AppEvent<ContentPanel>(AppEvents.NAVI_EVENT);
                    event.setData(GenericConstants.ASSOCIATED_CONTENT_PANEL, dummyComponent);
                    Dispatcher.get().dispatch(event);
                }
            });
        return button;
    }

    private TextField<String> createSearchQueryField()
    {
        final TextField<String> field = new TextField<String>();
        field.setWidth(200);
        return field;
    }

    class DomainChooser extends SimpleComboBox<String>
    {
        public DomainChooser()
        {
            setWidth(100);
            add("- All -");
            add("Samples");
            add("Experiments");
            add("Materials");
            setEditable(false);
            setValue(getStore().getAt(0));
        }
    }

    //
    // Helper classes
    //

    private final class LogoutButton extends TextToolItem
    {

        LogoutButton(final GenericViewContext viewContext)
        {
            super(viewContext.getMessage("header_logoutButtonLabel"));
            final SelectionListener<ComponentEvent> listener =
                    new SelectionListener<ComponentEvent>()
                        {

                            //
                            // SelectionListener
                            //

                            @Override
                            public final void componentSelected(final ComponentEvent ce)
                            {
                                viewContext.getService().logout(
                                        new AbstractAsyncCallback<Void>(viewContext)
                                            {

                                                //
                                                // AbstractAsyncCallback
                                                //

                                                @Override
                                                public final void process(final Void result)
                                                {
                                                    viewContext.getPageController().reload();
                                                }
                                            });
                            }
                        };
            addSelectionListener(listener);
            setId(LOGOUT_BUTTON_ID);
        }
    }

}
