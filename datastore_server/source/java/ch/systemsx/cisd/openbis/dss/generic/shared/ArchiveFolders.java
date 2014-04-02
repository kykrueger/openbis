/*
 * Copyright 2014 ETH Zuerich, Scientific IT Services
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import ch.systemsx.cisd.openbis.generic.shared.dto.DatasetDescription;

abstract class ArchiveFolders
{

    public abstract File getFolder(DatasetDescription dataSetDescription);

    public abstract Collection<File> getAllFolders();

    public static ArchiveFolders create(String foldersConfig, boolean createFolders)
    {
        if (StringUtils.isBlank(foldersConfig))
        {
            return null;
        }

        String[] folderPaths = foldersConfig.split(";");
        List<String> notBlankPaths = new ArrayList<String>();

        for (String folderPath : folderPaths)
        {
            if (false == StringUtils.isBlank(folderPath))
            {
                notBlankPaths.add(folderPath);
            }
        }

        if (notBlankPaths.isEmpty())
        {
            return null;
        } else if (notBlankPaths.size() == 1)
        {
            return new CommonArchiveFolder(notBlankPaths.get(0), createFolders);
        } else if (notBlankPaths.size() == 2)
        {
            return new SizeDependentArchiveFolders(notBlankPaths.get(0), notBlankPaths.get(1), createFolders);
        } else
        {
            throw new IllegalArgumentException(
                    "Found "
                            + notBlankPaths.size()
                            + " archive folders: "
                            + notBlankPaths
                            + ". Please specify only one folder for all sizes of data sets or two separate folders for big and small data sets respectively.");
        }
    }

    protected File prepareFolder(String folderPath, boolean createFolder)
    {
        File folder = new File(folderPath);

        if (createFolder)
        {
            if (folder.isFile())
            {
                throw new IllegalArgumentException("Archive folder '" + folder + "' is a file.");
            }
            if (folder.exists() == false)
            {
                boolean success = folder.mkdirs();
                if (success == false)
                {
                    throw new IllegalArgumentException("Couldn't create archive folder '" + folder + "'.");
                }
            }
        } else
        {
            if (folder.isDirectory() == false)
            {
                throw new IllegalArgumentException("Archive folder '" + folder + "' doesn't exists or is a file.");
            }
        }

        return folder;
    }

    static class CommonArchiveFolder extends ArchiveFolders
    {
        private File dataSetsFolder;

        private CommonArchiveFolder(String folderPath, boolean createFolder)
        {
            this.dataSetsFolder = prepareFolder(folderPath, createFolder);
        }

        @Override
        public File getFolder(DatasetDescription dataSetDescription)
        {
            return dataSetsFolder;
        }

        @Override
        public Collection<File> getAllFolders()
        {
            return Arrays.asList(dataSetsFolder);
        }

    }

    static class SizeDependentArchiveFolders extends ArchiveFolders
    {

        private File bigDataSetsFolder;

        private File smallDataSetsFolder;

        private SizeDependentArchiveFolders(String bigDataSetsFolderPath, String smallDataSetsFolderPath, boolean createFolders)
        {
            this.bigDataSetsFolder = prepareFolder(bigDataSetsFolderPath, createFolders);
            this.smallDataSetsFolder = prepareFolder(smallDataSetsFolderPath, createFolders);
        }

        @Override
        public File getFolder(DatasetDescription dataSetDescription)
        {
            // TODO read the limit from properties
            if (dataSetDescription.getDataSetSize() > 1000)
            {
                return bigDataSetsFolder;
            } else
            {
                return smallDataSetsFolder;
            }
        }

        @Override
        public Collection<File> getAllFolders()
        {
            return Arrays.asList(bigDataSetsFolder, smallDataSetsFolder);
        }

    }

}