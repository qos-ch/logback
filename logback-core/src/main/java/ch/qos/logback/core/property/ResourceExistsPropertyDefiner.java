package ch.qos.logback.core.property;

import java.net.URL;

import ch.qos.logback.core.PropertyDefinerBase;
import ch.qos.logback.core.util.Loader;

/**
 * @author XuHuisheng<xyz20003@gmail.com>
 */
public class ResourceExistsPropertyDefiner extends PropertyDefinerBase {

  String path;

  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public String getPropertyValue() {
    if(path == null)
      return "false";

    URL resourceURL = Loader.getResourceBySelfClassLoader(path);

    return (resourceURL != null) ? "true" : "false";
  }
}
