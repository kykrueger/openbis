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

import ch.systemsx.cisd.args4j.CmdLineParser;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public class AbstractCommand<T extends GlobalArguments>
{
    protected final T arguments;

    protected final CmdLineParser parser;

    /**
     *
     *
     */
    public AbstractCommand(T arguments)
    {
        this.arguments = arguments;
        parser = new CmdLineParser(arguments);
    }

    /**
     * How is this program invoked from the command line?
     */
    protected String getProgramCallString()
    {
        return "dss_client.sh";
    }

}