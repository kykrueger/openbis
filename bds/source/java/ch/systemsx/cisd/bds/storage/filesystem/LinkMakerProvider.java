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

package ch.systemsx.cisd.bds.storage.filesystem;

import ch.systemsx.cisd.common.Constants;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.utilities.IFileImmutableCopier;
import ch.systemsx.cisd.common.utilities.RecursiveHardLinkMaker;

/**
 * A provider of {@link IFileImmutableCopier} implementations.
 * 
 * @author Christian Ribeaud
 */
public final class LinkMakerProvider
{

    private static final String NO_HARD_LINK_EXECUTABLE = "No hard link executable has been found.";

    private static final int MAX_COPY_RETRIES = 7;

    private static IFileImmutableCopier hardLinkMaker;

    private LinkMakerProvider()
    {
        // This class can not be instantiated.
    }

    private final static IFileImmutableCopier tryCreateHardLinkMaker()
    {
        final IFileImmutableCopier copier =
                RecursiveHardLinkMaker.tryCreateRetrying(Constants.MILLIS_TO_WAIT_BEFORE_TIMEOUT,
                        MAX_COPY_RETRIES, Constants.MILLIS_TO_SLEEP_BEFORE_RETRYING);
        if (copier != null)
        {
            return copier;
        } else
        {
            throw new EnvironmentFailureException(NO_HARD_LINK_EXECUTABLE);
        }
    }

    /**
     * Returns an <code>IPathImmutableCopier</code> implementation which makes <i>hard links</i>
     * using the underlying <i>operating system</i>.
     */
    public final static IFileImmutableCopier getLinkMaker()
    {
        if (hardLinkMaker == null)
        {
            hardLinkMaker = tryCreateHardLinkMaker();
        }
        return hardLinkMaker;
    }
}
