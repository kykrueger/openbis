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

package ch.systemsx.cisd.common.utilities;

import java.io.File;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Some useful methods related to the operating system.
 * <p>
 * Does <em>not</em> depend on any library jar files. But before using or extending this class and if you do not mind
 * using <a href="http://jakarta.apache.org/commons/lang/">commons lang</a>, then have a look on
 * <code>SystemUtils</code>.
 * </p>
 * 
 * @author Bernd Rinn
 */
public class OSUtilities
{

    /** Platform specific line separator. */
    public static final String LINE_SEPARATOR = System.getProperty("line.separator");

    /**
     * @return <code>true</code> if the operating system is UNIX like.
     */
    public static boolean isUnix()
    {
        return (File.separatorChar == '/');
    }

    /**
     * @return <code>true</code> if the operating system is a MS Windows type.
     */
    public static boolean isWindows()
    {
        return (File.separatorChar == '\\');
    }

    /**
     * @return <code>true</code> if the the operating system is a flavor of MacOS.
     */
    public static boolean isMacOS()
    {
        return System.getProperty("java.vendor").startsWith("Apple");
    }

    /**
     * @return The name of user that runs this program.
     */
    public static String getUsername()
    {
        return System.getProperty("user.name");
    }

    /**
     * @return <code>true</code> if the user that runs this program is known to have root privileges (based on his
     *         name).
     */
    public static boolean isRoot()
    {
        if (isUnix())
        {
            return "root".equals(getUsername());
        } else
        {
            return "Administrator".equals(getUsername());
        }
    }

    /**
     * @return The <var>PATH</var> as provided by the operating system.
     */
    public static Set<String> getOSPath()
    {
        final String[] pathEntries = System.getenv("PATH").split(Pattern.quote(System.getProperty("path.separator")));
        return new LinkedHashSet<String>(Arrays.asList(pathEntries));
    }

    /**
     * @param root Whether the path should be prepared for root or not.
     * @return The path as provided by the operating system plus some path entries that should always be available.
     * @see #getOSPath()
     */
    public static Set<String> getSafeOSPath(boolean root)
    {
        final Set<String> pathEntries = getOSPath();
        if (isUnix())
        {
            pathEntries.add("/usr/local/bin");
            pathEntries.add("/usr/bin");
            pathEntries.add("/bin");
            if (root)
            {
                pathEntries.add("/usr/local/sbin");
                pathEntries.add("/usr/sbin");
                pathEntries.add("/sbin");
            }
            if (isMacOS())
            {
                pathEntries.add("/opt/local/bin"); // Darwin ports
                pathEntries.add("/sw/bin"); // Fink
                if (root)
                {
                    pathEntries.add("/opt/local/sbin");
                    pathEntries.add("/sw/sbin");
                }
            }
        }
        return pathEntries;
    }

    /**
     * Convenience method for {@link #getSafeOSPath(boolean)} with <code>root=false</code>.
     * 
     * @return The path as provided by the operating system plus some path entries that should always be available.
     * @see #getSafeOSPath(boolean)
     */
    public static Set<String> getSafeOSPath()
    {
        return getSafeOSPath(false);
    }

    /**
     * Search for the binary program with name <code>binaryName</code> in the operating system path..
     * 
     * @param executableName The name of the executable to search for. Under Windows, a name with and without
     *            <code>.exe</code> appended will work, but the executable found needs to have the .exe extension.
     * @return The binary file that has been found in the path, or <code>null</code>, if no binary file could be
     *         found.
     */
    public static File findExecutable(String executableName)
    {
        return OSUtilities.findExecutable(executableName, getSafeOSPath());
    }

    /**
     * Search for the binary program with name <code>binaryName</code> in the set of paths denoted by
     * <code>pathSet</code>.
     * 
     * @param executableName The name of the executable to search for. Under Windows, a name with and without
     *            <code>.exe</code> appended will work, but the executable found needs to have the .exe extension.
     * @param pathSet The set of paths to search for. It is recommended to use an ordered set like the
     *            {@link LinkedHashSet} here in order to get results that are independent of the JRE implementation.
     * @return The binary file that has been found in the path, or <code>null</code>, if no binary file could be
     *         found.
     */
    public static File findExecutable(String executableName, Set<String> pathSet)
    {
        final String executableNameWithExtension = addWindowsExecutableExtensionIfNecessary(executableName);
        for (String dir : pathSet)
        {
            final File fileToCheck = new File(dir, executableNameWithExtension);
            if (fileToCheck.exists())
            {
                return fileToCheck;
            }
        }
        return null;
    }

    /**
     * @return <code>true</code> if and only if an executable of name <var>executableName</var> exists.
     */
    public static boolean executableExists(String executableName)
    {
        return (new File(OSUtilities.addWindowsExecutableExtensionIfNecessary(executableName))).exists();
    }

    /**
     * @return <code>true</code> if and only if an executable of name <var>executableName</var> exists.
     */
    public static boolean executableExists(File executable)
    {
        return (new File(OSUtilities.addWindowsExecutableExtensionIfNecessary(executable.getPath()))).exists();
    }

    private static String addWindowsExecutableExtensionIfNecessary(String executableName)
    {
        if (isWindows() && executableName.indexOf('.') < 0)
        {
            return executableName + ".exe";
        } else
        {
            return executableName;
        }
    }

}
