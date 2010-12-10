/*
 * Copyright 2010 ETH Zuerich, CISD
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

import java.util.HashSet;
import java.util.Set;

import ch.systemsx.cisd.openbis.generic.shared.basic.ISerializable;

/**
 * Entity detail view configuration.
 * 
 * @author Izabela Adamczyk
 */
public class DetailViewConfiguration implements ISerializable
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    private Set<String> disabledTabs = new HashSet<String>();

    private Set<String> dataSetTypesWithImageOverview = new HashSet<String>();

    private boolean hideSmartView = false;

    private boolean hideFileView = false;

    public DetailViewConfiguration()
    {
    }

    public Set<String> getDisabledTabs()
    {
        return disabledTabs;
    }

    public void setDisabledTabs(Set<String> disabledTabs)
    {
        this.disabledTabs = disabledTabs;
    }

    public Set<String> getDataSetTypesWithImageOverview()
    {
        return dataSetTypesWithImageOverview;
    }

    public void setDataSetTypesWithImageOverview(Set<String> dataSetTypesWithImageOverview)
    {
        this.dataSetTypesWithImageOverview = dataSetTypesWithImageOverview;
    }

    public void setHideSmartView(boolean hideSmartView)
    {
        this.hideSmartView = hideSmartView;
    }

    public boolean isHideSmartView()
    {
        return hideSmartView;
    }

    public void setHideFileView(boolean hideFileView)
    {
        this.hideFileView = hideFileView;
    }

    public boolean isHideFileView()
    {
        return hideFileView;
    }

}
