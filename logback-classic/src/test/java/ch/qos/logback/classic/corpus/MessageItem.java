package ch.qos.logback.classic.corpus;

public class MessageItem {

  
  final String message;
  final int numberOfArguments;

  MessageItem(String message) {
    this(message, 0);
  }

  public MessageItem(String message, int numberOfArguments) {
    this.message = message;
    this.numberOfArguments = numberOfArguments;
  }

}
