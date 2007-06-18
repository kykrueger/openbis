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

package ch.systemsx.cisd.dbmigration;

/**
 * Provider of SQL scripts for creation and migration of database.
 *
 * @author Franz-Josef Elmer
 */
public interface ISqlScriptProvider
{
    /**
     * Returns the script to create database schemas.
     * 
     * @param version Version of the database.
     * @return <code>null</code> if there isn't such a script.
     */
    public Script getSchemaScript(String version);
    
    /**
     * Returns the script to create initial data.
     * 
     * @param version Version of the database.
     * @return <code>null</code> if there isn't such a script.
     */
    public Script getDataScript(String version);
    
    /**
     * Returns the migration script for migrating a database.
     * 
     * @param fromVersion The version of the current database.
     * @param toVersion The version of the database after migration.
     * @return <code>null</code> if there isn't such a migration script. 
     */
    public Script getMigrationScript(String fromVersion, String toVersion);
}
