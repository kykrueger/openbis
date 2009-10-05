/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.generic.shared;

import java.io.File;

import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;

/**
 * Handler of data sets after successful registration in openBIS.
 *
 * @author Franz-Josef Elmer
 */
public interface IPostRegistrationDatasetHandler
{
    /**
     * Handles specified original data file by using specified data set information.
     * Note, that <code>originalData</code> is already the path inside the data store.
     */
    public void handle(File originalData, final DataSetInformation dataSetInformation);

    /**
     * Reverts the previous invocation of {@link #handle(File, DataSetInformation)}.
     */
    public void undoLastOperation();

}