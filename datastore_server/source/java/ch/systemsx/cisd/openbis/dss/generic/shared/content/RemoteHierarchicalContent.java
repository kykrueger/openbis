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

import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.resource.IReleasable;
import ch.systemsx.cisd.common.resource.Resources;
import ch.systemsx.cisd.common.server.ISessionTokenProvider;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContent;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContentNode;
import ch.systemsx.cisd.openbis.dss.generic.shared.ISingleDataSetPathInfoProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetPathInfo;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IDatasetLocationNode;

/**
 * Implementation of HierchicalContent that is stored on remote datastore server.
 * 
 * @author anttil
 */
public class RemoteHierarchicalContent implements IHierarchicalContent
{

    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            RemoteHierarchicalContent.class);

    private final IDatasetLocationNode location;

    private final ISingleDataSetPathInfoProvider provider;

    private final ISessionTokenProvider sessionTokenProvider;

    private final IContentCache cache;

    private final IDssServiceRpcGenericFactory serviceFactory;

    private final Resources resources;

    public RemoteHierarchicalContent(IDatasetLocationNode location,
            ISingleDataSetPathInfoProvider pathInfoProvider,
            IDssServiceRpcGenericFactory serviceFactory,
            ISessionTokenProvider sessionTokenProvider, IContentCache cache)
    {
        this.location = location;
        this.provider = pathInfoProvider;
        this.serviceFactory = serviceFactory;
        this.sessionTokenProvider = sessionTokenProvider;
        this.cache = cache;
        this.resources = new Resources(operationLog);
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
            info = new DataSetPathInfo();
            info.setDirectory(true);
            info.setRelativePath("");
            info.setParent(null);
            info.setFileName("");
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
            throw new IllegalArgumentException("Resource '" + relativePath + "' does not exist.");
        }

        return createNode(info);
    }

    @Override
    public IHierarchicalContentNode tryGetNode(String relativePath)
    {
        if (provider == null)
        {
            return null;
        }
        final DataSetPathInfo infoOrNull = provider.tryGetPathInfoByRelativePath(relativePath);
        if (infoOrNull == null)
        {
            return null;
        } else
        {
            return createNode(infoOrNull);
        }
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
        resources.release();
    }

    private IHierarchicalContentNode createNode(DataSetPathInfo info)
    {
        final RemoteHierarchicalContentNode node =
                new RemoteHierarchicalContentNode(location.getLocation(), info, provider,
                        serviceFactory, sessionTokenProvider, cache);
        resources.add(new IReleasable()
            {
                @Override
                public void release()
                {
                    node.close();
                }
            });
        return node;
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

}
