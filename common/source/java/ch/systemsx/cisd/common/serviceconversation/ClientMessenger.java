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

package ch.systemsx.cisd.common.serviceconversation;

import java.io.Serializable;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.base.exceptions.TimeoutExceptionUnchecked;

/**
 * A class that a client can use to receive messages from a service.
 * 
 * @author Bernd Rinn
 */
class ClientMessenger implements IServiceConversation
{
    private final IServiceMessageTransport transportToService;

    private final String serviceConversationId;

    private final ClientResponseMessageQueue responseMessageQueue;

    private final ClientResponseMessageMultiplexer responseMessageMultiplexer;

    private int timeoutMillis;

    private int outgoingMessageIdx;

    ClientMessenger(ServiceConversationDTO serviceConversationDTO,
            IServiceMessageTransport transportToService,
            ClientResponseMessageQueue responseMessageQueue,
            ClientResponseMessageMultiplexer responseMessageMultiplexer)
    {
        assert transportToService != null;
        this.serviceConversationId = serviceConversationDTO.getServiceConversationId();
        assert serviceConversationId != null;
        this.timeoutMillis = serviceConversationDTO.getClientTimeoutInMillis();
        this.transportToService = transportToService;
        this.responseMessageQueue = responseMessageQueue;
        this.responseMessageMultiplexer = responseMessageMultiplexer;
        responseMessageMultiplexer.addConversation(serviceConversationId, responseMessageQueue);
    }

    public void send(Serializable message)
    {
        transportToService.send(new ServiceMessage(serviceConversationId,
                nextOutgoingMessageIndex(), false, message));
    }

    public void terminate()
    {
        transportToService.send(ServiceMessage.terminate(serviceConversationId));
        
    }

    private int nextOutgoingMessageIndex()
    {
        return outgoingMessageIdx++;
    }

    public <T extends Serializable> T receive(Class<T> messageClass)
    {
        try
        {
            return handleMessage(responseMessageQueue.poll(timeoutMillis), messageClass);
        } catch (InterruptedException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T handleMessage(ServiceMessage message, Class<T> messageClass)
    {
        if (message == null)
        {
            final TimeoutExceptionUnchecked exception =
                    new TimeoutExceptionUnchecked("Timeout while waiting on message from service.");
            final String exceptionDescription =
                    ServiceExecutionException.getDescriptionFromException(exception);
            transportToService.send(new ServiceMessage(serviceConversationId,
                    nextOutgoingMessageIndex(), true, exceptionDescription));
            throw exception;
        }
        if (message.isException())
        {
            throw new ServiceExecutionException(message.getConversationId(),
                    message.tryGetExceptionDescription());
        }
        final Object payload = message.getPayload();
        if (messageClass != null && messageClass.isAssignableFrom(payload.getClass()) == false)
        {
            throw new UnexpectedMessagePayloadException(payload.getClass(), messageClass);
        }
        return (T) payload;
    }

    public String getId()
    {
        return serviceConversationId;
    }

    public void close()
    {
        responseMessageMultiplexer.removeConversation(serviceConversationId);
    }

    @Override
    protected void finalize() throws Throwable
    {
        close();
        super.finalize();
    }

}
