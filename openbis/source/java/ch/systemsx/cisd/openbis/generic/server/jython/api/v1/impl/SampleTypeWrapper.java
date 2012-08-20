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

package ch.systemsx.cisd.openbis.generic.server.jython.api.v1.impl;

import ch.systemsx.cisd.openbis.generic.server.jython.api.v1.ISampleType;

/**
 * Wrapper of {@link SampleTypeImmutable} as {@link ISampleType} where setters do nothing.
 * 
 * @author Franz-Josef Elmer
 */
class SampleTypeWrapper extends SampleTypeImmutable implements ISampleType
{
    SampleTypeWrapper(SampleTypeImmutable sampleType)
    {
        super(sampleType.getSampleType());
    }

    @Override
    public void setListable(boolean isListable)
    {
    }

    @Override
    public void setShowContainer(boolean isShowContainer)
    {
    }

    @Override
    public void setShowParents(boolean showParents)
    {
    }

    @Override
    public void setSubcodeUnique(boolean isSubcodeUnique)
    {
    }

    @Override
    public void setAutoGeneratedCode(boolean isAutoGeneratedCode)
    {
    }

    @Override
    public void setGeneratedCodePrefix(String generatedCodePrefix)
    {
    }

    @Override
    public void setShowParentMetadata(boolean isShowParentMetadata)
    {
    }

    @Override
    public void setDescription(String description)
    {
    }

}
