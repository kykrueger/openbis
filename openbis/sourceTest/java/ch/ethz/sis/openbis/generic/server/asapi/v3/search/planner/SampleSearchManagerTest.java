/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.ISearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchOperator;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.EntityKind;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.roleassignment.Role;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.search.SampleSearchCriteria;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.sort.ISortAndPage;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.auth.AuthorisationInformation;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.auth.ISQLAuthorisationInformationProviderDAO;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.dao.ISQLSearchDAO;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Tests {@link SampleSearchManager}.
 *
 * @author Viktor Kovtun
 */
public class SampleSearchManagerTest
{
    private static final String USER_ID = "test";

    private Mockery context;

    private ISQLSearchDAO searchDAOMock;

    private ISQLAuthorisationInformationProviderDAO authInfoProviderMock;

    private SampleSearchManager searchManager;

    private ISortAndPage sortAndPageMock;

    @BeforeMethod
    public void setUpMocks()
    {
        context = new Mockery();
        searchDAOMock = context.mock(ISQLSearchDAO.class);
        authInfoProviderMock = context.mock(ISQLAuthorisationInformationProviderDAO.class);
        sortAndPageMock = context.mock(ISortAndPage.class);
        searchManager = new SampleSearchManager(searchDAOMock, authInfoProviderMock);
    }

    @AfterMethod
    public void assertIsSatisfied()
    {
        context.assertIsSatisfied();
    }

    /**
     * Tests {@link SampleSearchManager#searchForIDs(Long, SampleSearchCriteria)} for the case when there are main and parent
     * criteria and the OR logical operator is applied to the root criteria.
     */
    @Test
    public void testSearchForIDsParentCriteriaOr() throws Exception
    {
        final SampleSearchCriteria parentSearchCriterion1 = new SampleSearchCriteria();
        parentSearchCriterion1.withType().withCode().thatEquals("B");

        final SampleSearchCriteria parentSearchCriterion2 = new SampleSearchCriteria();
        parentSearchCriterion2.withProject();

        final List<ISearchCriteria> parentCriteria = Arrays.asList(parentSearchCriterion1, parentSearchCriterion2);

        final SampleSearchCriteria criterion = new SampleSearchCriteria();
        criterion.withType().withCode().thatEquals("A");

        final SampleSearchCriteria searchCriteria = new SampleSearchCriteria().withOrOperator();
        searchCriteria.setCriteria(new ArrayList<>(Arrays.asList(criterion)));
        searchCriteria.withParents().withOrOperator().setCriteria(new ArrayList<>(parentCriteria));

        final Set<Long> parentCriteriaIds = new HashSet<>(Arrays.asList(11L, 13L, 19L, 17L));
        final Set<Long> parentCriteriaResultingIds = new HashSet<>(Arrays.asList(1L, 3L, 9L, 7L));
        final Set<Long> mainCriteriaIds = new HashSet<>(Arrays.asList(1L, 2L, 5L));
        final Set<Long> expectedIds = new HashSet<>(Arrays.asList(1L, 2L, 3L, 5L, 7L, 9L));
        context.checking(new Expectations()
                {{
                    one(searchDAOMock).queryDBWithNonRecursiveCriteria(EntityKind.SAMPLE,
                            Collections.singletonList(criterion), SearchOperator.OR);
                    will(returnValue(mainCriteriaIds));

                    one(searchDAOMock).queryDBWithNonRecursiveCriteria(EntityKind.SAMPLE, parentCriteria,
                            SearchOperator.OR);
                    will(returnValue(parentCriteriaIds));

                    one(searchDAOMock).findChildIDs(EntityKind.SAMPLE, parentCriteriaIds);
                    will(returnValue(parentCriteriaResultingIds));
                }});

        final Set<Long> actualIds = searchManager.searchForIDs(null, searchCriteria);
        assertEquals(actualIds, expectedIds, "Actual and expected IDs are not equal.");
    }

