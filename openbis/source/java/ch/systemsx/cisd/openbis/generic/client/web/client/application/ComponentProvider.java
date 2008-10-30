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

import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.sample_browser.SampleBrowser;

/**
 * Creates and provides GUI modules/components (such as sample browser).
 * 
 * @author Izabela Adamczyk
 */
class ComponentProvider
{
    private final SampleBrowser sampleBrowser;

    private final DummyComponent dummyComponent;

    private final GroupsView groupsView;

    private final RolesView rolesView;

    private final PersonsView personsView;

    public SampleBrowser getSampleBrowser()
    {
        return sampleBrowser;
    }

    public DummyComponent getDummyComponent()
    {
        return dummyComponent;
    }

    public ComponentProvider(final GenericViewContext viewContext)
    {
        sampleBrowser = new SampleBrowser(viewContext);
        dummyComponent = new DummyComponent();
        groupsView = new GroupsView(viewContext);
        rolesView = new RolesView(viewContext);
        personsView = new PersonsView(viewContext);
    }

    public GroupsView getGroupsView()
    {
        return groupsView;
    }

    public RolesView getRolesView()
    {
        return rolesView;
    }

    public PersonsView getPersonsView()
    {
        return personsView;
    }
}