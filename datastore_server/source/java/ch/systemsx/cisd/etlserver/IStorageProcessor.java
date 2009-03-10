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

package ch.systemsx.cisd.etlserver;

import java.io.File;
import java.util.Properties;

import ch.systemsx.cisd.common.mail.IMailClient;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.StorageFormat;

/**
 * Takes care of storing the data in the store root directory.
 * <p>
 * Implementations of this interface are expected to have a constructor taking a {@link Properties}
 * object as their only argument.
 * </p>
 * 
 * @author Christian Ribeaud
 */
public interface IStorageProcessor extends IStoreRootDirectoryHolder
{
    /** Properties key prefix to find the {@link IStorageProcessor} implementation. */
    public static final String STORAGE_PROCESSOR_KEY = "storage-processor";

    /**
     * Stores the specified incoming data set file to the specified directory. In general some
     * processing and/or transformation of the incoming data takes place.
     * <p>
     * Do not try/catch exceptions that could occur here. Preferably let the upper layer handle
     * them.
     * </p>
     * 
     * @param experiment information about the related experiment.
     * @param dataSetInformation Information about the data set.
     * @param typeExtractor the {@link IProcedureAndDataTypeExtractor} implementation.
     * @param mailClient mail client.
     * @param incomingDataSetDirectory folder to store. Do not remove it after the implementation
     *            has finished processing. {@link TransferredDataSetHandler} takes care of this.
     * @param rootDir directory to whom the data will be stored.
     * @return folder which contains the stored data. This folder <i>must</i> be below the
     *         <var>rootDir</var>. Never returns <code>null</code> but prefers to throw an
     *         exception in case of unexpected behavior.
     */
    public File storeData(final ExperimentPE experiment,
            final DataSetInformation dataSetInformation,
            final IProcedureAndDataTypeExtractor typeExtractor, final IMailClient mailClient,
            final File incomingDataSetDirectory, final File rootDir);

    /**
     * Performs a rollback of
     * {@link #storeData(ExperimentPE, DataSetInformation, IProcedureAndDataTypeExtractor, IMailClient, File, File)}
     * The data created in <code>directory</code> will also be removed.
     * <p>
     * Call to this method is safe as implementations should try/catch exceptions that could occur
     * here.
     * </p>
     * 
     * @param incomingDataSetDirectory original folder to be restored.
     * @param storedDataDirectory directory which contains the data to be restored.
     */
    public void unstoreData(final File incomingDataSetDirectory, final File storedDataDirectory);

    /**
     * Returns the format that this storage processor is storing data sets in.
     */
    public StorageFormat getStorageFormat();

    /**
     * Returns the data set in the original proprietary format (before being processed) if
     * available, or <code>null</code>, if the original data set is no longer available.
     * <p>
     * <strong>Consider the data in the returned file / directory read only!</strong>
     */
    public File tryGetProprietaryData(final File storedDataDirectory);
}