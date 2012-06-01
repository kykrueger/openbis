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

package ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.authorization;

import java.util.Arrays;
import java.util.List;

/**
 * Predicate for checking that the current user has access to a data set specified by code.
 * <p>
 * <i>This is an internal class. Do not use it as a user of the API.</i>
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class DataSetCodeStringPredicate extends AbstractDataSetAccessPredicate<IDssServiceRpcGenericInternal, String>
{
    @Override
    public List<String> getDataSetCodes(String argument)
    {
        return Arrays.asList(argument);
    }
}
