package ch.qos.logback.core.json;

import junit.framework.TestCase;

import org.junit.Test;

import ch.qos.logback.core.json.JsonEscapeUtils;

/**
 * @author Pierre Queinnec
 */
public class JsonEscapeUtilsTest extends TestCase {

  @Test
  public void testEmptyString() throws Exception {
    String test = "";
    String correctResult = "";

    String result = JsonEscapeUtils.escape(test);
    System.out.println(result);
    assertEquals(correctResult, result);
  }

  @Test
  public void testDoubleQuotes() throws Exception {
    String test = "\"";
    String correctResult = "\\\"";

    String result = JsonEscapeUtils.escape(test);
    System.out.println(result);
    assertEquals(correctResult, result);
  }

  @Test
  public void testBackslash() throws Exception {
    String test = "\\";
    String correctResult = "\\\\";

    String result = JsonEscapeUtils.escape(test);
    System.out.println(result);
    assertEquals(correctResult, result);
  }

  @Test
  public void testBackspace() throws Exception {
    String test = "\b";
    String correctResult = "\\b";

    String result = JsonEscapeUtils.escape(test);
    System.out.println(result);
    assertEquals(correctResult, result);
  }

  @Test
  public void testFormFeed() throws Exception {
    String test = "\f";
    String correctResult = "\\f";

    String result = JsonEscapeUtils.escape(test);
    System.out.println(result);
    assertEquals(correctResult, result);
  }

  @Test
  public void testNewLine() throws Exception {
    String test = "\n";
    String correctResult = "\\n";

    String result = JsonEscapeUtils.escape(test);
    System.out.println(result);
    assertEquals(correctResult, result);
  }

  @Test
  public void testCarriageReturn() throws Exception {
    String test = "\r";
    String correctResult = "\\r";

    String result = JsonEscapeUtils.escape(test);
    System.out.println(result);
    assertEquals(correctResult, result);
  }

  @Test
  public void testTab() throws Exception {
    String test = "\t";
    String correctResult = "\\t";

    String result = JsonEscapeUtils.escape(test);
    System.out.println(result);
    assertEquals(correctResult, result);
  }

  @Test
  public void testComplexString() throws Exception {
    String test = "This is a so/called \"multi-line\" comment\n"
        + "\t- te\\st 1\b" + "- test 2\f\r\n";
    String correctResult = "This is a so/called \\\"multi-line\\\" comment\\n\\t- te\\\\st 1\\b- test 2\\f\\r\\n";

    String result = JsonEscapeUtils.escape(test);
    System.out.println(result);
    assertEquals(correctResult, result);
  }

}
