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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;

import ch.systemsx.cisd.args4j.CmdLineParser;
import ch.systemsx.cisd.args4j.Option;
import ch.systemsx.cisd.base.exceptions.IOExceptionUnchecked;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.dss.client.api.v1.IDataSetDss;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.FileInfoDssDTO;

/**
 * Command that lists files in the data set.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
class CommandGet extends AbstractCommand
{
    private static class CommandGetArguments extends DataSetArguments
    {
        @Option(name = "o", longName = "output", usage = "Path for output")
        private String output = "";

        public String getOutput()
        {
            return output;
        }
    }

    private static class CommandGetExecutor extends AbstractDataSetExecutor<CommandGetArguments>
    {
        CommandGetExecutor(CommandGetArguments arguments, AbstractCommand command)
        {
            super(arguments, command);
        }

        @Override
        protected void handle(FileInfoDssDTO[] fileInfos, IDataSetDss dataSet)
        {
            File outputDir;
            if (arguments.getOutput().length() > 0)
            {
                // create the directory specified by output
                outputDir = new File(arguments.getOutput());
                outputDir.mkdirs();
            } else
            {
                outputDir = new File(".");
            }

            try
            {
                System.out.println("output dir :  " + outputDir.getCanonicalPath());

            } catch (IOException e)
            {
                throw new IOExceptionUnchecked(e);
            }

            // This logic is a duplication of that in FileInfoDssDownloader -- if that class were to
            // be modified with a listener that issued notifications, this code could use that
            // class.
            // Download file in this thread -- could spawn threads for d/l in a future iteration
            for (FileInfoDssDTO fileInfo : fileInfos)
            {
                if (fileInfo.isDirectory())
                {
                    System.out.println("mkdir " + fileInfo.getPathInDataSet());
                    File dir = new File(outputDir, fileInfo.getPathInDataSet());
                    dir.mkdirs();
                } else
                {
                    System.out.println("downloading " + fileInfo.getPathInDataSet());
                    File file = new File(outputDir, fileInfo.getPathInDataSet());
                    // Make sure the parent exists
                    file.getParentFile().mkdirs();

                    downloadFile(fileInfo, file, dataSet);
                }
            }
            System.out.println("Finished.");
        }

        private void downloadFile(FileInfoDssDTO fileInfo, File file, IDataSetDss dataSet)
        {
            try
            {
                FileOutputStream fos = new FileOutputStream(file);
                InputStream is = dataSet.getFile(fileInfo.getPathInDataSet());
                IOUtils.copyLarge(is, fos);
            } catch (IOException e)
            {
                throw new IOExceptionUnchecked(e);
            }
        }

    }

    private final CommandGetArguments arguments;

    CommandGet()
    {
        arguments = new CommandGetArguments();
        parser = new CmdLineParser(arguments);
    }

    public int execute(String[] args) throws UserFailureException, EnvironmentFailureException
    {
        return new CommandGetExecutor(arguments, this).execute(args);
    }

    public String getName()
    {
        return "get";
    }
}
