/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.server.dataaccess.db.search;

import org.testng.AssertJUnit;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import ch.rinn.restrictions.Friend;

/**
 * @author Tomasz Pylak
 */
@Friend(toClasses = LuceneQueryBuilder.class)
public class LuceneQueryBuilderTest extends AssertJUnit
{
    @DataProvider(name = "basicModeQueries")
    protected Object[][] getQueriesToTestInBasicMode()
    {
        return new Object[][]
            {
                { "abc", "(*abc*)" },
                { "code:CP registrator:Joe", "(*code\\:CP*) (*registrator\\:Joe*)" },
                { "ab#c OR d", "(*ab* AND *c*) OR (*d*)" } };
    }

    @DataProvider(name = "wildcardModeQueries")
    protected Object[][] getQueriesToTestInWildcardMode()
    {
        return new Object[][]
            {
                { "abc", "abc" },
                { "code:CP registrator:Joe", "code\\:CP registrator\\:Joe" },
                { "*ab#c OR d", "*ab#c OR d" } };
    }

    @Test(dataProvider = "basicModeQueries")
    public final void testBasicSearchMode(String originalQuery, String adaptedQuery)
    {
        String query = LuceneQueryBuilder.adaptQuery(originalQuery, false);
        assertEquals(adaptedQuery, query);
    }

    @Test(dataProvider = "wildcardModeQueries")
    public final void testWildcardSearchMode(String originalQuery, String adaptedQuery)
    {
        String query = LuceneQueryBuilder.adaptQuery(originalQuery, true);
        assertEquals(adaptedQuery, query);
    }

}
