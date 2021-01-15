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
import ch.ethz.sis.microservices.download.server.logging.log4j.Log4J2LogFactory;
import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.Person;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.fetchoptions.PersonFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.search.PersonSearchCriteria;
import ch.systemsx.cisd.common.http.JettyHttpClientFactory;
import ch.systemsx.cisd.common.spring.HttpInvokerUtils;
import ch.systemsx.cisd.openbis.BuildAndEnvironmentInfo;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.BytesContentProvider;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static ch.ethz.sis.microservices.download.server.services.store.StatisticsReceiverHandler.DATE_FORMAT;

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
        final IApplicationServerApi applicationServerApi = HttpInvokerUtils.createServiceStub(
                IApplicationServerApi.class,
                "http://localhost:8888/openbis/openbis/rmi-application-server-v3", timeout);
        final String sessionToken = applicationServerApi.login("admin", "admin");

        final PersonSearchCriteria personSearchCriteria = new PersonSearchCriteria();
        final SearchResult<Person> personSearchResult = applicationServerApi.searchPersons(sessionToken,
                personSearchCriteria, new PersonFetchOptions());
        final long personsCount = personSearchResult.getObjects().stream().filter(Person::isActive).count();

        final Map<StatisticsKeys, String> statisticsMap = new HashMap<>(5);
        statisticsMap.put(StatisticsKeys.SERVER_ID, "01-23-45-67-89-AB");
        statisticsMap.put(StatisticsKeys.SUBMISSION_TIMESTAMP, DATE_FORMAT.format(new Date()));
        statisticsMap.put(StatisticsKeys.USERS_COUNT, String.valueOf(personsCount));
        statisticsMap.put(StatisticsKeys.COUNTRY_CODE, "CH");
        statisticsMap.put(StatisticsKeys.OPENBIS_VERSION, BuildAndEnvironmentInfo.INSTANCE.getVersion());

        final byte[] body = JacksonObjectMapper.getInstance().writeValue(statisticsMap);
        final long start = System.currentTimeMillis();

        final Request request = JettyHttpClientFactory.getHttpClient().POST("http://localhost:8080/statistics")
                .content(new BytesContentProvider(body));
        final byte[] response;
        try
        {
            response = request.send().getContent();
        } catch (InterruptedException | TimeoutException | ExecutionException e)
        {
            throw new RuntimeException("Error sending request.", e);
        }

        final long end = System.currentTimeMillis();
        System.out.println("Response Size: " + response.length);
        System.out.println("Time: " + (end - start) + " ms");
        System.out.println("Response: " + new String(response));
    }

}
