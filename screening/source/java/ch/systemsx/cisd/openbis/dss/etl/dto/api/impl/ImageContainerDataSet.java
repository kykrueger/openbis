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

package ch.systemsx.cisd.openbis.dss.etl.dto.api.impl;

import java.io.File;
import java.util.Collections;
import java.util.List;

import ch.systemsx.cisd.etlserver.registrator.DataSetRegistrationDetails;
import ch.systemsx.cisd.etlserver.registrator.api.v1.IDataSet;
import ch.systemsx.cisd.etlserver.registrator.api.v1.impl.DataSet;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.v1.IImageDataSet;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v1.IExperimentImmutable;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v1.ISampleImmutable;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ScreeningConstants;

/**
 * Represents an image data set for the registration API.
 * 
 * @author Tomasz Pylak
 */
public class ImageContainerDataSet extends DataSet<DataSetInformation> implements IImageDataSet
{
    private DataSet<ImageDataSetInformation> originalDataset;

    private List<IDataSet> thumbnailDatasets = Collections.emptyList();
    
    private boolean establishSampleLinkForContainedDataSets = false;

    public ImageContainerDataSet(
            DataSetRegistrationDetails<? extends DataSetInformation> registrationDetails,
            File dataSetFolder, IEncapsulatedOpenBISService service)
    {
        super(registrationDetails, dataSetFolder, service);
    }

    public void setAnalysisProcedure(String analysisProcedure)
    {
        setPropertyValue(ScreeningConstants.ANALYSIS_PROCEDURE, analysisProcedure);
    }

    public void establishSampleLinkForContainedDataSets()
    {
        establishSampleLinkForContainedDataSets = true;
    }

    public boolean sampleLinkForContainedDataSetsShouldBeEstablished()
    {
        return establishSampleLinkForContainedDataSets;
    }

    public DataSet<ImageDataSetInformation> getOriginalDataset()
    {
        return originalDataset;
    }

    public void setOriginalDataset(DataSet<ImageDataSetInformation> originalDataset)
    {
        this.originalDataset = originalDataset;
    }

    public List<IDataSet> getThumbnailDatasets()
    {
        return thumbnailDatasets;
    }

    public void setThumbnailDatasets(List<IDataSet> thumbnailDatasets)
    {
        this.thumbnailDatasets = thumbnailDatasets;
    }

    @Override
    public void setSample(ISampleImmutable sampleOrNull)
    {
        super.setSample(sampleOrNull);
        if (originalDataset != null)
        {
            originalDataset.setSample(sampleOrNull);
            originalDataset.getRegistrationDetails().getDataSetInformation()
                    .setLinkSample(establishSampleLinkForContainedDataSets);
        }
        for (IDataSet thumbnailDataset : thumbnailDatasets)
        {
            thumbnailDataset.setSample(sampleOrNull);
            if (thumbnailDatasets instanceof DataSet)
            {
                ((DataSet<?>) thumbnailDatasets).getRegistrationDetails().getDataSetInformation()
                        .setLinkSample(establishSampleLinkForContainedDataSets);
            }
        }
    }

    @Override
    public void setExperiment(IExperimentImmutable experimentOrNull)
    {
        super.setExperiment(experimentOrNull);
        if (originalDataset != null)
        {
            originalDataset.setExperiment(experimentOrNull);
        }
        for (IDataSet thumbnailDataset : thumbnailDatasets)
        {
            thumbnailDataset.setExperiment(experimentOrNull);
        }
    }

}
