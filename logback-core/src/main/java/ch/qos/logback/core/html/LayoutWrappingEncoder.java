package ch.qos.logback.core.html;

import java.io.IOException;
import java.io.OutputStream;

import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.Layout;
import ch.qos.logback.core.encoder.EncoderBase;

public class LayoutWrappingEncoder<E> extends EncoderBase<E> {

  protected Layout<E> layout;

  public Layout<E> getLayout() {
    return layout;
  }

  public void setLayout(Layout<E> layout) {
    this.layout = layout;
  }


  public void init(OutputStream os) throws IOException {
    super.init(os);
    writeHeader();
  }

  public void close() throws IOException {
    writeFooter();
  }

  private void appendIfNotNull(StringBuilder sb, String s) {
    if (s != null) {
      sb.append(s);
    }
  }
  
  void writeHeader() throws IOException {
    if (layout != null && (outputStream != null)) {
      StringBuilder sb = new StringBuilder();
      appendIfNotNull(sb, layout.getFileHeader());
      appendIfNotNull(sb, layout.getPresentationHeader());
      if (sb.length() > 0) {
        sb.append(CoreConstants.LINE_SEPARATOR);
        // If at least one of file header or presentation header were not
        // null, then append a line separator.
        // This should be useful in most cases and should not hurt.
        outputStream.write(sb.toString().getBytes());
        outputStream.flush();
      }
    }
  }

  void writeFooter() throws IOException {
    if (layout != null && outputStream != null) {
      StringBuilder sb = new StringBuilder();
      appendIfNotNull(sb, layout.getPresentationFooter());
      appendIfNotNull(sb, layout.getFileFooter());
      if (sb.length() > 0) {
        outputStream.write(sb.toString().getBytes());
        outputStream.flush();
      }

    }
  }

  public void doEncode(E event) throws IOException {
    String txt = layout.doLayout(event);
    outputStream.write(txt.getBytes());
    outputStream.flush();
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
