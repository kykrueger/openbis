/*
 * Copyright 2009 ETH Zuerich, CISD
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

/**
 * Enum holding OS specific attributes like line separator etc.
 * 
 *
 * @author Franz-Josef Elmer
 */
public enum OSKind
{
    MAC("\n"), WINDOWS("\r\n"), UNIX("\n"), OTHER("\n");
    
    private final String lineSeparator;

    private OSKind(String lineSeparator)
    {
        this.lineSeparator = lineSeparator;
    }

    public final String getLineSeparator()
    {
        return lineSeparator;
    }

}