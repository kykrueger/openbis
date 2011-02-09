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

package ch.systemsx.cisd.etlserver;

import java.io.File;

import ch.systemsx.cisd.common.mail.IMailClient;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;

/**
 * A storage processor that returns an object with the state necessary to commit or rollback.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public interface IStroageProcessorTransactional extends IStorageProcessor
{
    public static interface IStorageProcessorTransaction
    {
        /**
         * Commits the changes done by the recent {@link #storeData} call if the dataset has been
         * also successfully registered openBIS.
         * <p>
         * This operation is useful when the storage processor adds the data to an additional
         * database. If all the storage processor operations are done on the file system, the
         * implementation of this method will be usually empty.
         * </p>
         */
        public void commit();

        /**
         * Performs a rollback of
         * {@link #storeData(DataSetInformation, ITypeExtractor, IMailClient, File, File)} The data
         * created in <code>directory</code> will also be removed.
         * <p>
         * Call to this method is safe as implementations should try/catch exceptions that could
         * occur here.
         * </p>
         * 
         * @param exception an exception which has caused that the unstore operation has to be
         *            performed
         * @return an instruction what to do with the data in incoming directory
         */
        public UnstoreDataAction rollback(Throwable exception);
    }

    /**
     * Stores the specified incoming data set file to the specified directory. In general some
     * processing and/or transformation of the incoming data takes place.
     * <p>
     * Do not try/catch exceptions that could occur here. Preferably let the upper layer handle
     * them.
     * </p>
     * 
     * @param dataSetInformation Information about the data set.
     * @param typeExtractor the {@link ITypeExtractor} implementation.
     * @param mailClient mail client.
     * @param incomingDataSetDirectory folder to store. Do not remove it after the implementation
     *            has finished processing. {@link TransferredDataSetHandler} takes care of this.
     * @param rootDir directory to whom the data will be stored.
     * @return folder which contains the stored data. This folder <i>must</i> be below the
     *         <var>rootDir</var>. Never returns <code>null</code> but prefers to throw an exception
     *         in case of unexpected behavior.
     */
    public IStorageProcessorTransaction storeDataTransactionally(
            final DataSetInformation dataSetInformation, final ITypeExtractor typeExtractor,
            final IMailClient mailClient, final File incomingDataSetDirectory, final File rootDir);
}
