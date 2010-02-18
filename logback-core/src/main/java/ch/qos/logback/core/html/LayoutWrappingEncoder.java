package ch.qos.logback.core.html;

import java.io.IOException;
import java.io.OutputStream;

import ch.qos.logback.core.Encoder;
import ch.qos.logback.core.Layout;
import ch.qos.logback.core.spi.ContextAwareBase;

public class LayoutWrappingEncoder<E> extends ContextAwareBase implements Encoder<E>  {

  boolean started;
  protected Layout<E> layout;
  
  public Layout<E> getLayout() {
    return layout;
  }

  public void setLayout(Layout<E> layout) {
    this.layout = layout;
  }

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


}
