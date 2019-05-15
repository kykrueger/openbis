/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.server.asapi.v3.search;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchOperator;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.EntityKind;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions.SampleFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.search.SampleSearchCriteria;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.sort.ISortAndPage;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.sql.ISQLSearchDAO;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.sql.SpaceProjectIDsVO;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.*;

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

    private SampleSearchManager searchManager;

    private ISortAndPage sortAndPageMock;

    @BeforeMethod
    public void setUpMocks()
    {
        context = new Mockery();
        searchDAOMock = context.mock(ISQLSearchDAO.class);
        sortAndPageMock = context.mock(ISortAndPage.class);
        searchManager = new SampleSearchManager(searchDAOMock, sortAndPageMock);
    }

    @AfterMethod
    public void assertIsSatisfied()
    {
        context.assertIsSatisfied();
    }

    @Test
    public void testSearchForIDs()
    {
        final SampleSearchCriteria searchCriteria = new SampleSearchCriteria().withAndOperator();
        searchCriteria.withType().withCode().thatEquals("A");
        searchCriteria.setCriteria(new ArrayList<>(Arrays.asList(searchCriteria)));

        final SampleSearchCriteria parentSearchCriteria = new SampleSearchCriteria();
        parentSearchCriteria.withType().withCode().thatEquals("B");
        searchCriteria.withParents().setCriteria(new ArrayList<>(Arrays.asList(parentSearchCriteria)));

        context.checking(new Expectations()
                {{

                }});

        final Set<Long> actualIds = searchManager.searchForIDs(searchCriteria);
    }

    /**
     * Tests {@link SampleSearchManager#searchForIDs(SampleSearchCriteria)} for the case when only main criteria is
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

        // AND
        context.checking(new Expectations()
                {{
                    one(searchDAOMock).queryDBWithNonRecursiveCriteria(EntityKind.SAMPLE,
                            Collections.singletonList(criterion), SearchOperator.AND);
                    will(returnValue(expectedIds));
                }});

        Set<Long> actualIds = searchManager.searchForIDs(searchCriteria);
        assertTrue(actualIds.isEmpty(), "Searching with AND operator should produce empty result set.");

        // OR
        searchCriteria.withOrOperator();
        context.checking(new Expectations()
                {{
                    one(searchDAOMock).queryDBWithNonRecursiveCriteria(EntityKind.SAMPLE,
                            Collections.singletonList(criterion), SearchOperator.OR);
                    will(returnValue(expectedIds));
                }});

        actualIds = searchManager.searchForIDs(searchCriteria);
        assertEquals(actualIds, expectedIds, "Actual and expected IDs are not equal.");
    }

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

        final Set<Long> actualIds = searchManager.searchForIDs(new SampleSearchCriteria());
        assertEquals(actualIds, expectedIds);
    }

    @Test
    public void testFilterIDsByUserRights()
    {
        final long userId = 12345;
        final Set<Long> sampleIds = new HashSet<>(Arrays.asList(1L, 2L, 3L, 4L, 5L));
        final Set<Long> acceptedIds = new HashSet<Long>(Arrays.asList(1L, 3L, 5L));

        context.checking(new Expectations()
                {{
                    final Set<Long> spaceIds = new HashSet<>(Arrays.asList(10L, 11L, 12L));
                    final Set<Long> projectIds = new HashSet<>(Arrays.asList(20L, 21L, 22L, 23L));

                    one(searchDAOMock).getAuthorisedSpaceProjectIds(userId);
                    will(returnValue(new SpaceProjectIDsVO(spaceIds, projectIds)));

                    one(searchDAOMock).filterSampleIDsBySpaceAndProjectIDs(sampleIds,
                            new SpaceProjectIDsVO(spaceIds, projectIds));
                    will(returnValue(acceptedIds));
                }});

        final Set<Long> resultingIds = searchManager.filterIDsByUserRights(userId, sampleIds);
        assertEquals(resultingIds, acceptedIds);
    }

    @Test
    public void testSortAndPage()
    {
        final Set<Long> sampleIds = new HashSet<>(Arrays.asList(1L, 2L, 3L, 4L, 5L));

        final SampleSearchCriteria searchCriteria = new SampleSearchCriteria().withAndOperator();
        searchCriteria.withType().withCode().thatEquals("A");
        searchCriteria.setCriteria(new ArrayList<>(Arrays.asList(searchCriteria)));

        final SampleFetchOptions sampleFetchOptions = new SampleFetchOptions();

        final List<Long> expectedSortedIds = Arrays.asList(5L, 4L, 3L, 2L, 1L);

        context.checking(new Expectations()
                {{
                    one(sortAndPageMock).sortAndPage(new ArrayList<>(sampleIds), searchCriteria, sampleFetchOptions);
                    will(returnValue(expectedSortedIds));
                }});

        final List<Long> actualSortedIds = searchManager.sortAndPage(sampleIds, searchCriteria, sampleFetchOptions);
        assertEquals(actualSortedIds, expectedSortedIds);
    }


//    @SuppressWarnings("ArraysAsListWithZeroOrOneArgument")
//    @Test
//    public void testSamplesRestrictedByParentSamplesMoreParentsThanChildren()
//    {
//        final SampleSearchCriteria searchCriteria = new SampleSearchCriteria().withAndOperator();
//        searchCriteria.withType().withCode().thatEquals("A");
////        searchCriteria.setCriteria(new ArrayList<>(Arrays.asList(searchCriteria)));
//
//        final SampleSearchCriteria parentSearchCriteria = new SampleSearchCriteria();
//        parentSearchCriteria.withType().withCode().thatEquals("B");
//        searchCriteria.withParents().setCriteria(new ArrayList<>(Arrays.asList(parentSearchCriteria)));
//
//
//        // TODO: continue refactoring this test from here.
//
//        final RecordingMatcher<DetailedSearchCriteria> mainCriteriaMatcher =
//                new RecordingMatcher<DetailedSearchCriteria>();
//        final RecordingMatcher<DetailedSearchCriteria> parentCriteriaMatcher =
//                new RecordingMatcher<DetailedSearchCriteria>();
//        context.checking(new Expectations()
//            {
//                {
//                    one(searchDAO).getResultSetSizeLimit();
//                    will(returnValue(100));
//
//                    one(searchDAO).searchForEntityIds(with(USER_ID), with(mainCriteriaMatcher),
//                            with(EntityKind.SAMPLE),
//                            with(Arrays.<IAssociationCriteria> asList()));
//                    will(returnValue(new ArrayList<Long>(Arrays.asList(1L, 2L, 3L, 4L))));
//                    one(searchDAO).searchForEntityIds(with(USER_ID), with(parentCriteriaMatcher),
//                            with(EntityKind.SAMPLE),
//                            with(Arrays.<IAssociationCriteria> asList()));
//                    will(returnValue(new ArrayList<Long>(Arrays.asList(1L, 2L, 3L, 4L, 5L))));
//
//                    one(sampleLister).getChildToParentsIdsMap(Arrays.asList(1L, 2L, 3L, 4L));
//                    Map<Long, Set<Long>> result = new HashMap<Long, Set<Long>>();
//                    result.put(1L, new HashSet<Long>(Arrays.asList(4L, 6L)));
//                    result.put(2L, new HashSet<Long>(Arrays.asList(4L, 3L)));
//                    result.put(3L, new HashSet<Long>(Arrays.asList(7L, 8L)));
//                    will(returnValue(result));
//                }
//            });
//
//        List<Long> sampleIds =
//                new ArrayList<Long>(searchManager.searchForSampleIDs(USER_ID, searchCriteria));
//
//        Collections.sort(sampleIds);
//        assertEquals("[1, 2]", sampleIds.toString());
//        assertEquals("ATTRIBUTE SAMPLE_TYPE: A, [SAMPLE_PARENT: ATTRIBUTE SAMPLE_TYPE: B] "
//                + "(without wildcards)", mainCriteriaMatcher.recordedObject().toString());
//        assertEquals("ATTRIBUTE SAMPLE_TYPE: B (without wildcards)", parentCriteriaMatcher
//                .recordedObject().toString());
//        context.assertIsSatisfied();
//    }

//    @SuppressWarnings("ArraysAsListWithZeroOrOneArgument")
//    @Test
//    public void testSamplesRestrictedByParentSamplesMoreChildrenThanParents()
//    {
//        final SampleSearchCriteria searchCriteria = new SampleSearchCriteria().withAndOperator();
//        searchCriteria.withType().withCode().thatEquals("A");
////        searchCriteria.setCriteria(new ArrayList<>(Collections.singletonList(searchCriteria)));
//
//        final SampleSearchCriteria parentSearchCriteria = new SampleSearchCriteria();
//        parentSearchCriteria.withType().withCode().thatEquals("B");
//        searchCriteria.withParents().setCriteria(new ArrayList<>(Collections.singletonList(parentSearchCriteria)));
//
//        // TODO: continue refactoring this test from here.
//
//        final RecordingMatcher<DetailedSearchCriteria> mainCriteriaMatcher =
//                new RecordingMatcher<DetailedSearchCriteria>();
//        final RecordingMatcher<DetailedSearchCriteria> parentCriteriaMatcher =
//                new RecordingMatcher<DetailedSearchCriteria>();
//        context.checking(new Expectations()
//            {
//                {
//                    one(searchDAO).getResultSetSizeLimit();
//                    will(returnValue(100));
//
//                    one(searchDAO).searchForEntityIds(with(USER_ID), with(mainCriteriaMatcher),
//                            with(EntityKind.SAMPLE),
//                            with(Arrays.<IAssociationCriteria> asList()));
//                    will(returnValue(new ArrayList<Long>(Arrays.asList(1L, 2L, 3L, 4L, 5L))));
//                    one(searchDAO).searchForEntityIds(with(USER_ID), with(parentCriteriaMatcher),
//                            with(EntityKind.SAMPLE),
//                            with(Arrays.<IAssociationCriteria> asList()));
//                    will(returnValue(new ArrayList<Long>(Arrays.asList(1L, 2L, 3L, 4L))));
//
//                    one(sampleLister).getParentToChildrenIdsMap(Arrays.asList(1L, 2L, 3L, 4L));
//                    Map<Long, Set<Long>> result = new HashMap<Long, Set<Long>>();
//                    result.put(3L, new HashSet<Long>(Arrays.asList(2L)));
//                    result.put(4L, new HashSet<Long>(Arrays.asList(1L, 2L)));
//                    will(returnValue(result));
//                }
//            });
//
//        List<Long> sampleIds =
//                new ArrayList<Long>(searchManager.searchForSampleIDs(USER_ID, searchCriteria));
//
//        Collections.sort(sampleIds);
//        assertEquals("[1, 2]", sampleIds.toString());
//        assertEquals("ATTRIBUTE SAMPLE_TYPE: A, [SAMPLE_PARENT: ATTRIBUTE SAMPLE_TYPE: B] "
//                + "(without wildcards)", mainCriteriaMatcher.recordedObject().toString());
//        assertEquals("ATTRIBUTE SAMPLE_TYPE: B (without wildcards)", parentCriteriaMatcher
//                .recordedObject().toString());
//        context.assertIsSatisfied();
//    }

}
