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

package ch.systemsx.cisd.openbis.dss.generic.server.api.v1;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.base.exceptions.IOExceptionUnchecked;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.etlserver.api.v1.PutDataSetService;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.HierarchicalContentUtils;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContent;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContentNode;
import ch.systemsx.cisd.openbis.common.spring.IInvocationLoggerContext;
import ch.systemsx.cisd.openbis.dss.generic.server.AbstractDssServiceRpc;
import ch.systemsx.cisd.openbis.dss.generic.server.IStreamRepository;
import ch.systemsx.cisd.openbis.dss.generic.server.SessionWorkspaceProvider;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.tasks.IPluginTaskInfoProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.IHierarchicalContentProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.IShareIdManager;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.authorization.IDssServiceRpcGenericInternal;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.DataSetFileDTO;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.FileInfoDssDTO;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.HierarchicalFileInfoDssBuilder;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.NewDataSetDTO;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.DatasetLocationUtil;
import ch.systemsx.cisd.openbis.plugin.query.shared.api.v1.IQueryApiServer;
import ch.systemsx.cisd.openbis.plugin.query.shared.api.v1.dto.AggregationServiceDescription;
import ch.systemsx.cisd.openbis.plugin.query.shared.api.v1.dto.QueryTableModel;
import ch.systemsx.cisd.openbis.plugin.query.shared.api.v1.dto.ReportDescription;

