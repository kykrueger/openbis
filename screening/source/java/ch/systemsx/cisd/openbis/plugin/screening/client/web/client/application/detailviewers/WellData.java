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

package ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers;

import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.DatasetImagesReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.PlateImages;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellLocation;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellMetadata;

/**
 * Stores information about images and metadata of one well.
 * 
 * @author Tomasz Pylak
 */
class WellData
{
    private WellMetadata metadataOrNull;

    private WellImages imagesOrNull;

    private String experimentDisplayIdentifier;

    private TechId experimentId;

    /** metadata have to be set separately with {@link #setMetadata} */
    public static WellData create(PlateImages plateImages, WellLocation location)
    {
        Experiment experiment = plateImages.getPlate().getExperiment();
        WellImages wellImages = tryCreateWellImages(plateImages, location);
        return new WellData(wellImages, experiment.getId(), experiment.getIdentifier());
    }

    private static WellImages tryCreateWellImages(PlateImages plateImages, WellLocation location)
    {
        DatasetImagesReference images = plateImages.tryGetImages();
        if (images != null)
        {
            return new WellImages(images, location);
        } else
        {
            return null;
        }
    }

    private WellData(WellImages imagesOrNull, long experimentId, String experimentDisplayIdentifier)
    {
        this.imagesOrNull = imagesOrNull;
        this.experimentId = new TechId(experimentId);
        this.experimentDisplayIdentifier = experimentDisplayIdentifier;
    }

    public void setMetadata(WellMetadata well)
    {
        this.metadataOrNull = well;
    }

    public WellMetadata tryGetMetadata()
    {
        return metadataOrNull;
    }

    public WellImages tryGetImages()
    {
        return imagesOrNull;
    }

    public TechId getExperimentId()
    {
        return experimentId;
    }

    public String getExperimentDisplayIdentifier()
    {
        return experimentDisplayIdentifier;
    }

    public String getWellDescription()
    {
        if (metadataOrNull != null)
        {
            return metadataOrNull.getWellSample().getSubCode();
        } else
        {
            return "?";
        }
    }
}