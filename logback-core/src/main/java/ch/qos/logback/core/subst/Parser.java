package ch.qos.logback.core.subst;


import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.spi.ScanException;

import java.util.List;

// E = TE|T
//   = T(E|~)
// E = TEopt where Eopt = E|~
// T = LITERAL | { E } |'${' V '}'
// V = (E|E :- E)
//   = E(':-'E|~)
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
      case CURLY_LEFT:
        advanceTokenPointer();
        Node inner = E();
        Token right = getCurentToken();
        expectCurlyRight(right);
        advanceTokenPointer();
        Node curlyLeft = new Node(Node.Type.LITERAL, CoreConstants.LEFT_ACCOLADE);
        curlyLeft.next = inner;
        Node curlyRightNode = new Node(Node.Type.LITERAL, CoreConstants.RIGHT_ACCOLADE);
        if(inner == null)
          curlyLeft.next = curlyRightNode;
        else
          appendNode(inner, curlyRightNode);
        return curlyLeft;
      case START:
        advanceTokenPointer();
        Node v = V();
        Token w = getCurentToken();
        expectCurlyRight(w);
        advanceTokenPointer();
        return v;
      default:
        return null;
    }
  }

  private void appendNode(Node inner, Node additionalNode) {
     Node n = inner;
     while(true) {
       if(n.next == null) {
         n.next = additionalNode;
         return;
       }
       n = n.next;
     }
  }


  // V = E(':='E|~)
  private Node V() throws ScanException {
    Node e = E();
    Node variable = new Node(Node.Type.VARIABLE, e);
    Token t = getCurentToken();
    if (t != null && t.type == Token.Type.DEFAULT) {
      advanceTokenPointer();
      Node def = E();
      variable.defaultPart = def;
    }
    return variable;

  }

  void advanceTokenPointer() {
    pointer++;
  }

  void expectNotNull(Token t, String expected) {
    if (t == null) {
      throw new IllegalArgumentException("All tokens consumed but was expecting \""
              + expected + "\"");
    }
  }

  void expectCurlyRight(Token t) throws ScanException {
    expectNotNull(t, "}");
    if (t.type != Token.Type.CURLY_RIGHT) {
      throw new ScanException("Expecting }");
    }
  }

  Token getCurentToken() {
    if (pointer < tokenList.size()) {
      return (Token) tokenList.get(pointer);
    }
    return null;
  }

}