/**
 * Implementation of the generic RPC interface.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class DssServiceRpcGeneric extends AbstractDssServiceRpc<IDssServiceRpcGenericInternal>
        implements IDssServiceRpcGenericInternal
{
    /**
     * Logger with {@link LogCategory#OPERATION} with name of the concrete class, needs to be static
     * for our purpose.
     */
    @SuppressWarnings("hiding")
    protected static Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            DssServiceRpcGeneric.class);

    private final PutDataSetService putService;

    private final IQueryApiServer queryApiServer;

    private final File sessionWorkspaceRootDirectory;

    /**
     * The designated constructor.
     */
    public DssServiceRpcGeneric(IEncapsulatedOpenBISService openBISService,
            IQueryApiServer apiServer, IPluginTaskInfoProvider infoProvider)
    {
        // NOTE: IShareIdManager and IHierarchicalContentProvider will be lazily created by spring
        this(openBISService, apiServer, infoProvider, null, null);
    }

    DssServiceRpcGeneric(IEncapsulatedOpenBISService openBISService, IQueryApiServer apiServer,
            IPluginTaskInfoProvider infoProvider, IShareIdManager shareIdManager,
            IHierarchicalContentProvider contentProvider)
    {
        this(openBISService, apiServer, infoProvider, null, shareIdManager, contentProvider,
                new PutDataSetService(openBISService, operationLog));
    }

    /**
     * A constructor for testing.
     */
    public DssServiceRpcGeneric(IEncapsulatedOpenBISService openBISService,
            IQueryApiServer apiServer, IPluginTaskInfoProvider infoProvider,
            IStreamRepository streamRepository, IShareIdManager shareIdManager,
            IHierarchicalContentProvider contentProvider, PutDataSetService service)
    {
        super(openBISService, streamRepository, shareIdManager, contentProvider);
        queryApiServer = apiServer;
        putService = service;
        this.sessionWorkspaceRootDirectory = infoProvider.getSessionWorkspaceRootDir();
        operationLog.info("[rpc] Started DSS API V1 service.");
    }

    @Override
    public IDssServiceRpcGenericInternal createLogger(IInvocationLoggerContext context)
    {
        return new DssServiceRpcGenericLogger(context);
    }

    @Override
    public FileInfoDssDTO[] listFilesForDataSet(String sessionToken, String dataSetCode,
            String startPath, boolean isRecursive) throws IllegalArgumentException
    {
        IHierarchicalContent content = null;
        try
        {
            content = getHierarchicalContent(dataSetCode);
            IHierarchicalContentNode startPathNode = getContentNode(content, startPath);
            ArrayList<FileInfoDssDTO> list = new ArrayList<FileInfoDssDTO>();
            if (startPathNode.isDirectory())
            {
                appendFileInfosForFile(startPathNode, list, isRecursive);
            } else
            {
                list.add(new FileInfoDssDTO(startPathNode.getRelativePath(), startPathNode
                        .getName(), false, startPathNode.getFileLength(), startPathNode
                        .isChecksumCRC32Precalculated() ? startPathNode.getChecksumCRC32() : null));
            }
            FileInfoDssDTO[] fileInfos = new FileInfoDssDTO[list.size()];
            return list.toArray(fileInfos);
        } catch (IOException ex)
        {
            operationLog.info("listFiles: " + startPath + " caused an exception", ex);
            throw new IOExceptionUnchecked(ex);
        } catch (RuntimeException ex)
        {
            operationLog.info("listFiles: " + startPath + " caused an exception", ex);
            throw ex;
        } finally
        {
            if (content != null)
            {
                content.close();
            }
        }
    }

    @Override
    public InputStream getFileForDataSet(String sessionToken, String dataSetCode, String path)
            throws IOExceptionUnchecked, IllegalArgumentException
    {
        IHierarchicalContent content = null;
        try
        {
            content = getHierarchicalContent(dataSetCode);
            IHierarchicalContentNode contentNode = getContentNode(content, path);
            return HierarchicalContentUtils.getInputStreamAutoClosingContent(contentNode, content);
        } catch (RuntimeException ex)
        {
            operationLog.info("getFile: " + path + " caused an exception", ex);
            if (content != null)
            {
                // close content only on exception, otherwise stream close should close the content
                content.close();
            }
            throw ex;
        }
    }

    @Override
    public String getDownloadUrlForFileForDataSet(String sessionToken, String dataSetCode,
            String path) throws IOExceptionUnchecked, IllegalArgumentException
    {
        InputStream stream = getFileForDataSet(sessionToken, dataSetCode, path);
        return addToRepositoryAndReturnDownloadUrl(stream, path);
    }

    private IHierarchicalContentNode getContentNode(IHierarchicalContent content, String startPath)
    {
        // handle both relative and absolute paths for backward compatibility
        return content.getNode(startPath.startsWith("/") ? startPath.substring(1) : startPath);
    }

    @Override
    public String putDataSet(String sessionToken, NewDataSetDTO newDataSet, InputStream inputStream)
            throws IOExceptionUnchecked, IllegalArgumentException
    {
        return putService.putDataSet(sessionToken, newDataSet, inputStream);
    }

    @Override
    public long putFileToSessionWorkspace(String sessionToken, String filePath,
            InputStream inputStream) throws IOExceptionUnchecked
    {
        getOpenBISService().checkSession(sessionToken);
        if (filePath.contains("../"))
        {
            throw new IOExceptionUnchecked("filePath must not contain '../'");
        }
        final String subDir = FilenameUtils.getFullPath(filePath);
        final String filename = FilenameUtils.getName(filePath);
        final File workspaceDir =
                new SessionWorkspaceProvider(sessionWorkspaceRootDirectory, sessionToken)
                        .getSessionWorkspace();
        final File dir = new File(workspaceDir, subDir);
        dir.mkdirs();
        final File file = new File(dir, filename);
        OutputStream ostream = null;
        try
        {
            ostream = new FileOutputStream(file);
            long size = IOUtils.copyLarge(inputStream, ostream);
            ostream.close();
            return size;
        } catch (IOException ex)
        {
            file.delete();
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        } finally
        {
            IOUtils.closeQuietly(ostream);
        }
    }

    @Override
    public long putFileSliceToSessionWorkspace(String sessionToken, String filePath,
            long slicePosition, InputStream sliceInputStream) throws IOExceptionUnchecked
    {
        getOpenBISService().checkSession(sessionToken);
        if (filePath.contains("../"))
        {
            throw new IOExceptionUnchecked("filePath must not contain '../'");
        }
        final String subDir = FilenameUtils.getFullPath(filePath);
        final String filename = FilenameUtils.getName(filePath);
        final File workspaceDir =
                new SessionWorkspaceProvider(sessionWorkspaceRootDirectory, sessionToken)
                        .getSessionWorkspace();
        final File dir = new File(workspaceDir, subDir);
        dir.mkdirs();
        final File file = new File(dir, filename);

        return FileUtilities.writeToFile(file, slicePosition, sliceInputStream);
    }

    @Override
    public InputStream getFileFromSessionWorkspace(String sessionToken, String filePath)
            throws IOExceptionUnchecked
    {
        getOpenBISService().checkSession(sessionToken);
        if (filePath.contains("../"))
        {
            throw new IOExceptionUnchecked("filePath must not contain '../'");
        }
        final File workspaceDir =
                new SessionWorkspaceProvider(sessionWorkspaceRootDirectory, sessionToken)
                        .getSessionWorkspace();
        final File file = new File(workspaceDir, filePath);
        try
        {
            return new FileInputStream(file);
        } catch (FileNotFoundException ex)
        {
            throw new IOExceptionUnchecked(ex);
        }
    }

    @Override
    public boolean deleteSessionWorkspaceFile(String sessionToken, String path)
    {
        getOpenBISService().checkSession(sessionToken);
        if (path.contains("../"))
        {
            throw new IOExceptionUnchecked("path must not contain '../'");
        }
        final File workspaceDir =
                new SessionWorkspaceProvider(sessionWorkspaceRootDirectory, sessionToken)
                        .getSessionWorkspace();
        final File file = new File(workspaceDir, path);
        FileUtilities.deleteRecursively(file);
        return file.exists() == false;
    }

    @Override
    public int getMajorVersion()
    {
        return 1;
    }

    @Override
    public int getMinorVersion()
    {
        return 7;
    }

    /**
     * Append file info for the requested node of a file or file hierarchy. Assumes that the
     * parameters have been verified already.
     * 
     * @param requestedFile A file known to be accessible by the user
     * @param dataSetRoot The root of the file hierarchy; used to determine the absolute path of the
     *            file
     * @param listingRootNode The node which is a root of the list hierarchy; used to determine the
     *            relative path of the file
     * @param list The list the files infos are appended to
     * @param isRecursive If true, directories will be recursively appended to the list
     */
    private void appendFileInfosForFile(IHierarchicalContentNode listingRootNode,
            ArrayList<FileInfoDssDTO> list, boolean isRecursive) throws IOException
    {
        HierarchicalFileInfoDssBuilder factory =
                new HierarchicalFileInfoDssBuilder(listingRootNode);
        factory.appendFileInfos(list, isRecursive);
    }

    @Override
    public InputStream getFileForDataSet(String sessionToken, DataSetFileDTO fileOrFolder)
            throws IOExceptionUnchecked, IllegalArgumentException
    {
        return this.getFileForDataSet(sessionToken, fileOrFolder.getDataSetCode(),
                fileOrFolder.getPath());
    }

    @Override
    public String getDownloadUrlForFileForDataSet(String sessionToken, DataSetFileDTO fileOrFolder)
            throws IOExceptionUnchecked, IllegalArgumentException
    {
        InputStream stream = getFileForDataSet(sessionToken, fileOrFolder);
        return addToRepositoryAndReturnDownloadUrl(stream, fileOrFolder.getPath());
    }

    @Override
    public FileInfoDssDTO[] listFilesForDataSet(String sessionToken, DataSetFileDTO fileOrFolder)
            throws IOExceptionUnchecked, IllegalArgumentException
    {
        return this.listFilesForDataSet(sessionToken, fileOrFolder.getDataSetCode(),
                fileOrFolder.getPath(), fileOrFolder.isRecursive());
    }

    @Override
    public void setStoreDirectory(File aFile)
    {
        super.setStoreDirectory(aFile);
        putService.setStoreDirectory(aFile);
    }

    @Override
    public String getPathToDataSet(String sessionToken, String dataSetCode,
            String overrideStoreRootPathOrNull) throws IOExceptionUnchecked,
            IllegalArgumentException
    {
        final File dataSetRootDirectory =
                DatasetLocationUtil.getDatasetLocationPath(getStoreDirectory(), dataSetCode,
                        getShareIdManager().getShareId(dataSetCode), getHomeDatabaseInstance()
                                .getUuid());
        // see NOTE in interface documentation
        if (dataSetRootDirectory.exists() == false)
        {
            throw new IllegalArgumentException("Path to dataset '" + dataSetCode
                    + "' not available: this is a container dataset.");
        }
        return convertPath(getStoreDirectory(), dataSetRootDirectory, overrideStoreRootPathOrNull);
    }

    public static String convertPath(File storeRoot, File dataSetRoot,
            String overrideStoreRootPathOrNull)
    {
        String dataStoreRootPath = storeRoot.getAbsolutePath();
        String dataSetPath = dataSetRoot.getAbsolutePath();

        // No override specified; give the user the path as we understand it.
        if (null == overrideStoreRootPathOrNull
                || false == dataSetPath.startsWith(dataStoreRootPath))
        {
            return dataSetPath;
        }

        // Make the path begin with the user's store root override
        File usersPath =
                new File(overrideStoreRootPathOrNull, dataSetPath.substring(dataStoreRootPath
                        .length()));
        return usersPath.getPath();
    }

    @Override
    public String getValidationScript(String sessionToken, String dataSetTypeOrNull)
            throws IOExceptionUnchecked, IllegalArgumentException
    {
        return putService.getValidationScript(dataSetTypeOrNull);
    }

    @Override
    public QueryTableModel createReportFromAggregationService(String sessionToken,
            String aggregationServiceName, Map<String, Object> parameters)
    {
        QueryTableModel result =
                queryApiServer.createReportFromAggregationService(sessionToken, "STANDARD",
                        aggregationServiceName, parameters);
        return result;
    }

    @Override
    public QueryTableModel createReportFromDataSets(String sessionToken, String dataStoreCode,
            String serviceKey, List<String> dataSetCodes)
    {
        QueryTableModel result =
                queryApiServer.createReportFromDataSets(sessionToken, dataStoreCode, serviceKey,
                        dataSetCodes);
        return result;
    }

    @Override
    public List<AggregationServiceDescription> listAggregationServices(String sessionToken)
    {
        return queryApiServer.listAggregationServices(sessionToken);
    }

    @Override
    public List<ReportDescription> listTableReportDescriptions(String sessionToken)
    {
        return queryApiServer.listTableReportDescriptions(sessionToken);
    }
}
