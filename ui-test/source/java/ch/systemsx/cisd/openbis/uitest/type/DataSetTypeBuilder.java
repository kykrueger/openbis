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

import ch.systemsx.cisd.openbis.uitest.infra.application.ApplicationRunner;

/**
 * @author anttil
 */
@SuppressWarnings("hiding")
public class DataSetTypeBuilder implements Builder<DataSetType>
{

    private ApplicationRunner openbis;

    private String code;

    private String description;

    public DataSetTypeBuilder(ApplicationRunner openbis)
    {
        this.openbis = openbis;
        this.code = openbis.uid();
        this.description = "";
    }

    public DataSetTypeBuilder withCode(String code)
    {
        this.code = code;
        return this;
    }

    @Override
    public DataSetType create()
    {
        return openbis.create(build());
    }

    @Override
    public DataSetType build()
    {
        return new DataSetType(code, description);
    }
}
