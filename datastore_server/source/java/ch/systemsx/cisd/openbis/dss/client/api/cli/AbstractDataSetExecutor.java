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

package ch.systemsx.cisd.openbis.dss.client.api.cli;

import ch.systemsx.cisd.openbis.dss.client.api.v1.IDataSetDss;
import ch.systemsx.cisd.openbis.dss.client.api.v1.IDssComponent;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.FileInfoDssDTO;

/**
 * @author Franz-Josef Elmer
 */
abstract class AbstractDataSetExecutor<A extends DataSetArguments> extends AbstractExecutor<A>
{

    AbstractDataSetExecutor(A arguments, AbstractDssCommand<A> command)
    {
        super(arguments, command);
    }

    @Override
    protected final int doExecute(IDssComponent component)
    {
        IDataSetDss dataSet = component.getDataSet(arguments.getDataSetCode());
        FileInfoDssDTO[] fileInfos = getFileInfos(dataSet);
        handle(fileInfos, dataSet);
        return 0;
    }

    private FileInfoDssDTO[] getFileInfos(IDataSetDss dataSet)
    {
        return dataSet.listFiles(arguments.getRequestedPath(), arguments.isRecursive());
    }

    protected abstract void handle(FileInfoDssDTO[] fileInfos, IDataSetDss dataSet);

}
