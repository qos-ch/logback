package ch.qos.logback.core.filter;

import ch.qos.logback.core.spi.FilterReply;

public abstract class AbstractMatcherFilter<E> extends Filter<E> {

  protected FilterReply onMatch = FilterReply.NEUTRAL;
  protected FilterReply onMismatch = FilterReply.NEUTRAL;
  
  final public void setOnMatch(FilterReply reply) {
    this.onMatch = reply;
  }
  
  final public void setOnMismatch(FilterReply reply) {
    this.onMismatch = reply;
  }
  
  final public FilterReply getOnMatch() {
    return onMatch;
  }
  
  final public FilterReply getOnMismatch() {
    return onMismatch;
  }
}
