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

package ch.systemsx.cisd.bds;

import org.testng.annotations.Test;
import static org.testng.AssertJUnit.*;

/**
 * Test cases for corresponding {@link Format} class.
 * 
 * @author Christian Ribeaud
 */
public final class FormatTest
{

    @Test
    public final void testTryToCreateFormatFromString()
    {
        final Format format = Format.tryToCreateFormatFromString("UNKNOWN [A] V1.2", true);
        final Version version = format.getVersion();
        assertEquals(1, version.getMajor());
        assertEquals(2, version.getMinor());
        assertEquals("A", format.getVariant());
        assertEquals("UNKNOWN", format.getCode());
    }
}
