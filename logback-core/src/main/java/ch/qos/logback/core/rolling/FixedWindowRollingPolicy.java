/**
 * LOGBack: the reliable, fast and flexible logging library for Java.
 * 
 * Copyright (C) 1999-2006, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */
package ch.qos.logback.core.rolling;

import java.io.File;

import ch.qos.logback.core.rolling.helper.Compress;
import ch.qos.logback.core.rolling.helper.FileNamePattern;
import ch.qos.logback.core.rolling.helper.IntegerTokenConverter;
import ch.qos.logback.core.rolling.helper.RenameUtil;

/**
 * When rolling over, <code>FixedWindowRollingPolicy</code> renames files
 * according to a fixed window algorithm as described below.
 * 
 * <p>
 * The <b>ActiveFileName</b> property, which is required, represents the name
 * of the file where current logging output will be written. The
 * <b>FileNamePattern</b> option represents the file name pattern for the
 * archived (rolled over) log files. If present, the <b>FileNamePattern</b>
 * option must include an integer token, that is the string "%i" somewhere
 * within the pattern.
 * 
 * <p>
 * Let <em>max</em> and <em>min</em> represent the values of respectively
 * the <b>MaxIndex</b> and <b>MinIndex</b> options. Let "foo.log" be the value
 * of the <b>ActiveFile</b> option and "foo.%i.log" the value of
 * <b>FileNamePattern</b>. Then, when rolling over, the file
 * <code>foo.<em>max</em>.log</code> will be deleted, the file
 * <code>foo.<em>max-1</em>.log</code> will be renamed as
 * <code>foo.<em>max</em>.log</code>, the file
 * <code>foo.<em>max-2</em>.log</code> renamed as
 * <code>foo.<em>max-1</em>.log</code>, and so on, the file
 * <code>foo.<em>min+1</em>.log</code> renamed as
 * <code>foo.<em>min+2</em>.log</code>. Lastly, the active file
 * <code>foo.log</code> will be renamed as <code>foo.<em>min</em>.log</code>
 * and a new active file name <code>foo.log</code> will be created.
 * 
 * <p>
 * Given that this rollover algorithm requires as many file renaming operations
 * as the window size, large window sizes are discouraged. The current
 * implementation will automatically reduce the window size to 12 when larger
 * values are specified by the user.
 * 
 * @author Ceki G&uuml;lc&uuml;
 */
public class FixedWindowRollingPolicy extends RollingPolicyBase {
  static final String FNP_NOT_SET = "The FileNamePattern option must be set before using FixedWindowRollingPolicy. ";
  static final String SEE_FNP_NOT_SET = "See also http://logback.qos.ch/codes.html#tbr_fnp_not_set";
  int maxIndex;
  int minIndex;
  RenameUtil util = new RenameUtil();
  Compress compress = new Compress();

  /**
   * It's almost always a bad idea to have a large window size, say over 12.
   */
  private static int MAX_WINDOW_SIZE = 12;

  public FixedWindowRollingPolicy() {
    minIndex = 1;
    maxIndex = 7;
    activeFileName = null;
  }

  public void start() {
    // set the LR for our utility object
    util.setContext(this.context);
    compress.setContext(this.context);

    if (fileNamePatternStr != null) {
      fileNamePattern = new FileNamePattern(fileNamePatternStr, this.context);
      determineCompressionMode();
    } else {
      addWarn(FNP_NOT_SET);
      addWarn(SEE_FNP_NOT_SET);
      throw new IllegalStateException(FNP_NOT_SET + SEE_FNP_NOT_SET);
    }
    if (activeFileName == null) {
      addWarn("The ActiveFile name option must be set before using this rolling policy.");
      throw new IllegalStateException("The ActiveFileName option must be set.");
    }

    if (maxIndex < minIndex) {
      addWarn("MaxIndex (" + maxIndex + ") cannot be smaller than MinIndex ("
          + minIndex + ").");
      addWarn("Setting maxIndex to equal minIndex.");
      maxIndex = minIndex;
    }

    if ((maxIndex - minIndex) > MAX_WINDOW_SIZE) {
      addWarn("Large window sizes are not allowed.");
      maxIndex = minIndex + MAX_WINDOW_SIZE;
      addWarn("MaxIndex reduced to " + maxIndex);
    }

    IntegerTokenConverter itc = fileNamePattern.getIntegerTokenConverter();

    if (itc == null) {
      throw new IllegalStateException("FileNamePattern ["
          + fileNamePattern.getPattern()
          + "] does not contain a valid IntegerToken");
    }
  }

  public void rollover() throws RolloverFailure {
    // Inside this method it is guaranteed that the hereto active log file is
    // closed.
    // If maxIndex <= 0, then there is no file renaming to be done.
    if (maxIndex >= 0) {
      // Delete the oldest file, to keep Windows happy.
      File file = new File(fileNamePattern.convertInt(maxIndex));

      if (file.exists()) {
        file.delete();
      }

      // Map {(maxIndex - 1), ..., minIndex} to {maxIndex, ..., minIndex+1}
      for (int i = maxIndex - 1; i >= minIndex; i--) {
        String toRenameStr = fileNamePattern.convertInt(i);
        File toRename = new File(toRenameStr);
        // no point in trying to rename an inexistent file
        if (toRename.exists()) {
          util.rename(toRenameStr, fileNamePattern.convertInt(i + 1));
        } else {
          addInfo("Skipping roll=over for inexistent file " + toRenameStr);
        }
      }

      // move active file name to min
      switch (compressionMode) {
      case Compress.NONE:
        util.rename(activeFileName, fileNamePattern.convertInt(minIndex));
        break;
      case Compress.GZ:
        compress.GZCompress(activeFileName, fileNamePattern.convertInt(minIndex));
        break;
      case Compress.ZIP:
        compress.ZIPCompress(activeFileName, fileNamePattern.convertInt(minIndex));
        break;
      }
    }
  }

  /**
   * Return the value of the <b>ActiveFile</b> option.
   * 
   * @see {@link setActiveFileName}.
   */
  public String getActiveFileName() {
    // TODO This is clearly bogus.
    return activeFileName;
  }

  public int getMaxIndex() {
    return maxIndex;
  }

  public int getMinIndex() {
    return minIndex;
  }

  public void setMaxIndex(int maxIndex) {
    this.maxIndex = maxIndex;
  }

  public void setMinIndex(int minIndex) {
    this.minIndex = minIndex;
  }

}
