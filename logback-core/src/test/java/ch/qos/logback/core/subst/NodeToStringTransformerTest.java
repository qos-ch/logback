package ch.qos.logback.core.subst;

import ch.qos.logback.core.ContextBase;
import ch.qos.logback.core.spi.ScanException;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Ceki G&uuml;c&uuml;
 */
public class NodeToStringTransformerTest {

  ContextBase propertyContainer0 = new ContextBase();


  @Before
  public void setUp() {
    propertyContainer0.putProperty("k0", "v0");
    propertyContainer0.putProperty("zero", "0");
    propertyContainer0.putProperty("v0.jdbc.url", "http://..");

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
    NodeToStringTransformer nodeToStringTransformer = new NodeToStringTransformer(node, propertyContainer0);
    assertEquals(input, nodeToStringTransformer.transform());
  }

  @Test
  public void literalWithAccolades() throws ScanException {
    String input = "%logger{35}";
    Node node = makeNode(input);
    System.out.println(node);
    NodeToStringTransformer nodeToStringTransformer = new NodeToStringTransformer(node, propertyContainer0);
    assertEquals(input, nodeToStringTransformer.transform());
  }


 // %-4relative [%thread] %-5level %logger{35} - %msg%n

  @Test
  public void variable() throws ScanException {
    String input = "${k0}";
    Node node = makeNode(input);
    NodeToStringTransformer nodeToStringTransformer = new NodeToStringTransformer(node, propertyContainer0);
    assertEquals("v0", nodeToStringTransformer.transform());
  }

  @Test
  public void literalVariableLiteral() throws ScanException {
    String input = "a${k0}c";
    Node node = makeNode(input);
    NodeToStringTransformer nodeToStringTransformer = new NodeToStringTransformer(node, propertyContainer0);
    assertEquals("av0c", nodeToStringTransformer.transform());
  }

  @Test
  public void nestedVariable() throws ScanException {
    String input = "a${k${zero}}b";
    Node node = makeNode(input);
    NodeToStringTransformer nodeToStringTransformer = new NodeToStringTransformer(node, propertyContainer0);
    assertEquals("av0b", nodeToStringTransformer.transform());
  }

  @Test
   public void LOGBACK729() throws ScanException {
     String input = "${${k0}.jdbc.url}";
     Node node = makeNode(input);
     NodeToStringTransformer nodeToStringTransformer = new NodeToStringTransformer(node, propertyContainer0);
     assertEquals("http://..", nodeToStringTransformer.transform());
   }


  @Test
  public void withDefaultValue() throws ScanException {
    String input = "${k67:-b}c";
    Node node = makeNode(input);
    NodeToStringTransformer nodeToStringTransformer = new NodeToStringTransformer(node, propertyContainer0);
    assertEquals("bc", nodeToStringTransformer.transform());
  }

  @Test
  public void defaultValueNestedAsVar() throws ScanException {
    String input = "a${k67:-x${k0}}c";
    Node node = makeNode(input);
    NodeToStringTransformer nodeToStringTransformer = new NodeToStringTransformer(node, propertyContainer0);
    assertEquals("axv0c", nodeToStringTransformer.transform());
  }






}
