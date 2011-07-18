package ch.qos.logback.core.json;

import ch.qos.logback.core.LayoutBase;

/**
 * A simple JSON layout with no external dependencies.
 * @author Pierre Queinnec
 */
public abstract class JsonLayoutBase<E> extends LayoutBase<E> {

  public final static String CONTENT_TYPE = "application/json";

  protected boolean includeTimestamp;

  public JsonLayoutBase() {
    // defaults
    this.includeTimestamp = true;
  }

  @Override
  public String getContentType() {
    return CONTENT_TYPE;
  }

  public void setIncludeTimestamp(boolean includeTimestamp) {
    this.includeTimestamp = includeTimestamp;
  }

}
