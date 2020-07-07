/*
 * Copyright 2020 ETH Zuerich, SIS
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

package ch.ethz.sis.openbis.generic.asapi.v3.dto.common.update;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.Relationship;
import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * @author Franz-Josef Elmer
 */
@JsonObject("as.dto.common.update.RelationshipUpdate")
public class RelationshipUpdate implements Serializable
{
    private static final long serialVersionUID = 1L;

    private Relationship relationship = new Relationship();

    private Set<String> childAnnotationsToBeRemoved = new HashSet<>();

    private Set<String> parentAnnotationsToBeRemoved = new HashSet<>();

    @JsonIgnore
    public RelationshipUpdate addChildAnnotation(String key, String value)
    {
        relationship.addChildAnnotation(key, value);
        return this;
    }

    @JsonIgnore
    public RelationshipUpdate removeChildAnnotation(String key)
    {
        childAnnotationsToBeRemoved.add(key);
        return this;
    }

    @JsonIgnore
    public RelationshipUpdate addParentAnnotation(String key, String value)
    {
        relationship.addParentAnnotation(key, value);
        return this;
    }

    @JsonIgnore
    public RelationshipUpdate removeParentAnnotation(String key)
    {
        parentAnnotationsToBeRemoved.add(key);
        return this;
    }

    public Relationship getRelationship()
    {
        return relationship;
    }

    public void setRelationship(Relationship relationship)
    {
        this.relationship = relationship;
    }

    public Set<String> getChildAnnotationsToBeRemoved()
    {
        return childAnnotationsToBeRemoved;
    }

    public void setChildAnnotationsToBeRemoved(Set<String> childAnnotationsToBeRemoved)
    {
        this.childAnnotationsToBeRemoved = childAnnotationsToBeRemoved;
    }

    public Set<String> getParentAnnotationsToBeRemoved()
    {
        return parentAnnotationsToBeRemoved;
    }

    public void setParentAnnotationsToBeRemoved(Set<String> parentAnnotationsToBeRemoved)
    {
        this.parentAnnotationsToBeRemoved = parentAnnotationsToBeRemoved;
    }
}
