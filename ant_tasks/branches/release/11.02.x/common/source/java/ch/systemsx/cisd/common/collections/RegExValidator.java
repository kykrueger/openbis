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

package ch.systemsx.cisd.common.collections;

import java.util.regex.Pattern;

/**
 * A {@link IValidator} based on regular expressions.
 * 
 * @author Christian Ribeaud
 */
public final class RegExValidator implements IValidator<String>
{
    private final Pattern regEx;

    public RegExValidator(final Pattern regEx)
    {
        this.regEx = regEx;
    }

    //
    // Validator
    //

    public final boolean isValid(final String text)
    {
        return regEx.matcher(text).matches();
    }
}