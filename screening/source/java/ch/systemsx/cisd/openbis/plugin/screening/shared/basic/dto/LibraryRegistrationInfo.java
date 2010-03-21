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

package ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto;

import java.io.Serializable;

import com.google.gwt.user.client.rpc.IsSerializable;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ServiceVersionHolder;

/**
 * Keeps the details necessary to register new library.
 * 
 * @author Izabela Adamczyk
 */
public class LibraryRegistrationInfo implements IsSerializable, Serializable
{
    public enum RegistrationScope implements IsSerializable
    {
        PLATES, OLIGOS_PLATES, GENES_OLIGOS_PLATES;

        public boolean isPlates()
        {
            return true;
        }

        public boolean isOligos()
        {
            return this.equals(OLIGOS_PLATES) || this.equals(GENES_OLIGOS_PLATES);
        }

        public boolean isGenes()
        {
            return this.equals(GENES_OLIGOS_PLATES);
        }

    }

    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    private String experiment;

    private String plateGeometry;

    private String sessionKey;

    private String userEmail;

    private RegistrationScope scope;

    public LibraryRegistrationInfo()
    {
    }

    public RegistrationScope getScope()
    {
        return scope;
    }

    public LibraryRegistrationInfo setScope(RegistrationScope scope)
    {
        this.scope = scope;
        return this;
    }

    public String getExperiment()
    {
        return experiment;
    }

    public String getPlateGeometry()
    {
        return plateGeometry;
    }

    public String getSessionKey()
    {
        return sessionKey;
    }

    public String getUserEmail()
    {
        return userEmail;
    }

    public LibraryRegistrationInfo setExperiment(String experiment)
    {
        this.experiment = experiment;
        return this;
    }

    public LibraryRegistrationInfo setPlateGeometry(String plateGeometry)
    {
        this.plateGeometry = plateGeometry;
        return this;
    }

    public LibraryRegistrationInfo setSessionKey(String sessionKey)
    {
        this.sessionKey = sessionKey;
        return this;
    }

    public LibraryRegistrationInfo setUserEmail(String userEmail)
    {
        this.userEmail = userEmail;
        return this;
    }

}
