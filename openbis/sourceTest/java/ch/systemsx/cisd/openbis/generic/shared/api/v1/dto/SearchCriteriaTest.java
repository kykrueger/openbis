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

package ch.systemsx.cisd.openbis.generic.shared.api.v1.dto;

import org.testng.AssertJUnit;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria.MatchClause;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria.MatchClauseAttribute;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public class SearchCriteriaTest extends AssertJUnit
{
    private SearchCriteria searchCriteria;

    @BeforeMethod
    public void setUp()
    {
        searchCriteria = createSearchCriteria();

    }

    private SearchCriteria createSearchCriteria()
    {
        SearchCriteria sc = new SearchCriteria();
        sc.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.CODE, "a code"));
        sc.addMatchClause(MatchClause.createPropertyMatch("MY_PROPERTY2", "a property value"));
        return sc;
    }

    @Test
    public void testInitialValue()
    {
        assertEquals(SearchCriteria.SearchOperator.MATCH_ALL_CLAUSES, searchCriteria.getOperator());
    }

    @Test
    public void testEquals()
    {
        SearchCriteria sc = createSearchCriteria();
        assertTrue(searchCriteria.equals(sc));
        assertEquals(sc.hashCode(), searchCriteria.hashCode());
    }

    @Test
    public void testToString()
    {
        assertEquals(
                "SearchCriteria[MATCH_ALL_CLAUSES,[SearchCriteria.AttributeMatchClause[ATTRIBUTE,CODE,a code], SearchCriteria.PropertyMatchClause[PROPERTY,MY_PROPERTY2,a property value]]]",
                searchCriteria.toString());
    }
}
