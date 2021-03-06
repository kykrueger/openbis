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

package ch.systemsx.cisd.yeastx.eicml;

import net.lemnik.eodsql.AutoGeneratedKeys;
import net.lemnik.eodsql.ResultColumn;

import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * A class that represents a chromatogram in an eicML file.
 */
public class ChromatogramDTO
{
    @AutoGeneratedKeys
    private long id;

    @ResultColumn("EIC_MS_RUN_ID")
    private long eicMsRunId;

    @ResultColumn("Q1_MZ")
    private float q1Mz = Float.NaN;

    @ResultColumn("Q3_LOW_MZ")
    private float q3LowMz = Float.NaN;

    @ResultColumn("Q3_HIGH_MZ")
    private float q3HighMz = Float.NaN;

    private String label;

    private char polarity = '?';

    @ResultColumn("RUN_TIMES")
    private float[] runTimes;

    private float[] intensities;

    public void setEicMsRunId(long msRunId)
    {
        this.eicMsRunId = msRunId;
    }

    public long getEicMsRunId()
    {
        return eicMsRunId;
    }

    public void setId(long id)
    {
        this.id = id;
    }

    public long getId()
    {
        return id;
    }

    public void setQ1Mz(float q1Mz)
    {
        this.q1Mz = q1Mz;
    }

    public float getQ1Mz()
    {
        return q1Mz;
    }

    public void setQ3LowMz(float q3LowMz)
    {
        this.q3LowMz = q3LowMz;
    }

    public float getQ3LowMz()
    {
        return q3LowMz;
    }

    public void setQ3HighMz(float q3HighMz)
    {
        this.q3HighMz = q3HighMz;
    }

    public float getQ3HighMz()
    {
        return q3HighMz;
    }

    public void setLabel(String label)
    {
        this.label = label;
    }

    public String getLabel()
    {
        return label;
    }

    public void setPolarity(char polarity)
    {
        this.polarity = polarity;
    }

    public char getPolarity()
    {
        return polarity;
    }

    public void setRunTimes(float[] runTimes)
    {
        this.runTimes = runTimes;
    }

    public float[] getRunTimes()
    {
        return runTimes;
    }

    public void setIntensities(float[] intensities)
    {
        this.intensities = intensities;
    }

    public float[] getIntensities()
    {
        return intensities;
    }

    //
    // Object
    //

    @Override
    public String toString()
    {
        return ToStringBuilder.reflectionToString(this);
    }

}