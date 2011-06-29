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

package ch.systemsx.cisd.etlserver.registrator.api.v1.impl;

import java.util.ArrayList;
import java.util.List;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.IExperimentImmutable;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.ISampleImmutable;
import ch.systemsx.cisd.openbis.generic.shared.util.EntityHelper;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public class SampleImmutable implements ISampleImmutable
{
    private final ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample sample;

    private final boolean existingSample;

    public SampleImmutable(ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample sample)
    {
        this(sample, true);
    }

    public SampleImmutable(ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample sample,
            boolean existingSample)
    {
        this.sample = sample;
        this.existingSample = existingSample;
    }

    public IExperimentImmutable getExperiment()
    {
        ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment experiment =
                sample.getExperiment();
        return (null != experiment) ? new ExperimentImmutable(experiment) : null;
    }

    public String getSampleIdentifier()
    {
        return sample.getIdentifier();
    }

    public ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample getSample()
    {
        return sample;
    }

    public boolean isExistingSample()
    {
        return existingSample;
    }

    /**
     * Throw an exception if the sample does not exist
     */
    protected void checkExists()
    {
        if (false == isExistingSample())
        {
            throw new UserFailureException("Sample does not exist.");
        }
    }

    public String getPropertyValue(String propertyCode)
    {
        return EntityHelper.tryFindPropertyValue(sample, propertyCode);
    }

    public String getSampleType()
    {
        if (sample.getSampleType() != null)
        {
            return sample.getSampleType().getCode();
        }
        return null;
    }

    public String getSpace()
    {
        return sample.getSpace().getCode();
    }

    public String getCode()
    {
        return sample.getCode();
    }

    public List<ISampleImmutable> getContainedSamples()
    {
        List<ISampleImmutable> result = new ArrayList<ISampleImmutable>();
        List<ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample> containedSamples =
                sample.tryGetContainedSamples();
        if (containedSamples != null)
        {
            for (ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample contained : containedSamples)
            {
                result.add(new SampleImmutable(contained));
            }
        }
        return result;
    }

}
