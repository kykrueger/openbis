/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.datamover.console.server;

import ch.systemsx.cisd.common.servlet.IActionLog;
import ch.systemsx.cisd.datamover.console.client.IDatamoverConsoleService;

/**
 * Interface for logging invocations of methods of {@link IDatamoverConsoleService}.
 *
 * @author Franz-Josef Elmer
 */
public interface IConsoleActionLog extends IActionLog
{
    /**
     * Logs that the specified datamover has been started for the specified target.
     */
    public void logStartDatamover(String datamover, String targetName);

    /**
     * Logs that the specified datamover has been triggered for shuting down.
     */
    public void logShutdownDatamover(String datamover);

}
