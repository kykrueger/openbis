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

package ch.systemsx.cisd.openbis.uitest.rmi;

import java.util.Collection;

import ch.systemsx.cisd.openbis.uitest.dsl.Executor;
import ch.systemsx.cisd.openbis.uitest.request.CreateDataSet;
import ch.systemsx.cisd.openbis.uitest.type.DataSet;
import ch.systemsx.cisd.openbis.uitest.type.DataSetType;
import ch.systemsx.cisd.openbis.uitest.type.Experiment;
import ch.systemsx.cisd.openbis.uitest.type.MetaProject;
import ch.systemsx.cisd.openbis.uitest.type.Sample;

/**
 * @author anttil
 */
public class CreateDataSetRmi extends Executor<CreateDataSet, DataSet>
{
    @Override
    public DataSet run(final CreateDataSet request)
    {
        DataSet dataSet = request.getDataSet();
        DataSetCreator creator = new DataSetCreator("data set content");
        final String code =
                dss.putDataSet(session, creator.getMetadata(dataSet), creator.getData());

        return new DataSet()
            {

                @Override
                public String getCode()
                {
                    return code;
                }

                @Override
                public DataSetType getType()
                {
                    return request.getDataSet().getType();
                }

                @Override
                public Sample getSample()
                {
                    return request.getDataSet().getSample();
                }

                @Override
                public Experiment getExperiment()
                {
                    return request.getDataSet().getExperiment();
                }

                @Override
                public Collection<MetaProject> getMetaProjects()
                {
                    return request.getDataSet().getMetaProjects();
                }

                @Override
                public String toString()
                {
                    return "DataSet " + getCode();
                }
            };
    }
}
