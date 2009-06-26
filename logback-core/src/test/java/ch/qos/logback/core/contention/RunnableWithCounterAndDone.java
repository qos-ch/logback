package ch.qos.logback.core.contention;

/**
 * A runnable with 'done' and 'counter' fields.
 * 
 * @author ceki
 *
 */
abstract public class RunnableWithCounterAndDone implements Runnable {

  protected boolean done = false;
  protected long counter = 0;
  
  public long getCounter() {
    return counter;
  }

  public void setDone(boolean done) {
    this.done = done;
  }

  public boolean isDone() {
    return done;
  }
  
}
