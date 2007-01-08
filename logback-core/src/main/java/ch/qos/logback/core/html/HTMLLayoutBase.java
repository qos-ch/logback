package ch.qos.logback.core.html;

import java.util.Map;

import ch.qos.logback.core.LayoutBase;
import ch.qos.logback.core.pattern.Converter;
import ch.qos.logback.core.pattern.DynamicConverter;
import ch.qos.logback.core.pattern.parser.Node;
import ch.qos.logback.core.pattern.parser.Parser;
import ch.qos.logback.core.pattern.parser.ScanException;

/**
 * This class is a base class for logback component-specific HTMLLayout classes
 *
 * @author S&eacute;bastien Pennec
 */
public abstract class HTMLLayoutBase<E> extends LayoutBase<E> {

  protected String pattern;

  protected Converter head;

  protected String title = "Logback Log Messages";

  //It is the responsability of derived classes to set
  //this variable in their constructor to a default value.
  protected CssBuilder cssBuilder;

  protected IThrowableRenderer throwableRenderer; //no more initialization ??????

  // counter keeping track of the rows output
  protected long counter = 0;
  // max number of rows before we close the table and create a new one
  protected static final int ROW_LIMIT = 10000;
  
  /**
   * Set the <b>ConversionPattern </b> option. This is the string which controls
   * formatting and consists of a mix of literal content and conversion
   * specifiers.
   */
  public void setPattern(String conversionPattern) {
    pattern = conversionPattern;
  }

  /**
   * Returns the value of the <b>ConversionPattern </b> option.
   */
  public String getPattern() {
    return pattern;
  }

  public CssBuilder getCssBuilder() {
    return cssBuilder;
  }

  public void setCssBuilder(CssBuilder cssBuilder) {
    this.cssBuilder = cssBuilder;
  }

  /**
   * Parses the pattern and creates the Converter linked list.
   */
  @Override
  public void start() {
    int errorCount = 0;
    
    if (throwableRenderer == null) {
      addError("ThrowableRender cannot be null.");
      errorCount++;
    }
    
    try {
      Parser p = new Parser(pattern);
      p.setContext(getContext());
      Node t = p.parse();
      this.head = p.compile(t, getDefaultConverterMap());
      DynamicConverter.startConverters(this.head);
    } catch (ScanException ex) {
      addError("Incorrect pattern found", ex);
      errorCount++;
    }

    if (errorCount == 0) {
      super.started = true;
    }
  }
  
  protected abstract Map<String, String> getDefaultConverterMap();

  /**
   * The <b>Title </b> option takes a String value. This option sets the
   * document title of the generated HTML document.
   * 
   * <p>
   * Defaults to 'Logback Log Messages'.
   */
  public void setTitle(String title) {
    this.title = title;
  }

  /**
   * Returns the current value of the <b>Title </b> option.
   */
  public String getTitle() {
    return title;
  }

  /**
   * Returns the content type output by this layout, i.e "text/html".
   */
  @Override
  public String getContentType() {
    return "text/html";
  }

  /**
   * Returns appropriate HTML headers.
   */
  @Override
  public String getFileHeader() {
    StringBuffer sbuf = new StringBuffer();
    sbuf.append("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\"");
    sbuf.append(" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">");
    sbuf.append(LINE_SEP);
    sbuf.append("<html>");
    sbuf.append(LINE_SEP);
    sbuf.append("<head>");
    sbuf.append(LINE_SEP);
    sbuf.append("<title>");
    sbuf.append(title);
    sbuf.append("</title>");
    sbuf.append(LINE_SEP);
    
    cssBuilder.addCss(sbuf);
//    if (cssBuilder == null) {
//      DefaultCssBuilder.addDefaultCSS(sbuf);
//    } else {
//      cssBuilder.addExternalCSS(sbuf);
//    }
    sbuf.append(LINE_SEP);
    sbuf.append("</head>");
    sbuf.append(LINE_SEP);
    sbuf.append("<body>");
    sbuf.append(LINE_SEP);

    return sbuf.toString();
  }
  
  public String getPresentationHeader() {
    StringBuffer sbuf = new StringBuffer();
    sbuf.append("<hr size=\"1\" noshade=\"true\" width=\"50%\" align=\"left\" />");
    sbuf.append(LINE_SEP);
    sbuf.append("Log session start time ");
    sbuf.append(new java.util.Date());
    sbuf.append("<br />");
    sbuf.append(LINE_SEP);
    sbuf.append("<br />");
    sbuf.append(LINE_SEP);
    sbuf.append("<table cellspacing=\"0\">");
    sbuf.append(LINE_SEP);

    createTableHeader(sbuf);
    
    return sbuf.toString();
  }


  private void createTableHeader(StringBuffer sbuf) {
    Converter c = head;
    String name;
    sbuf.append("<tr class=\"header\">");
    sbuf.append(LINE_SEP);
    while (c != null) {
      name = computeConverterName(c);
      if (name == null) {
        c = c.getNext();
        continue;
      }
      sbuf.append("<td class=\"");
      sbuf.append(computeConverterName(c));
      sbuf.append("\">");
      sbuf.append(computeConverterName(c));
      sbuf.append("</td>");
      sbuf.append(LINE_SEP);
      c = c.getNext();
    }
    sbuf.append("</tr>");
    sbuf.append(LINE_SEP);
  }
  
  public String getPresentationFooter() {
    StringBuffer sbuf = new StringBuffer();
    sbuf.append("</table>");
    return sbuf.toString();    
  }

  /**
   * Returns the appropriate HTML footers.
   */
  @Override
  public String getFileFooter() {
    StringBuffer sbuf = new StringBuffer();
    sbuf.append(LINE_SEP);
    sbuf.append("</body></html>");
    return sbuf.toString();
  }
  
  protected void handleTableClosing(StringBuffer sbuf) {
    if (this.counter >= ROW_LIMIT) {
      counter = 0;
      sbuf.append("</table>");
      sbuf.append(LINE_SEP);
      sbuf.append("<br />");
      sbuf.append("<table cellspacing=\"0\">");
      sbuf.append(LINE_SEP);
      createTableHeader(sbuf);
    }
  }

  protected String computeConverterName(Converter c) {
    String className = c.getClass().getSimpleName();
    int index = className.indexOf("Converter");
    if (index == -1) {
      return className;
    } else {
      return className.substring(0, index);
    }
  }

  public IThrowableRenderer getThrowableRenderer() {
    return throwableRenderer;
  }

  public void setThrowableRenderer(IThrowableRenderer throwableRenderer) {
    this.throwableRenderer = throwableRenderer;
  }
  
  
  
}
