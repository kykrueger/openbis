/*
 * Copyright 2017 ETH Zuerich, SIS
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

package ch.ethz.sis.openbis.generic.server.dss.plugins.sync.common.entitygraph;

import java.util.Map;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.Space;

/**
 * @author Ganime Betul Akin
 */
public class SkinnyNode implements INode
{
    private final String permId;

    private final String entityKind;

    private final String identifier;

    private final String typeCodeOrNull;

    private final Space space;

    private final String code;

    public Space getSpace()
    {
        return space;
    }

    public SkinnyNode(String permId, String entityKind, String identifier, String typeCodeOrNull, Space space, String code)
    {
        this.permId = permId;
        this.entityKind = entityKind;
        this.identifier = identifier;
        this.typeCodeOrNull = typeCodeOrNull;
        this.space = space;
        this.code = code;
    }
    @Override
    public String getPermId()
    {
        return permId;
    }

    @Override
    public String getEntityKind()
    {
        return entityKind;
    }

    @Override
    public String getIdentifier()
    {
        return identifier;
    }

    @Override
    public String getTypeCodeOrNull()
    {
        return typeCodeOrNull;
    }

    @Override
    public String getCode()
    {
        return this.code;
    }

    @Override
    public Map<String, String> getPropertiesOrNull()
    {
        return null;
        //throw new UnsupportedOperationException();
    }

    @Override
    public void addConnection(EdgeNodePair enPair)
    {
        //do nothing
        //throw new UnsupportedOperationException();
    }

}
