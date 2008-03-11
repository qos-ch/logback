package ch.qos.logback.classic.spi;

// non serializable object
public class LuckyCharms {
  int id;
  
  LuckyCharms(int id) {
    this.id= id;
  }
  
  @Override
  public String toString() {
    return "LC("+id+")";
  }
}
