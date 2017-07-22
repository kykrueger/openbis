/*
 * Copyright 2017 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.proteomics.systemtests.authorization.validator.sample;

import ch.systemsx.cisd.openbis.datastoreserver.systemtests.authorization.common.SampleUtil;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.dto.IAuthSessionProvider;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;
import ch.systemsx.cisd.openbis.proteomics.systemtests.authorization.validator.CommonSampleValidatorProteomicsSystemTest;

/**
 * @author pkupczyk
 */
public class ParentSampleValidatorSystemTest extends CommonSampleValidatorProteomicsSystemTest<Sample>
{

    @Override
    protected SampleKind getSharedSampleKind()
    {
        return SampleKind.SHARED_READ;
    }

    @Override
    protected Sample createObject(SpacePE spacePE, ProjectPE projectPE, Object param)
    {
        Sample parent = SampleUtil.createObject(this, spacePE, projectPE, param);
        Sample sample = new Sample();
        sample.addParent(parent);
        return sample;
    }

    @Override
    protected Sample validateObject(IAuthSessionProvider sessionProvider, Sample object, Object param)
    {
        return getBean(SampleValidatorProteomicsTestService.class).testParentSampleValidator(sessionProvider, object);
    }

}
