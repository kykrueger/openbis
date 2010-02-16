/*
 * Copyright 2009 ETH Zuerich, CISD
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

/**
 * Property type specification of a time series data set type.
 * 
 * @author Izabela Adamczyk
 */
enum TimeSeriesPropertyType
{
    TECHNICAL_REPLICATE_CODE(DataHeaderProperty.TechnicalReplicateCode),

    TIME_SERIES_DATA_SET_TYPE(DataHeaderProperty.DataSetType),

    UPLOADER_EMAIL;

    private final DataHeaderProperty headerPropertyOrNull;

    private TimeSeriesPropertyType()
    {
        this(null);
    }

    private TimeSeriesPropertyType(DataHeaderProperty headerPropertyOrNull)
    {
        this.headerPropertyOrNull = headerPropertyOrNull;
    }

    public DataHeaderProperty getHeaderProperty()
    {
        if (headerPropertyOrNull == null)
        {
            throw new UnsupportedOperationException(name() + " does not have header property.");
        }
        return headerPropertyOrNull;
    }
}
