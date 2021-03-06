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

package ch.systemsx.cisd.ant.task.subversion;

import static org.testng.AssertJUnit.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.testng.annotations.Test;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.ant.common.StringUtils;

/**
 * Test cases for the {@link SVNBranchAndTagTask}.
 * 
 * @author Bernd Rinn
 */
@Friend(toClasses = SVNBranchAndTagTask.class)
public class SVNBranchAndTagTaskTest
{

    private static final class CopyItem
    {
        final String sourcePath;

        final String sourceRevision;

        final String destinationPath;

        final String logMessage;

        CopyItem(final String sourcePath, final String sourceRevision,
                final String destinationPath, final String logMessage)
        {
            assert sourcePath != null;
            assert sourceRevision != null;
            assert destinationPath != null;
            assert logMessage != null;

            this.sourcePath = sourcePath;
            this.sourceRevision = sourceRevision;
            this.destinationPath = destinationPath;
            this.logMessage = logMessage;
        }

        @Override
        public boolean equals(final Object obj)
        {
            if (obj instanceof CopyItem == false)
            {
                return false;
            }
            final CopyItem that = (CopyItem) obj;
            return this.sourcePath.equals(that.sourcePath)
                    && this.sourceRevision.equals(that.sourceRevision)
                    && this.destinationPath.equals(that.destinationPath)
                    && this.logMessage.equals(that.logMessage);
        }

        @Override
        public int hashCode()
        {
            return ((this.sourcePath.hashCode() * 37 + this.sourceRevision.hashCode()) * 37 + this.destinationPath
                    .hashCode())
                    * 37 + this.logMessage.hashCode();
        }

        @Override
        public String toString()
        {
            return String
                    .format(
                            "CopyItem: (sourcePath=%s, sourceRevision=%s, destinationPath=%s, logMessage=%s)",
                            sourcePath, sourceRevision, destinationPath, logMessage);
        }
    }

    private static final class MkdirItem
    {
        final String path;

        final String logMessage;

        MkdirItem(final String path, final String logMessage)
        {
            assert path != null;
            assert logMessage != null;

            this.path = path;
            this.logMessage = logMessage;
        }

        @Override
        public boolean equals(final Object obj)
        {
            if (obj instanceof MkdirItem == false)
            {
                return false;
            }
            final MkdirItem that = (MkdirItem) obj;
            return this.path.equals(that.path) && this.logMessage.equals(that.logMessage);
        }

        @Override
        public int hashCode()
        {
            return path.hashCode() * 37 + logMessage.hashCode();
        }

        @Override
        public String toString()
        {
            return "MkdirItem: (path=" + path + ", logMessage=" + logMessage + ")";
        }
    }

    private static final class MockSVNRepositoryActions implements ISVNActions
    {
        final Map<String, List<String>> listMap;

        final Map<String, String> catMap;

        List<MkdirItem> mkdirList = new ArrayList<MkdirItem>();

        List<CopyItem> copyList = new ArrayList<CopyItem>();

        public MockSVNRepositoryActions(final Map<String, List<String>> listMap,
                final Map<String, String> catMap)
        {
            super();
            this.listMap = listMap;
            this.catMap = catMap;
        }

        @Override
        public String cat(final String path) throws SVNException
        {
            System.out.println("CAT - " + path);
            final String result = catMap.get(path);
            return result == null ? "" : result;
        }

        @Override
        public List<String> list(final String path) throws SVNException
        {
            System.out.println("LIST - " + path);
            final List<String> result = listMap.get(path);
            assert result != null : "Path '" + path + "' does not exist.";
            return result;
        }

        @Override
        public void mkdir(final String path, final String logMessage) throws SVNException
        {
            mkdirList.add(new MkdirItem(path, logMessage));
        }

        @Override
        public void copy(final String sourcePath, final String sourceRevision,
                final String destinationPath, final String logMessage) throws SVNException
        {
            copyList.add(new CopyItem(sourcePath, sourceRevision, destinationPath, logMessage));
        }

