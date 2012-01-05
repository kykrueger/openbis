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

package ch.systemsx.cisd.openbis.generic.shared.basic.dto.builders;

import java.util.ArrayList;
import java.util.List;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ContainerDataSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;

/**
 * Builder class for creating an instance of {@link DataSet} or {@link ContainerDataSet}.
 * 
 * @author Franz-Josef Elmer
 */
public class ContainerDataSetBuilder extends AbstractDataSetBuilder<ContainerDataSetBuilder>
{
    public ContainerDataSetBuilder()
    {
        super(new ContainerDataSet());
    }

    public ContainerDataSetBuilder(long id)
    {
        this();
        dataSet.setId(id);
    }

    public final ContainerDataSet getContainerDataSet()
    {
        return dataSet.tryGetAsContainerDataSet();
    }

    public ContainerDataSetBuilder contains(DataSet contained)
    {
        List<ExternalData> containedDataSets =
                dataSet.tryGetAsContainerDataSet().getContainedDataSets();
        if (containedDataSets == null)
        {
            containedDataSets = new ArrayList<ExternalData>();
            dataSet.tryGetAsContainerDataSet().setContainedDataSets(containedDataSets);
        }
        containedDataSets.add(contained);
        return asConcreteSubclass();
    }

    @Override
    protected ContainerDataSetBuilder asConcreteSubclass()
    {
        return this;
    }
}
