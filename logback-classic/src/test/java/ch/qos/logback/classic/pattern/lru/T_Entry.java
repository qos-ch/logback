package ch.qos.logback.classic.pattern.lru;

public class T_Entry<K> implements Comparable {

  K k;
  long sequenceNumber;
  
  T_Entry(K k, long sn) {
    this.k = k;
    this.sequenceNumber = sn;
  }

  public int compareTo(Object o) {
    if(!(o instanceof T_Entry)) {
      throw new IllegalArgumentException("arguments must be of type "+T_Entry.class);
    }
    
    T_Entry other = (T_Entry) o;
    if(sequenceNumber > other.sequenceNumber) {
      return 1;
    }
    if(sequenceNumber == other.sequenceNumber) {
      return 0;
    }
    return -1;
  }
  @Override
  public String toString() {
    return "("+k+","+sequenceNumber+")";
    //return "("+k+")";
  }
}
