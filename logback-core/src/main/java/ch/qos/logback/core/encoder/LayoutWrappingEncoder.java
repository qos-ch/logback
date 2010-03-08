package ch.qos.logback.core.encoder;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;

import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.Layout;

public class LayoutWrappingEncoder<E> extends EncoderBase<E> {

  protected Layout<E> layout;

  /**
   * The charset to use when converting a String into bytes.
   * <p>
   * By default this property has the value
   * <code>null</null> which corresponds to 
   * the system's default charset.
   */
  private Charset charset;

  
  public Layout<E> getLayout() {
    return layout;
  }

  public void setLayout(Layout<E> layout) {
    this.layout = layout;
  }

  public Charset getCharset() {
    return charset;
  }

  /**
   * Set the charset to use when converting the string returned by the layout
   * into bytes.
   * <p>
   * By default this property has the value
   * <code>null</null> which corresponds to 
   * the system's default charset.
   * 
   * @param charset
   */
  public void setCharset(Charset charset) {
    this.charset = charset;
  }

  public void init(OutputStream os) throws IOException {
    super.init(os);
    writeHeader();
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
        outputStream.write(convertToBytes(sb.toString()));
        outputStream.flush();
      }
    }
  }

  public void close() throws IOException {
    writeFooter();
  }

  void writeFooter() throws IOException {
    if (layout != null && outputStream != null) {
      StringBuilder sb = new StringBuilder();
      appendIfNotNull(sb, layout.getPresentationFooter());
      appendIfNotNull(sb, layout.getFileFooter());
      if (sb.length() > 0) {
        outputStream.write(convertToBytes(sb.toString()));
        outputStream.flush();
      }

    }
  }

  private byte[] convertToBytes(String s) {
    if (charset == null) {
      return s.getBytes();
    } else {
      return s.getBytes(charset);
    }
  }

  public void doEncode(E event) throws IOException {
    String txt = layout.doLayout(event);
    outputStream.write(convertToBytes(txt));
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

  private void appendIfNotNull(StringBuilder sb, String s) {
    if (s != null) {
      sb.append(s);
    }
  }

}
