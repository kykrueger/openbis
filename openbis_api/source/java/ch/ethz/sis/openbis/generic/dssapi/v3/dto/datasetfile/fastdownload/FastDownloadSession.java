/*
 * Copyright 2019 ETH Zuerich, SIS
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

package ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.fastdownload;

import java.io.Serializable;
import java.util.List;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.ObjectToString;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.id.IDataSetFileId;

/**
 * @author Franz-Josef Elmer
 */
public class FastDownloadSession implements Serializable
{
    private static final long serialVersionUID = 1L;

    private String downloadUrl;

    private String fileTransferUserSessionId;

    private List<IDataSetFileId> files;

    private FastDownloadSessionOptions options;

    public FastDownloadSession(String downloadUrl, String fileTransferUserSessionId, List<IDataSetFileId> files, FastDownloadSessionOptions options)
    {
        this.downloadUrl = downloadUrl;
        this.fileTransferUserSessionId = fileTransferUserSessionId;
        this.files = files;
        this.options = options;
    }

    public String getDownloadUrl()
    {
        return downloadUrl;
    }

    public String getFileTransferUserSessionId()
    {
        return fileTransferUserSessionId;
    }

    public List<IDataSetFileId> getFiles()
    {
        return files;
    }

    public FastDownloadSessionOptions getOptions()
    {
        return options;
    }

    @Override
    public String toString()
    {
        return new ObjectToString(this).append("downloadUrl", downloadUrl)
                .append("fileTransferUserSessionId", fileTransferUserSessionId)
                .append("number of files", files.size())
                .append("options", options)
                .toString();
    }

}
