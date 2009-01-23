package ch.qos.logback.core.testUtil;

import java.util.ArrayList;
import java.util.List;

import ch.qos.logback.core.AppenderBase;
import ch.qos.logback.core.Layout;

public class StringListAppender<E> extends AppenderBase<E> {

  Layout<E> layout;
  public List<String> strList = new ArrayList<String>();

  public StringListAppender() {

  }

  public void start() {
    strList.clear();

    if (layout == null || !layout.isStarted()) {
      return;
    }
    super.start();
  }

  public void stop() {
    super.stop();
  }

  @Override
  protected void append(E eventObject) {
    String res = layout.doLayout(eventObject);
    strList.add(res);
  }

  @Override
  public Layout<E> getLayout() {
    return layout;
  }

  @Override
  public void setLayout(Layout<E> layout) {
    this.layout = layout;
  }
}
