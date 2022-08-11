package ch.qos.logback.classic.util;

import ch.qos.logback.classic.ClassicConstants;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.Configurator;
import ch.qos.logback.core.LogbackException;
import ch.qos.logback.core.spi.ContextAwareBase;
import ch.qos.logback.core.status.InfoStatus;
import ch.qos.logback.core.status.StatusManager;
import ch.qos.logback.core.util.Loader;
import ch.qos.logback.core.util.OptionHelper;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Set;

/**
 * @since 1.3.0-beta1
 */
public class DefaultJoranConfigurator extends ContextAwareBase implements Configurator {

    final public static String AUTOCONFIG_FILE = "logback.xml";
    final public static String TEST_AUTOCONFIG_FILE = "logback-test.xml";

    @Override
    public ExecutionStatus configure(LoggerContext loggerContext) {
        URL url = findURLOfDefaultConfigurationFile(true);
        if (url != null) {
            try {
                configureByResource(url);
            } catch (JoranException e) {
                e.printStackTrace();
            }
            // we tried and that counts Mary.
            return ExecutionStatus.DO_NOT_INVOKE_NEXT_IF_ANY;
        } else {
            return ExecutionStatus.INVOKE_NEXT_IF_ANY;
        }
    }

    public void configureByResource(URL url) throws JoranException {
        if (url == null) {
            throw new IllegalArgumentException("URL argument cannot be null");
        }
        final String urlString = url.toString();
        if (urlString.endsWith("xml")) {
            JoranConfigurator configurator = new JoranConfigurator();
            configurator.setContext(context);
            configurator.doConfigure(url);
        } else {
            throw new LogbackException(
                    "Unexpected filename extension of file [" + url.toString() + "]. Should be .xml");
        }
    }

    public URL findURLOfDefaultConfigurationFile(boolean updateStatus) {
        ClassLoader myClassLoader = Loader.getClassLoaderOfObject(this);
        URL url = findConfigFileURLFromSystemProperties(myClassLoader, updateStatus);
        if (url != null) {
            return url;
        }

        url = getResource(TEST_AUTOCONFIG_FILE, myClassLoader, updateStatus);
        if (url != null) {
            return url;
        }

        return getResource(AUTOCONFIG_FILE, myClassLoader, updateStatus);
    }

    private URL findConfigFileURLFromSystemProperties(ClassLoader classLoader, boolean updateStatus) {
        String logbackConfigFile = OptionHelper.getSystemProperty(ClassicConstants.CONFIG_FILE_PROPERTY);
        if (logbackConfigFile != null) {
            URL result = null;
            try {
                result = new URL(logbackConfigFile);
                return result;
            } catch (MalformedURLException e) {
                // so, resource is not a URL:
                // attempt to get the resource from the class path
                result = Loader.getResource(logbackConfigFile, classLoader);
                if (result != null) {
                    return result;
                }
                File f = new File(logbackConfigFile);
                if (f.exists() && f.isFile()) {
                    try {
                        result = f.toURI().toURL();
                        return result;
                    } catch (MalformedURLException e1) {
                    }
                }
            } finally {
                if (updateStatus) {
                    statusOnResourceSearch(logbackConfigFile, classLoader, result);
                }
            }
        }
        return null;
    }

    private URL getResource(String filename, ClassLoader myClassLoader, boolean updateStatus) {
        URL url = Loader.getResource(filename, myClassLoader);
        if (updateStatus) {
            statusOnResourceSearch(filename, myClassLoader, url);
        }
        return url;
    }

    private void statusOnResourceSearch(String resourceName, ClassLoader classLoader, URL url) {
        StatusManager sm = context.getStatusManager();
        if (url == null) {
            sm.add(new InfoStatus("Could NOT find resource [" + resourceName + "]", context));
        } else {
            sm.add(new InfoStatus("Found resource [" + resourceName + "] at [" + url.toString() + "]", context));
            multiplicityWarning(resourceName, classLoader);
        }
    }

    private void multiplicityWarning(String resourceName, ClassLoader classLoader) {
        Set<URL> urlSet = null;
        try {
            urlSet = Loader.getResources(resourceName, classLoader);
        } catch (IOException e) {
            addError("Failed to get url list for resource [" + resourceName + "]", e);
        }
        if (urlSet != null && urlSet.size() > 1) {
            addWarn("Resource [" + resourceName + "] occurs multiple times on the classpath.");
            for (URL url : urlSet) {
                addWarn("Resource [" + resourceName + "] occurs at [" + url.toString() + "]");
            }
        }
    }
}
