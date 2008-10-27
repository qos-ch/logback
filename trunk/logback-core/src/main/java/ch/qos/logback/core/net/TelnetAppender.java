package ch.qos.logback.core.net;

import ch.qos.logback.core.AppenderBase;

public class TelnetAppender extends AppenderBase {

  int port; 
  
  @Override
  public void start() {
    int errorCount = 0;
    if (port == 0) {
      errorCount++;
      addError("No port was configured for appender"
          + name
          + " For more information, please visit http://logback.qos.ch/codes.html#socket_no_port");
    }

    //ServerSocket serverSocket = new ServerSocket(port);
    
//    connect(address, port);

    if (errorCount == 0) {
      this.started = true;
    }
  }

  @Override
  protected void append(Object eventObject) {
    // TODO Auto-generated method stub

  }
  
  

}
