/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.shared.basic.dto.api;

import ch.systemsx.cisd.common.exceptions.UserFailureException;

/**
 * Exception thrown by jython scripts in case of invalid input.
 *
 * @author Franz-Josef Elmer
 */
public class ValidationException extends UserFailureException
{
    private static final long serialVersionUID = 1L;

    public ValidationException(String message)
    {
        super(message);
    }

    public ValidationException(String message, Throwable cause)
    {
        super(message, cause);
    }

}
