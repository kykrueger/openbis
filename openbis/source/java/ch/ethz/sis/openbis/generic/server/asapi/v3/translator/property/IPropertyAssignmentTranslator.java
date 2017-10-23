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

package ch.ethz.sis.openbis.generic.server.asapi.v3.translator.property;

import java.util.Collection;
import java.util.Map;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.PropertyAssignment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.fetchoptions.PropertyAssignmentFetchOptions;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.TranslationContext;

/**
 * @author Franz-Josef Elmer
 */
public interface IPropertyAssignmentTranslator
{

    public Map<Long, PropertyAssignment> getIdToAssignmentMap(TranslationContext context,
            Collection<PropertyAssignmentRecord> assignmentRecords,
            PropertyAssignmentFetchOptions assignmentFetchOptions);

    public Map<PropertyAssignmentKey, PropertyAssignment> getKeyToAssignmentMap(TranslationContext context,
            Collection<PropertyAssignmentRecord> assignmentRecords,
            PropertyAssignmentFetchOptions assignmentFetchOptions);

}
