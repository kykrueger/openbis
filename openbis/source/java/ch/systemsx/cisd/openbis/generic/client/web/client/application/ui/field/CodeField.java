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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field;

/**
 * The {@link VarcharField} extension suitable for registering a code.
 * 
 * @author Christian Ribeaud
 */
public final class CodeField extends VarcharField
{
    /**
     * The pattern for a correct code.
     * <p>
     * Note that this one accepts letters in lower case.
     * </p>
     */
    public final static String CODE_PATTERN = "^[a-zA-Z0-9_\\-]+$";

    public CodeField(final String label)
    {
        super(label, true);
        setRegex(CODE_PATTERN);
        getMessages().setRegexText(
                "Code contains invalid characters. Allowed characters are:"
                        + " letters, numbers, hyphen (\"-\") and underscore (\"_\").");
    }
}