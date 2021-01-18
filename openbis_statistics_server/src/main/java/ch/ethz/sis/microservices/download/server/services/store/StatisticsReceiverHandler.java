/*
 * Copyright 2016 ETH Zuerich, CISD
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

package ch.ethz.sis.microservices.download.server.services.store;

import ch.ethz.sis.microservices.download.server.json.jackson.JacksonObjectMapper;
import ch.ethz.sis.microservices.download.server.logging.LogManager;
import ch.ethz.sis.microservices.download.server.logging.Logger;
import ch.ethz.sis.microservices.download.server.services.Service;
import com.fasterxml.jackson.core.type.TypeReference;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StatisticsReceiverHandler extends Service
{

    private static final Logger LOGGER = LogManager.getLogger(StatisticsReceiverHandler.class);

    static final SimpleDateFormat TIMESTAMP_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");

    static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    private static final String FILE_SEPARATOR = System.getProperty("file.separator");

    private static final String STATISTICS_FILE_SUFFIX = "-statistics.csv";

    private static final String FLAGGED_STATISTICS_FILE_SUFFIX = "-statistics-flagged.csv";

    private static final String STATISTICS_FOLDER_PATH_PARAM = "statisticsFolderPath";

    protected void doAction(final HttpServletRequest request, final HttpServletResponse response)
            throws ServletException, IOException
    {
        final String todaysDate = DATE_FORMAT.format(new Date());
        final String statisticsFolderParam = this.getServiceConfig().getParameters().get(STATISTICS_FOLDER_PATH_PARAM);
        final String statisticsFilePath = statisticsFolderParam + FILE_SEPARATOR + todaysDate + STATISTICS_FILE_SUFFIX;
        final String flaggedStatisticsFilePath = statisticsFolderParam + FILE_SEPARATOR + todaysDate
                + FLAGGED_STATISTICS_FILE_SUFFIX;

        try
        {
            final Map<StatisticsKeys, String> statisticsMap = JacksonObjectMapper.getInstance().readValue(
                    request.getInputStream(), new TypeReference<HashMap<StatisticsKeys, String>>() {});

            final String serverId = statisticsMap.get(StatisticsKeys.SERVER_ID);
            final Date submissionTimestamp = TIMESTAMP_FORMAT.parse(
                    statisticsMap.get(StatisticsKeys.SUBMISSION_TIMESTAMP));
            final Integer usersCount = Integer.valueOf(statisticsMap.get(StatisticsKeys.USERS_COUNT));
            final String countryCode = statisticsMap.get(StatisticsKeys.COUNTRY_CODE);
            final String openbisVersion = statisticsMap.get(StatisticsKeys.OPENBIS_VERSION);

            LOGGER.info(String.format("Received following data. [serverId='%s', submissionTimestamp='%s', " +
                            "usersCount=%d, countryCode='%s', openbisVersion='%s']",
                    serverId, TIMESTAMP_FORMAT.format(submissionTimestamp), usersCount, countryCode, openbisVersion));

            final String csvLine = convertToCSV(serverId, TIMESTAMP_FORMAT.format(new Date()),
                    String.valueOf(usersCount), countryCode, openbisVersion);

            final File statisticsFile = new File(statisticsFilePath);
            statisticsFile.getParentFile().mkdirs();
            statisticsFile.createNewFile();
            try (final PrintWriter pw = new PrintWriter(new FileOutputStream(statisticsFile, true)))
            {
                pw.println(csvLine);
            }

            success(response);
        } catch (final Exception ex)
        {
            LOGGER.catching(ex);
            failure(response);
        }
    }

    private static String convertToCSV(String... data) {
        return Stream.of(data)
                .map(StatisticsReceiverHandler::escapeSpecialCharacters)
                .collect(Collectors.joining(","));
    }

    private static String escapeSpecialCharacters(String data) {
        String escapedData = data.replaceAll("\\R", " ");
        if (data.contains(",") || data.contains("\"") || data.contains("'")) {
            data = data.replace("\"", "\"\"");
            escapedData = "\"" + data + "\"";
        }
        return escapedData;
    }

    private static <T> T getParameterOfType(final HttpServletRequest request, final String parameterName,
            final Function<String, T> valueOf)
    {
        final String stringParameter = request.getParameter(parameterName);
        return stringParameter != null ? valueOf.apply(stringParameter) : null;
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException,
            IOException
    {
        doAction(request, response);
    }

    protected void writeOutput(HttpServletResponse response, int httpResponseCode, boolean success)
            throws IOException
    {
        byte[] resultAsBytes = null;
        try
        {
            Map<String, String> result = new HashMap<>();
            result.put("success", Boolean.toString(success));
            resultAsBytes = JacksonObjectMapper.getInstance().writeValue(result);
        } catch (Exception ex)
        {
            LOGGER.catching(ex);
        }

        response.setContentType("application/json; charset=utf-8");
        response.getOutputStream().write(resultAsBytes);
        response.setStatus(httpResponseCode);
    }

    protected void success(final HttpServletResponse response) throws ServletException, IOException
    {
        response.setStatus(HttpServletResponse.SC_OK);
    }

    protected void failure(final HttpServletResponse response) throws ServletException, IOException
    {
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }

}