    /**
     * Tests {@link SampleSearchManager#searchForIDs(Long, SampleSearchCriteria)} for the case when there are main and parent
     * criteria and the AND logical operator is applied to the root criteria.
     */
    @Test
    public void testSearchForIDsParentCriteriaAnd() throws Exception
    {
        final SampleSearchCriteria parentSearchCriterion1 = new SampleSearchCriteria();
        parentSearchCriterion1.withType().withCode().thatEquals("B");

        final SampleSearchCriteria parentSearchCriterion2 = new SampleSearchCriteria();
        parentSearchCriterion2.withProject();

        final List<ISearchCriteria> parentCriteria = Arrays.asList(parentSearchCriterion1, parentSearchCriterion2);

        final SampleSearchCriteria criterion = new SampleSearchCriteria();
        criterion.withType().withCode().thatEquals("A");

        final SampleSearchCriteria searchCriteria = new SampleSearchCriteria().withAndOperator();
        searchCriteria.setCriteria(new ArrayList<>(Arrays.asList(criterion)));
        searchCriteria.withParents().withOrOperator().setCriteria(new ArrayList<>(parentCriteria));

        final Set<Long> parentCriteriaIds = new HashSet<>(Arrays.asList(11L, 13L, 19L, 17L));
        final Set<Long> parentCriteriaResultingIds = new HashSet<>(Arrays.asList(1L, 3L, 9L, 7L));
        final Set<Long> mainCriteriaIds = new HashSet<>(Arrays.asList(1L, 2L, 3L));
        final Set<Long> expectedIds = new HashSet<>(Arrays.asList(1L, 3L));

        context.checking(new Expectations()
                {{
                    one(searchDAOMock).queryDBWithNonRecursiveCriteria(EntityKind.SAMPLE,
                            Collections.singletonList(criterion), SearchOperator.AND);
                    will(returnValue(mainCriteriaIds));

                    one(searchDAOMock).queryDBWithNonRecursiveCriteria(EntityKind.SAMPLE, parentCriteria,
                            SearchOperator.OR);
                    will(returnValue(parentCriteriaIds));

                    one(searchDAOMock).findChildIDs(EntityKind.SAMPLE, parentCriteriaIds);
                    will(returnValue(parentCriteriaResultingIds));
                }});

        final Set<Long> actualIds = searchManager.searchForIDs(null, searchCriteria);
        assertEquals(actualIds, expectedIds, "Actual and expected IDs are not equal.");
    }

    /**
     * Tests {@link SampleSearchManager#searchForIDs(Long, SampleSearchCriteria)} for the case when there are main and child
     * criteria and the OR logical operator is applied to the root criteria.
     */
    @Test
    public void testSearchForIDsChildCriteriaOr() throws Exception
    {
        final SampleSearchCriteria childSearchCriterion1 = new SampleSearchCriteria();
        childSearchCriterion1.withType().withCode().thatEquals("B");

        final SampleSearchCriteria childSearchCriterion2 = new SampleSearchCriteria();
        childSearchCriterion2.withProject();

        final List<ISearchCriteria> childCriteria = Arrays.asList(childSearchCriterion1, childSearchCriterion2);

        final SampleSearchCriteria criterion = new SampleSearchCriteria();
        criterion.withType().withCode().thatEquals("A");

        final SampleSearchCriteria searchCriteria = new SampleSearchCriteria().withOrOperator();
        searchCriteria.setCriteria(new ArrayList<>(Arrays.asList(criterion)));
        searchCriteria.withChildren().withAndOperator().setCriteria(new ArrayList<>(childCriteria));

        final Set<Long> childCriteriaIds = new HashSet<>(Arrays.asList(11L, 12L, 14L, 18L));
        final Set<Long> childCriteriaResultingIds = new HashSet<>(Arrays.asList(1L, 3L, 9L, 7L));
        final Set<Long> mainCriteriaIds = new HashSet<>(Arrays.asList(1L, 2L, 5L));
        final Set<Long> expectedIds = new HashSet<>(Arrays.asList(1L, 2L, 3L, 5L, 7L, 9L));
        context.checking(new Expectations()
                {{
                    one(searchDAOMock).queryDBWithNonRecursiveCriteria(EntityKind.SAMPLE, childCriteria,
                            SearchOperator.AND);
                    will(returnValue(childCriteriaIds));

                    one(searchDAOMock).queryDBWithNonRecursiveCriteria(EntityKind.SAMPLE,
                            Collections.singletonList(criterion), SearchOperator.OR);
                    will(returnValue(mainCriteriaIds));

                    one(searchDAOMock).findParentIDs(EntityKind.SAMPLE, childCriteriaIds);
                    will(returnValue(childCriteriaResultingIds));
                }});

        final Set<Long> actualIds = searchManager.searchForIDs(null, searchCriteria);
        assertEquals(actualIds, expectedIds, "Actual and expected IDs are not equal.");
    }

