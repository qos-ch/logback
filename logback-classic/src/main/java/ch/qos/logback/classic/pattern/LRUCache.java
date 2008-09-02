package ch.qos.logback.classic.pattern;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class LRUCache<K, V> {

  Map<K, Entry> map = new HashMap<K, Entry>();
  Entry head;
  Entry tail;

  int limit;

  LRUCache(int limit) {
    if(limit < 1) {
       throw new IllegalArgumentException("limit cannnot be smaller than 1");
    } 
    
    this.limit = limit;
 
    head = new Entry(null, null);
    tail = head;
  }

  public void put(K key, V value) {
    Entry entry = map.get(key);
    if (entry == null) {
      entry = new Entry(key, value);
      map.put(key, entry);
    } 
    moveToTail(entry);
    while(map.size() > limit) {
      removeHead();
    }
  }

  public V get(K key) {
    Entry existing = map.get(key);
    if (existing == null) {
      return null;
    } else {
      moveToTail(existing);
      return existing.value;
    }
  }

  private void removeHead() {
    //System.out.println("RemoveHead called");
    map.remove(head.key);
    head = head.next;
    head.prev = null;
  }

  private void moveToTail(Entry e) {
    rearrangePreexistingLinks(e);
    rearrangeTailLinks(e);
  }

  private void rearrangePreexistingLinks(Entry e) {
    if (e.prev != null) {
      e.prev.next = e.next;
    }
    if (e.next != null) {
      e.next.prev = e.prev;
    }
    if(head == e) {
      head = e.next;
    }
  }
  
  private void rearrangeTailLinks(Entry e) {
    if(head == tail) {
      head = e;
    }
    Entry preTail = tail.prev;
    if(preTail != null) {
      preTail.next = e;
    }
    e.prev = preTail;
    e.next = tail;
    tail.prev = e;
  }


  public void dump() {
    Entry e = head;
    System.out.print("N:");
    while (e != null) {
      //System.out.print(e+"->");
      System.out.print(e.key+", ");
      e = e.next;
    }
    System.out.println();
  }

  List<K> keyList() {
    List<K> result = new LinkedList<K>();
    Entry e = head;
    while (e != tail) {
      result.add(e.key);
      e = e.next;
    }
    return result;
  }
  
  // ================================================================ 
  private class Entry {
    Entry next;
    Entry prev;
    K key;
    V value;

    Entry(K k, V v) {
      this.key = k;
      this.value = v;
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((key == null) ? 0 : key.hashCode());
      return result;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj)
        return true;
      if (obj == null)
        return false;
      if (getClass() != obj.getClass())
        return false;
      final Entry other = (Entry) obj;
      if (key == null) {
        if (other.key != null)
          return false;
      } else if (!key.equals(other.key))
        return false;
      if (value == null) {
        if (other.value != null)
          return false;
      } else if (!value.equals(other.value))
        return false;
      return true;
    }

    @Override
    public String toString() {
      return "(" + key + ", " + value + ")";
    }
  }

}
