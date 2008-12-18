package ch.qos.logback.classic.sift;

import java.util.List;

import ch.qos.logback.core.Appender;

public interface AppenderTracker<E> {

  static int MILLIS_IN_ONE_SECOND = 1000;
  static int THRESHOLD = 30 * 60 * MILLIS_IN_ONE_SECOND; // 30 minutes

  void put(String key, Appender<E> value, long timestamp);
  Appender<E> get(String key, long timestamp);
  void stopStaleAppenders(long now);
  List<String> keyList();
  List<Appender<E>> valueList();


}