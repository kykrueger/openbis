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

package ch.systemsx.cisd.yeastx.etl;

import static ch.systemsx.cisd.yeastx.etl.ConstantsYeastX.ERROR_MARKER_FILE;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import ch.systemsx.cisd.common.collections.TableMap;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.etlserver.IDataSetHandler;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;

/**
 * {@link IDataSetHandler} implementation which for each dataset directory reads all the files
 * inside that directory and runs the primary dataset handler for it.
 * 
 * @author Tomasz Pylak
 */
public class BatchDataSetHandler implements IDataSetHandler
{
    private final IDataSetHandler delegator;

    private final DatasetMappingResolver datasetMappingResolver;

    public BatchDataSetHandler(Properties properties, IDataSetHandler delegator,
            IEncapsulatedOpenBISService openbisService)
    {
        this.delegator = delegator;
        this.datasetMappingResolver = new DatasetMappingResolver(properties, openbisService);
    }

    public List<DataSetInformation> handleDataSet(File datasetsParentDir)
    {
        List<DataSetInformation> list = new ArrayList<DataSetInformation>();
        if (canBatchBeProcessed(datasetsParentDir) == false)
        {
            return list;
        }
        TableMap<String, DataSetMappingInformation> datasetsMapping =
                DatasetMappingUtil.tryGetDatasetsMapping(datasetsParentDir);
        if (datasetsMapping == null)
        {
            touchErrorMarkerFile(datasetsParentDir);
            return list;
        }
        Set<String> processedFiles = new HashSet<String>();
        List<File> files = listAll(datasetsParentDir);
        for (File file : files)
        {
            if (canDatasetBeProcessed(file, datasetsMapping))
            {
                list.addAll(delegator.handleDataSet(file));
                processedFiles.add(file.getName().toLowerCase());
            }
        }
        cleanMappingFile(datasetsParentDir, processedFiles);
        finish(datasetsParentDir, datasetsMapping.values().size() - processedFiles.size());
        return list;
    }

    private boolean canBatchBeProcessed(File parentDir)
    {
        if (parentDir.isDirectory() == false)
        {
            return false;
        }
        if (errorMarkerFileExists(parentDir))
        {
            return false;
        }
        List<File> files = listAll(parentDir);
        // Do not treat empty directories as faulty.
        
        // The other reason of this check is that this handler is sometimes no able to delete
        // processed directories. It happens when they are mounted on NAS and there are some
        // hidden .nfs* files.
        if (files.size() == 0)
        {
            return false;
        }
        return true;
    }

    private static boolean errorMarkerFileExists(File datasetsParentDir)
    {
        return new File(datasetsParentDir, ERROR_MARKER_FILE).isFile();
    }

    private void cleanMappingFile(File datasetsParentDir, Set<String> processedFiles)
    {
        try
        {
            DatasetMappingUtil.cleanMappingFile(datasetsParentDir, processedFiles);
        } catch (IOException ex)
        {
            LogUtils.error(datasetsParentDir, "Cannot clean dataset mappings file: "
                    + ex.getMessage());
        }
    }

    private void finish(File datasetsParentDir, int unprocessedDatasetsCounter)
    {
        if (unprocessedDatasetsCounter == 0 && hasNoPotentialDatasetFiles(datasetsParentDir))
        {
            clean(datasetsParentDir);
        } else
        {
            touchErrorMarkerFile(datasetsParentDir);
        }
    }

    private static void touchErrorMarkerFile(File parentDir)
    {
        File errorMarkerFile = new File(parentDir, ERROR_MARKER_FILE);
        if (errorMarkerFile.isFile())
        {
            return;
        }
        boolean ok = false;
        try
        {
            ok = errorMarkerFile.createNewFile();
        } catch (IOException ex)
        {
        }
        if (ok == false)
        {
            LogUtils.adminError("Could not create an error marker file '%s'.", errorMarkerFile
                    .getPath());
        } else
        {
            LogUtils.warn(parentDir,
                    "Correct the errors and delete the '%s' file to start processing again.",
                    ERROR_MARKER_FILE);
        }
    }

    private void clean(File datasetsParentDir)
    {
        LogUtils.deleteUserLog(datasetsParentDir);
        DatasetMappingUtil.deleteMappingFile(datasetsParentDir);
        deleteEmptyDir(datasetsParentDir);
    }

    // Checks that the sample from the mapping exists and is assigned to the experiment - we do not
    // want to move datasets to unidentified directory in this case.
    private boolean canDatasetBeProcessed(File file,
            TableMap<String, DataSetMappingInformation> datasetsMapping)
    {
        if (DatasetMappingUtil.isMappingFile(file))
        {
            return false;
        }
        DataSetMappingInformation mapping =
                DatasetMappingUtil.tryGetDatasetMapping(file, datasetsMapping);
        if (mapping == null)
        {
            return false;
        }
        return datasetMappingResolver.isMappingCorrect(mapping, file.getParentFile());
    }

    private void deleteEmptyDir(File dir)
    {
        boolean ok = dir.delete();
        if (ok == false)
        {
            LogUtils.adminError(
                    "The directory '%s' cannot be deleted although it seems to be empty.", dir
                            .getPath());
        }
    }

    private boolean hasNoPotentialDatasetFiles(File dir)
    {
        List<File> files = listAll(dir);
        int datasetsCounter = files.size();
        for (File file : files)
        {
            if (LogUtils.isUserLog(file) || DatasetMappingUtil.isMappingFile(file))
            {
                datasetsCounter--;
            }
        }
        return datasetsCounter == 0;
    }

    private List<File> listAll(File dataSet)
    {
        return FileUtilities.listFilesAndDirectories(dataSet, false, null);
    }
}
