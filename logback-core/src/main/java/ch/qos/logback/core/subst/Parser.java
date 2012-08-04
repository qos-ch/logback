package ch.qos.logback.core.subst;


import ch.qos.logback.core.spi.ScanException;

import java.util.ArrayList;
import java.util.List;

// E = TE|T
//   = T(E|~)
// E = TEopt where Eopt = E|~
// T = LITERAL | '${' V '}'
// V = (E|E := E)
//   = E(':='E|~)
public class Parser {

  final List<Token> tokenList;
  int pointer = 0;

  public Parser(List<Token> tokenList) {
    this.tokenList = tokenList;
  }

  public Node parse() throws ScanException {
    return E();
  }

  private Node E() throws ScanException {
    Node t = T();
    if (t == null) {
      return null;
    }
    Node eOpt = Eopt();
    if (eOpt != null) {
      t.setNext(eOpt);
    }
    return t;
  }

  // Eopt = E|~
  private Node Eopt() throws ScanException {
    Token next = getCurentToken();
    // System.out.println("Current token is " + next);
    if (next == null) {
      return null;
    } else {
      return E();
    }
  }

  // T = LITERAL | '${' V '}'
  private Node T() throws ScanException {
    Token t = getCurentToken();

    switch (t.type) {
      case LITERAL:
        advanceTokenPointer();
        return new Node(Node.Type.LITERAL, t.payload);
      case START:
        advanceTokenPointer();
        Node v = V();
        Token w = getCurentToken();
        expectNotNull(w, "}");
        if (w.type == Token.Type.STOP) {
          advanceTokenPointer();
          return new Node(Node.Type.VARIABLE, v);
        } else {
          throw new ScanException("Expecting }");
        }
      default:
        return null;
    }
  }


  // V = E(':='E|~)
  private Node V() throws ScanException {
    Node e = E();
    Token t = getCurentToken();
    if(t != null && t.type == Token.Type.DEFAULT) {
      advanceTokenPointer();
      Node d = E();
      return new Node(Node.Type.VARIABLE2, e, d);
    } else {
      return e;
    }
  }

  void advanceTokenPointer() {
    pointer++;
  }

  void expectNotNull(Token t, String expected) {
    if (t == null) {
      throw new IllegalStateException("All tokens consumed but was expecting "
              + expected);
    }
  }

  Token getCurentToken() {
    if (pointer < tokenList.size()) {
      return (Token) tokenList.get(pointer);
    }
    return null;
  }

}
