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

package ch.systemsx.cisd.dbmigration.java;

import javax.sql.DataSource;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;

/**
 * An empty {@link IMigrationStep} implementation.
 * <p>
 * Just extend this class if you only want to partly implement the interface methods.
 * </p>
 * 
 * @author Christian Ribeaud
 */
public class MigrationStepAdapter implements IMigrationStep
{

    //
    // IMigrationStep
    //

    public void performPostMigration(final SimpleJdbcTemplate simpleJdbcTemplate,
            DataSource dataSource) throws DataAccessException
    {
    }

    public void performPreMigration(final SimpleJdbcTemplate simpleJdbcTemplate,
            DataSource dataSource) throws DataAccessException
    {
    }

}
