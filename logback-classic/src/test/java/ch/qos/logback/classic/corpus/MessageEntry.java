package ch.qos.logback.classic.corpus;

public class MessageEntry {

  
  final String message;
  final Object[] argumentArray;

  public MessageEntry(String message) {
    this(message, null);
  }

  
  public MessageEntry(String message, Object[] argumentArray) {
    this.message = message;
    this.argumentArray = argumentArray;
  }

  public String getMessage() {
    return message;
  }

  public Object[] getArgumentArray() {
    return argumentArray;
  }
  
}
