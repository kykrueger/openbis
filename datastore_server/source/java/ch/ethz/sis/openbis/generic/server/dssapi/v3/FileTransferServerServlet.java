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

package ch.ethz.sis.openbis.generic.server.dssapi.v3;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;

import ch.ethz.sis.filetransfer.Chunk;
import ch.ethz.sis.filetransfer.DefaultSerializerProvider;
import ch.ethz.sis.filetransfer.DownloadException;
import ch.ethz.sis.filetransfer.DownloadItemId;
import ch.ethz.sis.filetransfer.DownloadItemNotFoundException;
import ch.ethz.sis.filetransfer.DownloadPreferences;
import ch.ethz.sis.filetransfer.DownloadRange;
import ch.ethz.sis.filetransfer.DownloadServer;
import ch.ethz.sis.filetransfer.DownloadServerConfig;
import ch.ethz.sis.filetransfer.DownloadSession;
import ch.ethz.sis.filetransfer.DownloadSessionId;
import ch.ethz.sis.filetransfer.DownloadState;
import ch.ethz.sis.filetransfer.DownloadStreamId;
import ch.ethz.sis.filetransfer.FileChunk;
import ch.ethz.sis.filetransfer.IChunkProvider;
import ch.ethz.sis.filetransfer.IConcurrencyProvider;
import ch.ethz.sis.filetransfer.IDownloadItemId;
import ch.ethz.sis.filetransfer.IDownloadServer;
import ch.ethz.sis.filetransfer.ILogger;
import ch.ethz.sis.filetransfer.IUserSessionId;
import ch.ethz.sis.filetransfer.LogLevel;
import ch.ethz.sis.filetransfer.UserSessionId;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.fastdownload.FastDownloadMethod;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.fastdownload.FastDownloadParameter;
import ch.ethz.sis.openbis.generic.dssapi.v3.fastdownload.FastDownloadUtils;
import ch.ethz.sis.openbis.generic.server.dssapi.v3.download.IFileTransferSessionManager;
import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.properties.PropertyUtils;
import ch.systemsx.cisd.common.reflection.ClassUtils;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContent;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContentNode;
import ch.systemsx.cisd.openbis.dss.generic.server.ApplicationContext;
import ch.systemsx.cisd.openbis.dss.generic.server.DataStoreServer;
import ch.systemsx.cisd.openbis.dss.generic.shared.IHierarchicalContentProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.authorization.DssSessionAuthorizationHolder;

/**
 * Servlet which provides download service of data set files using the file-transfer protocol.
 * 
 * @author Franz-Josef Elmer
 */
public class FileTransferServerServlet extends HttpServlet
{
    private static final long serialVersionUID = 1L;

