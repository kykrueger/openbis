/*
 * Copyright 2014 ETH Zuerich, SIS
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

package ch.systemsx.cisd.openbis.screening.systemtests;

import java.io.File;

import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
@Test(groups = { "slow", "systemtest" })
public class Simple16BitImageDropboxTest extends AbstractImageDropboxTestCase
{
    @Override
    @BeforeTest
    public void dropAnExampleDataSet() throws Exception
    {
        super.dropAnExampleDataSet();
    }

    @Override
    protected String getDataFolderToDrop()
    {
        return "PLATE-16-BIT";
    }

    @Test
    public void test() throws Exception
    {
        AbstractExternalData dataSet = getRegisteredContainerDataSet();
        imageChecker.check(new File(getTestDataFolder(), "1_2_Merged_Default.png"), 
                new ImageLoader(dataSet, sessionToken).tileColumn(2));
        imageChecker.check(new File(getTestDataFolder(), "1_2_DAPI_Default.png"), 
                new ImageLoader(dataSet, sessionToken).tileColumn(2).channel("DAPI"));
        imageChecker.check(new File(getTestDataFolder(), "1_2_DAPI_256x256.png"), 
                new ImageLoader(dataSet, sessionToken).tileColumn(2).channel("DAPI").mode("thumbnail256x256"));
        imageChecker.check(new File(getTestDataFolder(), "1_2_Merged_1392x1040.png"), 
                new ImageLoader(dataSet, sessionToken).tileColumn(2).mode("thumbnail1392x1040"));
        imageChecker.check(new File(getTestDataFolder(), "1_2_GFP_thumbnail1392x1040.png"), 
                new ImageLoader(dataSet, sessionToken).tileColumn(2).channel("GFP").mode("thumbnail1392x1040"));
        imageChecker.assertNoFailures();
    }
}
