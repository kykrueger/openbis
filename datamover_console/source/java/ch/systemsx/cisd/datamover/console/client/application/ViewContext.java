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

package ch.systemsx.cisd.datamover.console.client.application;

import ch.systemsx.cisd.datamover.console.client.IDatamoverConsoleServiceAsync;

/**
 * @author Franz-Josef Elmer
 */
public class ViewContext
{
    private final IDatamoverConsoleServiceAsync service;

    private final ViewModel model = new ViewModel();

    private final IPageController pageController;

    private final IMessageResources messageResources;

    private final IImageBundle imageBundle;

    public ViewContext(IDatamoverConsoleServiceAsync service, IPageController pageController,
            IMessageResources messageResources, IImageBundle imageBundle)
    {
        this.service = service;
        this.pageController = pageController;
        this.messageResources = messageResources;
        this.imageBundle = imageBundle;
    }

    public final IDatamoverConsoleServiceAsync getService()
    {
        return service;
    }

    public final IPageController getPageController()
    {
        return pageController;
    }

    public final IMessageResources getMessageResources()
    {
        return messageResources;
    }

    public final IImageBundle getImageBundle()
    {
        return imageBundle;
    }

    public final ViewModel getModel()
    {
        return model;
    }

}
