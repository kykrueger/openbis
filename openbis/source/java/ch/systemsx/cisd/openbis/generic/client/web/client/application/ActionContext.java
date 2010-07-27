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

package ch.systemsx.cisd.openbis.generic.client.web.client.application;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;

/**
 * Context of the last action performed by the user.
 * 
 * @author Piotr Buczek
 */
public class ActionContext
{
    private Experiment experimentOrNull;

    private Project projectOrNull;

    private String spaceCodeOrNull;

    private SampleType sampleTypeOrNull;

    public ActionContext()
    {
    }

    public ActionContext(Experiment experiment)
    {
        this.experimentOrNull = experiment;
        this.projectOrNull = experiment.getProject();
        // this.spaceOrNull = projectOrNull.getSpace();
        this.spaceCodeOrNull = projectOrNull.getSpace().getCode();
    }

    public Experiment tryGetExperiment()
    {
        return experimentOrNull;
    }

    public void setExperiment(Experiment experiment)
    {
        this.experimentOrNull = experiment;
    }

    public Project tryGetProject()
    {
        return projectOrNull;
    }

    public void setProject(Project project)
    {
        this.projectOrNull = project;
    }

    public SampleType tryGetSampleType()
    {
        return sampleTypeOrNull;
    }

    public String tryGetSampleTypeCode()
    {
        return sampleTypeOrNull == null ? null : sampleTypeOrNull.getCode();
    }

    public void setSampleType(SampleType sampleType)
    {
        this.sampleTypeOrNull = sampleType;
    }

    public String tryGetSpaceCode()
    {
        return spaceCodeOrNull;
    }

    public void setSpaceCode(String spaceCode)
    {
        this.spaceCodeOrNull = spaceCode;
    }

}
