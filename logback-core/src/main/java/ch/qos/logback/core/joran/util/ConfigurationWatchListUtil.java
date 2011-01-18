package ch.qos.logback.core.joran.util;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.joran.spi.ConfigurationWatchList;

import java.net.URL;

/**
 * @author Ceki G&uuml;c&uuml;
 */
public class ConfigurationWatchListUtil {


  public static void updateWatchList(Context context, URL url) {
    ConfigurationWatchList cwl = getConfigurationWatchList(context);
    if(cwl == null) {
      cwl = new ConfigurationWatchList();
      cwl.setContext(context);
      context.putObject(CoreConstants.CONFIGURATION_WATCH_LIST, cwl);
    } else {
      cwl.clear();
    }
    cwl.setMainURL(url);
  }

   public static ConfigurationWatchList getConfigurationWatchList(Context context) {
     return  (ConfigurationWatchList) context.getObject(CoreConstants.CONFIGURATION_WATCH_LIST);
  }
}
