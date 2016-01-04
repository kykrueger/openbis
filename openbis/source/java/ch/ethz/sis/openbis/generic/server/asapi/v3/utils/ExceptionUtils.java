/*
 * Copyright 2014 ETH Zuerich, CISD
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
package ch.ethz.sis.openbis.generic.server.asapi.v3.utils;

import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.OperationContext;
import ch.systemsx.cisd.common.exceptions.UserFailureException;

public class ExceptionUtils
{
    public static RuntimeException create(OperationContext context, Throwable t)
    {
        if (t instanceof UserFailureException)
        {
            return new UserFailureException(createMessage(context, t), t);
        }
        return new RuntimeException(createMessage(context, t), t);
    }

    private static String createMessage(OperationContext context, Throwable t)
    {
        String message = t.getMessage() == null ? t.toString() : t.getMessage();
        return message + " (Context: " + context.getContextDescriptions().toString() + ")";
    }

}