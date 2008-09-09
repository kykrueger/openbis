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

package ch.systemsx.cisd.openbis.generic.shared.dto;

import org.apache.commons.lang.time.DateFormatUtils;

import ch.systemsx.cisd.authentication.BasicSession;
import ch.systemsx.cisd.authentication.Principal;
import ch.systemsx.cisd.lims.base.dto.PersonPE;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class Session extends BasicSession
{
    private static final long serialVersionUID = 1L;
    
    private PersonPE person;
    
    /**
     * Creates a new instance.
     */
    public Session(String sessionToken, String userName, Principal principal, String remoteHost,
            long sessionStart, int expirationTime)
    {
        super(sessionToken, userName, principal, remoteHost, sessionStart, expirationTime);
    }

    public final PersonPE tryToGetPerson()
    {
        return person;
    }

    public final void setPerson(PersonPE person)
    {
        this.person = person;
    }
    
    @Override
    public final String toString()
    {
        return "Session{user=" + getUserName() + ",group=" + person.getHomeGroup() + ",remoteHost="
                + getRemoteHost() + ",sessionstart="
                + DateFormatUtils.format(getSessionStart(), DATE_FORMAT_PATTERN) + "}";
    }


}
