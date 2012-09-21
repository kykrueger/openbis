/*
 * Copyright 2012 ETH Zuerich, CISD
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

import static ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ScreeningConstants.ANALYSIS_PROCEDURE;

import java.io.File;

import ch.systemsx.cisd.etlserver.registrator.DataSetRegistrationDetails;
import ch.systemsx.cisd.etlserver.registrator.api.v1.IDataSet;
import ch.systemsx.cisd.etlserver.registrator.api.v1.impl.DataSet;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.v2.IFeatureVectorDataSet;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v1.IExperimentImmutable;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v1.ISampleImmutable;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewProperty;

/**
 * @author jakubs
 */
public class FeatureVectorContainerDataSet extends DataSet<DataSetInformation> implements
        IFeatureVectorDataSet
{

    /**
     * @param registrationDetails
     * @param dataSetFolder
     * @param service
     */
    public FeatureVectorContainerDataSet(
            DataSetRegistrationDetails<? extends DataSetInformation> registrationDetails,
            File dataSetFolder, IEncapsulatedOpenBISService service)
    {
        super(registrationDetails, dataSetFolder, service);
    }

    private FeatureVectorDataSet originalDataset;

    public IDataSet getOriginalDataset()
    {
        return originalDataset;
    }

    public void setOriginalDataSet(FeatureVectorDataSet mainDataSet)
    {
        this.originalDataset = mainDataSet;
    }

    @Override
    public void setAnalysisProcedure(String analysisProcedure)
    {
        getRegistrationDetails().getDataSetInformation().getDataSetProperties()
                .add(new NewProperty(ANALYSIS_PROCEDURE, analysisProcedure));
    }

    @Override
    public void setSample(ISampleImmutable sampleOrNull)
    {
        super.setSample(sampleOrNull);
        if (originalDataset != null)
        {
            originalDataset.setSample(sampleOrNull);
            // calling this line assures, that the sample in the contained dataset will not be kept.
            originalDataset.getRegistrationDetails().getDataSetInformation().setLinkSample(false);
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
    }


    @Override
    public void setPropertyValue(String propertyCode, String propertyValue)
    {
        getRegistrationDetails().setPropertyValue(propertyCode, propertyValue);
    }

    @Override
    public File getDataSetStagingFolder()
    {
        return originalDataset.getDataSetStagingFolder();
    }
}
