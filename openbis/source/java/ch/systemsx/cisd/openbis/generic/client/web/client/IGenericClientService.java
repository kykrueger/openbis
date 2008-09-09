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

package ch.systemsx.cisd.openbis.generic.client.web.client;

import com.google.gwt.user.client.rpc.RemoteService;

import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ApplicationInfo;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SessionContext;

/**
 * Service interface for the generic GWT client.
 *
 * @author Franz-Josef Elmer
 */
public interface IGenericClientService extends RemoteService
{
    /**
     * Returns static information of the application needed by the client.
     */
    public ApplicationInfo getApplicationInfo();
    
    /**
     * Tries to return the current session context. If failed <code>null</code> is returned.
     */
    public SessionContext tryToGetCurrentSessionContext();
    
    /**
     * Tries to login with specified user ID and password. If failed <code>null</code> is returned.
     */
    public SessionContext tryToLogin(String userID, String password);
    
    /**
     * Logs out. 
     */
    public void logout();
}
