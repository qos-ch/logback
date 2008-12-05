package ch.qos.logback.classic.pattern.lru;

public class Event<K> {

  final public boolean put;
  final public K k;
  
  public Event(boolean put, K k) {
    this.put = put;
    this.k = k;
  }
  
  public String toString() {
    if(put) {
      return "Event: put, "+k;
    } else {
      return "Event: get, "+k;
    }
  }
}
