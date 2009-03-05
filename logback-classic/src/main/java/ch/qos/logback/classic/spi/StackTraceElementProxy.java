package ch.qos.logback.classic.spi;

import java.io.Serializable;

public class StackTraceElementProxy implements Serializable {

  private static final long serialVersionUID = -2374374378980555982L;
  
  final StackTraceElement ste;
  // save a byte or two during serialization, as we can
  // reconstruct this field from 'ste'
  transient private String steAsString;
  private ClassPackagingData cpd;

  public StackTraceElementProxy(StackTraceElement ste) {
    if (ste == null) {
      throw new IllegalArgumentException("ste cannot be null");
    }
    this.ste = ste;
  }

  
  public String getSTEAsString() {
    if (steAsString == null) {
      steAsString = "\tat " + ste.toString();
    }
    return steAsString;
  }
  
  public StackTraceElement getStackTraceElement() {
    return ste;
  }
  
  public void setClassPackagingData(ClassPackagingData cpd) {
    if(this.cpd != null) {
      throw new IllegalStateException("Packaging data has been already set");
    }
    this.cpd = cpd;
  }

  public ClassPackagingData getClassPackagingData() {
    return cpd;
  }

  @Override
  public int hashCode() {
    return ste.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    final StackTraceElementProxy other = (StackTraceElementProxy) obj;

    if (!ste.equals(other.ste)) {
      return false;
    }
    if (cpd == null) {
      if (other.cpd != null) {
        return false;
      }
    } else if (!cpd.equals(other.cpd)) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    return getSTEAsString();
  }
}
