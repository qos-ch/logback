package ch.qos.logback.classic;

import java.io.Serializable;

class Foo implements Serializable {
  private static final long serialVersionUID = 1L;
  final Logger logger;
  
  Foo(Logger logger) {
    this.logger = logger;
  }
  
  void doFoo() {
    logger.debug("hello");
  }
}

