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

package ch.systemsx.cisd.openbis.generic.shared.basic.dto.api;

import java.util.List;

import ch.systemsx.cisd.openbis.generic.shared.basic.ISerializable;

/**
 * The interface exposed to the Managed Property script.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public interface IManagedUiDescription extends ISerializable
{
    /**
     * Sets the given table model to define an output that will be shown in detail view of the
     * entity owning the property. The table will be shown in an extra tab.
     */
    void useTableOutput(ITableModel tableModel);

    IManagedUiAction addAction(String name);

    IManagedUiTableAction addTableAction(String name);

    List<IManagedUiAction> getActions();

    /**
     * Returns description of the widget that will be shown in detail view of the entity owning the
     * property.
     */
    IManagedOutputWidgetDescription getOutputWidgetDescription();

}
