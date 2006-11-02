package ch.qos.logback.core.filter;

/**
 *
 * This enum represents the possible replies that a filtering component
 * in logback can return. It is used by implementations of both 
 * {@link ch.qos.logback.core.filter.Filter} and
 * {@link ch.qos.logback.classic.turbo.TurboFilter} abstract classes.
 * 
 * Based on the order that the FilterReply values are declared,
 * FilterReply.ACCEPT.compareTo(FilterReply.DENY) will return 
 * a positive value.
 *
 * @author S&eacute;bastien Pennec
 */
public enum FilterReply {
  DENY,
  NEUTRAL,
  ACCEPT;
}
