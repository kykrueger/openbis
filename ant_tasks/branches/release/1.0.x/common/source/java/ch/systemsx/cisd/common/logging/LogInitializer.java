/*
 * Copyright 2007 ETH Zuerich, CISD
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

package ch.systemsx.cisd.common.logging;

import java.io.File;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.xml.DOMConfigurator;

/**
 * Initializes the logging system. The {@link #init()} method needs to be called once at system startup.
 * 
 * @author Bernd Rinn
 */
public class LogInitializer
{

    private static boolean initialized = false;

    public static synchronized void init()
    {
        if (initialized)
        {
            return;
        }

        final String logDirectory = "etc";
        final String logFilename = "log.xml";
        final File logFile = new File(logDirectory, logFilename);
        if (logFile.exists())
        {
            DOMConfigurator.configureAndWatch(logFile.getPath());
        } else
        {
            BasicConfigurator.configure();
        }
        initialized = true;
    }

}
