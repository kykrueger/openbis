/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.etlserver.path;

import java.io.File;
import java.sql.Connection;
import java.util.List;

import net.lemnik.eodsql.QueryTool;

import ch.systemsx.cisd.etlserver.IDataSetPathsInfoFeeder;

/**
 * Data set paths info feeder feeding a data base.
 *
 * @author Franz-Josef Elmer
 */
public class DatabaseBasedDataSetPathsInfoFeeder implements IDataSetPathsInfoFeeder
{
    private final IPathsInfoDAO dao;

    public DatabaseBasedDataSetPathsInfoFeeder(Connection connection)
    {
        this(QueryTool.getQuery(connection, IPathsInfoDAO.class));
    }
    
    DatabaseBasedDataSetPathsInfoFeeder(IPathsInfoDAO dao)
    {
        this.dao = dao;
    }

    public long addPaths(String dataSetCode, String location, File dataSetRoot)
    {
        long dataSetId = dao.createDataSet(dataSetCode, location);
        PathInfo root = PathInfo.createPathInfo(dataSetRoot);
        List<PathInfo> children = root.getChildren();
        for (PathInfo child : children)
        {
            addPaths(dataSetId, null, "", 1, child);
        }
        return root.getSizeInBytes();
    }

    private void addPaths(long dataSetId, Long parentId, String pathPrefix, int treeDepth,
            PathInfo pathInfo)
    {
        boolean directory = pathInfo.isDirectory();
        String relativePath = pathPrefix + pathInfo.getFileName();
        long id =
                dao.createDataSetFile(dataSetId, parentId, relativePath, treeDepth,
                        pathInfo.getSizeInBytes(), directory);
        if (directory)
        {
            List<PathInfo> children = pathInfo.getChildren();
            for (PathInfo child : children)
            {
                addPaths(dataSetId, id, relativePath + "/", treeDepth + 1, child);
            }
        }
    }

}
