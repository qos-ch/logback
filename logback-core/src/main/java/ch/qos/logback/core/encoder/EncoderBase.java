package ch.qos.logback.core.encoder;

import java.io.IOException;
import java.io.OutputStream;

import ch.qos.logback.core.spi.ContextAwareBase;

abstract public class EncoderBase<E> extends ContextAwareBase implements Encoder<E> {

  protected boolean started;

  protected OutputStream outputStream;
  
  public void init(OutputStream os) throws IOException {
    this.outputStream = os;
  }
  
  public boolean isStarted() {
    return started;
  }
  
  public void start() {
    started = true;
  }

  public void stop() {
    started = false;
  }
}  

