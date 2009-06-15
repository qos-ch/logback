package ch.qos.logback.core.issue;

/**
 * A runnable with 'done' and 'counter' fields.
 * 
 * @author ceki
 *
 */
abstract public class RunnableForThrougputComputation implements Runnable {

  protected boolean done = false;
  protected int counter = 0;
  
  public int getCounter() {
    return counter;
  }

  public void setDone(boolean done) {
    this.done = done;
  }

  public boolean isDone() {
    return done;
  }
  
}
