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

package ch.systemsx.cisd.datamover.console.client;

import java.util.List;
import java.util.Map;

import com.google.gwt.user.client.rpc.RemoteService;

import ch.systemsx.cisd.datamover.console.client.dto.ApplicationInfo;
import ch.systemsx.cisd.datamover.console.client.dto.DatamoverInfo;
import ch.systemsx.cisd.datamover.console.client.dto.User;

/**
 * Service interface used by the client.
 *
 * @author Franz-Josef Elmer
 */
public interface IDatamoverConsoleService extends RemoteService
{
    /**
     * Returns static information of the application needed by the client.
     */
    public ApplicationInfo getApplicationInfo();

    /**
     * Returns the currently logged-in {@link User}, or <code>null</code> if the user is not logged-in.
     */
    public User tryToGetCurrentUser() throws UserFailureException;

    /**
     * Authenticates the specified user with the specified password.
     * 
     * @return a {@link User} if the login was successful, <code>null</code> otherwise.
     */
    public User tryToLogin(final String user, final String password) throws UserFailureException;

    /**
     * Logout the current user.
     */
    public void logout();

    /**
     * Lists information of all datamovers.
     */
    public List<DatamoverInfo> listDatamoverInfos() throws UserFailureException;

    /**
     * Gets map of target symbol names to target locations.
     */
    public Map<String, String> getTargets() throws UserFailureException;

    /**
     * Starts the specified datamover using the specified location as the outgoing target.
     */
    public void startDatamover(String datamoverName, String targetLocation) throws UserFailureException;

    /**
     * Shuts down the specified datamover.
     */
    public void shutdownDatamover(String datamoverName) throws UserFailureException;

}
