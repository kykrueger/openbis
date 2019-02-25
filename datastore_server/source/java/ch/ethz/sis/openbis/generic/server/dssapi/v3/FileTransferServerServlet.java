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
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
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
import ch.ethz.sis.filetransfer.IUserSessionManager;
import ch.ethz.sis.filetransfer.InvalidUserSessionException;
import ch.ethz.sis.filetransfer.LogLevel;
import ch.ethz.sis.filetransfer.UserSessionId;
import ch.systemsx.cisd.common.properties.PropertyUtils;
import ch.systemsx.cisd.common.reflection.ClassUtils;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContent;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContentNode;
import ch.systemsx.cisd.openbis.dss.generic.server.ApplicationContext;
import ch.systemsx.cisd.openbis.dss.generic.server.DataStoreServer;
import ch.systemsx.cisd.openbis.dss.generic.server.OpenbisSessionTokenCache;
import ch.systemsx.cisd.openbis.dss.generic.shared.IHierarchicalContentProvider;

/**
 * @author Franz-Josef Elmer
 */
public class FileTransferServerServlet extends HttpServlet
{
    public static final String SERVLET_NAME = "file-transfer";

    private static final String METHOD_PARAMETER = "method";

    private static final long serialVersionUID = 1L;

    private IDownloadServer downloadServer;

    private JsonFactory jsonFactory;

    @Override
    public void init(ServletConfig servletConfig) throws ServletException
    {
        super.init(servletConfig);
        ServletContext context = servletConfig.getServletContext();
        ApplicationContext applicationContext = (ApplicationContext) context
                .getAttribute(DataStoreServer.APPLICATION_CONTEXT_KEY);

        DownloadServerConfig config = new DownloadServerConfig();
        ILogger logger = new Log4jBaseFileTransferLogger(LogLevel.INFO);
        config.setLogger(logger);
        config.setSessionManager(new SessionTokenBasedSessionManager(applicationContext.getSessionTokenCache()));
        Properties properties = applicationContext.getConfigParameters().getProperties();
        config.setChunkProvider(new DataSetChunkProvider(applicationContext, 600 * FileUtils.ONE_KB, logger));
        config.setConcurrencyProvider(new ConcurrencyProvider(properties));
        config.setSerializerProvider(new DefaultSerializerProvider(logger));
        downloadServer = new DownloadServer(config);
        jsonFactory = new JsonFactory();

    }

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        Map<String, String[]> parameterMap = request.getParameterMap();
        String method = request.getParameter(METHOD_PARAMETER);
        if ("startDownloadSession".equals(method))
        {
            handleStartDownloadSession(parameterMap, response);
        } else if ("queue".equals(method))
        {
            handleQueue(parameterMap, response);
        } else if ("download".equals(method))
        {
            handleDownload(parameterMap, response);
        } else if ("finishDownloadSession".equals(method))
        {
            handleFinishDownloadSession(parameterMap, response);
        } else
        {
            throw new ServletException("Unknown method '" + method + "'.");
        }
    }

    private void handleStartDownloadSession(Map<String, String[]> parameterMap, HttpServletResponse response) throws ServletException, IOException
    {
        IUserSessionId userSessionId = getUserSessionId(parameterMap);
        List<IDownloadItemId> itemIds = getDownloadItemIds(parameterMap);
        Integer wishedNumberOfStreams = getInteger(parameterMap, "wishedNumberOfStreams");
        DownloadPreferences preferences = wishedNumberOfStreams == null ? new DownloadPreferences() : new DownloadPreferences(wishedNumberOfStreams);
        DownloadSession downloadSession = downloadServer.startDownloadSession(userSessionId, itemIds, preferences);
        response.setContentType("application/json");
        JsonGenerator jsonGenerator = jsonFactory.createGenerator(response.getWriter());
        jsonGenerator.setPrettyPrinter(new DefaultPrettyPrinter());
        jsonGenerator.writeStartObject();
        jsonGenerator.writeObjectField("downloadSessionId", downloadSession.getDownloadSessionId().getId());
        jsonGenerator.writeObjectFieldStart("ranges");
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

    private void handleQueue(Map<String, String[]> parameterMap, HttpServletResponse response) throws ServletException
    {
        DownloadSessionId downloadSessionId = getDownloadSessionId(parameterMap);
        List<DownloadRange> ranges = new ArrayList<>();
        for (String range : getParameters(parameterMap, "ranges"))
        {
            try
            {
                String[] splitted = range.split(":");
                int start = Integer.parseInt(splitted[0]);
                int end = splitted.length == 1 ? start : Integer.parseInt(splitted[1]);
                ranges.add(new DownloadRange(start, end));
            } catch (NumberFormatException e)
            {
                throw new ServletException("Invalid range in parameter 'ranges': " + range);
            }
        }
        downloadServer.queue(downloadSessionId, ranges);
    }

    private void handleDownload(Map<String, String[]> parameterMap, HttpServletResponse response) throws ServletException, IOException
    {
        DownloadSessionId downloadSessionId = getDownloadSessionId(parameterMap);
        DownloadStreamId streamId = new DownloadStreamId();
        ClassUtils.setFieldValue(streamId, "id", getParameters(parameterMap, "downloadStreamId").get(0));
        Integer numberOfChunksOrNull = getInteger(parameterMap, "numberOfChunks");
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
        return new UserSessionId(getParameters(parameterMap, "userSessionId").get(0));
    }

    private List<IDownloadItemId> getDownloadItemIds(Map<String, String[]> parameterMap) throws ServletException
    {
        return getParameters(parameterMap, "downloadItemIds").stream().map(DownloadItemId::new).collect(Collectors.toList());
    }

    private DownloadSessionId getDownloadSessionId(Map<String, String[]> parameterMap) throws ServletException
    {
        DownloadSessionId downloadSessionId = new DownloadSessionId();
        ClassUtils.setFieldValue(downloadSessionId, "id", getParameters(parameterMap, "downloadSessionId").get(0));
        return downloadSessionId;
    }

    private List<String> getParameters(Map<String, String[]> parameterMap, String parameterName) throws ServletException
    {
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

    private Integer getInteger(Map<String, String[]> parameterMap, String parameterName) throws ServletException
    {
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

    private final class SessionTokenBasedSessionManager implements IUserSessionManager
    {
        private OpenbisSessionTokenCache sessionTokenCache;

        private SessionTokenBasedSessionManager(OpenbisSessionTokenCache sessionTokenCache)
        {
            this.sessionTokenCache = sessionTokenCache;
        }

        @Override
        public void validateDuringDownload(IUserSessionId userSessionId) throws InvalidUserSessionException
        {
            validateBeforeDownload(userSessionId);
        }

        @Override
        public void validateBeforeDownload(IUserSessionId userSessionId) throws InvalidUserSessionException
        {
            if (sessionTokenCache.isValidSessionToken(userSessionId.getId()) == false)
            {
                throw new InvalidUserSessionException(userSessionId);
            }
        }
    }

    private final class ConcurrencyProvider implements IConcurrencyProvider
    {
        private final Properties properties;
        
        private ConcurrencyProvider(Properties properties)
        {
            this.properties = properties;
        }
        
        @Override
        public int getAllowedNumberOfStreams(IUserSessionId userSessionId, Integer wishedNumberOfStreams, List<DownloadState> downloadStates)
                throws DownloadException
        {
            return PropertyUtils.getInt(properties, "fast-download.allowed-number-of-streams", 1);
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
