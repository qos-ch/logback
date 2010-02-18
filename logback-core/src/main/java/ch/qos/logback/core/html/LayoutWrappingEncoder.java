package ch.qos.logback.core.html;

import java.io.IOException;
import java.io.OutputStream;

import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.Encoder;
import ch.qos.logback.core.Layout;
import ch.qos.logback.core.spi.ContextAwareBase;

public class LayoutWrappingEncoder<E> extends ContextAwareBase implements
    Encoder<E> {

  boolean started;
  protected Layout<E> layout;

  public Layout<E> getLayout() {
    return layout;
  }

  public void setLayout(Layout<E> layout) {
    this.layout = layout;
  }


  public void init(OutputStream os) throws IOException {
    writeHeader(os);
  }

  public void close(OutputStream os) throws IOException {
    writeFooter(os);
  }

  private void appendIfNotNull(StringBuilder sb, String s) {
    if (s != null) {
      sb.append(s);
    }
  }
  
  void writeHeader(OutputStream os) throws IOException {
    if (layout != null && (os != null)) {
      StringBuilder sb = new StringBuilder();
      appendIfNotNull(sb, layout.getFileHeader());
      appendIfNotNull(sb, layout.getPresentationHeader());
      if (sb.length() > 0) {
        sb.append(CoreConstants.LINE_SEPARATOR);
        // If at least one of file header or presentation header were not
        // null, then append a line separator.
        // This should be useful in most cases and should not hurt.
        os.write(sb.toString().getBytes());
        os.flush();
      }
    }
  }

  void writeFooter(OutputStream os) throws IOException {
    if (layout != null && os != null) {
      StringBuilder sb = new StringBuilder();
      appendIfNotNull(sb, layout.getPresentationFooter());
      appendIfNotNull(sb, layout.getFileFooter());
      if (sb.length() > 0) {
        os.write(sb.toString().getBytes());
        os.flush();
      }

    }
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
    started = false;
  }

}