    /**
     * Tests {@link SampleSearchManager#searchForIDs(Long, SampleSearchCriteria)} for the case when there are main and child
     * criteria and the AND logical operator is applied to the root criteria.
     */
    @Test
    public void testSearchForIDsChildCriteriaAnd() throws Exception
    {
        final SampleSearchCriteria childSearchCriterion1 = new SampleSearchCriteria();
        childSearchCriterion1.withType().withCode().thatEquals("B");

        final SampleSearchCriteria childSearchCriterion2 = new SampleSearchCriteria();
        childSearchCriterion2.withProject();

        final List<ISearchCriteria> childCriteria = Arrays.asList(childSearchCriterion1, childSearchCriterion2);

        final SampleSearchCriteria criterion = new SampleSearchCriteria();
        criterion.withType().withCode().thatEquals("A");

        final SampleSearchCriteria searchCriteria = new SampleSearchCriteria().withAndOperator();
        searchCriteria.setCriteria(new ArrayList<>(Arrays.asList(criterion)));
        searchCriteria.withChildren().withOrOperator().setCriteria(new ArrayList<>(childCriteria));

        final Set<Long> childCriteriaIds = new HashSet<>(Arrays.asList(11L, 12L, 14L, 18L));
        final Set<Long> childCriteriaResultingIds = new HashSet<>(Arrays.asList(1L, 3L, 5L, 7L));
        final Set<Long> mainCriteriaIds = new HashSet<>(Arrays.asList(1L, 2L, 3L));
        final Set<Long> expectedIds = new HashSet<>(Arrays.asList(1L, 3L));

        context.checking(new Expectations()
        {{
            one(searchDAOMock).queryDBWithNonRecursiveCriteria(EntityKind.SAMPLE, childCriteria,
                    SearchOperator.OR);
            will(returnValue(childCriteriaIds));

            one(searchDAOMock).queryDBWithNonRecursiveCriteria(EntityKind.SAMPLE,
                    Collections.singletonList(criterion), SearchOperator.AND);
            will(returnValue(mainCriteriaIds));

            one(searchDAOMock).findParentIDs(EntityKind.SAMPLE, childCriteriaIds);
            will(returnValue(childCriteriaResultingIds));
        }});

        final Set<Long> actualIds = searchManager.searchForIDs(null, searchCriteria);
        assertEquals(actualIds, expectedIds, "Actual and expected IDs are not equal.");
    }

    /**
     * Tests {@link SampleSearchManager#searchForIDs(Long, SampleSearchCriteria)} for the case when only main criteria is
     * present.
     */
    @Test
    public void testSearchForIDsOnlyMainCriteria()
    {
        final SampleSearchCriteria criterion = new SampleSearchCriteria();
        criterion.withType().withCode().thatEquals("A");

        final SampleSearchCriteria searchCriteria = new SampleSearchCriteria();
        searchCriteria.withAndOperator().setCriteria(new ArrayList<>(Collections.singletonList(criterion)));

        final Set<Long> expectedIds = new HashSet<>(Arrays.asList(1L, 3L, 5L, 7L));

        context.checking(new Expectations()
                {{
                    one(searchDAOMock).queryDBWithNonRecursiveCriteria(EntityKind.SAMPLE,
                            Collections.singletonList(criterion), SearchOperator.AND);
                    will(returnValue(expectedIds));
                }});

        final Set<Long> actualIds = searchManager.searchForIDs(null, searchCriteria);
        assertEquals(actualIds, expectedIds, "Actual and expected IDs are not equal.");
    }

