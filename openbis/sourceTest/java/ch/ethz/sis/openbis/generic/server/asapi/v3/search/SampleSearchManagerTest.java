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

import org.testng.AssertJUnit;

/**
 * @author Viktor Kovtun
 */
public class SampleSearchManagerTest extends AssertJUnit
{
//    private static final String USER_ID = "test";
//
//    private Mockery context;
//
//    private ISQLSearchDAO searchDAO;
//
//    private ISampleLister sampleLister;
//
//    private SampleSearchManager searchManager;
//
//    @BeforeMethod
//    public void setUpMocks()
//    {
//        context = new Mockery();
//        searchDAO = context.mock(ISQLSearchDAO.class);
//        sampleLister = context.mock(ISampleLister.class);
//        searchManager = new SampleSearchManager(searchDAO, sampleLister);
//    }
//
//    @AfterMethod
//    public void assertIsSatisfied()
//    {
//        context.assertIsSatisfied();
//    }
//
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
//
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
