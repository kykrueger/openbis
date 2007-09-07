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

package ch.systemsx.cisd.datamover.testhelper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static ch.systemsx.cisd.datamover.testhelper.FileSystemHelper.*;

import ch.systemsx.cisd.common.utilities.CollectionIO;
import ch.systemsx.cisd.datamover.utils.MarkerFile;

/**
 * Immutable helper for creating a sample directory structure and manipulating it.
 * 
 * @author Tomasz Pylak on Aug 29, 2007
 */
public class FileStructEngine
{
    private static final String SAMPLE_FILE1 = "f1";

    private static final String SAMPLE_FILE2 = "f2";

    private final String sampleMovedDir;

    public FileStructEngine(String name)
    {
        this.sampleMovedDir = name + "_dir";
    }

    public String getMainStructName()
    {
        return sampleMovedDir;
    }

    public String getSampleCleansingRegExp()
    {
        return SAMPLE_FILE1;
    }

    public void assertSampleStructureCleaned(File parentDir) throws IOException
    {
        File dirInOutgoing = assertDirExists(parentDir, sampleMovedDir);
        assertSampleFileContent(dirInOutgoing, SAMPLE_FILE2);
    }

    public void createSampleStructure(File parentDir) throws IOException
    {
        File dir1 = createDir(parentDir, sampleMovedDir);
        createSampleFile(dir1, SAMPLE_FILE1);
        createSampleFile(dir1, SAMPLE_FILE2);
    }

    public void createPartialSampleStructure(File parentDir) throws IOException
    {
        File dir1 = createDir(parentDir, sampleMovedDir);
        createSampleFile(dir1, SAMPLE_FILE1);
    }

    public void createSampleFinishedMarkerFile(File parentDir)
    {
        createEmptyFile(createFinishedMarkerFile(parentDir, sampleMovedDir));
    }

    public void createSampleDeletionInProgressMarkerFile(File parentDir)
    {
        createEmptyFile(createDeletionInProgressMarkerFile(parentDir, sampleMovedDir));
    }

    private static File createFinishedMarkerFile(File parentDir, String originalName)
    {
        return new File(parentDir, MarkerFile.getCopyFinishedMarkerName(originalName));
    }
    
    private static File createDeletionInProgressMarkerFile(File parentDir, String originalName)
    {
        return new File(parentDir, MarkerFile.getDeletionInProgressMarkerName(originalName));
    }
    
    private static List<String> createSampleFileContent()
    {
        String[] lines = new String[]
            { "test line 1", "test line 2", "test line 3" };
        List<String> lineList = Arrays.asList(lines);
        return lineList;
    }

    private static File createSampleFile(File dir, String name)
    {
        return createFile(dir, name, createSampleFileContent());
    }

    private static void assertSampleFileContent(File dir, String fileName)
    {
        File file = new File(dir, fileName);
        assert file.isFile();
        List<String> lineList = new ArrayList<String>();
        assert CollectionIO.readCollection(file, lineList);
        assert lineList.equals(createSampleFileContent());
    }

    public void assertSampleStructureExists(File parentDir) throws IOException
    {
        File dirInOutgoing = assertDirExists(parentDir, sampleMovedDir);
        assertSampleFileContent(dirInOutgoing, SAMPLE_FILE1);
        assertSampleFileContent(dirInOutgoing, SAMPLE_FILE2);
    }

    public void assertSampleStructFinishMarkerExists(File parentDir)
    {
        File marker = createFinishedMarkerFile(parentDir, sampleMovedDir);
        assert marker.exists();
    }
}
