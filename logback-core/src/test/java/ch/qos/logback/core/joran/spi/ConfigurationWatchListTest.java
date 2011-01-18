package ch.qos.logback.core.joran.spi;

import org.junit.Test;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import static org.junit.Assert.assertEquals;

/**
 * @author Ceki G&uuml;c&uuml;
 */
public class ConfigurationWatchListTest {

  @Test
  // See http://jira.qos.ch/browse/LBCORE-119
  public void fileToURLAndBack() throws MalformedURLException {
    File file = new File("a b.xml");
    URL url = file.toURI().toURL();
    ConfigurationWatchList cwl = new ConfigurationWatchList();
    File back = cwl.convertToFile(url);
    assertEquals(file.getName(), back.getName());
  }
}
