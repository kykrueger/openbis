package ch.systemsx.cisd.common.utilities;

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

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.logging.LogInitializer;
import ch.systemsx.cisd.common.utilities.FileUtilities;

/**
 * Test cases for the <code>deleteRecursively</code> methods of {@link FileUtilities}.
 * 
 * @author Bernd Rinn
 */
public class FileUtilitiesDeleteRecursivelyTest
{

    private static final File unitTestRootDirectory = new File("targets" + File.separator + "unit-test-wd");

    private static final File workingDirectory = new File(unitTestRootDirectory, "CleansingPathHandlerDecoratorTest");

    private File dir1;

    private File dir1a;

    private File dir1b;

    private File file1a;

    private File file1b;

    private File file2;

    private File file3;

    private File dir2;

    private File file4;

    @BeforeClass
    public void init()
    {
        LogInitializer.init();
        unitTestRootDirectory.mkdirs();
        assert unitTestRootDirectory.isDirectory();
    }

    @BeforeMethod
    public void setUp()
    {
        FileUtilities.deleteRecursively(workingDirectory);
        workingDirectory.mkdirs();
        workingDirectory.deleteOnExit();
    }

    @Test
    public void deleteRecursivelyFile()
    {
        final File root = new File(workingDirectory, "delete-test");
        FileUtilities.deleteRecursively(root);
        root.mkdir();
        assert root.isDirectory();
        final File file1 = new File(root, "test1");
        try
        {
            file1.createNewFile();
        } catch (IOException e)
        {
            throw new CheckedExceptionTunnel(e);
        }
        assert file1.exists();
        assert FileUtilities.deleteRecursively(file1);
        assert file1.exists() == false;

        assert FileUtilities.deleteRecursively(root);
        assert root.exists() == false;
    }

    @Test
    public void deleteRecursivelyDirectory()
    {
        final File root = new File(workingDirectory, "delete-test");
        FileUtilities.deleteRecursively(root);
        root.mkdir();
        assert root.isDirectory();
        final File fileA = new File(root, "test1");
        try
        {
            fileA.createNewFile();
        } catch (IOException e)
        {
            throw new CheckedExceptionTunnel(e);
        }
        assert fileA.exists();
        final File directory = new File(root, "test2");
        directory.mkdir();
        assert directory.isDirectory();
        final File fileB = new File(directory, "test3");
        try
        {
            fileB.createNewFile();
        } catch (IOException e)
        {
            throw new CheckedExceptionTunnel(e);
        }
        assert fileB.exists();

        assert FileUtilities.deleteRecursively(root);
        assert root.exists() == false;
    }

    @Test
    public void deleteRecursivelyNonExistingDirectory()
    {
        final File root = new File(workingDirectory, "delete-test");
        FileUtilities.deleteRecursively(root);
        root.mkdir();
        final File nonExistent = new File(root, "non-existent");
        assert nonExistent.exists() == false;
        assert FileUtilities.deleteRecursively(nonExistent) == false;

        assert FileUtilities.deleteRecursively(root);
        assert root.exists() == false;
    }

    @Test
    public void testDeleteRecursivelyDeleteNothing() throws IOException
    {
        createStructure();
        FileUtilities.deleteRecursively(workingDirectory, new FileFilter()
            {
                public boolean accept(File pathname)
                {
                    return false;
                }
            });
        checkDirectories(true);
        checkFiles(true);
    }

    @Test
    public void testDeleteRecursivelyDeleteEverything() throws IOException
    {
        createStructure();
        FileUtilities.deleteRecursively(workingDirectory, new FileFilter()
            {
                public boolean accept(File pathname)
                {
                    return true;
                }
            });
        checkDirectories(false);
        checkFiles(false);
        assert workingDirectory.exists() == false;
    }

    @Test
    public void testDeleteRecursivelyDeleteFiles() throws IOException
    {
        createStructure();
        FileUtilities.deleteRecursively(workingDirectory, new FileFilter()
            {
                public boolean accept(File pathname)
                {
                    return pathname.isFile();
                }
            });
        checkDirectories(true);
        checkFiles(false);
    }

    @Test
    public void testDeleteRecursivelySelectiveFiles() throws IOException
    {
        createStructure();
        FileUtilities.deleteRecursively(workingDirectory, new FileFilter()
            {
                public boolean accept(File pathname)
                {
                    return pathname.getName().equals("file1a") || pathname.getName().equals("file3");
                }
            });
        checkDirectories(true);
        assert file1a.exists() == false;
        assert file1b.exists();
        assert file2.exists();
        assert file3.exists() == false;
        assert file4.exists();
    }

    @Test
    public void testDeleteRecursivelySelectiveDirectory() throws IOException
    {
        createStructure();
        FileUtilities.deleteRecursively(workingDirectory, new FileFilter()
            {
                public boolean accept(File pathname)
                {
                    return pathname.getName().equals("dir1");
                }
            });
        assert dir1.exists() == false;
        assert dir1a.exists() == false;
        assert dir1b.exists() == false;
        assert dir2.exists();
        assert file1a.exists() == false;
        assert file1b.exists() == false;
        assert file2.exists();
        assert file3.exists();
        assert file4.exists();
    }

    private void checkFiles(boolean exists)
    {
        assert file1a.exists() == exists;
        assert file1b.exists() == exists;
        assert file2.exists() == exists;
        assert file3.exists() == exists;
        assert file4.exists() == exists;
    }

    private void checkDirectories(boolean exists)
    {
        assert dir1.exists() == exists;
        assert dir1a.exists() == exists;
        assert dir1b.exists() == exists;
        assert dir2.exists() == exists;
    }

    private void createStructure() throws IOException
    {
        dir1 = createDirectory("dir1");
        dir1a = createDirectory(dir1, "dir1a");
        dir1b = createDirectory(dir1, "dir1b");
        file1a = createFile(dir1a, "file1a");
        file1b = createFile(dir1b, "file1b");
        file2 = createFile("file2");
        file3 = createFile("file3");
        dir2 = createDirectory("dir2");
        file4 = createFile(dir2, "file4");
    }

    private File createFile(String name) throws IOException
    {
        return createFile(workingDirectory, name);
    }

    private File createFile(File directory, String name) throws IOException
    {
        final File file = new File(directory, name);
        file.createNewFile();
        assert file.isFile();
        file.deleteOnExit();
        return file;
    }

    private File createDirectory(String name) throws IOException
    {
        return createDirectory(workingDirectory, name);
    }

    private File createDirectory(File directory, String name) throws IOException
    {
        final File file = new File(directory, name);
        file.mkdir();
        assert file.isDirectory();
        file.deleteOnExit();
        return file;
    }

}
