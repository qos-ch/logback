package ch.qos.logback.core.rolling.helper;

import ch.qos.logback.core.testUtil.RandomUtil;
import ch.qos.logback.core.util.CoreTestConstants;
import ch.qos.logback.core.util.EnvUtil;
import ch.qos.logback.core.util.FileUtil;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class FileStoreUtilTest {


  int diff = RandomUtil.getPositiveInt();
  String pathPrefix = CoreTestConstants.OUTPUT_DIR_PREFIX+"fs"+diff+"/";

  @Test
  public void filesOnSameFolderShouldBeOnTheSameFileStore() throws Exception {
    if(!EnvUtil.isJDK7OrHigher())
      return;

    File parent = new File(pathPrefix);
    File file = new File(pathPrefix+"filesOnSameFolderShouldBeOnTheSameFileStore");
    FileUtil.createMissingParentDirectories(file);
    file.createNewFile();
    assertTrue(FileStoreUtil.areOnSameFileStore(parent, file));
  }


  // test should be run manually
  @Ignore
  @Test
  public void manual_filesOnDifferentVolumesShouldBeDetectedAsSuch() throws Exception {
    if(!EnvUtil.isJDK7OrHigher())
      return;

    // author's computer has two volumes
    File c = new File("c:/tmp/");
    File d = new File("d:/");
    assertFalse(FileStoreUtil.areOnSameFileStore(c, d));
  }
}
