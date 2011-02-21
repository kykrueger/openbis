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

package ch.systemsx.cisd.ant.common;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

/**
 * @author Kaloyan Enimanev
 */
public class TopologicalSortTest extends AssertJUnit
{
    @Test
    public void testSort()
    {
        HashMap<String, List<String>> graph = new HashMap<String, List<String>>();

        graph.put("common", new ArrayList<String>());
        graph.put("server-common", Arrays.asList("common"));
        graph.put("openbis", Arrays.asList("common", "server-common"));
        graph.put("dss", Arrays.asList("openbis", "server-common", "common"));
        graph.put("screening", Arrays.asList("openbis", "dss"));
        
        List<String> sorted = TopologicalSort.sort(graph);
        
        assertEquals(Arrays.asList("common", "server-common", "openbis", "dss", "screening"),
                sorted);
    }

    @Test(expectedExceptions = RuntimeException.class)
    public void testCyclicGraph()
    {
        HashMap<String, List<String>> graph = new HashMap<String, List<String>>();

        graph.put("A", Arrays.asList("B"));
        graph.put("B", Arrays.asList("C"));
        graph.put("C", Arrays.asList("A"));
        
        TopologicalSort.sort(graph);
    }
}
