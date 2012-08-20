package ch.qos.logback.core.subst;

import ch.qos.logback.core.ContextBase;
import ch.qos.logback.core.spi.PropertyContainer;
import ch.qos.logback.core.spi.ScanException;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Ceki G&uuml;c&uuml;
 */
public class CompilerTest {

  ContextBase propertyContainer0 = new ContextBase();


  @Before
  public void setUp() {
    propertyContainer0.putProperty("k0", "v0");
    propertyContainer0.putProperty("zero", "0");
  }

  private Node  makeNode(String input) throws ScanException {
    Tokenizer tokenizer = new Tokenizer(input);
    Parser parser = new Parser(tokenizer.tokenize());
    return parser.parse();
  }

  @Test
  public void literal() throws ScanException {
    String input = "abv";
    Node node = makeNode(input);
    Compiler compiler = new Compiler(node, propertyContainer0);
    assertEquals(input, compiler.compile());
  }

  @Test
  public void variable() throws ScanException {
    String input = "${k0}";
    Node node = makeNode(input);
    Compiler compiler = new Compiler(node, propertyContainer0);
    assertEquals("v0", compiler.compile());
  }

  @Test
  public void literalVariableLiteral() throws ScanException {
    String input = "a${k0}c";
    Node node = makeNode(input);
    Compiler compiler = new Compiler(node, propertyContainer0);
    assertEquals("av0c", compiler.compile());
  }

  @Test
  public void nestedVariable() throws ScanException {
    String input = "a${k${zero}}b";
    Node node = makeNode(input);
    Compiler compiler = new Compiler(node, propertyContainer0);
    assertEquals("av0b", compiler.compile());
  }


  @Test
  public void withDefault() throws ScanException {
    String input = "${k67:-b}c";
    Node node = makeNode(input);
    Compiler compiler = new Compiler(node, propertyContainer0);
    assertEquals("bc", compiler.compile());
  }



}
