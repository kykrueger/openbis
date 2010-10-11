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

import java.io.PrintStream;

import ch.systemsx.cisd.args4j.ExampleMode;
import ch.systemsx.cisd.openbis.dss.client.api.v1.DssComponentFactory;
import ch.systemsx.cisd.openbis.dss.client.api.v1.IDssComponent;

/**
 * Superclass for dss command-line client commands.
 * <p>
 * Provides services used by most subclasses. In order for the AbstractCommand to work, subclasses
 * must do the following:
 * <ul>
 * <li>Set the parser ivar in their constructor
 * </ul>
 * 
 * @author Chandrasekhar Ramakrishnan
 */
abstract class AbstractDssCommand<T extends GlobalArguments> extends AbstractCommand<T> implements
        ICommand
{
    /**
     * @param arguments
     */
    public AbstractDssCommand(T arguments)
    {
        super(arguments);
    }

    /**
     * Print usage information about the command.
     */
    public void printUsage(PrintStream out)
    {
        out.println(getUsagePrefixString() + " [options] " + getRequiredArgumentsString());
        parser.printUsage(out);
        out.println("  Example : " + getCommandCallString() + " "
                + parser.printExample(ExampleMode.ALL) + " " + getRequiredArgumentsString());
    }

    protected String getRequiredArgumentsString()
    {
        return "<data set code> <path>";
    }

    /**
     * How is this command invoked from the command line? This is the program call string + command
     * name
     */
    protected String getCommandCallString()
    {
        return getProgramCallString() + " " + getName();
    }

    /**
     * Used for displaying help.
     */
    protected String getUsagePrefixString()
    {
        return "usage: " + getCommandCallString();
    }

    /**
     * Creates the DSS Component object and logs into the server.
     */
    protected IDssComponent login(GlobalArguments args)
    {
        IDssComponent component =
                DssComponentFactory.tryCreate(arguments.getUsername(), arguments.getPassword(),
                        arguments.getServerBaseUrl());
        if (null == component)
        {
            System.out.println("Username / password is invalid");
        }
        return component;
    }

}
