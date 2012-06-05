package ch.qos.logback.core.pattern.color;

import ch.qos.logback.core.pattern.CompositeConverter;
import static ch.qos.logback.core.pattern.color.ColorConstants.*;

public class RedCompositeConverter<E> extends CompositeConverter<E> {

  protected String transform(String s) {
    if (!started) {
      return s;
    }
    return ESC_START+RED_FG+ESC_END+s+ESC_START+DEFAULT_FG+ESC_END;
  }
}
