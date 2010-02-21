package ch.qos.logback.core.encoder;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import ch.qos.logback.core.testUtil.RandomUtil;
import ch.qos.logback.core.util.CoreTestConstants;

public class ObjectEncodeDecodeTest {

  ObjectStreamEncoder<String> encoder = new ObjectStreamEncoder<String>();
  EventObjectInputStream<String> eventStream;

  int diff = RandomUtil.getPositiveInt();
  protected String randomOutputDir = CoreTestConstants.OUTPUT_DIR_PREFIX + diff
      + "/";

  @Before
  public void setUp() {
    File randomOupurDirFile = new File(randomOutputDir);
    randomOupurDirFile.mkdirs();
  }

  void encodeList(File file, List<String> list) throws IOException {
    FileOutputStream fos = new FileOutputStream(file);
    encoder.init(fos);
    for (String s: list) {
      encoder.doEncode(s);
    }
    encoder.close();
    fos.close();
  }
  
  
  List<String> decodeList(File file) throws IOException {
    FileInputStream fis = new FileInputStream(file);
    eventStream = new EventObjectInputStream<String>(fis);
    List<String> back = new ArrayList<String>();
    String e;
    while((e=eventStream.readEvent()) != null) {
      back.add(e);
    }
    return back;
  }

  @Test
  public void singleBatch() throws IOException {
    File file = new File(randomOutputDir + "x.lbo");

    List<String> witness = new ArrayList<String>();
    for (int i = 0; i < 10; i++) {
      witness.add("hello" + i);
    }
    encodeList(file, witness);
    List<String> back = decodeList(file);
    assertEquals(witness, back);
  }

  @Test
  public void multipleBatches() throws IOException {
    File file = new File(randomOutputDir + "m.lbo");

    List<String> witness = new ArrayList<String>();
    for (int i = 0; i < 100*10; i++) {
      witness.add("hello" + i);
    }
    encodeList(file, witness);
    List<String> back = decodeList(file);
    assertEquals(witness, back);
  }

}
