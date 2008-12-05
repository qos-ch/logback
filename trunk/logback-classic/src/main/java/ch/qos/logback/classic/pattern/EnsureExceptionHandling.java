package ch.qos.logback.classic.pattern;

import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.pattern.Converter;
import ch.qos.logback.core.pattern.ConverterUtil;
import ch.qos.logback.core.pattern.PostCompileProcessor;

public class EnsureExceptionHandling implements
    PostCompileProcessor<LoggingEvent> {

  // public void process(Converter head) {
  // // TODO Auto-generated method stub
  //
  // }

  /**
   * This implementation checks if any of the converters in the chain handles
   * exceptions. If not, then this method adds a
   * {@link ExtendedThrowableProxyConverter} instance to the end of the chain.
   * <p>
   * This allows appenders using this layout to output exception information
   * event if the user forgets to add %ex to the pattern. Note that the
   * appenders defined in the Core package are not aware of exceptions nor
   * LoggingEvents.
   * <p>
   * If for some reason the user wishes to NOT print exceptions, then she can
   * add %nopex to the pattern.
   * 
   * 
   */
  public void process(Converter<LoggingEvent> head) {
    if (!chainHandlesThrowable(head)) {
      Converter<LoggingEvent> tail = ConverterUtil.findTail(head);
      Converter<LoggingEvent> exConverter = new ExtendedThrowableProxyConverter();
      if (tail == null) {
        head = exConverter;
      } else {
        tail.setNext(exConverter);
      }
    }
  }

  /**
   * This method computes whether a chain of converters handles exceptions or
   * not.
   * 
   * @param head
   *                The first element of the chain
   * @return true if can handle throwables contained in logging events
   */
  public boolean chainHandlesThrowable(Converter head) {
    Converter c = head;
    while (c != null) {
      if (c instanceof ThrowableHandlingConverter) {
        return true;
      }
      c = c.getNext();
    }
    return false;
  }
}