        @Override
        public SVNInfoRecord info(final String pathOrUrl)
        {
            throw new AssertionError("Unexpected call info()");
        }

        @Override
        public List<SVNItemStatus> status(final String path)
        {
            throw new AssertionError("Unexpected call status()");
        }

        @Override
        public boolean isMuccAvailable()
        {
            return false;
        }

        @Override
        public void mucc(final String logMessage, final String... args) throws SVNException
        {
            throw new AssertionError();
        }
    }

    @Test
    public void testCreateReleaseBranch()
    {
        final String repositoryRoot = "http://host/repos";
        final String groupName = "group";
        final String projectName = "testProject";
        final String branchName = "1.2.x";
        final Map<String, List<String>> listMap = new HashMap<String, List<String>>();
        final String sourceUrl =
                StringUtils.join(Arrays.asList(repositoryRoot, groupName, projectName, "trunk"),
                        "/");
        final String releaseBranchesUrl =
                StringUtils.join(Arrays.asList(repositoryRoot, groupName, projectName,
                        "branches/release"), "/");
        listMap.put(sourceUrl, Collections.<String> emptyList());
        listMap.put(releaseBranchesUrl, Collections.<String> emptyList());
        final Map<String, String> catMap = new HashMap<String, String>();
        final MockSVNRepositoryActions svn = new MockSVNRepositoryActions(listMap, catMap);
        final SVNBranchAndTagTask task = new SVNBranchAndTagTask()
            {
                @Override
                ISVNActions createSVNActions()
                {
                    return svn;
                }
            };
        task.setProject(new Project()); // Required for log not to throw a NPE.
        task.setRepositoryRoot(repositoryRoot);
        task.setGroup(groupName);
        task.setName(projectName);
        task.setReleaseBranch(branchName);
        task.execute();
        final String branchUrl =
                StringUtils.join(Arrays.asList(repositoryRoot, groupName, projectName,
                        "branches/release", branchName), "/");
        final String tagBaseUrl =
                StringUtils.join(Arrays.asList(repositoryRoot, groupName, projectName,
                        "tags/release", branchName), "/");
        final String logMessage = "Create branch '" + branchName + "'";
        final List<MkdirItem> expectedMkDirList =
                Arrays.asList(new MkdirItem(branchUrl, logMessage), new MkdirItem(tagBaseUrl,
                        logMessage));
        assertEquals(expectedMkDirList, svn.mkdirList);
        final String branchMainUrl = branchUrl + "/" + projectName;
        final String branchBuildResourcesUrl =
                branchUrl + "/" + SVNUtilities.BUILD_RESOURCES_PROJECT;
        final String sourceBuildResourcesUrl =
                StringUtils.join(Arrays.asList(repositoryRoot, groupName,
                        SVNUtilities.BUILD_RESOURCES_PROJECT, "trunk"), "/");
        final Set<CopyItem> expectedCopySet =
                new HashSet<CopyItem>(Arrays.asList(new CopyItem(sourceBuildResourcesUrl,
                        SVNUtilities.HEAD_REVISION, branchBuildResourcesUrl, logMessage),
                        new CopyItem(sourceUrl, SVNUtilities.HEAD_REVISION, branchMainUrl,
                                logMessage)));
        assertEquals(expectedCopySet, new HashSet<CopyItem>(svn.copyList));
    }

