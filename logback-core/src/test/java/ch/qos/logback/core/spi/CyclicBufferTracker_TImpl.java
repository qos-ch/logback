package ch.qos.logback.core.spi;

import ch.qos.logback.core.Appender;
import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.helpers.CyclicBuffer;
import ch.qos.logback.core.sift.tracker.TEntry;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Ceki G&uuml;c&uuml;
 */
public class CyclicBufferTracker_TImpl<E> implements CyclicBufferTracker<E> {

  int bufferSize = DEFAULT_BUFFER_SIZE;
  int maxNumBuffers = DEFAULT_NUMBER_OF_BUFFERS;
  int bufferCount = 0;


  List<TEntry> entryList = new LinkedList<TEntry>();
  long lastCheck = 0;

  public int getBufferSize() {
    return bufferSize;
  }

  public void setBufferSize(int size) {
  }

  public int getMaxNumberOfBuffers() {
    return maxNumBuffers;
  }

  public void setMaxNumberOfBuffers(int maxNumBuffers) {
    this.maxNumBuffers = maxNumBuffers;
  }

  private TEntry getEntry(String k) {
    for (int i = 0; i < entryList.size(); i++) {
      TEntry te = entryList.get(i);
      if (te.key.equals(k)) {
        return te;
      }
    }
    return null;
  }

  public CyclicBuffer<E> get(String key, long timestamp) {
    TEntry te = getEntry(key);
    if (te == null) {
      CyclicBuffer<E> cb = new CyclicBuffer<E>(bufferSize);
      te = new  TEntry<E>(key, cb, timestamp);
      entryList.add(te);
      return cb;
    } else {
      te.timestamp = timestamp;
      Collections.sort(entryList);
      return te.value;
    }

  }

  final private boolean isEntryStale(TEntry entry, long now) {
    return ((entry.timestamp + THRESHOLD) < now);
  }

  public void clearStaleBuffers(long now) {
   if (lastCheck + CoreConstants.MILLIS_IN_ONE_SECOND > now) {
      return;
    }
    lastCheck = now;
    Collections.sort(entryList);
    while (entryList.size() != 0 && isEntryStale(entryList.get(0), now)) {
      entryList.remove(0);
    }
  }

  // ==================================================================

  private class TEntry<E> implements Comparable {
    TEntry next;
    TEntry prev;

    String key;
    CyclicBuffer<E> value;
    long timestamp;

    TEntry(String k, CyclicBuffer<E> v, long timestamp) {
      this.key = k;
      this.value = v;
      this.timestamp = timestamp;
    }

    public void setTimestamp(long timestamp) {
      this.timestamp = timestamp;
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((key == null) ? 0 : key.hashCode());
      return result;
    }

    public int compareTo(Object o) {
      if (!(o instanceof TEntry)) {
        throw new IllegalArgumentException("arguments must be of type " + TEntry.class);
      }

      TEntry other = (TEntry) o;
      if (timestamp > other.timestamp) {
        return 1;
      }
      if (timestamp == other.timestamp) {
        return 0;
      }
      return -1;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj)
        return true;
      if (obj == null)
        return false;
      if (getClass() != obj.getClass())
        return false;
      final TEntry other = (TEntry) obj;
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
