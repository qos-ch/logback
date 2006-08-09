package ch.qos.logback.core.util;

public class IncompatibleClassException extends Exception {

  private static final long serialVersionUID = -5823372159561159549L;

  Class requestedClass;
  Class obtainedClass;
  
  IncompatibleClassException(Class requestedClass, Class obtainedClass) {
    super();
    this.requestedClass =requestedClass;
    this.obtainedClass = obtainedClass;
  }
}
