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

package ch.ethz.sis.openbis.generic.server.asapi.v3.translator.semanticannotation;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.PropertyAssignment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.fetchoptions.PropertyAssignmentFetchOptions;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.TranslationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.common.ObjectRelationRecord;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.common.ObjectToOneRelationTranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.property.IPropertyAssignmentTranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.sample.SampleQuery;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.lemnik.eodsql.QueryTool;

/**
 * @author pkupczyk
 */
@Component
public class SemanticAnnotationPropertyAssignmentTranslator extends ObjectToOneRelationTranslator<PropertyAssignment, PropertyAssignmentFetchOptions>
        implements ISemanticAnnotationPropertyAssignmentTranslator
{

    @Autowired
    private IPropertyAssignmentTranslator assignmentTranslator;

    @Override
    protected List<ObjectRelationRecord> loadRecords(LongOpenHashSet annotationIds)
    {
        SemanticAnnotationQuery query = QueryTool.getManagedQuery(SemanticAnnotationQuery.class);
        return query.getSampleTypePropertyTypeIds(new LongOpenHashSet(annotationIds));
    }

    @Override
    protected Map<Long, PropertyAssignment> translateRelated(TranslationContext context, Collection<Long> relatedIds,
            PropertyAssignmentFetchOptions relatedFetchOptions)
    {
        SampleQuery query = QueryTool.getManagedQuery(SampleQuery.class);
        return assignmentTranslator.getAssignments(context, query.getPropertyAssignments(new LongOpenHashSet(relatedIds)), relatedFetchOptions);
    }

}