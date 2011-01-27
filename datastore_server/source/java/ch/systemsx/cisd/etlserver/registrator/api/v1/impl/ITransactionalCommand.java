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

package ch.systemsx.cisd.etlserver.registrator.api.v1.impl;

import java.io.Serializable;

/**
 * Package-internal interface for tracking and executing parts of a transaction.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
interface ITransactionalCommand extends Serializable
{
    /**
     * Provisionally execute the actions requested by this step.
     */
    void execute();

    /**
     * Rollback any side-effects of the execute. Rollback is assumed to be idempotent -- multiple
     * invocations are allowed. If there is nothing to rollback, just ignore.
     */
    void rollback();
}