    @Test
    public void testCreateFeatureBranchWithDependency()
    {
        final String repositoryRoot = "http://host/repos";
        final String groupName = "group";
        final String projectName = "testProject";
        final String dependentProjectName = "someDependentProject";
        final String branchName = "someFeature";
        final Map<String, List<String>> listMap = new HashMap<String, List<String>>();
        final String sourceUrl =
                StringUtils.join(Arrays.asList(repositoryRoot, groupName, projectName, "trunk"),
                        "/");
        final String sourceUrlDependent =
                StringUtils.join(Arrays.asList(repositoryRoot, groupName, dependentProjectName,
                        "trunk"), "/");
        final String featureBranchesUrl =
                StringUtils.join(Arrays.asList(repositoryRoot, groupName, projectName,
                        "branches/feature"), "/");
        listMap.put(sourceUrl, Collections.singletonList(".classpath"));
        listMap.put(sourceUrlDependent, Collections.<String> emptyList());
        listMap.put(featureBranchesUrl, Collections.<String> emptyList());
        final String classPathFileContent =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                        + "<classpath>\n"
                        + String
                                .format(
                                        "   <classpathentry combineaccessrules=\"false\" kind=\"src\" path=\"/%s\"/>\n",
                                        dependentProjectName) + "</classpath>\n";
        final Map<String, String> catMap = new HashMap<String, String>();
        catMap.put(sourceUrl + "/.classpath", classPathFileContent);
        final MockSVNRepositoryActions svn = new MockSVNRepositoryActions(listMap, catMap);
        final SVNBranchAndTagTask task = new SVNBranchAndTagTask()
            {
                @Override
                ISVNActions createSVNActions()
                {
                    return svn;
                }
            };
        task.setProject(new Project()); // Required for log not to throw a NPE.
        task.setRepositoryRoot(repositoryRoot);
        task.setGroup(groupName);
        task.setName(projectName);
        task.setFeatureBranch(branchName);
        task.execute();
        final String branchUrl =
                StringUtils.join(Arrays.asList(repositoryRoot, groupName, projectName,
                        "branches/feature", branchName), "/");
        final String logMessage = "Create branch '" + branchName + "'";
        final List<MkdirItem> expectedMkDirList =
                Arrays.asList(new MkdirItem(branchUrl, logMessage));
        assertEquals(expectedMkDirList, svn.mkdirList);
        final String branchMainUrl = branchUrl + "/" + projectName;
        final String branchBuildResourcesUrl =
                branchUrl + "/" + SVNUtilities.BUILD_RESOURCES_PROJECT;
        final String branchDependendUrl = branchUrl + "/" + dependentProjectName;
        final String sourceBuildResourcesUrl =
                StringUtils.join(Arrays.asList(repositoryRoot, groupName,
                        SVNUtilities.BUILD_RESOURCES_PROJECT, "trunk"), "/");
        final Set<CopyItem> expectedCopySet =
                new HashSet<CopyItem>(Arrays.asList(new CopyItem(sourceBuildResourcesUrl,
                        SVNUtilities.HEAD_REVISION, branchBuildResourcesUrl, logMessage),
                        new CopyItem(sourceUrl, SVNUtilities.HEAD_REVISION, branchMainUrl,
                                logMessage), new CopyItem(sourceUrlDependent,
                                SVNUtilities.HEAD_REVISION, branchDependendUrl, logMessage)));
        assertEquals(expectedCopySet, new HashSet<CopyItem>(svn.copyList));
    }

