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

package ch.systemsx.cisd.openbis.uitest.type;

import ch.systemsx.cisd.openbis.uitest.dsl.Application;
import ch.systemsx.cisd.openbis.uitest.request.CreateExperimentType;
import ch.systemsx.cisd.openbis.uitest.uid.UidGenerator;

/**
 * @author anttil
 */
@SuppressWarnings("hiding")
public class ExperimentTypeBuilder implements Builder<ExperimentType>
{

    private String code;

    private String description;

    public ExperimentTypeBuilder(UidGenerator uid)
    {
        this.code = uid.uid();
        this.description = "";
    }

    public ExperimentTypeBuilder withCode(String code)
    {
        this.code = code;
        return this;
    }

    @Override
    public ExperimentType build(Application openbis)
    {
        return openbis.execute(new CreateExperimentType(new ExperimentType(code, description)));
    }
}
