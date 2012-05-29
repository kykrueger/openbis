/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard;

import java.io.File;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.FileUtils;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.filesystem.BooleanStatus;
import ch.systemsx.cisd.common.io.hierarchical_content.DefaultFileBasedHierarchicalContentFactory;
import ch.systemsx.cisd.common.io.hierarchical_content.api.IHierarchicalContent;
import ch.systemsx.cisd.common.io.hierarchical_content.api.IHierarchicalContentNode;
import ch.systemsx.cisd.common.utilities.PropertyUtils;
import ch.systemsx.cisd.openbis.dss.generic.shared.ArchiverTaskContext;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IDatasetLocation;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatasetDescription;

/**
 * Archiver plugin which copies data sets to a destination folder using rsync (if it is remote). The
 * destination can be
 * <ul>
 * <li>on the local file system,
 * <li>a mounted remote folder,
 * <li>a remote folder accessible via SSH,
 * <li>a remote folder accessible via an rsync server.
 * </ul>
 * 
 * @author Piotr Buczek
 */
public class RsyncArchiver extends AbstractArchiverProcessingPlugin
{
    @Private static final String ONLY_MARK_AS_DELETED_KEY = "only-mark-as-deleted";
    
    @Private static final String STAGING_FOLDER = "archive-staging";

    private static final long serialVersionUID = 1L;

    private static final Comparator<IHierarchicalContentNode> NODE_COMPARATOR =
            new Comparator<IHierarchicalContentNode>()
                {
                    public int compare(IHierarchicalContentNode n1, IHierarchicalContentNode n2)
                    {
                        return n1.getName().compareTo(n2.getName());
                    }
                };

    private enum DeleteAction
    {
        DELETE(Operation.DELETE_FROM_ARCHIVE)
        {
            @Override
            public Status execute(IDataSetFileOperationsManager manager, IDatasetLocation dataSet)
            {
                return manager.deleteFromDestination(dataSet);
            }
        },
        MARK_AS_DELETED(Operation.MARK_AS_DELETED)
        {
            @Override
            public Status execute(IDataSetFileOperationsManager manager, IDatasetLocation dataSet)
            {
                return manager.markAsDeleted(dataSet);
            }
        };
        private final Operation operation;

        private DeleteAction(Operation operation)
        {
            this.operation = operation;
        }

        public Operation getOperation()
        {
            return operation;
        }

        public abstract Status execute(IDataSetFileOperationsManager manager,
                IDatasetLocation dataSet);
    }

    private transient IDataSetFileOperationsManager fileOperationsManager;

    private final DeleteAction deleteAction;

    public RsyncArchiver(Properties properties, File storeRoot)
    {
        this(properties, storeRoot, new DataSetFileOperationsManager(properties,
                new RsyncArchiveCopierFactory(), new SshCommandExecutorFactory()));
    }

    @Private
    RsyncArchiver(Properties properties, File storeRoot,
            IDataSetFileOperationsManager fileOperationsManager)
    {
        super(properties, storeRoot, null, null);
        this.fileOperationsManager = fileOperationsManager;
        if (PropertyUtils.getBoolean(properties, ONLY_MARK_AS_DELETED_KEY, true))
        {
            deleteAction = DeleteAction.MARK_AS_DELETED;
        } else
        {
            deleteAction = DeleteAction.DELETE;
        }
    }

    @Override
    protected DatasetProcessingStatuses doArchive(List<DatasetDescription> datasets,
            ArchiverTaskContext context) throws UserFailureException
    {
        DatasetProcessingStatuses statuses = new DatasetProcessingStatuses();
        for (DatasetDescription dataset : datasets)
        {
            File originalData = getDatasetDirectory(context, dataset);
            Status status = doArchive(dataset, originalData);
            String dataSetCode = dataset.getDataSetCode();
            if (status.isOK())
            {
                IHierarchicalContent content =
                        context.getHierarchicalContentProvider().asContent(dataSetCode);
                File temp =
                        new File(context.getDirectoryProvider().getStoreRoot(), STAGING_FOLDER
                                + "/" + dataSetCode);
                temp.mkdirs();
                try
                {
                    fileOperationsManager.retrieveFromDestination(temp, dataset);
                    IHierarchicalContent retrievedContent =
                            new DefaultFileBasedHierarchicalContentFactory().asHierarchicalContent(
                                    temp, null);
                    IHierarchicalContentNode root = content.getRootNode();
                    IHierarchicalContentNode retrievedRoot = retrievedContent.getRootNode();
                    status = checkHierarchySizeAndChecksums(root, retrievedRoot);
                } finally
                {
                    FileUtils.deleteQuietly(temp);
                }
            }
            statuses.addResult(dataSetCode, status, Operation.ARCHIVE);
        }

        return statuses;
    }