    @Test
    public void testCreateReleaseTag()
    {
        final String repositoryRoot = "http://host/repos";
        final String groupName = "group";
        final String projectName = "testProject";
        final String tagName = "1.2.2";
        final String branchName = "1.2.x";
        final Map<String, List<String>> listMap = new HashMap<String, List<String>>();
        final String releaseBranchesUrl =
                StringUtils.join(Arrays.asList(repositoryRoot, groupName, projectName,
                        "branches/release"), "/");
        final String releaseTagsUrl =
            StringUtils.join(Arrays.asList(repositoryRoot, groupName, projectName,
                    "tags/release"), "/");
        listMap.put(releaseBranchesUrl, Collections.singletonList(branchName + "/"));
        listMap.put(releaseTagsUrl, Collections.singletonList(branchName + "/"));
        final Map<String, String> catMap = new HashMap<String, String>();
        final MockSVNRepositoryActions svn = new MockSVNRepositoryActions(listMap, catMap);
        final SVNBranchAndTagTask task = new SVNBranchAndTagTask()
            {
                @Override
                ISVNActions createSVNActions()
                {
                    return svn;
                }
            };
        task.setProject(new Project()); // Required for log not to throw a NPE.
        task.setRepositoryRoot(repositoryRoot);
        task.setGroup(groupName);
        task.setName(projectName);
        task.setReleaseTag(tagName);
        task.execute();
        final String tagUrlSuper =
            StringUtils.join(Arrays.asList(repositoryRoot, groupName, projectName,
                    "tags/release", branchName), "/");
        final String tagUrl =
                StringUtils.join(Arrays.asList(tagUrlSuper, tagName), "/");
        final String branchUrl =
                StringUtils.join(Arrays.asList(repositoryRoot, groupName, projectName,
                        "branches/release", branchName), "/");
        final String logMessage = "Create tag '" + tagName + "'";
        assertEquals(0, svn.mkdirList.size());
        final Set<CopyItem> expectedCopySet =
                new HashSet<CopyItem>(Collections.singletonList(new CopyItem(branchUrl,
                        SVNUtilities.HEAD_REVISION, tagUrl, logMessage)));
        assertEquals(expectedCopySet, new HashSet<CopyItem>(svn.copyList));
    }

    @Test
    public void testCreateSprintTag()
    {
        final String repositoryRoot = "http://host/repos";
        final String groupName = "group";
        final String projectName = "testProject";
        final String tagName = "S42.0";
        final String branchName = "S42.x";
        final Map<String, List<String>> listMap = new HashMap<String, List<String>>();
        final String tagUrl =
                StringUtils.join(Arrays.asList(repositoryRoot, groupName, projectName,
                        "tags/sprint"), "/");
        List<String> folders = new ArrayList<String>();
        folders.add("S41.x/");
        listMap.put(tagUrl, folders);
        final String branchUrl =
                StringUtils.join(Arrays.asList(repositoryRoot, groupName, projectName,
                        "branches/sprint"), "/");
        listMap.put(branchUrl, Collections.singletonList("S42.x/"));
        final Map<String, String> catMap = new HashMap<String, String>();
        final MockSVNRepositoryActions svn = new MockSVNRepositoryActions(listMap, catMap);
        final SVNBranchAndTagTask task = new SVNBranchAndTagTask()
            {
                @Override
                ISVNActions createSVNActions()
                {
                    return svn;
                }
            };
        task.setProject(new Project()); // Required for log not to throw a NPE.
        task.setRepositoryRoot(repositoryRoot);
        task.setGroup(groupName);
        task.setName(projectName);
        task.setSprintTag(tagName);
        task.execute();
        final String sourceMainUrl =
                StringUtils.join(Arrays.asList(repositoryRoot, groupName, projectName,
                        "branches/sprint", branchName), "/");
        final String targetSuperUrl =
            StringUtils.join(Arrays.asList(repositoryRoot, groupName, projectName,
                    "tags/sprint", branchName), "/");
        final String targetUrl =
                StringUtils.join(Arrays.asList(targetSuperUrl, tagName), "/");
        final String logMessage = "Create tag '" + tagName + "'";

        assertEquals(1, svn.mkdirList.size());
        assertEquals(new MkdirItem(targetSuperUrl, logMessage), svn.mkdirList.get(0));

        final Set<CopyItem> expectedCopySet =
                new HashSet<CopyItem>(Arrays.asList(new CopyItem(sourceMainUrl,
                        SVNUtilities.HEAD_REVISION, targetUrl, logMessage)));
        assertEquals(expectedCopySet, new HashSet<CopyItem>(svn.copyList));
    }

