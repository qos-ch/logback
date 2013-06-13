package ch.qos.logback.classic.pattern;

import java.io.IOException;

import ch.qos.logback.classic.spi.ILoggingEvent;

public class ProcessIdConverter extends ClassicConverter {
  ProcessId processId = new ProcessId();  
  
  public static class ProcessId {
    private String pid;
    public ProcessId() {
      // try to obtain from system property
      pid = System.getProperty("pid");
      if (pid != null) {
        pid = pid.trim();
        if (pid.isEmpty()) {
          pid = null;
        }
      }
      if (pid == null) {
        try {
          // try to obtain by exec'ing shell
          byte[] bo = new byte[100];
          String[] cmd = {"bash", "-c", "echo $PPID"};
          Process p = Runtime.getRuntime().exec(cmd);
          int len = p.getInputStream().read(bo);
          pid = new String(bo, 0, len).trim();
          if (pid.isEmpty()) {
            pid = null;
          }
        } catch (IOException e) {
        }
      }
    }
    
    @Override
    public String toString() {
      return pid == null ? "" : pid;
    }
  }

  @Override
  public String convert(ILoggingEvent event) {
    return processId.toString();
  }

}
