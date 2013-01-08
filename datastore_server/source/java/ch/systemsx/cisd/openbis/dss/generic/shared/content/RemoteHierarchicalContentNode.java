/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.generic.shared.content;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ch.systemsx.cisd.base.exceptions.IOExceptionUnchecked;
import ch.systemsx.cisd.base.io.IRandomAccessFile;
import ch.systemsx.cisd.base.io.RandomAccessFileImpl;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContentNode;
import ch.systemsx.cisd.openbis.dss.generic.shared.ISingleDataSetPathInfoProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.FileInfoDssDTO;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.IDssServiceRpcGeneric;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetPathInfo;
import ch.systemsx.cisd.openbis.generic.shared.dto.OpenBISSessionHolder;

/**
 * A node of hierarchical content that stored on a remote datastore server. If file content is
 * queried, it is downloaded to session workspace and cached there.
 * 
 * @author anttil
 */
public class RemoteHierarchicalContentNode implements IHierarchicalContentNode
{

    private String dataSetCode;

    private ISingleDataSetPathInfoProvider provider;

    private IDssServiceRpcGeneric remoteDss;

    private OpenBISSessionHolder sessionHolder;

    private DataSetPathInfo path;

    private String parentRelativePath;

    private final ContentCache cache;

    public RemoteHierarchicalContentNode(String dataSetCode, DataSetPathInfo path,
            ISingleDataSetPathInfoProvider provider, IDssServiceRpcGeneric remote,
            OpenBISSessionHolder sessionHolder, ContentCache contentCache)
    {
        this(dataSetCode, path, provider, remote, sessionHolder, contentCache, null);
    }

    private RemoteHierarchicalContentNode(String dataSetCode,
            DataSetPathInfo path,
            ISingleDataSetPathInfoProvider provider,
            IDssServiceRpcGeneric remote,
            OpenBISSessionHolder sessionHolder,
            ContentCache contentCache,
            String parentRelativePath)
    {
        this.dataSetCode = dataSetCode;
        this.path = path;
        this.provider = provider;
        this.remoteDss = remote;
        this.sessionHolder = sessionHolder;
        this.cache = contentCache;
        this.parentRelativePath = parentRelativePath;
    }

    @Override
    public String getName()
    {
        return path.getFileName();
    }

    @Override
    public String getRelativePath()
    {
        return path.getRelativePath();
    }

    @Override
    public String getParentRelativePath()
    {
        return parentRelativePath;
    }

    @Override
    public boolean exists()
    {
        return true;
    }

    @Override
    public boolean isDirectory()
    {
        return path.isDirectory();
    }

    @Override
    public long getLastModified()
    {
        if (path.getLastModified() != null)
        {
            return path.getLastModified().getTime();
        } else
        {
            return 0;
        }
    }

    @Override
    public long getFileLength() throws UnsupportedOperationException
    {
        return path.getSizeInBytes();
    }

    @Override
    public int getChecksumCRC32() throws UnsupportedOperationException
    {
        return path.getChecksumCRC32();
    }

    @Override
    public boolean isChecksumCRC32Precalculated()
    {
        return false;
    }

    @Override
    public List<IHierarchicalContentNode> getChildNodes() throws UnsupportedOperationException
    {

        List<IHierarchicalContentNode> children = new ArrayList<IHierarchicalContentNode>();

        if (provider != null)
        {
            for (DataSetPathInfo childPath : provider.listChildrenPathInfos(path))
            {
                children.add(new RemoteHierarchicalContentNode(dataSetCode, childPath, provider,
                        remoteDss, sessionHolder, cache, path.getRelativePath()));
            }
        } else
        {
            for (FileInfoDssDTO file : remoteDss.listFilesForDataSet(
                    sessionHolder.getSessionToken(), dataSetCode,
                    path.getRelativePath(), false))
            {
                DataSetPathInfo info = new DataSetPathInfo();
                info.setChecksumCRC32(file.tryGetCrc32Checksum());
                info.setDirectory(file.isDirectory());
                info.setFileName(file.getPathInDataSet());
                info.setRelativePath(file.getPathInDataSet());
                info.setSizeInBytes(file.getFileSize());
                info.setLastModified(new Date(0L));
                children.add(new RemoteHierarchicalContentNode(dataSetCode, info, provider,
                        remoteDss, sessionHolder, cache, path.getRelativePath()));
            }
        }
        return children;
    }

    @Override
    public File getFile() throws UnsupportedOperationException
    {
        return cache.getFile(dataSetCode, path);
    }

    @Override
    public File tryGetFile()
    {
        try
        {
            return getFile();
        } catch (Exception e)
        {
            return null;
        }
    }

    @Override
    public IRandomAccessFile getFileContent() throws UnsupportedOperationException,
            IOExceptionUnchecked
    {
        return new RandomAccessFileImpl(getFile(), "r");
    }

    @Override
    public InputStream getInputStream() throws UnsupportedOperationException, IOExceptionUnchecked
    {
        File f = getFile();
        try
        {
            return new FileInputStream(f);
        } catch (FileNotFoundException ex)
        {
            throw new IOExceptionUnchecked(ex);
        }
    }
}