    @Test
    public void testCreateSprintBranchAndTagWithSubversion()
    {
        final String repositoryRoot = "http://host/repos";
        final String groupName = "group";
        final String projectName = "testProject";
        final String branchName = "S42.x";
        final String tagName = "S42.0";
        final Map<String, List<String>> listMap = new HashMap<String, List<String>>();
        final String tagUrl =
                StringUtils.join(Arrays.asList(repositoryRoot, groupName, projectName,
                        "tags/sprint"), "/");
        List<String> folders = new ArrayList<String>();
        folders.add("S41/");
        folders.add("42/");
        folders.add("S43/");
        listMap.put(tagUrl, folders);
        final String branchUrl =
                StringUtils.join(Arrays.asList(repositoryRoot, groupName, projectName,
                        "branches/sprint"), "/");
        listMap.put(branchUrl, Collections.<String> emptyList());
        final Map<String, String> catMap = new HashMap<String, String>();
        final MockSVNRepositoryActions svn = new MockSVNRepositoryActions(listMap, catMap);
        final SVNBranchAndTagTask task = new SVNBranchAndTagTask()
            {
                @Override
                ISVNActions createSVNActions()
                {
                    return svn;
                }
            };
        task.setProject(new Project()); // Required for log not to throw a NPE.
        task.setRepositoryRoot(repositoryRoot);
        task.setGroup(groupName);
        task.setName(projectName);
        task.setSprintTag(tagName);
        task.setBranchIfNecessary(true);
        task.execute();
        final String sourceMainUrl =
                StringUtils.join(Arrays.asList(repositoryRoot, groupName, projectName, "trunk"),
                        "/");
        final String targetUrlBranch =
                StringUtils.join(Arrays.asList(repositoryRoot, groupName, projectName,
                        "branches/sprint", branchName), "/");
        final String targetUrlTag =
                StringUtils.join(Arrays.asList(repositoryRoot, groupName, projectName,
                        "tags/sprint", branchName, tagName), "/");
        final String targetBuildResourcesUrl =
                targetUrlBranch + "/" + SVNUtilities.BUILD_RESOURCES_PROJECT;
        final String targetMainUrl =
                StringUtils.join(Arrays.asList(targetUrlBranch, projectName), "/");
        final String logMessageBranch = "Create branch '" + branchName + "'";
        final String logMessageTag = "Create tag '" + tagName + "'";

        assertEquals(2, svn.mkdirList.size());
        assertEquals("http://host/repos/group/testProject/branches/sprint/S42.x", svn.mkdirList
                .get(0).path);
        assertEquals("http://host/repos/group/testProject/tags/sprint/S42.x", svn.mkdirList
                .get(1).path);

        final String sourceBuildResourcesUrl =
                StringUtils.join(Arrays.asList(repositoryRoot, groupName,
                        SVNUtilities.BUILD_RESOURCES_PROJECT, "trunk"), "/");
        final Set<CopyItem> expectedCopySet =
                new HashSet<CopyItem>(Arrays.asList(new CopyItem(sourceBuildResourcesUrl,
                        SVNUtilities.HEAD_REVISION, targetBuildResourcesUrl, logMessageBranch),
                        new CopyItem(sourceMainUrl, SVNUtilities.HEAD_REVISION, targetMainUrl,
                                logMessageBranch), new CopyItem(targetUrlBranch,
                                SVNUtilities.HEAD_REVISION, targetUrlTag, logMessageTag)));
        assertEquals(expectedCopySet, new HashSet<CopyItem>(svn.copyList));
    }

