package ch.qos.logback.classic.gaffer

import org.junit.Test
import static junit.framework.Assert.assertEquals

/**
 * @author Ceki G&uuml;c&uuml;
 */
class PropertyUtilTest {


  @Test
  void empty() {
    assertEquals("", PropertyUtil.upperCaseFirstLetter(""));
    assertEquals(null, PropertyUtil.upperCaseFirstLetter(null));
  }



  @Test
  void smoke() {
    assertEquals("Hello", PropertyUtil.upperCaseFirstLetter("hello"));
  }


}
