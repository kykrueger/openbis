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

package ch.systemsx.cisd.cina.client.util.cli;

import java.util.List;

import ch.systemsx.cisd.cina.client.util.v1.ICinaUtilities;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.dss.client.api.cli.GlobalArguments;
import ch.systemsx.cisd.openbis.dss.client.api.cli.ResultCode;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Experiment;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public class CommandExperimentLister extends
        AbstractCinaCommand<CommandExperimentLister.CommandExperimentListerArguments>
{
    static class CommandExperimentListerArguments extends GlobalArguments
    {
        public String getExperimentTypeCode()
        {
            return getArguments().get(0).toString().toUpperCase();
        }

        @Override
        public boolean isComplete()
        {
            if (getArguments().size() < 1)
            {
                return false;
            }
            if (getExperimentTypeCode().length() < 1)
            {
                return false;
            }

            if (false == super.isComplete())
                return false;

            return true;
        }
    }

    private static class ExperimentListerExecutor extends
            AbstractExecutor<CommandExperimentListerArguments>
    {
        /**
         * @param command The parent command
         */
        ExperimentListerExecutor(CommandExperimentLister command)
        {
            super(command);
        }

        @Override
        protected ResultCode doExecute(ICinaUtilities component)
        {
            List<Experiment> results =
                    component.listVisibleExperiments(arguments.getExperimentTypeCode());
            for (Experiment experiment : results)
            {
                System.out.println(experiment.toString());
            }
            return ResultCode.OK;
        }
    }

    public CommandExperimentLister()
    {
        super(new CommandExperimentListerArguments());
    }

    public ResultCode execute(String[] args) throws UserFailureException,
            EnvironmentFailureException
    {
        return new ExperimentListerExecutor(this).execute(args);
    }

    public String getName()
    {
        return "listexps";
    }

    @Override
    protected String getRequiredArgumentsString()
    {
        return "<experiment type>";
    }
}