    @Test(expectedExceptions =
        { BuildException.class })
    public void testCreateSprintBranchAndTagButTagDoesntStartBranch()
    {
        final String repositoryRoot = "http://host/repos";
        final String groupName = "group";
        final String projectName = "testProject";
        final String tagName = "S42.2";
        final Map<String, List<String>> listMap = new HashMap<String, List<String>>();
        final String tagUrl =
                StringUtils.join(Arrays.asList(repositoryRoot, groupName, projectName,
                        "tags/sprint"), "/");
        List<String> folders = new ArrayList<String>();
        folders.add("S41/");
        folders.add("42/");
        folders.add("S43/");
        listMap.put(tagUrl, folders);
        final String branchUrl =
                StringUtils.join(Arrays.asList(repositoryRoot, groupName, projectName,
                        "branches/sprint"), "/");
        listMap.put(branchUrl, Collections.<String> emptyList());
        final Map<String, String> catMap = new HashMap<String, String>();
        final MockSVNRepositoryActions svn = new MockSVNRepositoryActions(listMap, catMap);
        final SVNBranchAndTagTask task = new SVNBranchAndTagTask()
            {
                @Override
                ISVNActions createSVNActions()
                {
                    return svn;
                }
            };
        task.setProject(new Project()); // Required for log not to throw a NPE.
        task.setRepositoryRoot(repositoryRoot);
        task.setGroup(groupName);
        task.setName(projectName);
        task.setSprintTag(tagName);
        task.setBranchIfNecessary(true);
        task.execute();
    }

    @Test(expectedExceptions =
        { BuildException.class })
    public void testCreateSprintTagAlreadyExisting()
    {
        final String repositoryRoot = "http://host/repos";
        final String groupName = "group";
        final String projectName = "testProject";
        final String tagName = "S42";
        final Map<String, List<String>> listMap = new HashMap<String, List<String>>();
        final String tagUrl =
                StringUtils.join(Arrays.asList(repositoryRoot, groupName, projectName,
                        "tags/sprint"), "/");
        listMap.put(tagUrl, Collections.singletonList(tagName + "/"));
        final Map<String, String> catMap = new HashMap<String, String>();
        final MockSVNRepositoryActions svn = new MockSVNRepositoryActions(listMap, catMap);
        final SVNBranchAndTagTask task = new SVNBranchAndTagTask()
            {
                @Override
                ISVNActions createSVNActions()
                {
                    return svn;
                }
            };
        task.setProject(new Project()); // Required for log not to throw a NPE.
        task.setRepositoryRoot(repositoryRoot);
        task.setGroup(groupName);
        task.setName(projectName);
        task.setSprintTag(tagName);
        task.execute();
    }

    @Test(expectedExceptions =
        { BuildException.class })
    public void testCreateReleaseTagWithBranchMissing()
    {
        final String repositoryRoot = "http://host/repos";
        final String groupName = "group";
        final String projectName = "testProject";
        final String tagName = "1.2.0";
        final Map<String, List<String>> listMap = new HashMap<String, List<String>>();
        final String releaseBranchesUrl =
                StringUtils.join(Arrays.asList(repositoryRoot, groupName, projectName,
                        "branches/release"), "/");
        listMap.put(releaseBranchesUrl, Collections.<String> emptyList());
        final Map<String, String> catMap = new HashMap<String, String>();
        final MockSVNRepositoryActions svn = new MockSVNRepositoryActions(listMap, catMap);
        final SVNBranchAndTagTask task = new SVNBranchAndTagTask()
            {
                @Override
                ISVNActions createSVNActions()
                {
                    return svn;
                }
            };
        task.setProject(new Project()); // Required for log not to throw a NPE.
        task.setRepositoryRoot(repositoryRoot);
        task.setGroup(groupName);
        task.setName(projectName);
        task.setReleaseTag(tagName);
        task.execute();
    }

