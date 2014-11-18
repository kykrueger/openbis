/*
 * Copyright 2014 ETH Zuerich, CISD
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

package ch.systemsx.cisd.etlserver.plugins.grouping;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;

/**
 * @author Jakub Straszewski
 */

public enum Grouping implements IGroupKeyProvider
{
    Space
    {
        @Override
        public String getGroupKey(AbstractExternalData dataset)
        {
            return dataset.getExperiment().getProject().getSpace().getCode();
        }
    },
    Project
    {
        @Override
        public String getGroupKey(AbstractExternalData dataset)
        {
            return dataset.getExperiment().getProject().getIdentifier();
        }
    },
    Experiment
    {
        @Override
        public String getGroupKey(AbstractExternalData dataset)
        {
            return dataset.getExperiment().getIdentifier();
        }

    },
    Sample
    {
        @Override
        public String getGroupKey(AbstractExternalData dataset)
        {
            Sample sample = dataset.getSample();
            return sample != null ? sample.getIdentifier() : "no_sample";
        }
    },
    DataSetType
    {
        @Override
        public String getGroupKey(AbstractExternalData dataset)
        {
            return dataset.getDataSetType().getCode();
        }
    },
    ExperimentAndDataSetType
    {
        @Override
        public String getGroupKey(AbstractExternalData dataset)
        {
            return dataset.getExperiment().getIdentifier() + "#" + dataset.getDataSetType().getCode();
        }
    }

}
