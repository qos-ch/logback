package ch.qos.logback.core.subst;

public class Token {

  static public Token START_TOKEN = new Token(Type.START, null);
  static public Token STOP_TOKEN = new Token(Type.STOP, null);
  static public Token DEFAULT_SEP_TOKEN = new Token(Type.DEFAULT, null);

  public enum Type {LITERAL, START, STOP, DEFAULT}

  Type type;
  String payload;

  public Token(Type type, String payload) {
    this.type = type;
    this.payload = payload;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Token token = (Token) o;

    if (type != token.type) return false;
    if (payload != null ? !payload.equals(token.payload) : token.payload != null) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = type != null ? type.hashCode() : 0;
    result = 31 * result + (payload != null ? payload.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    String result = "Token{" +
            "type=" + type;
    if (payload != null)
      result += ", payload='" + payload + '\'';

    result += '}';
    return result;
  }
}
