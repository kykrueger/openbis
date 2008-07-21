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

package ch.systemsx.cisd.dbmigration;

import org.springframework.jdbc.core.JdbcTemplate;

import ch.systemsx.cisd.common.exceptions.Status;

/**
 * An interface which must be implemented by all classes providing Java code that performs migration
 * steps prior (pre) or after (post) the SQL migration script ran. Canonical name of class
 * implementing this interface (preceded by <code>-- JAVA </code>) may be included in the first
 * line of SQL migration script. <Example:
 * <code> -- JAVA ch.systemsx.cisd.openbis.db.migration.MigrationStepFrom022To023</code>
 * 
 * @author Izabela Adamczyk
 */
public interface IMigrationStep
{
    /**
     * Called before the SQL migration is performed.
     */
    public Status performPreMigration(JdbcTemplate jdbcTemplate);

    /**
     * Called after the SQL migration has been performed.
     */
    public Status performPostMigration(JdbcTemplate jdbcTemplate);

}
