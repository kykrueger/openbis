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

import java.util.ArrayList;
import java.util.List;

import ch.systemsx.cisd.common.spring.HttpInvokerUtils;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContent;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContentNode;
import ch.systemsx.cisd.openbis.dss.generic.shared.ISingleDataSetPathInfoProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.FileInfoDssDTO;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.IDssServiceRpcGeneric;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetPathInfo;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IDatasetLocationNode;
import ch.systemsx.cisd.openbis.generic.shared.dto.OpenBISSessionHolder;

/**
 * Implementation of HierchicalContent that is stored on remote datastore server.
 * 
 * @author anttil
 */
public class RemoteHierarchicalContent implements IHierarchicalContent
{

    private final IDatasetLocationNode location;

    private final ISingleDataSetPathInfoProvider provider;

    private final OpenBISSessionHolder sessionHolder;

    private final IDssServiceRpcGeneric local;

    private final String sessionWorkspaceRoot;

    private final boolean trustAllCertificates;

    public RemoteHierarchicalContent(IDatasetLocationNode location,
            ISingleDataSetPathInfoProvider pathInfoProvider,
            OpenBISSessionHolder sessionHolder,
            IDssServiceRpcGeneric local,
            String sessionWorkspaceRoot,
            boolean trustAllCertificates)
    {
        this.location = location;
        this.provider = pathInfoProvider;
        this.sessionHolder = sessionHolder;
        this.local = local;
        this.sessionWorkspaceRoot = sessionWorkspaceRoot;
        this.trustAllCertificates = trustAllCertificates;
    }

    @Override
    public IHierarchicalContentNode getRootNode()
    {

        DataSetPathInfo info = null;
        if (provider != null)
        {
            info = provider.getRootPathInfo();
        }

        if (info == null)
        {
            FileInfoDssDTO[] files =
                    getRemoteDss().listFilesForDataSet(sessionHolder.getSessionToken(),
                            location.getLocation().getDataSetCode(), "", false);

            info = convert(files[0]);
        }

        return createNode(info);
    }

    @Override
    public IHierarchicalContentNode getNode(String relativePath) throws IllegalArgumentException
    {

        DataSetPathInfo info = null;

        if (provider != null)
        {
            info = provider.tryGetPathInfoByRelativePath(relativePath);
        }

        if (info == null)
        {
            FileInfoDssDTO[] files =
                    getRemoteDss().listFilesForDataSet(sessionHolder.getSessionToken(),
                            location.getLocation().getDataSetCode(), relativePath, false);
            info = convert(files[0]);
        }

        return createNode(info);
    }

    @Override
    public List<IHierarchicalContentNode> listMatchingNodes(String relativePathPattern)
    {
        List<DataSetPathInfo> paths = null;

        if (provider == null)
        {
            paths = provider.listMatchingPathInfos(relativePathPattern);
        }

        if (paths == null)
        {
            throw new UnsupportedOperationException(
                    "pattern matching not available without pathinfo db");
        }

        return createNodes(paths);
    }

    @Override
    public List<IHierarchicalContentNode> listMatchingNodes(String startingPath,
            String fileNamePattern)
    {
        List<DataSetPathInfo> paths = null;

        if (provider == null)
        {
            paths = provider.listMatchingPathInfos(startingPath, fileNamePattern);
        }

        if (paths == null)
        {
            throw new UnsupportedOperationException(
                    "pattern matching not available without pathinfo db");
        }

        return createNodes(paths);
    }

    @Override
    public void close()
    {
    }

    private DataSetPathInfo convert(FileInfoDssDTO dto)
    {
        DataSetPathInfo info = new DataSetPathInfo();
        info.setChecksumCRC32(dto.tryGetCrc32Checksum());
        info.setDirectory(dto.isDirectory());
        info.setFileName(dto.getPathInDataSet());
        info.setLastModified(null);
        info.setRelativePath(dto.getPathInDataSet());
        info.setSizeInBytes(dto.getFileSize());
        return info;
    }

    private IHierarchicalContentNode createNode(DataSetPathInfo info)
    {

        return new RemoteHierarchicalContentNode(
                location.getLocation().getDataSetCode(),
                info,
                provider,
                local,
                getRemoteDss(),
                sessionHolder,
                sessionWorkspaceRoot,
                trustAllCertificates);
    }

    private List<IHierarchicalContentNode> createNodes(List<DataSetPathInfo> paths)
    {
        List<IHierarchicalContentNode> nodes = new ArrayList<IHierarchicalContentNode>();
        for (DataSetPathInfo info : paths)
        {
            nodes.add(createNode(info));
        }
        return nodes;
    }

    private IDssServiceRpcGeneric getRemoteDss()
    {
        return HttpInvokerUtils.createServiceStub(IDssServiceRpcGeneric.class, location
                .getLocation().getDataStoreUrl() + "/datastore_server/rmi-dss-api-v1", 300000);
    }
}
