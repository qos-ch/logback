package ch.qos.logback.core.sift.tracker;


public class SimulationEvent {

  public String key;
  public long timestamp;

  public SimulationEvent(String key, long timestamp) {
    this.key = key;
    this.timestamp = timestamp;
  }

  public String toString() {
      return "Event: k=" + key +", timestamp=" + timestamp;
  }
}
