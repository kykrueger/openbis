/*
 * Copyright 2007 ETH Zuerich, CISD
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

package ch.systemsx.cisd.common.utilities;

import java.util.Arrays;
import java.util.List;

import org.testng.annotations.Test;

import ch.systemsx.cisd.common.utilities.StringUtilities;

import static org.testng.AssertJUnit.*;

/**
 * Test cases for the {@link StringUtilities}.
 * 
 * @author Bernd Rinn
 */
public class StringUtilitiesTest
{

    @Test
    public void testCapitalize()
    {
        assertEquals("Test", StringUtilities.capitalize("test"));
        assertEquals("Test", StringUtilities.capitalize("TEST"));
        assertEquals("Test", StringUtilities.capitalize("tEsT"));
        assertEquals("Test test", StringUtilities.capitalize("Test Test"));
    }

    @Test
    public void testConcatenateEmptyList()
    {
        final List<String> list = Arrays.asList();
        assertEquals("", StringUtilities.concatenateWithNewLine(list));
    }
    
    @Test
    public void testConcatenate()
    {
        final List<String> list = Arrays.asList("one", "two", "three");
        assertEquals("onetwothree", StringUtilities.concatenate(list, ""));
        assertEquals("one two three", StringUtilities.concatenateWithSpace(list));
        final String brokenDownInLines = String.format("one%1$stwo%1$sthree", System.getProperty("line.separator")); 
        assertEquals(brokenDownInLines, StringUtilities.concatenateWithNewLine(list));
    }

}
