/*
 * Copyright 2018 ETH Zuerich, SIS
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

package ch.ethz.sis.openbis.generic.server.dss.plugins.sync.harvester.synchronizer;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ch.ethz.sis.openbis.generic.server.dss.plugins.sync.common.SyncEntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Identifier;

public class IncomingEntity<T extends Identifier<T>> extends AbstractTimestampsAndUserHolder
{
    private final Identifier<T> entity;

    private final SyncEntityKind entityKind;

    private List<Connection> connections = new ArrayList<Connection>();

    private boolean hasAttachments;

    public List<Connection> getConnections()
    {
        return connections;
    }

    void addConnection(Connection conn)
    {
        this.connections.add(conn);
    }

    public SyncEntityKind getEntityKind()
    {
        return entityKind;
    }

    public void setConnections(List<Connection> conns)
    {
        // TODO do this better
        this.connections = conns;
    }

    public boolean hasAttachments()
    {
        return hasAttachments;
    }

    public void setHasAttachments(boolean hasAttachments)
    {
        this.hasAttachments = hasAttachments;
    }

    public Identifier<T> getEntity()
    {
        return entity;
    }

    public String getIdentifer()
    {
        return getEntity().getIdentifier();
    }

    public String getPermID()
    {
        return getEntity().getPermID();
    }

    public Date getLastModificationDate()
    {
        return lastModificationDate;
    }

    private final Date lastModificationDate;

    IncomingEntity(Identifier<T> entity, SyncEntityKind entityKind, Date lastModDate)
    {
        this.entity = entity;
        this.entityKind = entityKind;
        this.lastModificationDate = lastModDate;
    }
}