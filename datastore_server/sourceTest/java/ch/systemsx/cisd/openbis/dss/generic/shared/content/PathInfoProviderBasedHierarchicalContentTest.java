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

package ch.systemsx.cisd.openbis.dss.generic.shared.content;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.io.hierarchical_content.DefaultFileBasedHierarchicalContentTest;
import ch.systemsx.cisd.common.io.hierarchical_content.api.IHierarchicalContent;
import ch.systemsx.cisd.common.io.hierarchical_content.api.IHierarchicalContentNode;
import ch.systemsx.cisd.common.utilities.HierarchicalContentUtils;
import ch.systemsx.cisd.common.utilities.IDelegatedAction;
import ch.systemsx.cisd.openbis.dss.generic.shared.ISingleDataSetPathInfoProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetPathInfo;

/**
 * Unit tests for {@link PathInfoProviderBasedHierarchicalContent}. The tests are based on
 * {@link DefaultFileBasedHierarchicalContentTest} that uses real files even though tests here work
 * with mocks and actual file structure is not important.
 * 
 * @author Piotr Buczek
 */
public class PathInfoProviderBasedHierarchicalContentTest extends AbstractFileSystemTestCase
{

    private File rootDir;

    private File file1;

    private File file2;

    private File subDir;

    private File subFile1;

    private File subFile2;

    private File subFile3;

    private File subSubDir;

    private File subSubFile;

    // mocks

    private Mockery context;

    private ISingleDataSetPathInfoProvider pathInfoProvider;

    private IDelegatedAction onCloseAction;

    @BeforeMethod
    public void beforeMethod() throws Exception
    {
        context = new Mockery();
        pathInfoProvider = context.mock(ISingleDataSetPathInfoProvider.class);
        onCloseAction = context.mock(IDelegatedAction.class);

        rootDir = new File(workingDirectory, "rootDir");
        rootDir.mkdirs();
        file1 = new File(rootDir, "file1");
        file2 = new File(rootDir, "file2");

        subDir = new File(rootDir, "subDir");
        subDir.mkdirs();
        subFile1 = new File(subDir, "subFile1");
        subFile2 = new File(subDir, "subFile2");
        subFile3 = new File(subDir, "subFile3");

        subSubDir = new File(subDir, "subSubDir");
        subSubDir.mkdirs();
        subSubFile = new File(subSubDir, "subSubFile");

        for (File f : Arrays.asList(file1, file2, subFile1, subFile2, subFile3, subSubFile))
        {
            FileUtilities.writeToFile(f, f.getName() + " data");
        }
    }

    @AfterMethod
    public void tearDown()
    {
        // To following line of code should also be called at the end of each test method.
        // Otherwise one do not known which test failed.
        context.assertIsSatisfied();
    }

    private IHierarchicalContent createContent(File file)
    {
        return new PathInfoProviderBasedHierarchicalContent(pathInfoProvider, file, onCloseAction);
    }

    @Test
    public void testCreateSucceedsWithNonExistentRoot()
    {
        // NOTE: we want to traverse through archived data set files even if they don't exist
        final File fakeFile = new File(workingDirectory, "fakeFile");
        createContent(fakeFile);

        context.assertIsSatisfied();
    }

    @Test
    public void testEqualsAndHashCode()
    {
        IHierarchicalContent rootContent = createContent(rootDir);
        IHierarchicalContent rootContentSameFile = createContent(rootDir);
        assertEquals(rootContent, rootContentSameFile);
        assertEquals(rootContent.hashCode(), rootContentSameFile.hashCode());

        File sameRootDir = new File(workingDirectory, "rootDir");
        IHierarchicalContent rootContentSameNewFile = createContent(sameRootDir);
        assertEquals(rootContent, rootContentSameNewFile);
        assertEquals(rootContent.hashCode(), rootContentSameNewFile.hashCode());

        File differentRootDir = new File(workingDirectory, "rootDir2");
        differentRootDir.mkdir();
        IHierarchicalContent rootContentDifferentFile = createContent(differentRootDir);
        assertFalse(rootContentDifferentFile.equals(rootContent));
        assertFalse(rootContent.hashCode() == rootContentDifferentFile.hashCode());

        context.assertIsSatisfied();
    }

    @Test
    public void testClose()
    {
        IHierarchicalContent rootContent = createContent(rootDir);

        context.checking(new Expectations()
            {
                {
                    one(onCloseAction).execute();
                }
            });
        rootContent.close();

        context.assertIsSatisfied();
    }

    @Test
    public void testGetRootNode()
    {
        final IHierarchicalContent rootContent = createContent(rootDir);
        prepareCreateRootNode(rootContent);

        IHierarchicalContentNode root1 = rootContent.getRootNode();
        assertSame(rootDir, root1.getFile());
        IHierarchicalContentNode root2 = rootContent.getRootNode();
        assertSame(root1, root2);

        context.assertIsSatisfied();
    }

    @Test
    public void testGetNodeWithBlankPath()
    {
        final IHierarchicalContent rootContent = createContent(rootDir);
        prepareCreateRootNode(rootContent);

        IHierarchicalContentNode rootNode = rootContent.getRootNode();
        IHierarchicalContentNode nullNode = rootContent.getNode(null);
        IHierarchicalContentNode emptyNode = rootContent.getNode("");
        checkNodeMatchesFile(rootNode, rootDir);
        assertSame(rootNode, nullNode);
        assertSame(rootNode, emptyNode);

        context.assertIsSatisfied();
    }

