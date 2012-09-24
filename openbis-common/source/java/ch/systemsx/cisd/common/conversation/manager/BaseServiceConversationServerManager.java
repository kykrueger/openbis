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

package ch.systemsx.cisd.common.conversation.manager;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.InitializingBean;

import ch.systemsx.cisd.common.conversation.client.ServiceConversationClientDetails;
import ch.systemsx.cisd.common.serviceconversation.ServiceConversationDTO;
import ch.systemsx.cisd.common.serviceconversation.ServiceMessage;
import ch.systemsx.cisd.common.serviceconversation.UnknownServiceConversationException;
import ch.systemsx.cisd.common.serviceconversation.server.ServiceConversationServer;
import ch.systemsx.cisd.common.spring.HttpInvokerUtils;

/**
 * @author pkupczyk
 */
public abstract class BaseServiceConversationServerManager implements
        IServiceConversationServerManagerRemote, InitializingBean
{

    private ServiceConversationServer server;

    private Map<ServiceConversationClientDetails, IServiceConversationClientManagerRemote> clientDetailsToClientMap =
            new HashMap<ServiceConversationClientDetails, IServiceConversationClientManagerRemote>();

    private Map<String, ServiceConversationClientDetails> conversationIdToClientDetailsMap =
            new HashMap<String, ServiceConversationClientDetails>();

    public BaseServiceConversationServerManager()
    {
        server = new ServiceConversationServer();
    }

    @Override
    public void afterPropertiesSet() throws Exception
    {
        initializeServices();
    }

    protected abstract void initializeServices();

    protected void addService(String serviceName, Object service)
    {
        ServiceConversationServiceFactory serviceFactory =
                new ServiceConversationServiceFactory(server, serviceName, service)
                    {
                        @Override
                        protected int getProgressInterval(String conversationId)
                        {
                            ServiceConversationClientDetails clientDetails =
                                    conversationIdToClientDetailsMap.get(conversationId);

                            if (clientDetails == null)
                            {
                                throw new UnknownServiceConversationException(
                                        String.format(
                                                "Tried to report progress for an unknown service conversation '%s'",
                                                conversationId));
                            } else
                            {
                                // try to report progress 10 times within the timeout time,
                                // but keep the interval in range(1, 60) seconds
                                return Math.min(Math.max(clientDetails.getTimeout() / 10, 1000),
                                        60 * 1000);
                            }
                        }

                        @Override
                        protected void onConversationFinish(String conversationId)
                        {
                            conversationIdToClientDetailsMap.remove(conversationId);
                        }
                    };
        server.addServiceType(serviceFactory);
    }

    @Override
    public ServiceConversationDTO startConversation(String sessionToken, String serviceName,
            Object clientId)
    {
        ServiceConversationClientDetails clientDetails = getClientDetailsForClientId(clientId);

        if (clientDetails == null)
        {
            throw new IllegalArgumentException(
                    "Received a service conversation request from an unknown client (" + clientId
                            + ")");
        }

        server.addClientResponseTransport(sessionToken, getClientForClientDetails(clientDetails));
        ServiceConversationDTO conversation = server.startConversation(serviceName, sessionToken);
        conversationIdToClientDetailsMap
                .put(conversation.getServiceConversationId(), clientDetails);

        return conversation;
    }

    @Override
    public void send(ServiceMessage message)
    {
        server.getIncomingMessageTransport().send(message);
    }

    @Override
    public void ping()
    {
    }

    protected abstract ServiceConversationClientDetails getClientDetailsForClientId(Object clientId);

    private synchronized IServiceConversationClientManagerRemote getClientForClientDetails(
            ServiceConversationClientDetails clientDetails)
    {
        IServiceConversationClientManagerRemote client =
                clientDetailsToClientMap.get(clientDetails);

        if (client == null)
        {
            client =
                    HttpInvokerUtils.createServiceStub(
                            IServiceConversationClientManagerRemote.class, clientDetails.getUrl(),
                            clientDetails.getTimeout());
            clientDetailsToClientMap.put(clientDetails, client);
        }

        return client;
    }

}
