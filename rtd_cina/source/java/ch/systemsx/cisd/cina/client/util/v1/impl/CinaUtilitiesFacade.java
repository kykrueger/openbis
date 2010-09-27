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

package ch.systemsx.cisd.cina.client.util.v1.impl;

import java.util.List;

import org.springframework.remoting.RemoteConnectFailureException;

import ch.systemsx.cisd.cina.client.util.v1.ICinaUtilities;
import ch.systemsx.cisd.common.api.client.ServiceFinder;
import ch.systemsx.cisd.common.exceptions.AuthorizationFailureException;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.IGeneralInformationService;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public class CinaUtilitiesFacade implements ICinaUtilities
{
    /**
     * Public factory method for creating a DssComponent with a username and password.
     * 
     * @param user The user name
     * @param password The user's password
     * @param openBISUrl The URL to openBIS
     */
    public static CinaUtilitiesFacade tryCreate(String user, String password, String openBISUrl)
    {
        CinaUtilitiesFacade component = new CinaUtilitiesFacade(openBISUrl, null);
        try
        {
            component.login(user, password);
        } catch (AuthorizationFailureException e)
        {
            // User name / Password is incorrect.
            return null;
        }
        return component;
    }

    /**
     * Public factory method for creating a DssComponent for a user that has already been
     * authenticated.
     * 
     * @param sessionToken The session token provided by authentication
     * @param openBISUrl The URL to openBIS
     */
    public static CinaUtilitiesFacade tryCreate(String sessionToken, String openBISUrl)
    {
        CinaUtilitiesFacade component = new CinaUtilitiesFacade(openBISUrl, sessionToken);
        return component;
    }

    private static IGeneralInformationService createGeneralInformationService(String openBISURL)
    {
        ServiceFinder generalInformationServiceFinder =
                new ServiceFinder("openbis", IGeneralInformationService.SERVICE_URL);
        IGeneralInformationService service =
                generalInformationServiceFinder.createService(IGeneralInformationService.class,
                        openBISURL);
        return service;
    }

    /** The interface for accessing the remote services. */
    private final IGeneralInformationService generalInformationService;

    /** The current state of the facade */
    private AbstractCinaFacadeState state;

    /**
     * Create a DSS component that connects to the openBIS instance specified by the URL.
     * <p>
     * The DSS component needs to connect to openBIS to find out which DSS manages a given data set.
     * Once it has a connection to openBIS, it can figure out how to connect to DSS servers itself.
     * 
     * @param openBISURL The url to connect to openBIS
     * @param sessionTokenOrNull A session token; If null is passed in, then login needs to be
     *            called.
     */
    private CinaUtilitiesFacade(String openBISURL, String sessionTokenOrNull)
    {
        this(createGeneralInformationService(openBISURL), sessionTokenOrNull);
    }

    /**
     * Internal constructor, also used for testing.
     * 
     * @param generalInformationService A proxy to the openBIS application server's general
     *            information service
     * @param sessionTokenOrNull A session token, if the user has already logged in, or null
     *            otherwise.
     */
    protected CinaUtilitiesFacade(IGeneralInformationService generalInformationService,
            String sessionTokenOrNull)

    {
        this.generalInformationService = generalInformationService;
        if (sessionTokenOrNull == null)
        {
            this.state = new UnauthenticatedState(generalInformationService);
        } else
        {
            this.state = new AuthenticatedState(generalInformationService, sessionTokenOrNull);
        }
    }

    /**
     * Authenticates the <code>user</code> with given <code>password</code>.
     * 
     * @throws AuthorizationFailureException Thrown if the username / password do not authenticate.
     * @throws EnvironmentFailureException Thrown in cases where it is not possible to connect to
     *             the server.
     */
    void login(String user, String password) throws AuthorizationFailureException,
            EnvironmentFailureException
    {
        // login and transition to the authenticated state
        state.login(user, password);
        state = new AuthenticatedState(generalInformationService, state.getSessionToken());
    }

    public void logout()
    {
        // logout and transition to the unauthenticated state
        state.logout();
        state = new UnauthenticatedState(generalInformationService);
    }

    public String getSessionToken() throws IllegalStateException
    {
        return state.getSessionToken();
    }

    public List<Sample> searchForSamples(SearchCriteria searchCriteria)
            throws IllegalStateException, EnvironmentFailureException
    {
        return state.searchForSamples(searchCriteria);
    }
}

/**
 * Superclass for component states, which make the state machine of the Cina facade explicit.
 * <p>
 * By default, all methods just throw an exception. Subclasses should override the methods they
 * accept.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
abstract class AbstractCinaFacadeState implements ICinaUtilities
{
    protected final IGeneralInformationService service;

    AbstractCinaFacadeState(IGeneralInformationService service)
    {
        this.service = service;
    }

    public void checkSession() throws IllegalStateException
    {
        throw new IllegalStateException("Please log in");
    }

    public List<Sample> searchForSamples(SearchCriteria searchCriteria)
            throws IllegalStateException, EnvironmentFailureException
    {
        throw new IllegalStateException("Please log in");
    }

    /**
     * Authenticates the <code>user</code> with given <code>password</code>.
     * 
     * @throws AuthorizationFailureException Thrown if the username / password do not authenticate.
     * @throws EnvironmentFailureException Thrown in cases where it is not possible to connect to
     *             the server.
     */
    abstract void login(final String user, final String password)
            throws AuthorizationFailureException, EnvironmentFailureException;

    /**
     * Package visible method used to transfer context information between states.
     */
    public abstract String getSessionToken();
}

/**
 * An object representing an unauthenticated state. Being in this state implies that the user has
 * not yet logged in. Only login and logout are allowed in this state.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
class UnauthenticatedState extends AbstractCinaFacadeState
{
    private String sessionTokenOrNull;

    UnauthenticatedState(IGeneralInformationService service)
    {
        super(service);
    }

    @Override
    public String getSessionToken()
    {
        if (sessionTokenOrNull == null)
            throw new IllegalStateException("Please log in");
        return sessionTokenOrNull;
    }

    @Override
    void login(String user, String password) throws AuthorizationFailureException,
            EnvironmentFailureException
    {
        try
        {
            sessionTokenOrNull = service.tryToAuthenticateForAllServices(user, password);
        } catch (RemoteConnectFailureException e)
        {
            throw new EnvironmentFailureException("Could not connect to server", e);
        }
        if (sessionTokenOrNull == null)
            throw new AuthorizationFailureException("Login or Password invalid");
    }

    public void logout()
    {
        return;
    }
}

/**
 * An object representing an authenticated state. Being in this state means that the user has logged
 * in and all operations are available, except login.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
class AuthenticatedState extends AbstractCinaFacadeState
{
    private final String sessionToken;

    /**
     * @param service
     */
    AuthenticatedState(IGeneralInformationService service, String sessionToken)
    {
        super(service);
        this.sessionToken = sessionToken;
    }

    @Override
    void login(String user, String password) throws AuthorizationFailureException
    {
        throw new IllegalStateException("Already logged in.");
    }

    public void logout()
    {
        service.logout(getSessionToken());
    }

    @Override
    public List<Sample> searchForSamples(SearchCriteria searchCriteria)
            throws IllegalStateException, EnvironmentFailureException
    {
        // This functionality has only been supported since version 1.1
        int minorVersion = service.getMinorVersion();
        if (minorVersion < 1)
        {
            throw new EnvironmentFailureException("Server does not support this feature.");
        }

        return service.searchForSamples(sessionToken, searchCriteria);
    }

    @Override
    public String getSessionToken()
    {
        return sessionToken;
    }
}