    public static final String SERVLET_NAME = "file-transfer";

    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            FileTransferServerServlet.class);

    private IDownloadServer downloadServer;

    private JsonFactory jsonFactory;

    private IFileTransferSessionManager sessionManager;

    @Override
    public void init(ServletConfig servletConfig) throws ServletException
    {
        super.init(servletConfig);
        ServletContext context = servletConfig.getServletContext();
        ApplicationContext applicationContext = (ApplicationContext) context.getAttribute(DataStoreServer.APPLICATION_CONTEXT_KEY);
        DownloadServerConfig config = new DownloadServerConfig();
        ILogger logger = new Log4jBaseFileTransferLogger(LogLevel.INFO);
        config.setLogger(logger);
        sessionManager = ServiceProvider.getApplicationContext().getBean(IFileTransferSessionManager.class);
        config.setSessionManager(sessionManager);
        Properties properties = applicationContext.getConfigParameters().getProperties();
        config.setChunkProvider(new DataSetChunkProvider(applicationContext, FileUtils.ONE_MB, logger));
        config.setConcurrencyProvider(new ConcurrencyProvider(properties));
        config.setSerializerProvider(new DefaultSerializerProvider(logger));
        downloadServer = new DownloadServer(config);
        jsonFactory = new JsonFactory();
        operationLog.info("Servlet initialized");
    }

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        try
        {
            Map<String, String[]> parameterMap = request.getParameterMap();
            String method = request.getParameter(FastDownloadParameter.METHOD_PARAMETER.getParameterName());
            if (FastDownloadMethod.START_DOWNLOAD_SESSION_METHOD.getMethodName().equals(method))
            {
                handleStartDownloadSession(parameterMap, response);
            } else if (FastDownloadMethod.QUEUE_METHOD.getMethodName().equals(method))
            {
                handleQueue(parameterMap, response);
            } else if (FastDownloadMethod.DOWNLOAD_METHOD.getMethodName().equals(method))
            {
                handleDownload(parameterMap, response);
            } else if (FastDownloadMethod.FINISH_DOWNLOAD_SESSION_METHOD.getMethodName().equals(method))
            {
                handleFinishDownloadSession(parameterMap, response);
            } else
            {
                throw new IllegalArgumentException("Unknown method '" + method + "'.");
            }
        } catch (Exception e)
        {
            response.setContentType("application/json");
            JsonGenerator jsonGenerator = jsonFactory.createGenerator(response.getWriter());
            FastDownloadUtils.renderAsJson(jsonGenerator, e);
            operationLog.error(e.getMessage(), e);
        }
    }

    private void handleStartDownloadSession(Map<String, String[]> parameterMap, HttpServletResponse response) throws ServletException, IOException
    {
        IUserSessionId userSessionId = getUserSessionId(parameterMap);
        String sessionToken = sessionManager.getSessionToken(userSessionId.getId());
        List<IDownloadItemId> itemIds = filterByAccessRights(getDownloadItemIds(parameterMap), sessionToken);
        Integer wishedNumberOfStreams = getInteger(parameterMap, FastDownloadParameter.WISHED_NUMBER_OF_STREAMS_PARAMETER);
        DownloadPreferences preferences = new DownloadPreferences(wishedNumberOfStreams);
        DownloadSession downloadSession = downloadServer.startDownloadSession(userSessionId, itemIds, preferences);
        response.setContentType("application/json");
        JsonGenerator jsonGenerator = jsonFactory.createGenerator(response.getWriter());
        jsonGenerator.setPrettyPrinter(new DefaultPrettyPrinter());
        jsonGenerator.writeStartObject();
        jsonGenerator.writeObjectField(FastDownloadParameter.DOWNLOAD_SESSION_ID_PARAMETER.getParameterName(),
                downloadSession.getDownloadSessionId().getId());
        jsonGenerator.writeObjectFieldStart(FastDownloadParameter.RANGES_PARAMETER.getParameterName());
        for (Entry<IDownloadItemId, DownloadRange> entry : downloadSession.getRanges().entrySet())
        {
            IDownloadItemId downloadItemId = entry.getKey();
            DownloadRange downloadRange = entry.getValue();
            jsonGenerator.writeObjectField(downloadItemId.getId(), downloadRange.getStart() + ":" + downloadRange.getEnd());
        }
        jsonGenerator.writeEndObject();
        jsonGenerator.writeArrayFieldStart("streamIds");
        for (DownloadStreamId streamId : downloadSession.getStreamIds())
        {
            jsonGenerator.writeObject(streamId.getId());
        }
        jsonGenerator.writeEndArray();
        jsonGenerator.writeEndObject();
        jsonGenerator.flush();
    }

    private List<IDownloadItemId> filterByAccessRights(List<IDownloadItemId> itemIds, String sessionToken)
    {
        List<IDownloadItemId> filteredIds = new ArrayList<>();
        Set<String> alreadyAccessApprovedDataSets = new HashSet<>();
        for (IDownloadItemId itemId : itemIds)
        {
            String[] splitted = itemId.getId().split("/", 2);
            if (canAccess(alreadyAccessApprovedDataSets, splitted[0], sessionToken))
            {
                filteredIds.add(itemId);
            }
        }
        return filteredIds;
    }

    private boolean canAccess(Set<String> alreadyAccessApprovedDataSets, String dataSetCode, String sessionToken)
    {
        if (alreadyAccessApprovedDataSets.contains(dataSetCode))
        {
            return true;
        }
        Status authorizationStatus = DssSessionAuthorizationHolder.getAuthorizer().checkDatasetAccess(sessionToken, dataSetCode);
        if (authorizationStatus.isOK() == false)
        {
            return false;
        }
        alreadyAccessApprovedDataSets.add(dataSetCode);
        return true;
    }

    private void handleQueue(Map<String, String[]> parameterMap, HttpServletResponse response) throws ServletException
    {
        DownloadSessionId downloadSessionId = getDownloadSessionId(parameterMap);
        List<DownloadRange> ranges = new ArrayList<>();
        for (String range : getParameters(parameterMap, FastDownloadParameter.RANGES_PARAMETER))
        {
            try
            {
                String[] splitted = range.split(":");
                int start = Integer.parseInt(splitted[0]);
                int end = splitted.length == 1 ? start : Integer.parseInt(splitted[1]);
                ranges.add(new DownloadRange(start, end));
            } catch (NumberFormatException e)
            {
                throw new ServletException("Invalid range in parameter '"
                        + FastDownloadParameter.RANGES_PARAMETER.getParameterName() + "': " + range);
            }
        }
        downloadServer.queue(downloadSessionId, ranges);
    }

    private void handleDownload(Map<String, String[]> parameterMap, HttpServletResponse response) throws ServletException, IOException
    {
        DownloadSessionId downloadSessionId = getDownloadSessionId(parameterMap);
        DownloadStreamId streamId = new DownloadStreamId();
        ClassUtils.setFieldValue(streamId, "id",
                getParameters(parameterMap, FastDownloadParameter.DOWNLOAD_STREAM_ID_PARAMETER).get(0));
        Integer numberOfChunksOrNull = getInteger(parameterMap, FastDownloadParameter.NUMBER_OF_CHUNKS_PARAMETER);
        InputStream stream = downloadServer.download(downloadSessionId, streamId, numberOfChunksOrNull);
        response.setContentType("application/octet-stream");
        IOUtils.copyLarge(stream, response.getOutputStream());
    }

    private void handleFinishDownloadSession(Map<String, String[]> parameterMap, HttpServletResponse response) throws ServletException
    {
        downloadServer.finishDownloadSession(getDownloadSessionId(parameterMap));
    }

    private IUserSessionId getUserSessionId(Map<String, String[]> parameterMap) throws ServletException
    {
        return new UserSessionId(getParameters(parameterMap, FastDownloadParameter.USER_SESSION_ID_PARAMETER).get(0));
    }

    private List<IDownloadItemId> getDownloadItemIds(Map<String, String[]> parameterMap) throws ServletException
    {
        return getParameters(parameterMap, FastDownloadParameter.DOWNLOAD_ITEM_IDS_PARAMETER)
                .stream().map(DownloadItemId::new).collect(Collectors.toList());
    }

    private DownloadSessionId getDownloadSessionId(Map<String, String[]> parameterMap) throws ServletException
    {
        DownloadSessionId downloadSessionId = new DownloadSessionId();
        ClassUtils.setFieldValue(downloadSessionId, "id", getParameters(parameterMap,
                FastDownloadParameter.DOWNLOAD_SESSION_ID_PARAMETER).get(0));
        return downloadSessionId;
    }

    private List<String> getParameters(Map<String, String[]> parameterMap, FastDownloadParameter parameter) throws ServletException
    {
        String parameterName = parameter.getParameterName();
        String[] items = parameterMap.get(parameterName);
        if (items == null)
        {
            throw new ServletException("Unspecified parameter '" + parameterName + "'.");
        }
        List<String> result = new ArrayList<>();
        for (String item : items)
        {
            String[] splitted = item.split(",");
            for (String element : splitted)
            {
                result.add(element.trim());
            }
        }
        return result;
    }

    private Integer getInteger(Map<String, String[]> parameterMap, FastDownloadParameter parameter) throws ServletException
    {
        String parameterName = parameter.getParameterName();
        String[] parameters = parameterMap.get(parameterName);
        if (parameters == null)
        {
            return null;
        }
        try
        {
            return new Integer(parameters[0]);
        } catch (NumberFormatException e)
        {
            throw new ServletException("Parameter '" + parameterName + "' is not an integer: " + parameters[0]);
        }
    }

    private final class ConcurrencyProvider implements IConcurrencyProvider
    {
        private int maximumNumberOfAllowedStreams;

        private ConcurrencyProvider(Properties properties)
        {
            maximumNumberOfAllowedStreams = PropertyUtils.getInt(properties, "api.v3.fast-download.maximum-number-of-allowed-streams", 10);
            operationLog.info("max number of allowed streams: " + maximumNumberOfAllowedStreams);
        }

        @Override
        public int getAllowedNumberOfStreams(IUserSessionId userSessionId, Integer wishedNumberOfStreams, List<DownloadState> downloadStates)
                throws DownloadException
        {
            int currentNumberOfStreams = downloadStates.stream().collect(Collectors.summingInt(DownloadState::getCurrentNumberOfStreams));
            int freeNumberOfStreams = maximumNumberOfAllowedStreams - currentNumberOfStreams;
            int allowedNumberOfStreams = freeNumberOfStreams / 2;
            if (wishedNumberOfStreams != null && wishedNumberOfStreams < allowedNumberOfStreams)
            {
                allowedNumberOfStreams = wishedNumberOfStreams;
            }
            operationLog.info("current number of streams: " + currentNumberOfStreams + ", wished number of streams: "
                    + (wishedNumberOfStreams == null ? "unspecified" : wishedNumberOfStreams)
                    + ", allowed number of streams: " + allowedNumberOfStreams);
            return allowedNumberOfStreams;
        }
    }

    private final class DataSetChunkProvider implements IChunkProvider
    {
        private final ILogger logger;

        private final ApplicationContext applicationContext;

        private long chunkSize;

        private DataSetChunkProvider(ApplicationContext applicationContext, long chunkSize, ILogger logger)
        {
            this.applicationContext = applicationContext;
            this.chunkSize = chunkSize;
            this.logger = logger;
        }

        @Override
        public Map<IDownloadItemId, List<Chunk>> getChunks(List<IDownloadItemId> itemIds)
                throws DownloadItemNotFoundException, DownloadException
        {
            IHierarchicalContentProvider contentProvider = applicationContext.getHierarchicalContentProvider(null);

            Map<IDownloadItemId, List<Chunk>> result = new HashMap<IDownloadItemId, List<Chunk>>();
            AtomicInteger sequenceNumber = new AtomicInteger(0);

            for (IDownloadItemId itemId : itemIds)
            {
                String[] splitted = itemId.getId().split("/", 2);
                String dataSetCode = splitted[0];
                IHierarchicalContent content = contentProvider.asContent(dataSetCode);
                String path = splitted[1];
                IHierarchicalContentNode node = content.getNode(path);
                List<Chunk> chunks = new ArrayList<>();
                addChunks(chunks, sequenceNumber, node, itemId);
                result.put(itemId, chunks);
            }

            return result;
        }

        private void addChunks(List<Chunk> chunks, AtomicInteger sequenceNumber, IHierarchicalContentNode node,
                IDownloadItemId itemId)
        {
            boolean directory = node.isDirectory();
            if (directory)
            {
                for (IHierarchicalContentNode childNode : node.getChildNodes())
                {
                    addChunks(chunks, sequenceNumber, childNode, itemId);
                }
            } else
            {
                long fileSize = node.getFileLength();
                long fileOffset = 0;
                do
                {
                    int payloadLength = (int) (Math.min(fileOffset + chunkSize, fileSize) - fileOffset);
                    chunks.add(new FileChunk(sequenceNumber.getAndIncrement(), itemId, node.getRelativePath(),
                            fileOffset, payloadLength, node.getFile().toPath(), logger));
                    fileOffset += chunkSize;
                } while (fileOffset < fileSize);
            }
        }
    }

}
