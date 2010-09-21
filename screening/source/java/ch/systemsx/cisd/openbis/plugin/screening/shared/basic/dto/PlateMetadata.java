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

package ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto;

import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;

/**
 * DTO with metadata of a plate and all its wells.
 * 
 * @author Tomasz Pylak
 */
public class PlateMetadata implements IsSerializable
{
    private Sample plate;

    private List<WellMetadata> wellsMetadata;

    // plate dimension
    private int plateRowsNum, plateColsNum;

    // GWT only
    @SuppressWarnings("unused")
    private PlateMetadata()
    {
    }

    public PlateMetadata(Sample plate, List<WellMetadata> wellsMetadata, int plateRowsNum,
            int plateColsNum)
    {
        this.plate = plate;
        this.wellsMetadata = wellsMetadata;
        this.plateRowsNum = plateRowsNum;
        this.plateColsNum = plateColsNum;
    }

    public List<WellMetadata> getWells()
    {
        return wellsMetadata;
    }

    public Sample getPlate()
    {
        return plate;
    }

    public int getRowsNum()
    {
        return plateRowsNum;
    }

    public int getColsNum()
    {
        return plateColsNum;
    }
}
