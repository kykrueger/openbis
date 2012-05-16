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

package ch.systemsx.cisd.etlserver.registrator;

import java.io.File;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import static ch.systemsx.cisd.common.Constants.IS_FINISHED_PREFIX;
/**
 * 
 *
 * @author jakubs
 */
public class MarkerFileUtilityTest extends AssertJUnit
{
    @Test
    public void testMarkerFileNameGeneration()
    {
        File original = new File("a/b/c/d");
        
        File markerPath = MarkerFileUtility.getMarkerFileFromIncoming(original);
        
        assertEquals(markerPath, new File("a/b/c", IS_FINISHED_PREFIX + original.getName()));
        
        assertEquals(original, MarkerFileUtility.getIncomingFromMarkerFile(markerPath));
    }
}
