appender("includedConsole", ConsoleAppender) {
  encoder(PatternLayoutEncoder) {
    pattern = '"%d - %m%n"'
  }
}