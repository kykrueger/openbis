/*
 * Copyright 2013 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.generic.server;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.zip.CRC32;

import ch.systemsx.cisd.common.io.IOUtilities;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContent;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContentNode;
import ch.systemsx.cisd.openbis.dss.generic.shared.IHierarchicalContentProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.DataSetExistenceChecker;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.generic.shared.translator.DataSetTranslator;

/**
 * Abstract super class of packagers which package all files of a data set together with a meta data file.
 *
 * @author Franz-Josef Elmer
 */
public abstract class AbstractDataSetPackager
{
    public static final String META_DATA_FILE_NAME = "meta-data.tsv";
    
    private final IHierarchicalContentProvider contentProvider;
    private final DataSetExistenceChecker dataSetExistenceChecker;

    protected AbstractDataSetPackager(IHierarchicalContentProvider contentProvider, 
            DataSetExistenceChecker dataSetExistenceChecker)
    {
        this.contentProvider = contentProvider;
        this.dataSetExistenceChecker = dataSetExistenceChecker;
    }

    /**
     * Adds an entry with specified entry path and last modification date filled with data from
     * specified input stream.
     * 
     * @param size Number of bytes.
     * @param checksum Checksum. Can be 0 if {@link #isChecksumNeeded()} return <code>false</code>.
     */
    public abstract void addEntry(String entryPath, long lastModified, long size, long checksum, InputStream in);
    
    /**
     * Returns <code>true</code> if the checksum is needed.
     */
    protected abstract boolean isChecksumNeeded();
    
    /**
     * Closes the package.
     */
    public abstract void close();
    
    public void addDataSetTo(String rootPath, AbstractExternalData externalData)
    {
        try
        {
            byte[] bytes = MetaDataBuilder.createMetaData(externalData).getBytes();
            CRC32 checksumCalculator = new CRC32();
            for (byte b : bytes)
            {
                checksumCalculator.update(0xff & b);
            }
            Long checksum = checksumCalculator.getValue();
            addEntry(rootPath + META_DATA_FILE_NAME, System.currentTimeMillis(), new Long(bytes.length), checksum,
                    new ByteArrayInputStream(bytes));
        } catch (Exception ex)
        {
            throw new RuntimeException(
                    "Couldn't package meta data for data set '" + externalData.getCode() + "'.", ex);
        }
        if (dataSetExistenceChecker.dataSetExists(DataSetTranslator.translateToDescription(externalData)) == false)
        {
            throw handleNonExistingDataSet(externalData, null);
        }
        IHierarchicalContent root = null;
        try
        {
            root = contentProvider.asContent(externalData.getCode());
        } catch (Exception ex)
        {
            throw handleNonExistingDataSet(externalData, ex);
        }
        try
        {
            addTo(rootPath, root.getRootNode());
        } catch (Exception ex)
        {
            throw new RuntimeException("Couldn't package data set '" + externalData.getCode() + "'.", ex);
        } finally
        {
            if (root != null)
            {
                root.close();
            }
        }
    }
    
    private RuntimeException handleNonExistingDataSet(AbstractExternalData externalData, Exception ex)
    {
        return new RuntimeException("Data set '" + externalData.getCode() + "' does not exist.", ex);
    }

    private void addTo(String newRootPath, IHierarchicalContentNode node)
    {
        if (node.isDirectory())
        {
            List<IHierarchicalContentNode> childNodes = node.getChildNodes();
            for (IHierarchicalContentNode childNode : childNodes)
            {
                addTo(newRootPath, childNode);
            }
        } else
        {
            long size = node.getFileLength();
            long checksum = 0;
            if (isChecksumNeeded())
            {
                boolean checksumCRC32Precalculated = node.isChecksumCRC32Precalculated();
                if (checksumCRC32Precalculated)
                {
                    checksum = node.getChecksumCRC32();
                } else 
                {
                    checksum = IOUtilities.getChecksumCRC32(node.getInputStream());
                }
            }
            addEntry(newRootPath + node.getRelativePath(), node.getLastModified(), size, checksum,
                    node.getInputStream());
        }
    }
}
