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

import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SearchCriteriaTranslator.DATE_FORMAT;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SearchCriteriaTranslator.DATE_HOURS_MINUTES_SECONDS_FORMAT;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.condition.utils.TranslatorUtils.parseDate;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.DatePropertySearchCriteria;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.id.ObjectPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IPermIdHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.AbstractEntitySearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.NumberPropertySearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.StringPropertySearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.EntityTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.DataType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.id.PropertyTypePermId;
import ch.ethz.sis.openbis.systemtest.asapi.v3.util.Operator;

/**
 * @author Franz-Josef Elmer
 */
public abstract class AbstractSearchPropertyTest extends AbstractTest
{
    @DataProvider
    protected Object[][] withPropertyExamples()
    {
        return new Object[][] {
                { DataType.VARCHAR, "12", "== 12", true },
                { DataType.VARCHAR, "ab", "<= abc", true },
                { DataType.VARCHAR, "12", "> 100", true },
                { DataType.VARCHAR, "ac3", "contains bc and endsWith 4", false },
                { DataType.VARCHAR, "abc3", "contains bc and endsWith 4", false },
                { DataType.VARCHAR, "ab34", "contains bc and endsWith 4", false },
                { DataType.VARCHAR, "abc34", "contains bc and endsWith 4", true },
                { DataType.MULTILINE_VARCHAR, "ac3", "contains bc or endsWith 4", false },
                { DataType.MULTILINE_VARCHAR, "abc3", "contains bc or endsWith 4", true },
                { DataType.MULTILINE_VARCHAR, "ab34", "contains bc or endsWith 4", true },
                { DataType.MULTILINE_VARCHAR, "abc34", "contains bc or endsWith 4", true },
                { DataType.MULTILINE_VARCHAR, "12", "> 100 and <= 13", true },
                { DataType.BOOLEAN, "true", "== true", true },
                { DataType.BOOLEAN, "true", "== false", false },
                { DataType.BOOLEAN, "false", "== true", false },
                { DataType.BOOLEAN, "false", "== false", true },
        };
    }

