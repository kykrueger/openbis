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

import org.testng.annotations.Test;

import static org.testng.AssertJUnit.*;

/**
 * Test cases for {@link SVNUtilities}.
 * 
 * @author Bernd Rinn
 */
public class SVNUtilitiesTest
{

    @Test
    public void testNormalizeUrlOnNormalizedURls()
    {
        final String normalizedFileUrl = "file:///home/cisd/repos/test/trunk";
        assertEquals(normalizedFileUrl, SVNUtilities.normalizeUrl(normalizedFileUrl));
        final String normalizedHTTPUrl = "http://home/cisd/repos/test/trunk";
        assertEquals(normalizedHTTPUrl, SVNUtilities.normalizeUrl(normalizedHTTPUrl));
    }

    @Test
    public void testNormalizeUrlOnNonNormalizedURls()
    {
        final String nonNormalizedFileUrl = "file:///home/cisd//repos/test///trunk//";
        final String normalizedFileUrl = "file:///home/cisd/repos/test/trunk";
        assertEquals(normalizedFileUrl, SVNUtilities.normalizeUrl(nonNormalizedFileUrl));
        final String nonNormalizedHTTPUrl = "http://home//cisd/repos/test/trunk/";
        final String normalizedHTTPUrl = "http://home/cisd/repos/test/trunk";
        assertEquals(normalizedHTTPUrl, SVNUtilities.normalizeUrl(nonNormalizedHTTPUrl));
    }

    @Test
    public void testNormalizeUrlOnNonNormalizedProtocolUrl()
    {
        final String nonNormalizedFileUrl = "file:////home/cisd/repos/test/trunk";
        final String normalizedFileUrl = "file:///home/cisd/repos/test/trunk";
        assertEquals(normalizedFileUrl, SVNUtilities.normalizeUrl(nonNormalizedFileUrl));
        final String nonNormalizedHTTPUrl = "http:///home/cisd/repos/test/trunk";
        final String normalizedHTTPUrl = "http://home/cisd/repos/test/trunk";
        assertEquals(normalizedHTTPUrl, SVNUtilities.normalizeUrl(nonNormalizedHTTPUrl));
        final String nonNormalizedHTTPSUrl = "https:///home/cisd/repos/test/trunk";
        final String normalizedHTTPSUrl = "https://home/cisd/repos/test/trunk";
        assertEquals(normalizedHTTPSUrl, SVNUtilities.normalizeUrl(nonNormalizedHTTPSUrl));
        final String nonNormalizedSVNUrl = "svn:///home/cisd/repos/test/trunk";
        final String normalizedSVNUrl = "svn://home/cisd/repos/test/trunk";
        assertEquals(normalizedSVNUrl, SVNUtilities.normalizeUrl(nonNormalizedSVNUrl));
        final String nonNormalizedSVNSSHUrl = "svn+ssh:///home/cisd/repos/test/trunk";
        final String normalizedSVNSSHUrl = "svn+ssh://home/cisd/repos/test/trunk";
        assertEquals(normalizedSVNSSHUrl, SVNUtilities.normalizeUrl(nonNormalizedSVNSSHUrl));
    }

    @Test
    public void testGetParent()
    {
        final String parentUrl = "file:///home/cisd/repos/test";
        final String url = parentUrl + "/trunk";
        assertEquals(parentUrl, SVNUtilities.getParent(url));
    }

    @Test
    public void testGetParentNonNormalized()
    {
        final String parentUrl = "file:///home/cisd/repos/test";
        final String url = parentUrl + "//trunk/";
        assertEquals(parentUrl, SVNUtilities.getParent(url));
    }

    @Test
    public void testGetTopLevelDirectory()
    {
        assertEquals("one", SVNUtilities.getTopLevelDirectory("/one/two/three"));
        assertEquals("one", SVNUtilities.getTopLevelDirectory("/one/"));
        assertEquals("one", SVNUtilities.getTopLevelDirectory("/one"));
    }

    @Test
    public void testGetBranchForTagRelease()
    {
        assertEquals("0.9.x", SVNUtilities.getBranchForTagRelease("0.9.0"));
        assertEquals("1.0.x", SVNUtilities.getBranchForTagRelease("1.0.10"));
        assertEquals("8.04.x", SVNUtilities.getBranchForTagRelease("8.04.10"));
    }

    @Test
    public void testGetBranchForTagSprint()
    {
        assertEquals("S4.x", SVNUtilities.getBranchForTagSprint("S4.0"));
        assertEquals("S11.x", SVNUtilities.getBranchForTagSprint("S11.0"));
        assertEquals("S12.x", SVNUtilities.getBranchForTagSprint("S12.10"));
    }

    @Test
    public void testGetFirstTagForReleaseBranch()
    {
        assertEquals("0.9.0", SVNUtilities.getFirstTagForBranch(
                SVNProjectVersionType.RELEASE_BRANCH, "0.9.x"));
        assertEquals("1.5.0", SVNUtilities.getFirstTagForBranch(
                SVNProjectVersionType.RELEASE_BRANCH, "1.5.x"));
        assertEquals("8.04.0", SVNUtilities.getFirstTagForBranch(
                SVNProjectVersionType.RELEASE_BRANCH, "8.04.x"));
    }

    @Test
    public void testGetFirstTagForSprintBranch()
    {
        assertEquals("S9.0", SVNUtilities.getFirstTagForBranch(
                SVNProjectVersionType.SPRINT_BRANCH, "S9.x"));
        assertEquals("S15.0", SVNUtilities.getFirstTagForBranch(
                SVNProjectVersionType.SPRINT_BRANCH, "S15.x"));
    }

}
