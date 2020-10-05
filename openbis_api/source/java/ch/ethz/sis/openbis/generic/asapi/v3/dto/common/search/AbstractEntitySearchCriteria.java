/*
 * Copyright 2014 ETH Zuerich, Scientific IT Services
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

package ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.id.IObjectId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.search.ModifierSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.search.RegistratorSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.search.TagSearchCriteria;
import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * @author pkupczyk
 */
@JsonObject("as.dto.common.search.AbstractEntitySearchCriteria")
public abstract class AbstractEntitySearchCriteria<ID extends IObjectId> extends AbstractObjectSearchCriteria<ID>
{

    private static final long serialVersionUID = 1L;

    public CodeSearchCriteria withCode()
    {
        return with(new CodeSearchCriteria());
    }

    public CodesSearchCriteria withCodes()
    {
        return with(new CodesSearchCriteria());
    }

    public PermIdSearchCriteria withPermId()
    {
        return with(new PermIdSearchCriteria());
    }

    public RegistratorSearchCriteria withRegistrator()
    {
        return with(new RegistratorSearchCriteria());
    }

    public ModifierSearchCriteria withModifier()
    {
        return with(new ModifierSearchCriteria());
    }

    public RegistrationDateSearchCriteria withRegistrationDate()
    {
        return with(new RegistrationDateSearchCriteria());
    }

    public ModificationDateSearchCriteria withModificationDate()
    {
        return with(new ModificationDateSearchCriteria());
    }

    public TagSearchCriteria withTag()
    {
        return with(new TagSearchCriteria());
    }

    public NumberPropertySearchCriteria withNumberProperty(String propertyName)
    {
        return with(new NumberPropertySearchCriteria(propertyName));
    }

    /**
     * @deprecated This method casts any string to the actual type of the property been queried.
     * This leads in some cases to cast errors or missing operators for the type latter on during the query parsing.
     * More often than not these errors are completely avoidable simply using the typed methods:
     * {@link AbstractEntitySearchCriteria#withDateProperty(String)}
     * {@link AbstractEntitySearchCriteria#withNumberProperty(String)}
     * {@link AbstractEntitySearchCriteria#withStringProperty(String)}
     * {@link AbstractEntitySearchCriteria#withBooleanProperty(String)}
     */
    @Deprecated
    public StringPropertySearchCriteria withProperty(String propertyName)
    {
        return with(new StringPropertySearchCriteria(propertyName));
    }

    public DatePropertySearchCriteria withDateProperty(String propertyName)
    {
        return with(new DatePropertySearchCriteria(propertyName));
    }

    /**
     * @deprecated This method doesn't cast any string to the actual type of the property been queried.
     * Instead it does string comparison, being inconsistent with the rest of the query engine.
     * It is an inefficient replacement of full text search that actually does a very slow full table scan that can lead to heavy slowdowns on big databases.
     * {@link AbstractEntitySearchCriteria#withAnyDateProperty(String)}
     * {@link AbstractEntitySearchCriteria#withAnyNumberProperty(String)}
     * {@link AbstractEntitySearchCriteria#withAnyStringProperty(String)}
     * {@link AbstractEntitySearchCriteria#withAnyBooleanProperty(String)}
     */
    @Deprecated
    public AnyPropertySearchCriteria withAnyProperty()
    {
        return with(new AnyPropertySearchCriteria());
    }

    public AnyFieldSearchCriteria withAnyField()
    {
        return with(new AnyFieldSearchCriteria());
    }

    public abstract AbstractEntitySearchCriteria<ID> withOrOperator();

    public abstract AbstractEntitySearchCriteria<ID> withAndOperator();
}
