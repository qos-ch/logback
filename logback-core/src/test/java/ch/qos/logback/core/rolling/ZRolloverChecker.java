package ch.qos.logback.core.rolling;


import ch.qos.logback.core.util.Compare;
import ch.qos.logback.core.util.CoreTestConstants;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertTrue;

public class ZRolloverChecker implements RolloverChecker {

  String testId;

  public ZRolloverChecker(String testId) {
    this.testId = testId;
  }

  public void check(List<String> expectedFilenameList) throws IOException {
    int lastIndex = expectedFilenameList.size() - 1;
    String lastFile = expectedFilenameList.get(lastIndex);
    String witnessFileName = CoreTestConstants.TEST_SRC_PREFIX + "witness/rolling/tbr-" + testId;
    assertTrue(Compare.compare(lastFile, witnessFileName));
  }
}