    @Private static Status checkHierarchySizeAndChecksums(IHierarchicalContentNode node,
            IHierarchicalContentNode retrievedNode)
    {
        String relativePath = node.getRelativePath();
        String relativePathOfRetrieved = retrievedNode.getRelativePath();
        if (relativePath.equals(relativePathOfRetrieved) == false)
        {
            return Status.createError("Different paths: Path in the store is '" + relativePath
                    + "' and in the archive '" + relativePathOfRetrieved + "'.");
        }
        boolean directory = node.isDirectory();
        boolean directoryOfRetrieved = retrievedNode.isDirectory();
        if (directory != directoryOfRetrieved)
        {
            return Status.createError("The path '" + relativePath
                    + "' should be in store and archive either "
                    + "both directories or files but not mixed: In the store it is a "
                    + render(directory) + " but in the archive it is a "
                    + render(directoryOfRetrieved) + ".");
        }
        if (directory)
        {
            List<IHierarchicalContentNode> childNodes = getChildNodes(node);
            List<IHierarchicalContentNode> childNodesOfRetrieved = getChildNodes(retrievedNode);
            int size = childNodes.size();
            int sizeOfRetrieved = childNodesOfRetrieved.size();
            if (size != sizeOfRetrieved)
            {
                return Status.createError("The directory '" + relativePath + "' has in the store "
                        + size + " files but " + sizeOfRetrieved + " in the archive.");
            }
            for (int i = 0; i < size; i++)
            {
                Status status =
                        checkHierarchySizeAndChecksums(childNodes.get(i),
                                childNodesOfRetrieved.get(i));
                if (status.isError())
                {
                    return status;
                }
            }
        } else
        {
            long fileLength = node.getFileLength();
            long fileLengthOfRetrieved = retrievedNode.getFileLength();
            if (fileLength != fileLengthOfRetrieved)
            {
                return Status.createError("The file '" + relativePath + "' has in the store "
                        + fileLength + " bytes but " + fileLengthOfRetrieved + " in the archive.");
            }
            long checksum = node.getChecksumCRC32();
            long checksumOfRetrieved = retrievedNode.getChecksumCRC32();
            if (checksum != checksumOfRetrieved)
            {
                return Status.createError("The file '" + relativePath
                        + "' has in the store the checksum " + renderChecksum(checksum) + " but "
                        + renderChecksum(checksumOfRetrieved) + " in the archive.");
            }
        }
        return Status.OK;
    }

    private static List<IHierarchicalContentNode> getChildNodes(IHierarchicalContentNode node)
    {
        List<IHierarchicalContentNode> childNodes = node.getChildNodes();
        Collections.sort(childNodes, NODE_COMPARATOR);
        return childNodes;
    }

    private static String render(boolean directory)
    {
        return directory ? "directory" : "file";
    }

    private static String renderChecksum(long checksum)
    {
        return String.format("%08X", checksum);
    }

    @Override
    protected DatasetProcessingStatuses doUnarchive(List<DatasetDescription> datasets,
            ArchiverTaskContext context) throws UserFailureException
    {
        DatasetProcessingStatuses statuses = new DatasetProcessingStatuses();
        for (DatasetDescription dataset : datasets)
        {
            context.getUnarchivingPreparation().prepareForUnarchiving(dataset);
            File originalData = getDatasetDirectory(context, dataset);
            Status status = doUnarchive(dataset, originalData);
            statuses.addResult(dataset.getDataSetCode(), status, Operation.UNARCHIVE);
        }

        return statuses;
    }

    @Override
    protected DatasetProcessingStatuses doDeleteFromArchive(List<? extends IDatasetLocation> datasets)
    {
        DatasetProcessingStatuses statuses = new DatasetProcessingStatuses();
        for (IDatasetLocation dataset : datasets)
        {
            Status status = deleteAction.execute(fileOperationsManager, dataset);
            statuses.addResult(dataset.getDataSetCode(), status, deleteAction.getOperation());
        }
        return statuses;
    }
    
    @Override
    protected BooleanStatus isDataSetSynchronizedWithArchive(DatasetDescription dataset,
            ArchiverTaskContext context)
    {
        File originalData = getDatasetDirectory(context, dataset);
        return fileOperationsManager.isSynchronizedWithDestination(originalData, dataset);
    }

    @Override
    protected BooleanStatus isDataSetPresentInArchive(DatasetDescription dataset)
    {
        return fileOperationsManager.isPresentInDestination(dataset);
    }

    private Status doArchive(DatasetDescription dataset, File originalData)
    {
        return fileOperationsManager.copyToDestination(originalData, dataset);
    }

    private Status doUnarchive(DatasetDescription dataset, File originalData)
    {
        return fileOperationsManager.retrieveFromDestination(originalData, dataset);
    }

    private File getDatasetDirectory(ArchiverTaskContext context, DatasetDescription dataset)
    {
        return context.getDirectoryProvider().getDataSetDirectory(dataset);
    }

}
