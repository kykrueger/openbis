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

package ch.systemsx.cisd.yeastx.db.generic;

import net.lemnik.eodsql.AutoGeneratedKeys;
import net.lemnik.eodsql.ResultColumn;

/**
 * The generic information about a data set for the data mart.
 * 
 * @author Bernd Rinn
 */
public class DMDataSetDTO
{
    @AutoGeneratedKeys
    private long id;

    @ResultColumn("PERM_ID")
    private String permId;

    @ResultColumn("EXPE_ID")
    private long experimentId;

    @ResultColumn("SAMP_ID")
    private Long sampleId;

    // not null only if experiment is null
    private DMSampleDTO sampleOrNull;

    // not null only if sample is null
    private DMExperimentDTO experimentOrNull;

    public DMDataSetDTO()
    {
        // Bean compatibility.
    }

    public DMDataSetDTO(String dsPermId, String sampPermIdOrNull, String sampNameOrNull,
            String expePermId, String experimentName)
    {
        this.permId = dsPermId;
        final DMExperimentDTO experiment = new DMExperimentDTO(expePermId);
        experiment.setName(experimentName);
        if (sampPermIdOrNull != null)
        {
            assert sampNameOrNull != null : "sample name must be given when sample permId is given";
            this.sampleOrNull = new DMSampleDTO(sampPermIdOrNull);
            sampleOrNull.setName(sampNameOrNull);
            sampleOrNull.setExperiment(experiment);
        } else
        {
            this.experimentOrNull = experiment;
        }
    }

    public long getId()
    {
        return id;
    }

    public void setId(long id)
    {
        this.id = id;
    }

    public String getPermId()
    {
        return permId;
    }

    public void setPermId(String permId)
    {
        this.permId = permId;
    }

    public long getExperimentId()
    {
        return experimentId;
    }

    public void setExperimentId(long experimentId)
    {
        this.experimentId = experimentId;
    }

    public Long getSampleId()
    {
        return sampleId;
    }

    public void setSampleId(Long sampleId)
    {
        this.sampleId = sampleId;
    }

    public DMSampleDTO getSample()
    {
        return sampleOrNull;
    }

    public void setSample(DMSampleDTO sample)
    {
        this.sampleOrNull = sample;
        this.sampleId = sample.getId();
        this.experimentId = sample.getExperimentId();
    }

    public DMExperimentDTO getExperiment()
    {
        if (sampleOrNull != null)
        {
            return sampleOrNull.getExperiment();
        } else
        {
            return experimentOrNull;
        }
    }

}
