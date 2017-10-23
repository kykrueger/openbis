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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.property;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.CodeSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.CodesSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.ISearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.IdSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.IdsSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.id.IPropertyTypeId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.id.PropertyTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.search.PropertyTypeSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.semanticannotation.search.SemanticAnnotationSearchCriteria;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search.AbstractSearchObjectManuallyExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search.CodeMatcher;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search.CodesMatcher;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search.Matcher;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.semanticannotation.ISearchSemanticAnnotationExecutor;
import ch.systemsx.cisd.openbis.generic.shared.dto.PropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SemanticAnnotationPE;

/**
 * @author pkupczyk
 */
@Component
public class SearchPropertyTypeExecutor extends AbstractSearchObjectManuallyExecutor<PropertyTypeSearchCriteria, PropertyTypePE>
        implements ISearchPropertyTypeExecutor
{

    @Autowired
    private IPropertyTypeAuthorizationExecutor authorizationExecutor;

    @Autowired
    private ISearchSemanticAnnotationExecutor searchSemanticAnnotationExecutor;

    @Override
    public List<PropertyTypePE> search(IOperationContext context, PropertyTypeSearchCriteria criteria)
    {
        authorizationExecutor.canSearch(context);
        return super.search(context, criteria);
    }

    @Override
    protected List<PropertyTypePE> listAll()
    {
        return daoFactory.getPropertyTypeDAO().listAllEntities();
    }

    @Override
    protected Matcher<PropertyTypePE> getMatcher(ISearchCriteria criteria)
    {
        if (criteria instanceof IdSearchCriteria<?>)
        {
            return new IdMatcher();
        } else if (criteria instanceof IdsSearchCriteria)
        {
            return new IdsMatcher();
        } else if (criteria instanceof CodeSearchCriteria)
        {
            return new CodeMatcher<PropertyTypePE>();
        } else if (criteria instanceof CodesSearchCriteria)
        {
            return new CodesMatcher<PropertyTypePE>();
        } else if (criteria instanceof SemanticAnnotationSearchCriteria)
        {
            return new SemanticAnnotationMatcher();
        } else
        {
            throw new IllegalArgumentException("Unknown search criteria: " + criteria.getClass());
        }
    }

    private class IdMatcher extends Matcher<PropertyTypePE>
    {

        @SuppressWarnings("unchecked")
        @Override
        public List<PropertyTypePE> getMatching(IOperationContext context, List<PropertyTypePE> objects, ISearchCriteria criteria)
        {
            IPropertyTypeId id = ((IdSearchCriteria<IPropertyTypeId>) criteria).getId();

            if (id == null)
            {
                return objects;
            } else
            {
                IdsSearchCriteria<IPropertyTypeId> idsCriteria = new IdsSearchCriteria<IPropertyTypeId>();
                idsCriteria.thatIn(Arrays.asList(id));
                return new IdsMatcher().getMatching(context, objects, idsCriteria);
            }
        }

    }

    private class IdsMatcher extends Matcher<PropertyTypePE>
    {

        @SuppressWarnings("unchecked")
        @Override
        public List<PropertyTypePE> getMatching(IOperationContext context, List<PropertyTypePE> objects, ISearchCriteria criteria)
        {
            Collection<IPropertyTypeId> ids = ((IdsSearchCriteria<IPropertyTypeId>) criteria).getFieldValue();

            if (ids != null && false == ids.isEmpty())
            {
                Collection<String> codes = new HashSet<String>();

                for (IPropertyTypeId id : ids)
                {
                    if (id instanceof PropertyTypePermId)
                    {
                        codes.add(((PropertyTypePermId) id).getPermId());
                    } else
                    {
                        throw new IllegalArgumentException("Unknown id: " + id.getClass());
                    }
                }

                List<PropertyTypePE> matches = new ArrayList<PropertyTypePE>();

                for (PropertyTypePE object : objects)
                {
                    if (codes.contains(object.getCode()))
                    {
                        matches.add(object);
                    }
                }

                return matches;
            } else
            {
                return new ArrayList<PropertyTypePE>();
            }
        }

    }

    private class SemanticAnnotationMatcher extends Matcher<PropertyTypePE>
    {
        @Override
        public List<PropertyTypePE> getMatching(IOperationContext context, List<PropertyTypePE> objects, ISearchCriteria criteria)
        {
            List<SemanticAnnotationPE> annotations =
                    searchSemanticAnnotationExecutor.search(context, (SemanticAnnotationSearchCriteria) criteria);

            Set<PropertyTypePE> propertyTypesSet = new HashSet<PropertyTypePE>(objects);
            Set<PropertyTypePE> matches = new HashSet<PropertyTypePE>();

            for (SemanticAnnotationPE annotation : annotations)
            {
                if (annotation.getPropertyType() != null && propertyTypesSet.contains(annotation.getPropertyType()))
                {
                    matches.add(annotation.getPropertyType());
                }
            }

            return new ArrayList<PropertyTypePE>(matches);
        }
    }

}
