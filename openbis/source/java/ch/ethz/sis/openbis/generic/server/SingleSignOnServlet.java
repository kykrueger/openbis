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

package ch.ethz.sis.openbis.generic.server;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import ch.systemsx.cisd.authentication.IPrincipalProvider;
import ch.systemsx.cisd.authentication.Principal;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.spring.ExposablePropertyPlaceholderConfigurer;
import ch.systemsx.cisd.openbis.generic.client.web.server.AbstractServlet;
import ch.systemsx.cisd.openbis.generic.server.ComponentNames;
import ch.systemsx.cisd.openbis.generic.shared.IOpenBisSessionManager;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;

/**
 * @author Franz-Josef Elmer
 */
@Controller
public class SingleSignOnServlet extends AbstractServlet
{
    public static final String SERVLET_NAME = "ssos";

    public static final String SESSION_ID_KEY = "session-id-key";

    public static final String DEFAULT_SESSION_ID_KEY = "Shib-Session-ID";

    public static final String USER_ID_KEY = "user-id-key";

    public static final String DEFAULT_USER_ID_KEY = "mail";

    public static final String FIRST_NAME_KEY = "first-name-key";

    public static final String DEFAULT_FIRST_NAME_KEY = "givenName";

    public static final String LAST_NAME_KEY = "last-name-key";

    public static final String DEFAULT_LAST_NAME_KEY = "surname";

    public static final String EMAIL_KEY = "email-key";

    public static final String DEFAULT_EMAIL_KEY = "mail";
    
    public static final String REDIRECT_URL_KEY = "redirect-url";
    
    public static final String DEFAULT_REDIRECT_URL = "webapp/eln-lims";

    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION, SingleSignOnServlet.class);

    @Resource(name = ExposablePropertyPlaceholderConfigurer.PROPERTY_CONFIGURER_BEAN_NAME)
    private ExposablePropertyPlaceholderConfigurer configurer;

    @Resource(name = ComponentNames.SESSION_MANAGER)
    private IOpenBisSessionManager sessionManager;
    
    private final Map<String, String> sessionTokenBySessionId = new HashMap<>();

    @Override
    @RequestMapping({ SERVLET_NAME })
    protected void respondToRequest(HttpServletRequest request, HttpServletResponse response) throws Exception, IOException
    {
        String sessionId = getHeader(request, SESSION_ID_KEY, DEFAULT_SESSION_ID_KEY);
        String sessionToken = sessionTokenBySessionId.get(sessionId);
        if (sessionToken != null)
        {
            Session session = sessionManager.tryGetSession(sessionToken);
            if (session != null)
            {
                return;
            }
        }
        String userId = getHeader(request, USER_ID_KEY, DEFAULT_USER_ID_KEY);
        String firstName = getHeader(request, FIRST_NAME_KEY, DEFAULT_FIRST_NAME_KEY);
        String lastName = getHeader(request, LAST_NAME_KEY, DEFAULT_LAST_NAME_KEY);
        String email = getHeader(request, EMAIL_KEY, DEFAULT_EMAIL_KEY);
        Principal principal = new Principal(userId, firstName, lastName, email, true);
        sessionToken = sessionManager.tryToOpenSession(userId, new IPrincipalProvider()
            {
                
                @Override
                public Principal tryToGetPrincipal(String userID)
                {
                    return principal;
                }
            });
        sessionTokenBySessionId.put(sessionId, sessionToken);
        operationLog.info("Session " + sessionToken + " created for session id " + sessionId);
        response.sendRedirect(configurer.getResolvedProps().getProperty(REDIRECT_URL_KEY, DEFAULT_REDIRECT_URL));
    }

    private String getHeader(HttpServletRequest request, String keyProperty, String defaultKey)
    {
        String key = configurer.getResolvedProps().getProperty(keyProperty, defaultKey);
        String header = request.getHeader(key);
        if (header != null)
        {
            return header;
        }
        throw new IllegalArgumentException("Missing header '" + key + "'.");
    }

}
