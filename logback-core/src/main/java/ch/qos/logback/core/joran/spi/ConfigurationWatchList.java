package ch.qos.logback.core.joran.spi;


import ch.qos.logback.core.spi.ContextAwareBase;

import java.io.File;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Ceki G&uuml;c&uuml;
 */
public class ConfigurationWatchList extends ContextAwareBase {

  URL mainURL;
  List<File> fileWatchList = new ArrayList<File>();
  List<Long> lastModifiedList = new ArrayList<Long>();

  public void clear() {
    this.mainURL = null;
    lastModifiedList.clear();
    fileWatchList.clear();
  }

  public void setMainURL(URL mainURL) {
    this.mainURL = mainURL;
    addAsFileToWatch(mainURL);
  }

  private void addAsFileToWatch(URL url) {
    File file = convertToFile(url);
    if (file != null) {
      fileWatchList.add(file);
      lastModifiedList.add(file.lastModified());
    }
  }

  public void addToWatchList(URL url) {
    addAsFileToWatch(url);
  }

  public URL getMainURL() {
    return mainURL;
  }

  public List<File>  getCopyOfFileWatchList() {
    return new ArrayList<File>(fileWatchList);
  }
  public boolean changeDetected() {
    int len = fileWatchList.size();
    for (int i = 0; i < len; i++) {
      long lastModified = lastModifiedList.get(i);
      File file = fileWatchList.get(i);
      if (lastModified != file.lastModified()) {
        return true;
      }
    }
    return false;
    //return (lastModified != fileToScan.lastModified() && lastModified != SENTINEL);
  }

  @SuppressWarnings("deprecation")
  File convertToFile(URL url) {
    String protocol = url.getProtocol();
    if ("file".equals(protocol)) {
      File file = new File(URLDecoder.decode(url.getFile()));
      return file;
    } else {
      addInfo("URL [" + url + "] is not of type file");
      return null;
    }
  }

}
