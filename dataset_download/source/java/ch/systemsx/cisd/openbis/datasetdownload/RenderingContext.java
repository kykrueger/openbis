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

package ch.systemsx.cisd.openbis.datasetdownload;

import java.io.File;

import ch.systemsx.cisd.common.utilities.FileUtilities;

final class RenderingContext
{
    private final File rootDir;
    private final String relativePathOrNull;
    private File file;
    private String urlPrefix;
    private String relativeParentPath;

    RenderingContext(File rootDir, String requestURI, String relativePathOrNull)
    {
        this.rootDir = rootDir;
        this.relativePathOrNull = relativePathOrNull;
        file = rootDir;
        urlPrefix = requestURI;
        if (relativePathOrNull != null && relativePathOrNull.length() > 0)
        {
            file = new File(rootDir, relativePathOrNull);
            urlPrefix = requestURI.substring(0, requestURI.length() - relativePathOrNull.length());
            relativeParentPath = FileUtilities.getRelativeFile(rootDir, file.getParentFile());
            if (relativeParentPath == null)
            {
                relativeParentPath = "";
            }
        }
    }

    public final File getRootDir()
    {
        return rootDir;
    }

    public final String getRelativePathOrNull()
    {
        return relativePathOrNull;
    }

    public final File getFile()
    {
        return file;
    }

    public final String getUrlPrefix()
    {
        return urlPrefix;
    }

    public final String getRelativeParentPath()
    {
        return relativeParentPath;
    }
    
}