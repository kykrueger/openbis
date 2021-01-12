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

import ch.ethz.sis.microservices.download.server.logging.LogManager;
import ch.ethz.sis.microservices.download.server.logging.log4j.Log4J2LogFactory;
import ch.ethz.sis.microservices.download.server.startup.HttpClient;
import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.systemsx.cisd.common.spring.HttpInvokerUtils;

import java.util.HashMap;
import java.util.Map;

import static ch.ethz.sis.microservices.download.server.services.store.StatisticsReceiverHandler.*;

public class StatisticsReceiverHandlerTest
{

    static
    {
        // Configuring Logging
        LogManager.setLogFactory(new Log4J2LogFactory());
    }

    public static void main(String[] args) throws Exception
    {
        // Obtain session token from openBIS
        final int timeout = 10000;
        final IApplicationServerApi v3As = HttpInvokerUtils.createServiceStub(IApplicationServerApi.class,
                "http://localhost:8888/openbis/openbis/rmi-application-server-v3", timeout);
        final String sessionToken = v3As.login("admin", "admin");

        final Map<String, String> parameters = new HashMap<>();
        parameters.put("sessionToken", sessionToken);
        parameters.put(SERVER_ID_PARAM, "01-23-45-67-89-AB");
        parameters.put(SUBMISSION_TIMESTAMP_PARAM, String.valueOf(System.currentTimeMillis()));
        parameters.put(TOTAL_USERS_COUNT_PARAM, String.valueOf(20));
        parameters.put(ACTIVE_USERS_COUNT_PARAM, String.valueOf(10));
        parameters.put(IP_ADDRESS_PARAM, "127.0.0.1");
        parameters.put(GEOLOCATION_PARAM, "0.0,0.0");

        final long start = System.currentTimeMillis();
        final byte[] response = HttpClient.doPost("http://localhost:8080/statistics", parameters);
        final long end = System.currentTimeMillis();
        System.out.println("Response Size: " + response.length);
        System.out.println("Time: " + (end - start) + " ms");
        System.out.println("Response: " + new String(response));
    }
}
