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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class StatisticsReceiverHandler extends Service
{

    private static final Logger LOGGER = LogManager.getLogger(StatisticsReceiverHandler.class);

    static final String SERVER_ID_PARAM = "serverId";

    static final String SUBMISSION_TIMESTAMP_PARAM = "submissionTimestamp";

    static final String TOTAL_USERS_COUNT_PARAM = "totalUsersCount";

    static final String ACTIVE_USERS_COUNT_PARAM = "activeUsersCount";

    static final String IP_ADDRESS_PARAM = "ipAddress";

    static final String GEOLOCATION_PARAM = "geolocation";

    protected void doAction(final HttpServletRequest request, final HttpServletResponse response)
            throws ServletException, IOException
    {
//        // Service Configuration
//        final String openbisURL = this.getServiceConfig().getParameters().get("openbis-url");
//        final String datastoreURL = this.getServiceConfig().getParameters().get("datastore-url");
//        final int servicesTimeout = Integer.parseInt(this.getServiceConfig().getParameters().get("services-timeout"));

        // Request parameters
        try
        {
            final String serverId = request.getParameter(SERVER_ID_PARAM);
            final Long submissionTimestamp = getParameterOfType(request, SUBMISSION_TIMESTAMP_PARAM, Long::valueOf);
            final Integer totalUsersCount = getParameterOfType(request, TOTAL_USERS_COUNT_PARAM, Integer::valueOf);
            final Integer activeUsersCount = getParameterOfType(request, ACTIVE_USERS_COUNT_PARAM, Integer::valueOf);
            final String ipAddress = request.getParameter(IP_ADDRESS_PARAM);
            final String geolocation = request.getParameter(GEOLOCATION_PARAM);

            LOGGER.info(String.format("Received following data. [serverId=%s, submissionTimestamp=%d, " +
                            "totalUsersCount=%d, activeUsersCount=%d, ipAddress=%s, geolocation=%s]",
                    serverId, submissionTimestamp, totalUsersCount, activeUsersCount, ipAddress, geolocation));
            success(response);
        } catch (final Exception ex)
        {
            LOGGER.catching(ex);
            failure(response);
        }
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
        writeOutput(response, HttpServletResponse.SC_OK, true);
    }

    protected void failure(final HttpServletResponse response) throws ServletException, IOException
    {
        writeOutput(response, HttpServletResponse.SC_OK, false);
    }

}
