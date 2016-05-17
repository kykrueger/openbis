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

package ch.systemsx.cisd.common.exceptions;

/**
 * Used to signal the error of getting last modification time of a store item
 * 
 * @author Tomasz Pylak
 */
public class UnknownLastChangedException extends EnvironmentFailureException
{
    private static final long serialVersionUID = 1L;

    public UnknownLastChangedException(String errorMsg)
    {
        super(errorMsg);
    }
}