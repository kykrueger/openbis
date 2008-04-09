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

import java.io.File;

/**
 * Interface for administration of a database.
 * 
 * @author Franz-Josef Elmer
 */
public interface IDatabaseAdminDAO
{
    /**
     * Returns the name of the database to be created / dropped.
     */
    public String getDatabaseName();

    /**
     * Returns the complete URL of the database to be created / dropped.
     */
    public String getDatabaseURL();

    /**
     * Creates the owner/user of the database. Implementation should handle the case of already
     * existing owner/user gracefully.
     */
    public void createOwner();

    /**
     * Creates the database and the 'database_version_logs' table.
     */
    public void createDatabase();

    /**
     * Restores the database from previously created dump.
     */
    public void restoreDatabaseFromDump(File dumpFolder, String version);

    /**
     * Drops the database.
     */
    public void dropDatabase();
}
