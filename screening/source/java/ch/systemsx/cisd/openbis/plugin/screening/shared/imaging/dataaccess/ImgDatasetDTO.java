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

package ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess;

import net.lemnik.eodsql.ResultColumn;

/**
 * @author Tomasz Pylak
 */
public class ImgDatasetDTO extends AbstractImageTransformerFactoryHolder
{
    @ResultColumn("PERM_ID")
    private String permId;

    @ResultColumn("FIELDS_WIDTH")
    private Integer fieldNumberOfColumnsOrNull;

    @ResultColumn("FIELDS_HEIGHT")
    private Integer fieldNumberOfRowsOrNull;

    @ResultColumn("CONT_ID")
    private Long containerId;

    // a redundant information if there are timepoint or depth stack data for any spots in this
    // dataset
    @ResultColumn("IS_MULTIDIMENSIONAL")
    private boolean isMultidimensional;

    @SuppressWarnings("unused")
    private ImgDatasetDTO()
    {
        // All Data-Object classes must have a default constructor.
    }

    public ImgDatasetDTO(String permId, Integer fieldNumberOfRowsOrNull,
            Integer fieldNumberOfColumnsOrNull, Long containerId, boolean isMultidimensional)
    {
        this.permId = permId;
        this.fieldNumberOfColumnsOrNull = fieldNumberOfColumnsOrNull;
        this.fieldNumberOfRowsOrNull = fieldNumberOfRowsOrNull;
        this.containerId = containerId;
        this.isMultidimensional = isMultidimensional;
    }

    public String getPermId()
    {
        return permId;
    }

    public void setPermId(String permId)
    {
        this.permId = permId;
    }

    public Integer getFieldNumberOfColumns()
    {
        return fieldNumberOfColumnsOrNull;
    }

    public void setFieldNumberOfColumns(Integer numberOfColumns)
    {
        this.fieldNumberOfColumnsOrNull = numberOfColumns;
    }

    public Integer getFieldNumberOfRows()
    {
        return fieldNumberOfRowsOrNull;
    }

    public void setFieldNumberOfRows(Integer numberOfRows)
    {
        this.fieldNumberOfRowsOrNull = numberOfRows;
    }

    /** can be null */
    public Long getContainerId()
    {
        return containerId;
    }

    public void setContainerId(Long containerId)
    {
        this.containerId = containerId;
    }

    public boolean getIsMultidimensional()
    {
        return isMultidimensional;
    }

    public void setMultidimensional(boolean isMultidimensional)
    {
        this.isMultidimensional = isMultidimensional;
    }
}
