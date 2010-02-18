package ch.qos.logback.core;

import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ch.qos.logback.core.encoder.EchoEncoder;
import ch.qos.logback.core.testUtil.Env;
import ch.qos.logback.core.testUtil.RandomUtil;
import ch.qos.logback.core.util.StatusPrinter;

public class FileAppenderResilienceTest {

  static String MOUNT_POINT = "/mnt/loop/";

  static String LONG_STR = " xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx";

  static String PATH_LOOPFS_SCRIPT = "/home/ceki/java/logback/logback-core/src/test/loopfs.sh";

  
  enum LoopFSCommand {
    setup, shake, teardown;
  }

  Context context = new ContextBase();
  int diff = RandomUtil.getPositiveInt();
  String outputDirStr = MOUNT_POINT + "resilience-" + diff + "/";
  String logfileStr = outputDirStr + "output.log";
  
  FileAppender<Object> fa = new FileAppender<Object>();

  static boolean isConformingHost() {
    return Env.isLocalHostNameInList(new String[] {"gimmel"});
  }
  
  @Before
  public void setUp() throws IOException, InterruptedException {
    if(!isConformingHost()) {
      return;
    }
    Process p = runLoopFSScript(LoopFSCommand.setup);
    p.waitFor();

    dump("/tmp/loopfs.log");

    fa.setContext(context);
    File outputDir = new File(outputDirStr);
    outputDir.mkdirs();
    System.out.println("FileAppenderResilienceTest output dir [" + outputDirStr
        + "]");

    fa.setName("FILE");
    fa.setEncoder(new EchoEncoder<Object>());
    fa.setFile(logfileStr);
    fa.start();
  }

  void dump(String file) throws IOException {
    FileInputStream fis = null;
    try {
      fis = new FileInputStream(file);
      int r;
      while ((r = fis.read()) != -1) {
        char c = (char) r;
        System.out.print(c);
      }
    } finally {
      if (fis != null) {
        fis.close();
      }
    }
  }

  
  @After
  public void tearDown() throws IOException, InterruptedException {
    if(!isConformingHost()) {
      return;
    }
    StatusPrinter.print(context);
    fa.stop();
    Process p = runLoopFSScript(LoopFSCommand.teardown);
    p.waitFor();
    System.out.println("Tearing down");
  }

  static int TOTAL_DURATION = 5000;
  static int NUM_STEPS = 500;
  static int DELAY = TOTAL_DURATION / NUM_STEPS;

  @Test
  public void go() throws IOException, InterruptedException {
    if(!isConformingHost()) {
      return;
    }
    Process p = runLoopFSScript(LoopFSCommand.shake);
    for (int i = 0; i < NUM_STEPS; i++) {
      fa.append(String.valueOf(i) + LONG_STR);
      Thread.sleep(DELAY);
    }
    p.waitFor();
    verify(logfileStr);
    System.out.println("Done go");
  }

  // the loopfs script is tightly coupled with the host machine
  // it needs to be Unix, with sudo privileges granted to the script
  Process runLoopFSScript(LoopFSCommand cmd) throws IOException,
      InterruptedException {
    // causing a NullPointerException is better than locking the whole
    // machine which the next operation can and will do.
    if(!isConformingHost()) {
      return null;
    }
    ProcessBuilder pb = new ProcessBuilder();
    pb.command("/usr/bin/sudo", PATH_LOOPFS_SCRIPT, cmd.toString());
    Process process = pb.start();
    return process;
  }

  void verify(String logfile) throws NumberFormatException, IOException {
    FileReader fr = new FileReader(logfile);
    BufferedReader br = new BufferedReader(fr);
    String regExp = "^(\\d{1,3}) x*$";
    Pattern p = Pattern.compile(regExp);
    String line;
    
    int totalLines = 0;
    int oldNum = -1;
    int gaps = 0;
    while ((line = br.readLine()) != null) {
      Matcher m = p.matcher(line);
      if (m.matches()) {
        totalLines++;
        String g = m.group(1);
        int num = Integer.parseInt(g);
        if(num != oldNum+1) {
          gaps++;
        }
        oldNum = num;
      }
    }
    fr.close();
    br.close();

    // at least 40% of the logs should have been written
    int lowerLimit = (int) (NUM_STEPS*0.4);
    assertTrue("totalLines="+totalLines+" less than "+lowerLimit, totalLines > lowerLimit);
    
    // we want some gaps which indicate recuperation
    assertTrue("gaps="+gaps+" less than 3", gaps > 3);
    
  }

}
