/*
 * Copyright 2015 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.server.api.v3.translator.entity.material.sql;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import net.lemnik.eodsql.QueryTool;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.server.api.v3.translator.TranslationContext;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.entity.common.sql.ObjectToOneRecord;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.entity.common.sql.ObjectToOneRelation;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.entity.person.sql.IPersonSqlTranslator;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.person.Person;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.person.PersonFetchOptions;

/**
 * @author pkupczyk
 */
@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class MaterialRegistratorRelation extends ObjectToOneRelation<Person, PersonFetchOptions>
{

    @Autowired
    private IPersonSqlTranslator personTranslator;

    public MaterialRegistratorRelation(TranslationContext context, Collection<Long> objectIds, PersonFetchOptions relatedFetchOptions)
    {
        super(context, objectIds, relatedFetchOptions);
    }

    @Override
    protected List<ObjectToOneRecord> load(LongOpenHashSet objectIds)
    {
        MaterialQuery query = QueryTool.getManagedQuery(MaterialQuery.class);
        return query.getRegistrators(objectIds);
    }

    @Override
    protected Map<Long, Person> translate(TranslationContext context, Collection<Long> relatedIds, PersonFetchOptions relatedFetchOptions)
    {
        return personTranslator.translate(context, relatedIds, relatedFetchOptions);
    }

}
