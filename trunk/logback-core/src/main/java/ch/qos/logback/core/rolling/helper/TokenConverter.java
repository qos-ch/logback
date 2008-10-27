
package ch.qos.logback.core.rolling.helper;


/**
 * <code>TokenConverter</code> offers some basic functionality used by more 
 * specific token  converters. 
 * <p>
 * It basically sets up the chained architecture for tokens. It also forces 
 * derived classes to fix their type.
 * 
 * @author Ceki
 * @since 1.3
 */
public class TokenConverter {
  
  
  static final int IDENTITY = 0;
  static final int INTEGER = 1;
  static final int DATE = 1;
  int type;
  TokenConverter next;

  protected TokenConverter(int t) {
    type = t;
  }

  public TokenConverter getNext() {
    return next;
  }

  public void setNext(TokenConverter next) {
    this.next = next;
  }
 
  public int getType() {
    return type;
  }

  public void setType(int i) {
    type = i;
  }

}
