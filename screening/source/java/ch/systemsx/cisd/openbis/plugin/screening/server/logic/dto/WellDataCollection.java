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

package ch.systemsx.cisd.openbis.plugin.screening.server.logic.dto;

import java.util.List;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.CodeAndLabel;

/**
 * List of well data and description of each feature.
 * 
 * @author Tomasz Pylak
 */
public class WellDataCollection
{
    private final List<IWellExtendedData> wellDataList;

    // has the same length as feature vectors in all wells
    private final List<CodeAndLabel> featureDescriptions;

    public WellDataCollection(List<IWellExtendedData> wellDataList,
            List<CodeAndLabel> featureDescriptions)
    {
        this.wellDataList = wellDataList;
        this.featureDescriptions = featureDescriptions;
    }

    public List<IWellExtendedData> getWellDataList()
    {
        return wellDataList;
    }

    public List<CodeAndLabel> getFeatureDescriptions()
    {
        return featureDescriptions;
    }
}