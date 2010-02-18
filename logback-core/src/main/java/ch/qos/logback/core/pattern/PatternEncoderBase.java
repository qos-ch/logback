package ch.qos.logback.core.pattern;

import java.io.IOException;
import java.io.OutputStream;

import ch.qos.logback.core.Encoder;
import ch.qos.logback.core.spi.ContextAwareBase;

public class PatternEncoderBase<E> extends ContextAwareBase implements Encoder<E> {

  boolean started;
  protected PatternLayoutBase<E> layout;
  String pattern;
  
  public void close(OutputStream os) throws IOException {
  }

  public void doEncode(E event, OutputStream os) throws IOException {
    String txt = layout.doLayout(event);
    os.write(txt.getBytes());
    os.flush();
  }

  public boolean isStarted() {
    return false;
  }

  public void start() {
    started = true;
  }

  public void stop() {
  }

  public String getPattern() {
    return pattern;
  }

  public void setPattern(String pattern) {
    this.pattern = pattern;
  }

  

}
