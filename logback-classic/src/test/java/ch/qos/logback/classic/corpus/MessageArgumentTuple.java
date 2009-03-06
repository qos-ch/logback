package ch.qos.logback.classic.corpus;

public class MessageArgumentTuple {
  
  final String message;
  final int numberOfArguments;

  MessageArgumentTuple(String message) {
    this(message, 0);
  }

  public MessageArgumentTuple(String message, int numberOfArguments) {
    this.message = message;
    this.numberOfArguments = numberOfArguments;
  }

}
