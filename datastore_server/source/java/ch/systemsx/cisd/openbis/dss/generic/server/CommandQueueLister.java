/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.generic.server;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A class that provides a lister for the commands in the command queue of the store.
 * 
 * @author Bernd Rinn
 */
public final class CommandQueueLister
{
    private CommandQueueLister()
    {
        // Cannot be instantiated.
    }

    public static void listQueuedCommand()
    {
        final ConfigParameters configParams = DataStoreServer.getConfigParameters();
        DataSetCommandExecutor.listQueuedCommands(configParams.getCommandQueueDir());
    }

    public static Set<String> getDataSetCodesFromCommandQueue()
    {
        final ConfigParameters configParams = DataStoreServer.getConfigParameters();
        List<IDataSetCommand> commands = DataSetCommandExecutor.getQueuedCommands(configParams.getCommandQueueDir());

        Set<String> datasetCodes = new HashSet<String>();
        for (IDataSetCommand command : commands)
        {
            datasetCodes.addAll(command.getDataSetCodes());
        }
        return datasetCodes;
    }
}
