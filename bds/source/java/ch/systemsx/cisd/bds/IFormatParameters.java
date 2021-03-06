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

/**
 * Read-only access interface for a set of {@link FormatParameter} objects.
 * 
 * @author Franz-Josef Elmer
 */
public interface IFormatParameters extends Iterable<FormatParameter>
{
    /**
     * Returns the value of the specified parameter.
     * 
     * @throws IllegalArgumentException if there is no parameter named as specified.
     */
    public Object getValue(final String parameterName);

    /** Whether given <var>parameterName</var> has already been added. */
    public boolean containsParameter(final String parameterName);
}
