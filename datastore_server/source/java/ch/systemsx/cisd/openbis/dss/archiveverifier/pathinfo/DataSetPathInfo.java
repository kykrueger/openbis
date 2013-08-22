/*
 * Copyright 2013 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.archiveverifier.pathinfo;

import java.util.Map;

import ch.systemsx.cisd.openbis.dss.archiveverifier.verifier.IArchiveFileContent;

/**
 * PathInfo data for single dataset.
 * 
 * @author anttil
 */
public class DataSetPathInfo implements IArchiveFileContent
{

    private final Map<String, PathInfoEntry> data;

    public DataSetPathInfo(Map<String, PathInfoEntry> data)
    {
        this.data = data;
    }

    @Override
    public Long getFileCrc(String file)
    {
        PathInfoEntry entry = data.get(file);
        return entry != null ? entry.getCrc() : null;
    }

    @Override
    public Long getFileSize(String file)
    {
        PathInfoEntry entry = data.get(file);
        return entry != null ? entry.getSize() : null;
    }
}
