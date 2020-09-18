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

package ch.ethz.sis.openbis.systemtest.asapi.v3;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.id.ObjectPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IPermIdHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.AbstractEntitySearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.NumberPropertySearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.EntityTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.DataType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.id.PropertyTypePermId;

/**
 * @author Franz-Josef Elmer
 */
public abstract class AbstractSearchPropertyTest extends AbstractTest
{
    @DataProvider
    protected Object[][] withNumberPropertyExamples()
    {
        return new Object[][] {
                { DataType.INTEGER, 12, "== 12", true },
                { DataType.REAL, 12.5, "== 12.5", true },
                { DataType.INTEGER, 13, "> 13", false },
                { DataType.INTEGER, 13, ">= 13", true },
                { DataType.INTEGER, 13, "< 13", false },
                { DataType.INTEGER, 13, "<= 13", true },
                { DataType.INTEGER, 13, "<= 13.0", true },
                { DataType.INTEGER, 13, "< 13.001", true },
                { DataType.REAL, 13, "> 13", false },
                { DataType.REAL, 13.001, "> 13", true },
                { DataType.REAL, 13, "< 13", false },
                { DataType.REAL, 12.999, "< 13", true },
                { DataType.INTEGER, 12, "> 13", false },
                { DataType.INTEGER, 14, "> 13 and <= 19.5", true },
                { DataType.INTEGER, 19, "> 13 and <= 19.5", true },
                { DataType.REAL, 19, "> 13 and <= 19.5", true },
                { DataType.REAL, 19.5, "> 13 and <= 19.5", true },
                { DataType.REAL, 19.6, ">= 23.5 or <= 19.5", false },
                { DataType.REAL, 19, ">= 23.5 or <= 19.5", true },
                { DataType.REAL, 23.5, ">= 23.5 or <= 19.5", true },
                { DataType.INTEGER, 19, ">= 23.5 or <= 19.5", true },
                { DataType.INTEGER, 24, ">= 23.5 or <= 19.5", true },
                { DataType.INTEGER, 19, ">= 24 or <= 19", true },
                { DataType.INTEGER, 24, ">= 24 or <= 19", true },
        };
    }

    @Test(dataProvider = "withNumberPropertyExamples")
    public void testWithNumberProperty(DataType dataType, Number value, String queryString, boolean found)
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        PropertyTypePermId propertyTypeId = createAPropertyType(sessionToken, dataType);
        ObjectPermId entityPermId = createEntity(sessionToken, propertyTypeId, value);
        AbstractEntitySearchCriteria<?> searchCriteria = createSearchCriteria();
        new NumberQueryInjector(searchCriteria, propertyTypeId).buildCriteria(queryString);

        // When
        List<? extends IPermIdHolder> entities = search(sessionToken, searchCriteria);

        // Then
        assertEquals(entities.size(), found ? 1 : 0);
        if (found)
        {
            assertEquals(entities.get(0).getPermId().toString(), entityPermId.getPermId());
        }
    }

    @DataProvider
    protected Object[][] withNumberPropertyThrowingExceptionExamples()
    {
        return new Object[][] {
            { DataType.DATE, "2012-1-1" },
            { DataType.TIMESTAMP, "2012-1-1 10:11:12" },
            { DataType.BOOLEAN, "true" },
            { DataType.VARCHAR, "abc" },
            { DataType.MULTILINE_VARCHAR, "abc" },
            { DataType.XML, "3" },
            { DataType.HYPERLINK, "3" },
            { DataType.CONTROLLEDVOCABULARY, "3" },
            { DataType.SAMPLE, "3" },
            { DataType.MATERIAL, "3" },
        };
    }

//    @Test(dataProvider = "withNumberPropertyThrowingExceptionExamples")
    public void testWithNumberPropertyThrowingException(DataType dataType, String value)
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        PropertyTypePermId propertyTypeId = createAPropertyType(sessionToken, dataType);
        AbstractEntitySearchCriteria<?> searchCriteria = createSearchCriteria();
