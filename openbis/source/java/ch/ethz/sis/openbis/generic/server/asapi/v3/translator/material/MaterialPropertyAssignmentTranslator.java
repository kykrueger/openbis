/*
 * Copyright 2016 ETH Zuerich, SIS
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

package ch.ethz.sis.openbis.generic.server.asapi.v3.translator.material;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.PropertyAssignment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.fetchoptions.PropertyAssignmentFetchOptions;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.TranslationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.common.ObjectRelationRecord;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.common.ObjectToManyRelationTranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.property.IPropertyAssignmentTranslator;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.lemnik.eodsql.QueryTool;

/**
 * @author Franz-Josef Elmer
 */
@Component
public class MaterialPropertyAssignmentTranslator extends ObjectToManyRelationTranslator<PropertyAssignment, PropertyAssignmentFetchOptions>
        implements IMaterialPropertyAssignmentTranslator
{

    @Autowired
    private IPropertyAssignmentTranslator assignmentTranslator;

    @Override
    protected List<ObjectRelationRecord> loadRecords(LongOpenHashSet materialTypeIds)
    {
        MaterialQuery query = QueryTool.getManagedQuery(MaterialQuery.class);
        return query.getPropertyAssignmentIds(materialTypeIds);
    }

    @Override
    protected Map<Long, PropertyAssignment> translateRelated(TranslationContext context,
            Collection<Long> materialTypePropertyTaypeIds, PropertyAssignmentFetchOptions relatedFetchOptions)
    {
        MaterialQuery query = QueryTool.getManagedQuery(MaterialQuery.class);
        return assignmentTranslator.getAssignments(context, query.getPropertyAssignments(new LongOpenHashSet(materialTypePropertyTaypeIds)),
                relatedFetchOptions);
    }

    @Override
    protected Collection<PropertyAssignment> createCollection()
    {
        return new ArrayList<>();
    }

}
