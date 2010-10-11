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

import java.util.Arrays;
import java.util.List;

/**
 * @author Chandrasekhar Ramakrishnan
 */
class CommandFactory extends AbstractCommandFactory
{
    private static enum Command
    {
        LS, GET, HELP, PUT
    }

    @Override
    public ICommand tryCommandForName(String name)
    {
        ICommand superCommandOrNull = super.tryCommandForName(name);
        if (null != superCommandOrNull)
        {
            return superCommandOrNull;
        }

        Command command;
        try
        {
            command = Command.valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e)
        {
            return null;
        }

        ICommand result;
        switch (command)
        {
            case LS:
                result = new CommandLs();
                break;
            case GET:
                result = new CommandGet();
                break;
            case HELP:
                result = getHelpCommand();
                break;
            case PUT:
                result = new CommandPut();
                break;
            default:
                result = null;
                break;
        }

        return result;
    }

    public ICommand getHelpCommand()
    {
        return new CommandHelp(this);
    }

    public List<String> getKnownCommands()
    {
        String[] commands =
            { "ls", "get", "put" };
        return Arrays.asList(commands);
    }
}