    @Test
    public void testCreateReleaseTagWithBranchCreation()
    {
        final String repositoryRoot = "http://host/repos";
        final String groupName = "group";
        final String projectName = "testProject";
        final String tagName = "1.2.0";
        final String branchName = "1.2.x";
        final Map<String, List<String>> listMap = new HashMap<String, List<String>>();
        final String sourceUrl =
                StringUtils.join(Arrays.asList(repositoryRoot, groupName, projectName, "trunk"),
                        "/");
        final String releaseBranchesUrl =
                StringUtils.join(Arrays.asList(repositoryRoot, groupName, projectName,
                        "branches/release"), "/");
        final String tagsBranchesUrl =
            StringUtils.join(Arrays.asList(repositoryRoot, groupName, projectName,
                    "tags/release"), "/");
        listMap.put(releaseBranchesUrl, Collections.<String> emptyList());
        listMap.put(tagsBranchesUrl, Collections.singletonList(branchName + "/"));
        final Map<String, String> catMap = new HashMap<String, String>();
        final MockSVNRepositoryActions svn = new MockSVNRepositoryActions(listMap, catMap);
        final SVNBranchAndTagTask task = new SVNBranchAndTagTask()
            {
                @Override
                ISVNActions createSVNActions()
                {
                    return svn;
                }
            };
        task.setProject(new Project()); // Required for log not to throw a NPE.
        task.setRepositoryRoot(repositoryRoot);
        task.setGroup(groupName);
        task.setName(projectName);
        task.setReleaseTag(tagName);
        task.setBranchIfNecessary(true);
        task.execute();
        final String tagBaseUrl =
                StringUtils.join(Arrays.asList(repositoryRoot, groupName, projectName,
                        "tags/release", branchName), "/");
        final String tagUrl = tagBaseUrl + "/" + tagName;
        final String branchUrl =
                StringUtils.join(Arrays.asList(repositoryRoot, groupName, projectName,
                        "branches/release", branchName), "/");
        final String logMessageBranch = "Create branch '" + branchName + "'";
        final String logMessageTag = "Create tag '" + tagName + "'";
        final List<MkdirItem> expectedMkDirList =
                Arrays.asList(new MkdirItem(branchUrl, logMessageBranch), new MkdirItem(tagBaseUrl,
                        logMessageBranch));
        assertEquals(expectedMkDirList, svn.mkdirList);
        final String branchMainUrl = branchUrl + "/" + projectName;
        final String branchBuildResourcesUrl =
                branchUrl + "/" + SVNUtilities.BUILD_RESOURCES_PROJECT;
        final String sourceBuildResourcesUrl =
                StringUtils.join(Arrays.asList(repositoryRoot, groupName,
                        SVNUtilities.BUILD_RESOURCES_PROJECT, "trunk"), "/");
        final List<CopyItem> expectedCopyList =
                Arrays.asList(new CopyItem(sourceUrl, SVNUtilities.HEAD_REVISION, branchMainUrl,
                        logMessageBranch), new CopyItem(sourceBuildResourcesUrl,
                        SVNUtilities.HEAD_REVISION, branchBuildResourcesUrl, logMessageBranch),
                        new CopyItem(branchUrl, SVNUtilities.HEAD_REVISION, tagUrl, logMessageTag));
        assertEquals(expectedCopyList, svn.copyList);
    }

    @Test(expectedExceptions =
        { BuildException.class })
    public void testCreateReleaseTagWithBranchCreationButIllegalTag()
    {
        final String repositoryRoot = "http://host/repos";
        final String groupName = "group";
        final String projectName = "testProject";
        final String tagName = "1.2.1";
        final Map<String, List<String>> listMap = new HashMap<String, List<String>>();
        final String releaseBranchesUrl =
                StringUtils.join(Arrays.asList(repositoryRoot, groupName, projectName,
                        "branches/release"), "/");
        listMap.put(releaseBranchesUrl, Collections.<String> emptyList());
        final Map<String, String> catMap = new HashMap<String, String>();
        final MockSVNRepositoryActions svn = new MockSVNRepositoryActions(listMap, catMap);
        final SVNBranchAndTagTask task = new SVNBranchAndTagTask()
            {
                @Override
                ISVNActions createSVNActions()
                {
                    return svn;
                }
            };
        task.setProject(new Project()); // Required for log not to throw a NPE.
        task.setRepositoryRoot(repositoryRoot);
        task.setGroup(groupName);
        task.setName(projectName);
        task.setReleaseTag(tagName);
        task.setBranchIfNecessary(true);
        task.execute();
    }
}
