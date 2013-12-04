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

package ch.systemsx.cisd.openbis.uitest.rmi.eager;

import java.util.Collection;
import java.util.HashSet;

import ch.systemsx.cisd.openbis.generic.shared.ICommonServer;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Metaproject;
import ch.systemsx.cisd.openbis.uitest.type.DataSet;
import ch.systemsx.cisd.openbis.uitest.type.DataSetType;
import ch.systemsx.cisd.openbis.uitest.type.Experiment;
import ch.systemsx.cisd.openbis.uitest.type.MetaProject;
import ch.systemsx.cisd.openbis.uitest.type.Sample;

/**
 * @author anttil
 */
public class DataSetRmi extends DataSet
{

    private final ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSet dataSet;

    @SuppressWarnings("unused")
    private final String session;

    @SuppressWarnings("unused")
    private final ICommonServer commonServer;

    public DataSetRmi(ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSet dataSet,
            String session, ICommonServer commonServer)
    {
        this.dataSet = dataSet;
        this.session = session;
        this.commonServer = commonServer;
    }

    @Override
    public Collection<MetaProject> getMetaProjects()
    {
        Collection<MetaProject> metaProjects = new HashSet<MetaProject>();
        for (Metaproject m : dataSet.getMetaprojects())
        {
            metaProjects.add(new MetaProjectRmi(m));
        }
        return metaProjects;
    }

    @Override
    public String getCode()
    {
        return dataSet.getCode();
    }

    @Override
    public DataSetType getType()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Sample getSample()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Experiment getExperiment()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Collection<DataSet> getParents()
    {
        throw new UnsupportedOperationException();
    }
}
