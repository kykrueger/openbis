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

package ch.systemsx.cisd.openbis.generic.server.dataaccess.dynamic_property.calculator;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.regex.RegexQuery;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.hibernate.search.FullTextQuery;
import org.hibernate.search.FullTextSession;
import org.hibernate.search.Search;

import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.search.LuceneQueryBuilder;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.dynamic_property.IDynamicPropertyEvaluator;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.dynamic_property.calculator.api.IEntityAdaptor;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.dynamic_property.calculator.api.IEntityPropertyAdaptor;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityPropertyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.IEntityPropertiesHolder;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.hibernate.SearchFieldConstants;

/**
 * Abstract {@link IEntityAdaptor} implementation.
 * 
 * @author Piotr Buczek
 */
public class AbstractEntityAdaptor implements IEntityAdaptor
{
    private final String code;

    protected final IEntityPropertiesHolder propertiesHolder;

    protected Map<String, IEntityPropertyAdaptor> propertiesByCode;

    protected final IDynamicPropertyEvaluator evaluator;

    protected static String ENTITY_TYPE_CODE_FIELD = SearchFieldConstants.PREFIX_ENTITY_TYPE
            + SearchFieldConstants.CODE;

    protected static String ENTITY_TYPE_ANY_CODE_REGEXP = ".*";

    public AbstractEntityAdaptor(String code, Collection<IEntityPropertyAdaptor> properties)
    {
        this.code = code;
        this.propertiesHolder = null;
        this.evaluator = null;
        initPropertiesMap(properties);
    }

    public AbstractEntityAdaptor(IEntityPropertiesHolder propertiesHolder,
            IDynamicPropertyEvaluator propertyEvaluator)
    {
        this.code = propertiesHolder.getCode();
        this.propertiesHolder = propertiesHolder;
        this.evaluator = propertyEvaluator;
    }

    private void initPropertiesMap()
    {
        if (propertiesHolder == null)
        {
            throw new IllegalStateException(
                    "Couldn't init properties if the properties holder has not been set");
        }
        if (evaluator == null)
        {
            throw new IllegalStateException(
                    "Couldn't init properties if the property evaluator has not been set");
        }

        propertiesByCode = new HashMap<String, IEntityPropertyAdaptor>();

        for (EntityPropertyPE property : propertiesHolder.getProperties())
        {
            EntityTypePropertyTypePE etpt = property.getEntityTypePropertyType();
            final PropertyTypePE propertyType = etpt.getPropertyType();
            final String propertyTypeCode = propertyType.getCode();
            if (etpt.isDynamic())
            {
                addProperty(new DynamicPropertyAdaptor(propertyTypeCode, this, property, evaluator));
            } else
            {
                final String value;
                if (property.getMaterialValue() != null)
                {
                    final MaterialPE material = property.getMaterialValue();
                    value =
                            MaterialIdentifier.print(material.getCode(), material.getEntityType()
                                    .getCode());
                } else if (property.getVocabularyTerm() != null)
                {
                    value = property.getVocabularyTerm().getCode();
                } else
                {
                    value = property.getValue();
                }
                if (propertyType.getTransformation() == null)
                {
                    addProperty(new BasicPropertyAdaptor(propertyTypeCode, value, property));
                } else
                {
                    addProperty(new XmlPropertyAdaptor(propertyTypeCode, value, property,
                            propertyType.getTransformation()));
                }
            }
        }
    }

    private void initPropertiesMap(Collection<IEntityPropertyAdaptor> properties)
    {
        propertiesByCode = new HashMap<String, IEntityPropertyAdaptor>();

        if (properties != null)
        {
            for (IEntityPropertyAdaptor property : properties)
            {
                addProperty(property);
            }
        }
    }

    private Map<String, IEntityPropertyAdaptor> propertiesMap()
    {
        if (propertiesByCode == null)
        {
            initPropertiesMap();
        }

        return propertiesByCode;
    }

    private void addProperty(IEntityPropertyAdaptor property)
    {
        propertiesMap().put(property.propertyTypeCode().toUpperCase(), property);
    }

    @Override
    public String code()
    {
        return code;
    }

    @Override
    public IEntityPropertyAdaptor property(String propertyTypeCode)
    {
        return propertiesMap().get(propertyTypeCode.toUpperCase());
    }

    @Override
    public String propertyValue(String propertyTypeCode)
    {
        final IEntityPropertyAdaptor propertyOrNull = property(propertyTypeCode);
        return propertyOrNull == null ? "" : propertyOrNull.valueAsString();
    }

    @Override
    public String propertyRendered(String propertyTypeCode)
    {
        final IEntityPropertyAdaptor propertyOrNull = property(propertyTypeCode);
        return propertyOrNull == null ? "" : propertyOrNull.renderedValue();
    }

    @Override
    public Collection<IEntityPropertyAdaptor> properties()
    {
        return propertiesMap().values();
    }

    protected Query regexpConstraint(String field, String value)
    {
        return new RegexQuery(new Term(field, value.toLowerCase()));
    }

    protected Query constraint(String field, String value)
    {
        return LuceneQueryBuilder.parseQuery(field, value,
                LuceneQueryBuilder.createSearchAnalyzer());
    }

    protected Query and(Query... queries)
    {
        BooleanQuery query = new BooleanQuery();
        for (Query subquery : queries)
        {
            query.add(subquery, BooleanClause.Occur.MUST);
        }
        return query;
    }

    protected ScrollableResults execute(Query query, Class<?> resultClass, Session session)
    {
        FullTextSession fullTextSession = Search.getFullTextSession(session);
        FullTextQuery ftQuery = fullTextSession.createFullTextQuery(query, resultClass);
        ftQuery.setFetchSize(10);
        return ftQuery.scroll(ScrollMode.FORWARD_ONLY);
    }
}
