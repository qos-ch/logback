package ch.qos.logback.core.helpers;

public class StackTraceElementProxy {

  final StackTraceElement ste;
  private String steAsString;
  private PackageInfo pi;
  
  StackTraceElementProxy(StackTraceElement ste) {
    this.ste = ste;
  }
  
  public String getSTEAsString() {
    if(steAsString == null) {
      steAsString = "\tat "+ste.toString();
    }
    return steAsString;
  }
  
  public PackageInfo getPI() {
    // compute pi from ste
    return pi;
  }
  
}
