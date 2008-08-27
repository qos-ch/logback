package ch.qos.logback.core.html;

import ch.qos.logback.core.html.IThrowableRenderer;


public class NOPThrowableRenderer implements IThrowableRenderer {

  public void render(StringBuilder sbuf, Object event) {
    return;
  }

}
