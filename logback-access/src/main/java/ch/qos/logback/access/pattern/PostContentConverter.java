package ch.qos.logback.access.pattern;

import ch.qos.logback.access.spi.AccessEvent;

public class PostContentConverter extends AccessConverter {

  @Override
  protected String convert(AccessEvent accessEvent) {
    return accessEvent.getPostContent();
  }

}
