package ch.qos.logback.core;

import java.io.File;
import java.io.IOException;
import java.nio.channels.FileChannel;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import ch.qos.logback.core.contention.RunnableWithCounterAndDone;
import ch.qos.logback.core.encoder.EchoEncoder;
import ch.qos.logback.core.recovery.ResilientFileOutputStream;
import ch.qos.logback.core.status.OnConsoleStatusListener;
import ch.qos.logback.core.testUtil.RandomUtil;
import ch.qos.logback.core.util.CoreTestConstants;
import ch.qos.logback.core.util.ResilienceUtil;

public class FileAppenderResilienceTest {

  FileAppender<Object> fa = new FileAppender<Object>();
  Context context = new ContextBase();
  int diff = RandomUtil.getPositiveInt();
  String outputDirStr = CoreTestConstants.OUTPUT_DIR_PREFIX + "resilience-"
      + diff + "/";

  //String outputDirStr = "\\\\192.168.1.3\\lbtest\\" + "resilience-"+ diff + "/";; 
  String logfileStr = outputDirStr + "output.log";

  @Before
  public void setUp() throws InterruptedException {

    context.getStatusManager().add(new OnConsoleStatusListener());
    
    File outputDir = new File(outputDirStr);
    outputDir.mkdirs();

    fa.setContext(context);
    fa.setName("FILE");
    fa.setEncoder(new EchoEncoder<Object>());
    fa.setFile(logfileStr);
    fa.start();

  }

  @Test
  @Ignore
  public void manual() throws InterruptedException, IOException {
    Runner runner = new Runner(fa);
    Thread t = new Thread(runner);
    t.start();

    while (true) {
      Thread.sleep(110);
    }
  }


  @Test
  public void smoke() throws InterruptedException, IOException {
    Runner runner = new Runner(fa);
    Thread t = new Thread(runner);
    t.start();

    for (int i = 0; i < 10; i++) {
      Thread.sleep(100);
      ResilientFileOutputStream resilientFOS = (ResilientFileOutputStream) fa
          .getOutputStream();
      FileChannel fileChannel = resilientFOS.getChannel();
      fileChannel.close();
    }
    runner.setDone(true);
    t.join();

    ResilienceUtil
        .verify(logfileStr, "^hello (\\d{1,5})$", runner.getCounter());
  }
}

class Runner extends RunnableWithCounterAndDone {
  FileAppender<Object> fa;

  Runner(FileAppender<Object> fa) {
    this.fa = fa;
  }

  public void run() {
    while (!isDone()) {
      counter++;
      fa.doAppend("hello " + counter);
      if (counter % 1024 == 0) {
        try {
          Thread.sleep(10);
        } catch (InterruptedException e) {
        }
      }
    }
  }

}