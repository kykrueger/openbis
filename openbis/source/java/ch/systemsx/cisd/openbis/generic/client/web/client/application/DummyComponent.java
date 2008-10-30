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

import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.layout.CenterLayout;

/**
 * Component of the gui displayed when some functionality has not been implemented yet.
 * 
 * @author Izabela Adamczyk
 */
class DummyComponent extends ContentPanel
{
    private static final String PREFIX = "dummy-component";

    public static final String ID = GenericConstants.ID_PREFIX + PREFIX;

    public DummyComponent()
    {
        setId(ID);
        setHeading("Not implemented feature");
        setHeaderVisible(false);
        setLayout(new CenterLayout());
        addText("<div class='dummy-component'>This feature will be implemented later...</div>");
        setBodyBorder(false);
    }
}