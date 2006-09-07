package ch.qos.logback.classic;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * A <em>Mapped Diagnostic Context</em>, or MDC in short, is an instrument for
 * distinguishing interleaved log output from different sources. Log output is
 * typically interleaved when a server handles multiple clients
 * near-simultaneously.
 * <p>
 * <b><em>The MDC is managed on a per thread basis</em></b>. A child thread
 * automatically inherits a <em>copy</em> of the mapped diagnostic context of
 * its parent.
 * <p>
 * 
 * @author Ceki G&uuml;lc&uuml;
 */
public class MDC {
  private static final ThreadLocal<HashMap<String, String>> threadLocal = new ThreadLocal<HashMap<String, String>>();

  private MDC() {
  }

  /**
   * Put a context value (the <code>val</code> parameter) as identified with
   * the <code>key</code> parameter into the current thread's context map.
   * 
   * <p>
   * If the current thread does not have a context map it is created as a side
   * effect of this call.
   */
  public static void put(String key, String val) {
    HashMap<String, String> hashMap = threadLocal.get();

    if (hashMap == null) {
      hashMap = new HashMap<String, String>();
      threadLocal.set(hashMap);
    }

    hashMap.put(key, val);
  }

  /**
   * Get the context identified by the <code>key</code> parameter.
   * 
   * <p>
   * This method has no side effects.
   */
  public static String get(String key) {
    HashMap<String, String> hashMap = threadLocal.get();

    if ((hashMap != null) && (key != null)) {
      return hashMap.get(key);
    } else {
      return null;
    }
  }

  /**
   * Remove the the context identified by the <code>key</code> parameter.
   */
  public static void remove(String key) {
    HashMap<String, String> hashMap = threadLocal.get();

    if (hashMap != null) {
      hashMap.remove(key);
    }
  }

  /**
   * Clear all entries in the MDC.
   */
  public static void clear() {
    HashMap<String, String> hashMap = threadLocal.get();

    if (hashMap != null) {
      hashMap.clear();
      threadLocal.remove();
    }
  }

  /**
   * Get the current thread's MDC as a map. This method is intended to be used
   * internally.
   */
  public static Map<String, String> getContext() {
    return threadLocal.get();
  }

  /**
   * Returns the keys in the MDC as a {@link Set}. The returned value
   * can be null.
   */
  public static Set<String> getKeys() {
    HashMap<String, String> hashMap = threadLocal.get();

    if (hashMap != null) {
      return hashMap.keySet();
    } else {
      return null;
    }
  }
}
