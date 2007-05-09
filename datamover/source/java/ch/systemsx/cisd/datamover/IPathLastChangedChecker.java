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

package ch.systemsx.cisd.datamover;

import java.io.File;

/**
 * Interface that represents a role that can check when there has been the last write access to a given directory.
 * 
 * @author Bernd Rinn
 */
public interface IPathLastChangedChecker
{

    /**
     * Returns the last time when there was a write access to <var>directory</var>.
     * 
     * @param path The path to check for last write activity.
     * @return The time (in milliseconds since the start of the epoch) when <var>path</var> was last changed.
     */
    public long lastChanged(File path);

}
