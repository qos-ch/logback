package ch.qos.logback.core.property;

import ch.qos.logback.core.PropertyDefinerBase;
import java.io.File;

/**
 * @author Ceki G&uuml;c&uuml;
 */
public class FileExistsPropertyDefiner extends PropertyDefinerBase {

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
    File file = new File(path);
    System.out.println(file.getAbsolutePath());
    if(file.exists())
      return "true";
    else
    return "false";
  }
}
