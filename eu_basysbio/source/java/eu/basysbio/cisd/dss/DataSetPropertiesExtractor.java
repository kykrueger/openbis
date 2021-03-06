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

package eu.basysbio.cisd.dss;

import java.io.File;
import java.util.List;
import java.util.Properties;

import ch.systemsx.cisd.common.properties.PropertyUtils;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewProperty;

/**
 * @author Franz-Josef Elmer
 */
class DataSetPropertiesExtractor implements IDataSetPropertiesExtractor
{
    protected final boolean ignoreEmptyLines;

    private final boolean allowMultipleValues;

    DataSetPropertiesExtractor(Properties properties, boolean allowMultipleValues)
    {
        this.allowMultipleValues = allowMultipleValues;
        ignoreEmptyLines =
                PropertyUtils.getBoolean(properties,
                        TimeSeriesDataSetUploaderParameters.IGNORE_EMPTY_LINES_KEY, true);
    }

    @Override
    public List<NewProperty> extractDataSetProperties(File incomingDataSetPath)
    {
        return HeaderUtils.extractHeaderProps(incomingDataSetPath, ignoreEmptyLines, false,
                allowMultipleValues);
    }

}
