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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.util;

/**
 * @author Tomasz Pylak
 */
public class RendererTestUtil
{
    public static String invalidCode(final String code)
    {
        return "<div class=\"invalid\">" + code + "</div>";
    }
    
    public static String link(final String text)
    {
        return "<div class=\"link-style\">" + text + "</div>";
    }

    public static String linkInvalid(final String text)
    {
        return "<div class=link-style><div class=invalid>" + text + "</div></div>";
    }
    
}
