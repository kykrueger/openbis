/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.generic.server;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.utilities.PropertyParametersUtil;
import ch.systemsx.cisd.common.utilities.PropertyParametersUtil.SectionProperties;
import ch.systemsx.cisd.common.utilities.PropertyUtils;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.PluginServletConfig;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.DssPropertyParametersUtil;

/**
 * Configuration parameters for the Data Set Download Server.
 * 
 * @author Franz-Josef Elmer
 */
public final class ConfigParameters implements IServletPropertiesManager
{

    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            ConfigParameters.class);

    static final String SERVER_URL_KEY = "server-url";

    static final String PORT_KEY = "port";

    static final String STOREROOT_DIR_KEY = "storeroot-dir";

    static final String RPC_INCOMING_DIR = "rpc-incoming-dir";

    static final String SESSION_TIMEOUT_KEY = "session-timeout";

    static final String DOWNLOAD_URL = "download-url";

    private static final String KEYSTORE = "keystore.";

    static final String USE_SSL = "use-ssl";

    static final String USE_NIO = "use-nio-selector-socket";

    static final String AUTH_CACHE_EXPIRATION_TIME = "authorization-cache-expiration-time";

    static final int DEFAULT_AUTH_CACHE_EXPIRATION_TIME_MINS = 5;

    static final String AUTH_CACHE_CLEANUP_TIMER_PERIOD =
            "authorization-cache-cleanup-timer-period";

    static final int DEFAULT_AUTH_CACHE_CLEANUP_TIMER_PERIOD_MINS = 3 * 60;

    public static final String KEYSTORE_PATH_KEY = KEYSTORE + "path";

    public static final String KEYSTORE_PASSWORD_KEY = KEYSTORE + "password";

    public static final String KEYSTORE_KEY_PASSWORD_KEY = KEYSTORE + "key-password";

    static final String PLUGIN_SERVICES_LIST_KEY = "plugin-services";

    static final String PLUGIN_SERVICE_CLASS_KEY = "class";

    static final String PLUGIN_SERVICE_PATH_KEY = "path";

    /**
     * The path that contains the jars for webstart applications. This is used to initialize
     * ResourceHandler, which is used to serve the jars.
     * <p>
     * If no value is supplied, this defaults to lib.
     */
    static final String WEBSTART_JAR_PATH = "webstart-jar-path";

    static final String WEBSTART_JAR_PATH_DEFAULT = "lib";

    // PropertyParametersUtil

    private final File storePath;

    private final File dssInternalTempDir;

    private final File rpcIncomingDirectory;

    private final int port;

    private final String serverURL;

    private final String downloadURL;

    private final int sessionTimeout;

    private final boolean useSSL;

    private final boolean useNIO;

    private final String keystorePath;

    private final String keystorePassword;

    private final String keystoreKeyPassword;

    private final int authCacheExpirationTimeMins;

    private final int authCacheCleanupTimerPeriodMins;

    private final Map<String, PluginServletConfig> pluginServlets;

    private final Properties properties;

    private final String webstartJarPath;

    /**
     * Creates an instance based on the specified properties.
     * 
     * @throws ConfigurationFailureException if a property is missed or has an invalid value.
     */
    ConfigParameters(final Properties properties)
    {
        this.properties = properties;
        storePath = new File(PropertyUtils.getMandatoryProperty(properties, STOREROOT_DIR_KEY));
        dssInternalTempDir = getInternalTempDirectory(properties);
        rpcIncomingDirectory = getRpcIncomingDirectory(properties);
        port = getMandatoryIntegerProperty(properties, PORT_KEY);
        serverURL = PropertyUtils.getMandatoryProperty(properties, SERVER_URL_KEY);
        downloadURL = PropertyUtils.getMandatoryProperty(properties, DOWNLOAD_URL);
        sessionTimeout = getMandatoryIntegerProperty(properties, SESSION_TIMEOUT_KEY) * 60;
        useSSL = PropertyUtils.getBoolean(properties, USE_SSL, true);
        if (useSSL == true)
        {
            keystorePath = PropertyUtils.getMandatoryProperty(properties, KEYSTORE_PATH_KEY);
            keystorePassword =
                    PropertyUtils.getMandatoryProperty(properties, KEYSTORE_PASSWORD_KEY);
            keystoreKeyPassword =
                    PropertyUtils.getMandatoryProperty(properties, KEYSTORE_KEY_PASSWORD_KEY);
        } else
        {
            keystorePath = keystorePassword = keystoreKeyPassword = null;
        }
        useNIO = PropertyUtils.getBoolean(properties, USE_NIO, false);
        authCacheExpirationTimeMins =
                PropertyUtils.getInt(properties, AUTH_CACHE_EXPIRATION_TIME,
                        DEFAULT_AUTH_CACHE_EXPIRATION_TIME_MINS);
        authCacheCleanupTimerPeriodMins =
                PropertyUtils.getInt(properties, AUTH_CACHE_CLEANUP_TIMER_PERIOD,
                        DEFAULT_AUTH_CACHE_CLEANUP_TIMER_PERIOD_MINS);
        pluginServlets = new LinkedHashMap<String, PluginServletConfig>();
        SectionProperties[] pluginServicesProperties =
                PropertyParametersUtil.extractSectionProperties(properties,
                        PLUGIN_SERVICES_LIST_KEY, false);
        addServletsProperties("", pluginServicesProperties);

        webstartJarPath =
                PropertyUtils.getProperty(properties, WEBSTART_JAR_PATH, WEBSTART_JAR_PATH_DEFAULT);
    }

    private File getInternalTempDirectory(Properties properties)
    {
        return DssPropertyParametersUtil.getDssInternalTempDir(properties);
    }

    private static File getRpcIncomingDirectory(final Properties properties)
    {
        String incomingDirPath = PropertyUtils.getProperty(properties, RPC_INCOMING_DIR);
        if (null != incomingDirPath)
        {
            return new File(incomingDirPath);
        } else
        {
            return new File(System.getProperty("java.io.tmpdir"), "dss_rpc_incoming");
        }
    }

    public void addServletsProperties(String keyPrefix, SectionProperties[] servletsProperties)
    {
        for (SectionProperties sectionProperties : servletsProperties)
        {
            Properties servletProps = sectionProperties.getProperties();
            String key = keyPrefix + sectionProperties.getKey();
            addServletProperties(key, servletProps);
        }
    }

    public void addServletProperties(String propertiesName, Properties servletProperties)
    {
        String servletClass =
                PropertyUtils.getMandatoryProperty(servletProperties, PLUGIN_SERVICE_CLASS_KEY);
        String servletPath =
                PropertyUtils.getMandatoryProperty(servletProperties, PLUGIN_SERVICE_PATH_KEY);
        PluginServletConfig servletConfig =
                new PluginServletConfig(servletClass, servletPath, servletProperties);
        if (pluginServlets.containsKey(servletPath))
        {
            throw new ConfigurationFailureException("Servlet configuration [" + propertiesName
                    + "]: There has already been a servlet configured for the path '" + servletPath
                    + "'.");
        }
        pluginServlets.put(servletPath, servletConfig);
    }

    private final static int getMandatoryIntegerProperty(final Properties properties,
            final String key)
    {
        final String property = PropertyUtils.getMandatoryProperty(properties, key);
        try
        {
            return Integer.parseInt(property);
        } catch (final NumberFormatException ex)
        {
            throw new ConfigurationFailureException("Configuration parameter '" + key
                    + "' is not an integer number: " + property);
        }
    }

    public final File getStorePath()
    {
        return storePath;
    }

    public final File getRpcIncomingDirectory()
    {
        return rpcIncomingDirectory;
    }

    public File getDssInternalTempDir()
    {
        return dssInternalTempDir;
    }

    public final int getPort()
    {
        return port;
    }

    public final String getServerURL()
    {
        return serverURL;
    }

    public String getDownloadURL()
    {
        return downloadURL;
    }

    public final int getSessionTimeout()
    {
        return sessionTimeout;
    }

    public final String getKeystorePath()
    {
        return keystorePath;
    }

    public final String getKeystorePassword()
    {
        return keystorePassword;
    }

    public final String getKeystoreKeyPassword()
    {
        return keystoreKeyPassword;
    }

    public final List<PluginServletConfig> getPluginServlets()
    {
        return new ArrayList<PluginServletConfig>(pluginServlets.values());
    }

    public boolean isUseSSL()
    {
        return useSSL;
    }

    public boolean isUseNIO()
    {
        return useNIO;
    }

    public int getAuthCacheExpirationTimeMins()
    {
        return authCacheExpirationTimeMins;
    }

    public int getAuthCacheCleanupTimerPeriodMins()
    {
        return authCacheCleanupTimerPeriodMins;
    }

    public Properties getProperties()
    {
        return properties;
    }

    public String getWebstartJarPath()
    {
        return webstartJarPath;
    }

    public final void log()
    {
        if (operationLog.isInfoEnabled())
        {
            operationLog.info(String.format("Store root directory: '%s'.", storePath));
            operationLog.info(String.format("Temp file directory: '%s'.", dssInternalTempDir));
            operationLog.info(String.format("RPC incoming directory: '%s'.", rpcIncomingDirectory));
            operationLog.info(String.format("Port number: %d.", port));
            operationLog.info(String.format("URL of openBIS server: '%s'.", serverURL));
            operationLog.info(String.format("Session timeout (seconds): %d.", sessionTimeout));
            operationLog.info(String.format("Use SSL: %s.", useSSL));
            operationLog.info(String.format("Use NIO sockets: %s", useNIO));
            operationLog.info(String.format("Authorization cache expiration time (minutes): %s",
                    authCacheExpirationTimeMins));
            operationLog.info(String.format(
                    "Authorization cache cleanup timer period (minutes): %s",
                    authCacheCleanupTimerPeriodMins));
            operationLog.info(String.format("Keystore path: '%s'.", keystorePath));
            for (PluginServletConfig pluginServlet : getPluginServlets())
            {
                operationLog.info("Plugin servlet: " + pluginServlet);
            }
        }
    }

}
