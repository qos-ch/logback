package ch.qos.logback.classic.spi;

import ch.qos.logback.classic.LoggerContext;

public class BasicContextListener implements LoggerContextListener {

  enum UpdateType { NONE, START, RESET, STOP};
  
  UpdateType updateType = UpdateType.NONE;
  LoggerContext context;
  
  boolean resetResistant;
  
  public void setResetResistant(boolean resetResistant) {
    this.resetResistant = resetResistant;
  }
  
  public void onReset(LoggerContext context) {
    updateType =  UpdateType.RESET;
    this.context = context;
    
  }
  public void onStart(LoggerContext context) {
    updateType =  UpdateType.START;;
    this.context = context;
  }
  
  public void onStop(LoggerContext context) {
    updateType =  UpdateType.STOP;;
    this.context = context;
  }
  
  public boolean isResetResistant() {
    return resetResistant;
  }
}
