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

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchOperator;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

public class AbstractSearchManagerTest
{

    @Test
    public void testMergeResults()
    {
        final Collection<Set<Integer>> sets = new ArrayList<>();
        sets.add(new HashSet<>(Arrays.asList(1, 2, 3, 4, 5)));
        sets.add(new HashSet<>(Arrays.asList(6, 7, 3, 8)));
        sets.add(new HashSet<>(Arrays.asList(3, 5)));
        sets.add(new HashSet<>(Arrays.asList(2, 3, 9)));

        final Set<Integer> intersection = AbstractSearchManager.mergeResults(SearchOperator.AND, sets);
        assertEquals(intersection, new HashSet<>(Arrays.asList(3)));

        final Set<Integer> union = AbstractSearchManager.mergeResults(SearchOperator.OR, sets);
        assertEquals(union, new HashSet<>(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9)));
    }

    @Test
    public void testIntersection()
    {
        final Collection<Set<Integer>> sets = new ArrayList<>();
        sets.add(new HashSet<>(Arrays.asList(1, 2, 3, 4, 5)));
        sets.add(new HashSet<>(Arrays.asList(6, 7, 3, 8)));
        sets.add(new HashSet<>(Arrays.asList(3, 5)));
        sets.add(new HashSet<>(Arrays.asList(2, 3, 9)));

        final Set<Integer> intersection = AbstractSearchManager.intersection(sets);
        assertEquals(intersection, new HashSet<>(Arrays.asList(3)));

        final Set<Object> intersection2 = AbstractSearchManager.intersection(Collections.emptyList());
        assertTrue(intersection2.isEmpty());
    }

    @Test
    public void testUnion()
    {
        final Collection<Set<Integer>> sets = new ArrayList<>();
        sets.add(new HashSet<>(Arrays.asList(1, 2, 3, 4, 5)));
        sets.add(new HashSet<>(Arrays.asList(6, 7, 3, 8)));
        sets.add(new HashSet<>(Arrays.asList(3, 5)));
        sets.add(new HashSet<>(Arrays.asList(2, 3, 9)));

        final Set<Integer> union = AbstractSearchManager.union(sets);
        assertEquals(union, new HashSet<>(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9)));

        final Set<Object> union2 = AbstractSearchManager.union(Collections.emptyList());
        assertTrue(union2.isEmpty());
    }

    @Test
    public void testGetSmallest()
    {
        final Collection<Set<Integer>> sets = new ArrayList<>();
        sets.add(new HashSet<>(Arrays.asList(1, 2, 3, 4, 5)));
        sets.add(new HashSet<>(Arrays.asList(6, 7, 3, 8)));
        sets.add(new HashSet<>(Arrays.asList(3, 5)));
        sets.add(new HashSet<>(Arrays.asList(2, 3, 9)));

        Set<Integer> smallestSet = AbstractSearchManager.getSmallestSet(sets);
        assertEquals(smallestSet, new HashSet<>(Arrays.asList(3, 5)));

        final Collection<Set<Integer>> setsWithEmpty = new ArrayList<>();
        sets.add(new HashSet<>(Arrays.asList(1, 2, 3, 4, 5)));
        sets.add(new HashSet<>(Arrays.asList()));
        sets.add(new HashSet<>(Arrays.asList(3, 5)));
        sets.add(new HashSet<>(Arrays.asList(2, 3, 9)));

        smallestSet = AbstractSearchManager.getSmallestSet(sets);
        assertEquals(smallestSet, Collections.emptySet());

        smallestSet = AbstractSearchManager.getSmallestSet(Collections.emptyList());
        assertNull(smallestSet);

        final HashSet<Integer> singletonSet = new HashSet<>(Arrays.asList(1, 3, 5));
        smallestSet = AbstractSearchManager.getSmallestSet(Collections.singletonList(singletonSet));
        assertEquals(smallestSet, singletonSet);
    }

}