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

package ch.systemsx.cisd.cina.shared.metadata;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import ch.systemsx.cisd.cina.shared.constants.BundleStructureConstants;
import ch.systemsx.cisd.cina.shared.labview.LVData;
import ch.systemsx.cisd.cina.shared.labview.LVDataParser;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public class BundleMetadataExtractor
{
    private static final String COLLECTIONS_FOLDER_NAME =
            BundleStructureConstants.COLLECTIONS_FOLDER_NAME;

    private static final String METADATA_FOLDER_NAME =
            BundleStructureConstants.METADATA_FOLDER_NAME;

    private static final String BUNDLE_METADATA_FILE_NAME =
            BundleStructureConstants.BUNDLE_METADATA_FILE_NAME;

    private static final String BUNDLE_METADATA_FOLDER_NAME =
            BundleStructureConstants.BUNDLE_METADATA_FOLDER_NAME;

    private static final String GRID_PREP_SAMPLE_CODE_KEY =
            BundleStructureConstants.GRID_PREP_SAMPLE_CODE_KEY;

    private final ArrayList<CollectionMetadataExtractor> replicaMetadataExtractors;

    private final File bundleMetadataFile;

    private final HashMap<String, String> metadataMap;

    private final File bundle;

    private LVData lvdata;

    private boolean hasBeenPrepared = false;

    /**
     * Create a metadata extractor for the bundle located at bundle
     * 
     * @param bundle The bundle file
     */
    public BundleMetadataExtractor(File bundle)
    {
        this.bundle = bundle;
        replicaMetadataExtractors = new ArrayList<CollectionMetadataExtractor>();
        bundleMetadataFile =
                new File(new File(bundle, BUNDLE_METADATA_FOLDER_NAME), BUNDLE_METADATA_FILE_NAME);
        this.metadataMap = new HashMap<String, String>();
    }

    /**
     * Read the files
     */
    public void prepare()
    {
        if (hasBeenPrepared)
        {
            // we've already prepared
            return;
        }

        parseBundleMetadata();
        prepareReplicaMetadataExtractors();

        hasBeenPrepared = true;
    }

    /**
     * Get the metadata extractors for each of the replica files in this bundle. The method {@link #prepare} must be called before getting the
     * metadata extractors.
     */
    public List<CollectionMetadataExtractor> getReplicaMetadataExtractors()
    {
        checkPrepared();
        return replicaMetadataExtractors;
    }

    /**
     * Return the sample code for the grid preparation sample.
     * 
     * @return Return the code, or null if none was found
     */
    public String tryGridPrepSampleCode()
    {
        checkPrepared();
        return metadataMap.get(GRID_PREP_SAMPLE_CODE_KEY);
    }

    private void processDirectory(File file)
    {
        File annotationsDirectory = new File(file, METADATA_FOLDER_NAME);

        if (false == annotationsDirectory.exists())
        {
            return;
        }

        if (false == annotationsDirectory.isDirectory())
        {
            return;
        }

        if (false == CollectionMetadataExtractor.doesFolderContainReplicaMetadata(annotationsDirectory))
        {
            return;
        }

        CollectionMetadataExtractor replicaMetadataExtractor =
                new CollectionMetadataExtractor(annotationsDirectory);
        replicaMetadataExtractor.prepare();
        replicaMetadataExtractors.add(replicaMetadataExtractor);
    }

    private void checkPrepared()
    {
        assert hasBeenPrepared;
    }

    private void prepareReplicaMetadataExtractors()
    {
        File metadataFolder = new File(bundle, COLLECTIONS_FOLDER_NAME);

        // Then get the replica metadata
        File[] replicaContents = metadataFolder.listFiles();
        for (File replicaFile : replicaContents)
        {
            if (false == replicaFile.isDirectory())
            {
                continue;
            }

            processDirectory(replicaFile);
        }
    }

    private void parseBundleMetadata()
    {
        lvdata = LVDataParser.parse(bundleMetadataFile);
        if (null == lvdata)
        {
            throw new IllegalArgumentException("Could not parse metadata in file "
                    + bundleMetadataFile.getAbsolutePath());
        }
        new LabViewXMLToHashMap(lvdata, metadataMap).appendIntoMap();
    }
}
