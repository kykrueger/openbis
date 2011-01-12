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

package ch.systemsx.cisd.openbis.generic.shared.basic.dto;

import java.util.List;

import ch.systemsx.cisd.openbis.generic.shared.basic.ISerializable;

/**
 * Object that declaratively describes a UI (labels, fields, their ordering, table content).
 * 
 * @author Piotr Buczek
 */
public class ManagedUiDescription implements ISerializable
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    private List<IManagedWidgetDescription> widgetDescriptions;

    public ManagedUiDescription()
    {
    }

    public List<IManagedWidgetDescription> getWidgetDescriptions()
    {
        return widgetDescriptions;
    }

    public void setWidgetDescriptions(List<IManagedWidgetDescription> widgetDescriptions)
    {
        this.widgetDescriptions = widgetDescriptions;
    }

    public void addWidgetDescription(IManagedWidgetDescription widgetDescription)
    {
        widgetDescriptions.add(widgetDescription);
    }
}
