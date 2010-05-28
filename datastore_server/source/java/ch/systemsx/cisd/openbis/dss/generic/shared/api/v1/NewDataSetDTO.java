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

package ch.systemsx.cisd.openbis.dss.generic.shared.api.v1;

import java.io.Serializable;
import java.util.List;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * Represents a new data set that the DSS should register.
 * <p>
 * The information required to register a new data set are the path of the data set and the name of
 * the storage process that should handle registering it.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class NewDataSetDTO implements Serializable
{
    private static final long serialVersionUID = 1L;

    private final String storageProcessName;

    private final List<FileInfoDssDTO> fileInfos;

    /**
     * Constructor
     * 
     * @param storageProcessName The storage process that should handle this data set
     */
    public NewDataSetDTO(String storageProcessName, List<FileInfoDssDTO> fileInfos)
    {
        this.storageProcessName = storageProcessName;
        this.fileInfos = fileInfos;
    }

    public String getStorageProcessName()
    {
        return storageProcessName;
    }

    public List<FileInfoDssDTO> getFileInfos()
    {
        return fileInfos;
    }

    @Override
    public String toString()
    {
        ToStringBuilder sb = new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE);
        sb.append(getStorageProcessName());
        sb.append(getFileInfos());
        return sb.toString();
    }
}
