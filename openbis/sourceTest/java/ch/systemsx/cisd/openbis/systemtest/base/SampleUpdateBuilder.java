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

package ch.systemsx.cisd.openbis.systemtest.base;

import static ch.systemsx.cisd.openbis.systemtest.base.BaseTest.id;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ch.systemsx.cisd.openbis.generic.server.ICommonServerForInternalUse;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewAttachment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Space;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.DatabaseInstanceIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SpaceIdentifier;
import ch.systemsx.cisd.openbis.plugin.generic.shared.IGenericServer;

public class SampleUpdateBuilder extends UpdateBuilder<SampleUpdatesDTO>
{
    private TechId sampleId;

    private String sampleCode;

    private ExperimentIdentifier experimentId;

    private Date version;

    private SampleIdentifier sampleIdentifier;

    private List<Sample> parents;

    private Sample container;

    public SampleUpdateBuilder(ICommonServerForInternalUse commonServer,
            IGenericServer genericServer, Sample sample)
    {
        super(commonServer, genericServer);
        sample = refresh(sample);
        this.sampleId = new TechId(sample.getId());
        this.sampleCode = sample.getCode();
        if (sample.getExperiment() != null)
        {
            this.experimentId = new ExperimentIdentifier(sample.getExperiment());
        }
        this.version = sample.getModificationDate();
        this.sampleIdentifier = id(sample);
        this.parents = new ArrayList<Sample>();
        this.container = null;
    }

    public SampleUpdateBuilder toExperiment(Experiment experiment)
    {
        inSpace(experiment.getProject().getSpace());
        this.experimentId = new ExperimentIdentifier(experiment);
        return this;
    }

    public SampleUpdateBuilder removingExperiment()
    {
        this.experimentId = null;
        return this;
    }

    public SampleUpdateBuilder withoutSpace()
    {
        this.experimentId = null;
        this.sampleIdentifier =
                new SampleIdentifier(new DatabaseInstanceIdentifier("CISD"), this.sampleCode);
        return this;
    }

    public SampleUpdateBuilder inSpace(Space space)
    {
        this.experimentId = null;
        this.sampleIdentifier =
                new SampleIdentifier(new SpaceIdentifier(new DatabaseInstanceIdentifier("CISD"),
                        space.getCode()), this.sampleCode);
        return this;
    }

    public SampleUpdateBuilder withParent(Sample sample)
    {
        return this.withParents(sample);
    }

    public SampleUpdateBuilder withParents(Sample... samples)
    {
        for (Sample parent : samples)
        {
            this.parents.add(parent);
        }
        return this;
    }

    public SampleUpdateBuilder withContainer(Sample sample)
    {
        this.container = sample;
        return this;
    }

    /*
     *     public SampleUpdatesDTO(TechId sampleId, List<IEntityProperty> properties,
            ExperimentIdentifier experimentIdentifierOrNull, Collection<NewAttachment> attachments,
            Date version, SampleIdentifier sampleIdentifier, String containerIdentifierOrNull,
            String[] modifiedParentCodesOrNull)
     */

    @Override
    public SampleUpdatesDTO create()
    {
        String[] parentCodes = new String[this.parents.size()];
        for (int i = 0; i < this.parents.size(); i++)
        {
            parentCodes[i] = this.parents.get(i).getCode();
        }
        return new SampleUpdatesDTO(this.sampleId, new ArrayList<IEntityProperty>(),
                this.experimentId, new ArrayList<NewAttachment>(), this.version,
                this.sampleIdentifier, this.container != null ? this.container.getIdentifier()
                        : null, parentCodes);
    }

    @Override
    public void perform()
    {
        commonServer.updateSample(this.sessionToken, this.create());
    }

}