//        ObjectPermId entityPermId = createEntity(sessionToken, propertyTypeId, value);
        searchCriteria.withNumberProperty(propertyTypeId.getPermId()).thatEquals(42);

        // When
        assertUserFailureException(Void -> search(sessionToken, searchCriteria), 
                // Then
                "The data type of property " + propertyTypeId + " has to be one of [REAL, INTEGER");

    }

    private ObjectPermId createEntity(String sessionToken, PropertyTypePermId propertyTypeId, Object value)
    {
        EntityTypePermId entityTypeId = createEntityType(sessionToken, propertyTypeId);
        return createEntity(sessionToken, "ENTITY_TO_BE_DELETED", entityTypeId, propertyTypeId.getPermId(), value.toString());
    }

    protected abstract EntityTypePermId createEntityType(String sessionToken, PropertyTypePermId propertyTypeId);

    protected abstract ObjectPermId createEntity(String sessionToken, String code, EntityTypePermId entityTypeId,
            String propertyType, String value);

    protected abstract AbstractEntitySearchCriteria<?> createSearchCriteria();

    protected abstract List<? extends IPermIdHolder> search(String sessionToken,
            AbstractEntitySearchCriteria<?> searchCriteria);

    private static enum Operator
    {
        EQUAL("=="),
        LESS("<"),
        LESS_OR_EQUAL("<="),
        GREATER(">"),
        GREATER_OR_EQUAL(">="),
        ;
        private String symbol;

        private Operator(String symbol)
        {
            this.symbol = symbol;
        }

        public String getSymbol()
        {
            return symbol;
        }

        public static Map<String, Operator> asMap()
        {
            Map<String, Operator> map = new TreeMap<>();
            for (Operator operator : Operator.values())
            {
                map.put(operator.getSymbol(), operator);
            }
            return map;
        }
    }

    private abstract static class AbstractQueryInjector
    {
        protected AbstractEntitySearchCriteria<?> searchCriteria;

        protected PropertyTypePermId propertyTypeId;

        AbstractQueryInjector(AbstractEntitySearchCriteria<?> searchCriteria, PropertyTypePermId propertyTypeId)
        {
            this.searchCriteria = searchCriteria;
            this.propertyTypeId = propertyTypeId;
        }

        void buildCriteria(String queryString)
        {
            boolean withAnd = queryString.contains("and");
            if (withAnd && queryString.contains("or"))
            {
                failQuery(queryString, "Only 'and' or 'or' allowed.");
            }
            String[] terms;
            if (withAnd)
            {
                searchCriteria.withAndOperator();
                terms = queryString.split("and");
            } else
            {
                searchCriteria.withOrOperator();
                terms = queryString.split("or");
            }
            Map<String, Operator> operators = Operator.asMap();
            for (String term : terms)
            {
                String[] termParts = term.trim().split(" ");
                if (termParts.length != 2)
                {
                    failQuery(queryString, "Invalid term '" + term.trim() + "'.");
                }
                Operator operator = operators.get(termParts[0]);
                if (operator == null)
                {
                    failQuery(queryString, "Unknown operator in term '" + term.trim() + '.');
                }
                try
                {
                    injectQuery(operator, termParts[1]);
                } catch (RuntimeException e)
                {
                    failQuery(queryString, e.toString());
                }
            }
        }

        private void failQuery(String queryString, String message)
        {
            fail("Invalid query '" + queryString + "':" + message);
        }

        protected abstract void injectQuery(Operator operator, String operand);
    }

    private static final class NumberQueryInjector extends AbstractQueryInjector
    {
        NumberQueryInjector(AbstractEntitySearchCriteria<?> searchCriteria, PropertyTypePermId propertyTypeId)
        {
            super(searchCriteria, propertyTypeId);
        }

        @Override
        protected void injectQuery(Operator operator, String operand)
        {
            Number number = new Double(operand);
            NumberPropertySearchCriteria criteria = searchCriteria.withNumberProperty(propertyTypeId.getPermId());
            switch (operator)
            {
                case EQUAL:
                    criteria.thatEquals(number);
                    break;
                case GREATER:
                    criteria.thatIsGreaterThan(number);
                    break;
                case GREATER_OR_EQUAL:
                    criteria.thatIsGreaterThanOrEqualTo(number);
                    break;
                case LESS:
                    criteria.thatIsLessThan(number);
                    break;
                case LESS_OR_EQUAL:
                    criteria.thatIsLessThanOrEqualTo(number);
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported operator " + operator);
            }
        }
    }

}
