package ch.ethz.sis.openbis.generic.server.asapi.v3.search;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchOperator;
import org.testng.annotations.Test;

import java.util.*;

import static org.testng.Assert.assertEquals;

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
    }

}