    @Test(dataProvider = "withPropertyExamples")
    public void testWithProperty(DataType dataType, String value, String queryString, boolean found)
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        PropertyTypePermId propertyTypeId = createAPropertyType(sessionToken, dataType);
        ObjectPermId entityPermId = createEntity(sessionToken, propertyTypeId, value);
        AbstractEntitySearchCriteria<?> searchCriteria = createSearchCriteria();
        new StringQueryInjector(searchCriteria, propertyTypeId).buildCriteria(queryString);

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
        ObjectPermId entityPermId = createEntity(sessionToken, propertyTypeId, value.toString());
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
                { DataType.DATE },
                { DataType.TIMESTAMP },
                { DataType.BOOLEAN },
                { DataType.VARCHAR },
                { DataType.MULTILINE_VARCHAR },
                { DataType.XML },
                { DataType.HYPERLINK },
                { DataType.CONTROLLEDVOCABULARY },
                { DataType.SAMPLE },
                { DataType.MATERIAL },
        };
    }

    @Test(dataProvider = "withNumberPropertyThrowingExceptionExamples")
    public void testWithNumberPropertyThrowingException(DataType dataType)
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        PropertyTypePermId propertyTypeId = createAPropertyType(sessionToken, dataType);
        AbstractEntitySearchCriteria<?> searchCriteria = createSearchCriteria();
        searchCriteria.withNumberProperty(propertyTypeId.getPermId()).thatEquals(42);

        // When
        assertUserFailureException(Void -> search(sessionToken, searchCriteria),
                // Then
                "cannot be applied to the data type " + dataType);
    }

    @DataProvider
    protected Object[][] withDatePropertyExamples()
    {
        return new Object[][] {
                { DataType.DATE, createDate(2020, Calendar.FEBRUARY, 15, 0, 0, 0), "== 2020-02-15", true },
                { DataType.DATE, createDate(2020, Calendar.FEBRUARY, 15, 0, 0, 0), "== 2020-02-14", false },
        };
    }

    @Test(dataProvider = "withDatePropertyExamples")
    public void testWithDateProperty(final DataType dataType, final Date value, final String queryString,
            final boolean found)
    {
        // Given
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);
        final PropertyTypePermId propertyTypeId = createAPropertyType(sessionToken, dataType);
        final String formattedDate = dataType == DataType.DATE ? DATE_FORMAT.format(value)
                : DATE_HOURS_MINUTES_SECONDS_FORMAT.format(value);
        final ObjectPermId entityPermId = createEntity(sessionToken, propertyTypeId, formattedDate);
        final AbstractEntitySearchCriteria<?> searchCriteria = createSearchCriteria();
        new DateQueryInjector(searchCriteria, propertyTypeId, true).buildCriteria(queryString);

        // When
        final List<? extends IPermIdHolder> entities = search(sessionToken, searchCriteria);

        // Then
        assertEquals(entities.size(), found ? 1 : 0);
        if (found)
        {
            assertEquals(entities.get(0).getPermId().toString(), entityPermId.getPermId());
        }
    }

    @Test(dataProvider = "withDatePropertyExamples")
    public void testWithDatePropertyComparedToJavaDateThrowingException(final DataType dataType, final Date value,
            final String queryString, final boolean found)
    {
        // Given
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);
        final PropertyTypePermId propertyTypeId = createAPropertyType(sessionToken, dataType);
        final String formattedDate = dataType == DataType.DATE ? DATE_FORMAT.format(value)
                : DATE_HOURS_MINUTES_SECONDS_FORMAT.format(value);
        createEntity(sessionToken, propertyTypeId, formattedDate);
        final AbstractEntitySearchCriteria<?> searchCriteria = createSearchCriteria();
        new DateQueryInjector(searchCriteria, propertyTypeId, false).buildCriteria(queryString);

        // When
        assertUserFailureException(Void -> search(sessionToken, searchCriteria),
                // Then
                String.format("Search criteria with time stamp doesn't make sense for property %s of data type %s.",
                        propertyTypeId, dataType));
    }

    private Date createDate(final int year, final int month, final int date, final int hrs, final int min,
            final int sec)
    {
        final Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, date, hrs, min, sec);
        return calendar.getTime();
    }

    private ObjectPermId createEntity(String sessionToken, PropertyTypePermId propertyTypeId, String value)
    {
        EntityTypePermId entityTypeId = createEntityType(sessionToken, propertyTypeId);
        return createEntity(sessionToken, "ENTITY_TO_BE_DELETED", entityTypeId, propertyTypeId.getPermId(), value);
    }

    protected abstract EntityTypePermId createEntityType(String sessionToken, PropertyTypePermId propertyTypeId);

    protected abstract ObjectPermId createEntity(String sessionToken, String code, EntityTypePermId entityTypeId,
            String propertyType, String value);

    protected abstract AbstractEntitySearchCriteria<?> createSearchCriteria();

    protected abstract List<? extends IPermIdHolder> search(String sessionToken,
            AbstractEntitySearchCriteria<?> searchCriteria);

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

    private static final class StringQueryInjector extends AbstractQueryInjector
    {
        StringQueryInjector(AbstractEntitySearchCriteria<?> searchCriteria, PropertyTypePermId propertyTypeId)
        {
            super(searchCriteria, propertyTypeId);
        }

        @Override
        protected void injectQuery(Operator operator, String operand)
        {
            StringPropertySearchCriteria criteria = searchCriteria.withProperty(propertyTypeId.getPermId());
            switch (operator)
            {
                case CONTAINS:
                    criteria.thatContains(operand);
                    break;
                case STARTS_WITH:
                    criteria.thatStartsWith(operand);
                    break;
                case ENDS_WITH:
                    criteria.thatEndsWith(operand);
                    break;
                case EQUAL:
                    criteria.thatEquals(operand);
                    break;
                case GREATER:
                    criteria.thatIsGreaterThan(operand);
                    break;
                case GREATER_OR_EQUAL:
                    criteria.thatIsGreaterThanOrEqualTo(operand);
                    break;
                case LESS:
                    criteria.thatIsLessThan(operand);
                    break;
                case LESS_OR_EQUAL:
                    criteria.thatIsLessThanOrEqualTo(operand);
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported operator " + operator);
            }
        }
    }

    private static final class DateQueryInjector extends AbstractQueryInjector
    {

        private final boolean asString;

        DateQueryInjector(final AbstractEntitySearchCriteria<?> searchCriteria,
                final PropertyTypePermId propertyTypeId, final boolean asString)
        {
            super(searchCriteria, propertyTypeId);
            this.asString = asString;
        }

        @Override
        protected void injectQuery(final Operator operator, final String operand)
        {
            final Date date = parseDate(operand);
            final DatePropertySearchCriteria criteria = searchCriteria.withDateProperty(propertyTypeId.getPermId());

            if (asString)
            {
                final String dateStr = DATE_FORMAT.format(date);
                switch (operator)
                {
                    case EQUAL:
                    {
                        criteria.thatEquals(dateStr);
                        break;
                    }
                    case GREATER:
                    {
                        criteria.thatIsLaterThan(dateStr);
                        break;
                    }
                    case GREATER_OR_EQUAL:
                    {
                        criteria.thatIsLaterThanOrEqualTo(dateStr);
                        break;
                    }
                    case LESS:
                    {
                        criteria.thatIsEarlierThan(dateStr);
                        break;
                    }
                    case LESS_OR_EQUAL:
                    {
                        criteria.thatIsEarlierThanOrEqualTo(dateStr);
                        break;
                    }
                    default:
                    {
                        throw new IllegalArgumentException("Unsupported operator " + operator);
                    }
                }
            } else
            {
                switch (operator)
                {
                    case EQUAL:
                    {
                        criteria.thatEquals(date);
                        break;
                    }
                    case GREATER:
                    {
                        criteria.thatIsLaterThan(date);
                        break;
                    }
                    case GREATER_OR_EQUAL:
                    {
                        criteria.thatIsLaterThanOrEqualTo(date);
                        break;
                    }
                    case LESS:
                    {
                        criteria.thatIsEarlierThan(date);
                        break;
                    }
                    case LESS_OR_EQUAL:
                    {
                        criteria.thatIsEarlierThanOrEqualTo(date);
                        break;
                    }
                    default:
                    {
                        throw new IllegalArgumentException("Unsupported operator " + operator);
                    }
                }
            }
        }

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
