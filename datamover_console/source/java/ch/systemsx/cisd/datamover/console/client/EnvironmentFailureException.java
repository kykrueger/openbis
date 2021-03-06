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

package ch.systemsx.cisd.datamover.console.client;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * The <code>EnvironmentFailureException</code> is the super class of all exceptions that have their cause in a failure condition in the environment
 * of the system. This implies that the user himself cannot fix the problem.
 * 
 * @author Bernd Rinn
 */
public class EnvironmentFailureException extends RuntimeException implements IsSerializable
{

    private static final long serialVersionUID = 1L;

    // An non-empty constructor is mandatory in GWT for serializable objects
    public EnvironmentFailureException()
    {
        super();
    }

    public EnvironmentFailureException(final String message)
    {
        super(message);
    }
}