    @Test
    public void testGetNodeOfExistingFile()
    {
        final IHierarchicalContent rootContent = createContent(rootDir);

        final List<File> existingFiles =
                Arrays.asList(file1, file2, subDir, subFile1, subFile2, subFile3, subSubFile);

        context.checking(new Expectations()
            {
                {
                    for (File existingFile : existingFiles)
                    {
                        final String relativePath =
                                FileUtilities.getRelativeFilePath(rootDir, existingFile);
                        one(pathInfoProvider).tryGetPathInfoByRelativePath(relativePath);
                        will(returnValue(createDummyFileBasedPath(rootDir, existingFile)));
                    }
                }
            });
        for (File existingFile : existingFiles)
        {
            String relativePath = FileUtilities.getRelativeFilePath(rootDir, existingFile);
            IHierarchicalContentNode fileNode = rootContent.getNode(relativePath);
            assertEquals(relativePath, fileNode.getRelativePath());
            checkNodeMatchesFile(fileNode, existingFile);
        }

        context.assertIsSatisfied();
    }

    @Test
    public void testGetNodeOfNonExistingFileFails()
    {
        final IHierarchicalContent rootContent = createContent(rootDir);

        File fakeFile = new File(rootDir, "fakeFile");
        File subFakeFile = new File(subDir, "subFakeFile");
        File subFakeDir = new File(rootDir, "subFakeDir");
        File subFakeDirFile = new File(subFakeDir, "subFakeDirFile"); // dir doesn't exist too
        final List<File> nonExistingFiles =
                Arrays.asList(fakeFile, subFakeFile, subFakeDir, subFakeDirFile);

        for (final File nonExistingFile : nonExistingFiles)
        {
            final String relativePath = FileUtilities.getRelativeFilePath(rootDir, nonExistingFile);
            try
            {
                context.checking(new Expectations()
                    {
                        {
                            one(pathInfoProvider).tryGetPathInfoByRelativePath(relativePath);
                            will(returnValue(null));
                        }
                    });
                rootContent.getNode(relativePath);
                fail("expected IllegalArgumentException for non existent resource: " + relativePath);
            } catch (IllegalArgumentException ex)
            {
                assertEquals("Resource '" + relativePath + "' does not exist.", ex.getMessage());
            }
        }

        context.assertIsSatisfied();
    }

    @Test
    public void testListMatchingNodesWithRelativePathPattern()
    {
        final IHierarchicalContent rootContent = createContent(rootDir);

        // nothing matches
        final String nonMatchingPattern = ".*non-mathching-pattern.*";
        context.checking(new Expectations()
            {
                {
                    one(pathInfoProvider).listMatchingPathInfos(nonMatchingPattern);
                    will(returnValue(Collections.emptyList()));
                }
            });
        assertEquals(0, rootContent.listMatchingNodes(nonMatchingPattern).size());

        // some nodes match
        final String matchingPattern = "file.*";
        context.checking(new Expectations()
            {
                {
                    one(pathInfoProvider).listMatchingPathInfos(matchingPattern);
                    will(returnValue(Arrays.asList(createDummyFileBasedPath(rootDir, file1),
                            createDummyFileBasedPath(rootDir, subFile1),
                            createDummyFileBasedPath(rootDir, subSubFile))));
                }
            });
        List<IHierarchicalContentNode> matchingNodes =
                rootContent.listMatchingNodes(matchingPattern);
        assertEquals(3, matchingNodes.size());
        sortNodes(matchingNodes);
        assertEquals(file1, matchingNodes.get(0).getFile());
        assertEquals(subFile1, matchingNodes.get(1).getFile());
        assertEquals(subSubFile, matchingNodes.get(2).getFile());

        context.assertIsSatisfied();
    }

    @Test
    public void testListMatchingNodesWithStartingPath()
    {
        final IHierarchicalContent rootContent = createContent(rootDir);

        final String startingPath = "subDir";
        final String pattern = ".*[fF]ile.*";
        context.checking(new Expectations()
            {
                {
                    one(pathInfoProvider).listMatchingPathInfos(startingPath, pattern);
                    will(returnValue(Arrays.asList(createDummyFileBasedPath(rootDir, subFile1),
                            createDummyFileBasedPath(rootDir, subFile2),
                            createDummyFileBasedPath(rootDir, subFile3),
                            createDummyFileBasedPath(rootDir, subSubFile))));
                }
            });
        List<IHierarchicalContentNode> matchingNodes =
                rootContent.listMatchingNodes(startingPath, pattern);
        assertEquals(4, matchingNodes.size());
        sortNodes(matchingNodes);
        checkNodeMatchesFile(matchingNodes.get(0), subFile1);
        checkNodeMatchesFile(matchingNodes.get(1), subFile2);
        checkNodeMatchesFile(matchingNodes.get(2), subFile3);
        checkNodeMatchesFile(matchingNodes.get(3), subSubFile);

        context.assertIsSatisfied();
    }

    private void prepareCreateRootNode(final IHierarchicalContent rootContent)
    {
        context.checking(new Expectations()
            {
                {
                    one(pathInfoProvider).getRootPathInfo();
                    will(returnValue(createDummyFileBasedRootPath(rootDir)));
                }
            });
    }

    private static void checkNodeMatchesFile(IHierarchicalContentNode node, File expectedFile)
    {
        assertEquals(expectedFile, node.getFile());
    }

    private static void sortNodes(List<IHierarchicalContentNode> nodes)
    {
        HierarchicalContentUtils.sortNodes(nodes);
    }

    private static DataSetPathInfo createDummyFileBasedRootPath(final File root)
    {
        return createDummyFileBasedPath(root, root);
    }

    private static DataSetPathInfo createDummyFileBasedPath(final File root, final File file)
    {
        DataSetPathInfo result = new DataSetPathInfo();
        result.setFileName(file.getName());
        result.setDirectory(file.isDirectory());
        result.setRelativePath(FileUtilities.getRelativeFilePath(root, file));
        result.setSizeInBytes(file.length());
        return result;
    }

}
