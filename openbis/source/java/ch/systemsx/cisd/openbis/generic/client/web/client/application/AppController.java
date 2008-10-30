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

import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Controller;

/**
 * Main application controller.
 * 
 * @author Izabela Adamczyk
 */
public class AppController extends Controller
{
    private AppView appView;

    private final GenericViewContext viewContext;

    public AppController(final GenericViewContext viewContext)
    {
        this.viewContext = viewContext;
        registerEventTypes(AppEvents.INIT);
        registerEventTypes(AppEvents.NAVI_EVENT);
    }

    @Override
    public void handleEvent(final AppEvent<?> event)
    {
        switch (event.type)
        {
            case AppEvents.INIT:
                onInit(event);
                break;
            case AppEvents.NAVI_EVENT:
                onLeftMenuSelectionChanged(event);
                break;
            default:
                throw new IllegalArgumentException("Unknow event '" + event + "'.");
        }
    }

    private void onLeftMenuSelectionChanged(final AppEvent<?> event)
    {
        forwardToView(appView, event);
    }

    @Override
    public void initialize()
    {
        appView = new AppView(this, viewContext);
    }

    private void onInit(final AppEvent<?> event)
    {
        forwardToView(appView, event);
    }

}
