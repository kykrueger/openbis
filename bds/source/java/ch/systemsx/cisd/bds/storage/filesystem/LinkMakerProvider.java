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

import ch.systemsx.cisd.common.utilities.OSUtilities;

/**
 * A provider of {@link ILinkMaker} implementations.
 * 
 * @author Christian Ribeaud
 */
public final class LinkMakerProvider
{

    private LinkMakerProvider()
    {
        // This class can not be instantiated.
    }

    /**
     * Returns a default <code>ILinkMaker</code> implementation.
     * <p>
     * Never returns <code>null</code> but could return the default implementation.
     * </p>
     * 
     * @see ILinkMaker#DEFAULT
     */
    public final static ILinkMaker getDefaultLinkMaker()
    {
        if (OSUtilities.isWindows())
        {
            return ILinkMaker.DEFAULT;
        }
        return HardLinkMaker.getInstance();
    }

}