    /**
     * Tests if the method {$ SampleSearchManager#searchForIDs(SampleSearchCriteria)} returns all values when no
     * criteria are specified.
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testSearchForIDsNoCriteria()
    {
        final Set<Long> expectedIds = new HashSet(Arrays.asList(1L, 2L, 3L, 4L));
        context.checking(new Expectations()
                {{
                    one(searchDAOMock).queryDBWithNonRecursiveCriteria(with(equal(EntityKind.SAMPLE)),
                            with(any(List.class)), with(any(SearchOperator.class)));
                    will(returnValue(expectedIds));
                }});

        final Set<Long> actualIds = searchManager.searchForIDs(null, new SampleSearchCriteria());
        assertEquals(actualIds, expectedIds);
    }

    @Test
    public void testSearchForIDsNoValuesFound()
    {
        final SampleSearchCriteria criterion = new SampleSearchCriteria();
        criterion.withType().withCode().thatEquals("A");

        final SampleSearchCriteria searchCriteria = new SampleSearchCriteria();
        searchCriteria.withAndOperator().setCriteria(new ArrayList<>(Collections.singletonList(criterion)));

        context.checking(new Expectations()
                {{
                    one(searchDAOMock).queryDBWithNonRecursiveCriteria(with(equal(EntityKind.SAMPLE)),
                            with(any(List.class)), with(any(SearchOperator.class)));
                    will(returnValue(Collections.emptySet()));
                }});

        final Set<Long> actualIds = searchManager.searchForIDs(null, new SampleSearchCriteria());
        assertTrue(actualIds.isEmpty());
    }

    @Test
    public void testFilterIDsByUserRights() throws Exception
    {
        final long userId = 12345;
        final Set<Long> sampleIds = new HashSet<>(Arrays.asList(1L, 2L, 3L, 4L, 5L));
        final Set<Long> acceptedIds = new HashSet<Long>(Arrays.asList(1L, 3L, 5L));

        context.checking(new Expectations()
                {{
                    final Set<Long> spaceIds = new HashSet<>(Arrays.asList(10L, 11L, 12L));
                    final Set<Long> projectIds = new HashSet<>(Arrays.asList(20L, 21L, 22L, 23L));
                    final Set<Role> instanceRoles = new HashSet<>(Arrays.asList(Role.ETL_SERVER));

                    one(authInfoProviderMock).findAuthorisedSpaceProjectIDs(userId);
                    will(returnValue(new AuthorisationInformation(instanceRoles, spaceIds, projectIds)));

                    one(authInfoProviderMock).getAuthorisedSamples(sampleIds,
                            new AuthorisationInformation(instanceRoles, spaceIds, projectIds));
                    will(returnValue(acceptedIds));
                }});

        final Set<Long> resultingIds = searchManager.filterIDsByUserRights(userId, sampleIds);
        assertEquals(resultingIds, acceptedIds);
    }

//    @Test
//    public void testSortAndPage()
//    {
//        final Set<Long> sampleIds = new HashSet<>(Arrays.asList(1L, 2L, 3L, 4L, 5L));
//
//        final SampleSearchCriteria searchCriteria = new SampleSearchCriteria().withAndOperator();
//        searchCriteria.withType().withCode().thatEquals("A");
//        searchCriteria.setCriteria(new ArrayList<>(Arrays.asList(searchCriteria)));
//
//        final SampleFetchOptions sampleFetchOptions = new SampleFetchOptions();
//
//        final List<Long> expectedSortedIds = Arrays.asList(5L, 4L, 3L, 2L, 1L);
//
//        context.checking(new Expectations()
//                {{
//                    one(sortAndPageMock).sortAndPage(new ArrayList<>(sampleIds), searchCriteria, sampleFetchOptions);
//                    will(returnValue(expectedSortedIds));
//                }});
//
//        final List<Long> actualSortedIds = searchManager.sortAndPage(sampleIds, searchCriteria, sampleFetchOptions);
//        assertEquals(actualSortedIds, expectedSortedIds);
//    }

}
