/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.cina.shared.labview;

import java.io.File;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import ch.systemsx.cisd.cina.shared.labview.Cluster;
import ch.systemsx.cisd.cina.shared.labview.LVData;
import ch.systemsx.cisd.cina.shared.labview.LVDataParser;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public class LVDataTest extends AssertJUnit
{
    private LVData parse(String path)
    {
        File file = new File(path);
        return LVDataParser.parse(file);
    }

    @Test
    public void testParseFile()
    {
        LVData lvdata = parse("sourceTest/java/ch/systemsx/cisd/cina/shared/labview/lvdata.xml");
        assertNotNull(lvdata);

        assertEquals("8.6.1", lvdata.getVersion());
        assertEquals(1, lvdata.getClusters().size());

        Cluster cluster = lvdata.getClusters().get(0);
        assertEquals(1, cluster.getStrings().size());
        assertEquals("Macro", cluster.getStrings().get(0).getName());
    }
}
