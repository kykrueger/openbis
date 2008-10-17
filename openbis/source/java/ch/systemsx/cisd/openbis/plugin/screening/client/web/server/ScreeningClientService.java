/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.plugin.screening.client.web.server;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Sample;
import ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException;
import ch.systemsx.cisd.openbis.generic.client.web.server.util.UserFailureExceptionTranslater;
import ch.systemsx.cisd.openbis.generic.shared.IServer;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifierFactory;
import ch.systemsx.cisd.openbis.plugin.AbstractClientService;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.IScreeningClientService;
import ch.systemsx.cisd.openbis.plugin.screening.shared.IScreeningServer;

/**
 * The {@link IScreeningClientService} implementation.
 * 
 * @author Christian Ribeaud
 */
@Component(value = ScreeningConstants.SCREENING_SERVICE)
public final class ScreeningClientService extends AbstractClientService implements
        IScreeningClientService
{

    @Resource(name = ScreeningConstants.SCREENING_SERVER)
    private IScreeningServer screeningServer;

    @Override
    protected final IServer getServer()
    {
        return screeningServer;
    }

    //
    // IScreeningClientService
    //

    public final Sample getSampleInfo(final String sampleIdentifier) throws UserFailureException
    {
        try
        {
            final String sessionToken = getSessionToken();
            final SampleIdentifier identifier = SampleIdentifierFactory.parse(sampleIdentifier);
            final SamplePE samplePE = screeningServer.getSampleInfo(sessionToken, identifier);
            final Sample sample = new Sample();
            sample.setCode(samplePE.getCode());
            return sample;
        } catch (final ch.systemsx.cisd.common.exceptions.UserFailureException e)
        {
            throw UserFailureExceptionTranslater.translate(e);
        }
    }
}
