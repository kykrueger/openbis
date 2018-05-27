package ch.systemsx.cisd.openbis.generic.server;

import java.io.File;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.testng.annotations.Test;

import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;

public class SessionWorkspaceProviderTest extends AbstractFileSystemTestCase
{

    @Test
    public void testGetSessionWorkspace() throws Exception
    {
        Properties properties = new Properties();
        properties.setProperty(SessionWorkspaceProvider.SESSION_WORKSPACE_ROOT_DIR_KEY, workingDirectory.getPath());

        SessionWorkspaceProvider provider = new SessionWorkspaceProvider(properties);
        provider.init();

        Map<String, File> sessionWorkspaces = provider.getSessionWorkspaces();
        assertEquals("[]", sessionWorkspaces.keySet().toString());

        File sessionWorkspace = provider.getSessionWorkspace("token");

        sessionWorkspaces = provider.getSessionWorkspaces();
        assertEquals("[token]", sessionWorkspaces.keySet().toString());

        assertEquals(true, sessionWorkspace.exists());
        assertEquals(workingDirectory, sessionWorkspace.getParentFile());
    }

    @Test
    public void testGetSessionWorkspaces() throws Exception
    {
        Properties properties = new Properties();
        properties.setProperty(SessionWorkspaceProvider.SESSION_WORKSPACE_ROOT_DIR_KEY, workingDirectory.getPath());

        SessionWorkspaceProvider provider = new SessionWorkspaceProvider(properties);
        provider.init();

        Map<String, File> sessionWorkspaces = provider.getSessionWorkspaces();
        assertEquals("[]", sessionWorkspaces.keySet().toString());

        provider.getSessionWorkspace("token1");
        provider.getSessionWorkspace("token2");

        sessionWorkspaces = provider.getSessionWorkspaces();
        assertEquals("[token1, token2]", sessionWorkspaces.keySet().toString());

        provider.deleteSessionWorkspace("token1");

        sessionWorkspaces = provider.getSessionWorkspaces();
        assertEquals("[token2]", sessionWorkspaces.keySet().toString());
    }

    @Test
    public void testDeleteSessionWorkspace() throws Exception
    {
        Properties properties = new Properties();
        properties.setProperty(SessionWorkspaceProvider.SESSION_WORKSPACE_ROOT_DIR_KEY, workingDirectory.getPath());

        SessionWorkspaceProvider provider = new SessionWorkspaceProvider(properties);
        provider.init();

        File workspace1 = provider.getSessionWorkspace("token1");
        File workspace2 = provider.getSessionWorkspace("token2");

        FileUtils.writeStringToFile(new File(workspace1, "file1A"), "1A");
        FileUtils.writeStringToFile(new File(workspace1, "file1B"), "1B");
        FileUtils.writeStringToFile(new File(workspace2, "file2"), "2");

        Map<String, File> sessionWorkspaces = provider.getSessionWorkspaces();
        assertEquals("[token1, token2]", sessionWorkspaces.keySet().toString());

        provider.deleteSessionWorkspace("token1");

        sessionWorkspaces = provider.getSessionWorkspaces();
        assertEquals("[token2]", sessionWorkspaces.keySet().toString());

        provider.deleteSessionWorkspace("token2");

        sessionWorkspaces = provider.getSessionWorkspaces();
        assertEquals("[]", sessionWorkspaces.keySet().toString());

        provider.deleteSessionWorkspace("token3");

        sessionWorkspaces = provider.getSessionWorkspaces();
        assertEquals("[]", sessionWorkspaces.keySet().toString());
    }

}
