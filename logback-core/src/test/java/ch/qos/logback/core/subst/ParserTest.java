package ch.qos.logback.core.subst;

import ch.qos.logback.core.spi.ScanException;
import org.junit.Test;

/**
 * Created with IntelliJ IDEA.
 * User: ceki
 * Date: 05.08.12
 * Time: 00:15
 * To change this template use File | Settings | File Templates.
 */
public class ParserTest {



  @Test
  public void literal() throws ScanException {
    Tokenizer tokenizer = new Tokenizer("abc");
    Parser parser = new Parser(tokenizer.tokenize());
    Node node = parser.parse();
    dump(node);
  }

  @Test
  public void variable() throws ScanException {
    Tokenizer tokenizer = new Tokenizer("${abc}");
    Parser parser = new Parser(tokenizer.tokenize());
    Node node = parser.parse();
    dump(node);
  }

  @Test
  public void variable2() throws ScanException {
    Tokenizer tokenizer = new Tokenizer("a${b}c");
    Parser parser = new Parser(tokenizer.tokenize());
    Node node = parser.parse();
    dump(node);
  }

  @Test
  public void nested() throws ScanException {
    Tokenizer tokenizer = new Tokenizer("a${b${c}}d");
    Parser parser = new Parser(tokenizer.tokenize());
    Node node = parser.parse();
    dump(node);
  }

  @Test
    public void withDefault() throws ScanException {
      Tokenizer tokenizer = new Tokenizer("${b:-c}");
      Parser parser = new Parser(tokenizer.tokenize());
      Node node = parser.parse();
      dump(node);
    }


  private void dump(Node node) {
    while(node != null) {
      System.out.println(node.toString());
      node = node.next;
    }
  }


}
