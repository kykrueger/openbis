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

package ch.systemsx.cisd.openbis.generic.server.dataaccess.db.dynamic_property;

/**
 * {@link IDynamicPropertyEvaluationScheduler} extension with methods for reading from the queue.
 * 
 * @author Piotr Buczek
 */
public interface IDynamicPropertyEvaluationSchedulerWithQueue extends
        IDynamicPropertyEvaluationScheduler
{
    /**
     * Retrieves, but does not remove, an operation from the head of this queue, waiting if no
     * elements are present on this queue.
     * 
     * @return an operation from the head of this queue
     * @throws InterruptedException if interrupted while waiting.
     */
    DynamicPropertyEvaluationOperation peekWait() throws InterruptedException;

    /**
     * Retrieves and removes an operation from the head of this queue, waiting if no elements are
     * present on this queue.
     * 
     * @return an operation from the head of this queue
     * @throws InterruptedException if interrupted while waiting.
     */
    DynamicPropertyEvaluationOperation take() throws InterruptedException;
}