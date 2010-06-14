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

package ch.systemsx.cisd.openbis.dss.etl.dataaccess;

import java.io.File;

import ch.systemsx.cisd.bds.hcs.Geometry;
import ch.systemsx.cisd.bds.hcs.Location;
import ch.systemsx.cisd.common.io.FileBasedContent;
import ch.systemsx.cisd.common.io.IContent;
import ch.systemsx.cisd.openbis.dss.etl.AbsoluteImageReference;
import ch.systemsx.cisd.openbis.dss.etl.IContentRepository;
import ch.systemsx.cisd.openbis.dss.etl.IHCSDatasetLoader;

/**
 * Helper class for easy handling of HCS image dataset standard structure.
 * 
 * @author Tomasz Pylak
 * @author Piotr Buczek
 */
public class HCSDatasetLoader implements IHCSDatasetLoader
{
    private final IImagingUploadDAO query;

    private final ImgDatasetDTO dataset;

    private ImgContainerDTO container;

    private Integer channelCount;

    private final IContentRepository contentRepository;

    public HCSDatasetLoader(IImagingUploadDAO query, String datasetPermId, IContentRepository contentRepository)
    {
        this.contentRepository = contentRepository;
        this.query = query;
        this.dataset = query.tryGetDatasetByPermId(datasetPermId);
        if (dataset == null)
        {
            throw new IllegalStateException(String.format("Dataset '%s' not found", datasetPermId));
        }
    }

    /** has to be called at the end */
    public void close()
    {
        query.close();
    }

    private ImgContainerDTO getContainer()
    {
        if (container == null)
        {
            container = query.getContainerById(dataset.getContainerId());
        }
        return container;
    }

    public Geometry getPlateGeometry()
    {
        return new Geometry(getContainer().getNumberOfRows(), getContainer().getNumberOfColumns());
    }

    private ImgDatasetDTO getDataset()
    {
        return dataset;
    }

    public Geometry getWellGeometry()
    {
        return new Geometry(getDataset().getFieldNumberOfRows(), getDataset()
                .getFieldNumberOfColumns());
    }

    public int getChannelCount()
    {
        if (channelCount == null)
        {
            channelCount =
                    query.countChannelByDatasetIdOrExperimentId(getDataset().getId(),
                            getContainer().getExperimentId());
        }
        return channelCount;
    }

    /**
     * @param chosenChannel start from 1
     * @return image (with absolute path, page and color)
     */
    public AbsoluteImageReference tryGetImage(int chosenChannel, Location wellLocation,
            Location tileLocation)
    {
        assert chosenChannel > 0;
        assert tileLocation.getX() <= getDataset().getFieldNumberOfColumns();
        assert tileLocation.getY() <= getDataset().getFieldNumberOfRows();
        assert wellLocation.getX() <= getContainer().getNumberOfColumns();
        assert wellLocation.getY() <= getContainer().getNumberOfRows();

        long[] channelIds =
                query.getChannelIdsByDatasetIdOrExperimentId(getDataset().getId(), getContainer()
                        .getExperimentId());
        assert chosenChannel <= channelIds.length;

        long chosenChannelId = channelIds[chosenChannel - 1];

        ImgImageDTO imageDTO =
                query
                        .tryGetImage(chosenChannelId, getDataset().getId(), tileLocation,
                                wellLocation);
        if (imageDTO != null)
        {
            IContent content = contentRepository.getContent(imageDTO.getFilePath());
            return new AbsoluteImageReference(content, imageDTO.getPage(),
                    imageDTO.getColorComponent());
        } else
        {
            return null;
        }
    }
}