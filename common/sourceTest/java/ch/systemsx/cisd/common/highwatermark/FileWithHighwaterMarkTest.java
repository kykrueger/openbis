/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.common.highwatermark;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.fail;

import java.io.File;
import java.util.Properties;

import org.testng.annotations.Test;

import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;

/**
 * Test cases for the {@link FileWithHighwaterMarkTest}.
 * 
 * @author Christian Ribeaud
 */
public final class FileWithHighwaterMarkTest
{

    @Test
    public final void testFromProperties()
    {
        boolean fail = true;
        try
        {
            FileWithHighwaterMark.fromProperties(null, "");
        } catch (final AssertionError e)
        {
            fail = false;
        }
        assertFalse(fail);
        final String filePropertyKey = "file";
        try
        {
            FileWithHighwaterMark.fromProperties(new Properties(), filePropertyKey);
            fail("");
        } catch (final ConfigurationFailureException e)
        {
        }
        final Properties properties = new Properties();
        final String path = "/my/path";
        properties.setProperty(filePropertyKey, path);
        FileWithHighwaterMark fileWithHighwaterMark =
                FileWithHighwaterMark.fromProperties(properties, filePropertyKey);
        // Default value is -1
        assertEquals(new File(path), fileWithHighwaterMark.getFile());
        assertEquals(-1, fileWithHighwaterMark.getHighwaterMark());
        // 100Kb
        properties.setProperty(filePropertyKey + FileWithHighwaterMark.SEP
                + FileWithHighwaterMark.HIGHWATER_MARK_PROPERTY_KEY, "100");
        fileWithHighwaterMark = FileWithHighwaterMark.fromProperties(properties, filePropertyKey);
        assertEquals(new File(path), fileWithHighwaterMark.getFile());
        assertEquals(100, fileWithHighwaterMark.getHighwaterMark());
        // Meaningless value
        properties.setProperty(filePropertyKey + FileWithHighwaterMark.SEP
                + FileWithHighwaterMark.HIGHWATER_MARK_PROPERTY_KEY, "notANumber");
        fileWithHighwaterMark = FileWithHighwaterMark.fromProperties(properties, filePropertyKey);
        assertEquals(new File(path), fileWithHighwaterMark.getFile());
        assertEquals(-1, fileWithHighwaterMark.getHighwaterMark());
    }
}