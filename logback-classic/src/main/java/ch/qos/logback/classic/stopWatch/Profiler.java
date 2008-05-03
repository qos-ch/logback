package ch.qos.logback.classic.stopwatch;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import ch.qos.logback.core.pattern.SpacePadder;

/*
 +  Profiler [BAS]
 |-- elapsed time            [doX]     0 milliseconds.
 |-- elapsed time        [doYYYYY]    56 milliseconds.
 |--+ Profiler Y
    |-- elapsed time            [doZ]    21 milliseconds.
    |-- elapsed time            [doZ]    21 milliseconds.
    |-- Total elapsed time        [Y]    78 milliseconds.
 |-- elapsed time            [doZ]    21 milliseconds.
 |-- Total elapsed time      [BAS]    78 milliseconds.
*/
             
public class Profiler {

  final static int MIN_SW_NAME_LENGTH = 10;
  final static int MIN_SW_ELAPSED_TIME_NUMBER_LENGTH = 5;
  
  final String name;
  final StopWatch globalStopWatch;

  List<StopWatch> stopwatchList = new ArrayList<StopWatch>();

  public Profiler(String name) {
    this.name = name;
    this.globalStopWatch = new StopWatch(name);
  }

  public void start(String name) {
    stopLastStopWatch();
    StopWatch newSW = new StopWatch(name);
    stopwatchList.add(newSW);
  }

  StopWatch getLastStopWatch() {
    if (stopwatchList.size() > 0) {
      return stopwatchList.get(stopwatchList.size() - 1);
    } else {
      return null;
    }
  }

  void stopLastStopWatch() {
    StopWatch last = getLastStopWatch();
    if (last != null) {
      last.stop();
    }
  }

  public void stop() {
    stopLastStopWatch();
    globalStopWatch.stop();
    DurationUnit du = Util.selectDurationUnitForDisplay(globalStopWatch);
    for (StopWatch sw : stopwatchList) {
      printChildStopWatch(System.out, sw, du);
    }
    printGlobalSW(System.out, globalStopWatch, du);
  }

  static void printChildStopWatch(PrintStream ps, StopWatch sw, DurationUnit du) {
    StringBuffer buf = new StringBuffer();
    buf.append("  |-");
    buf.append(" elapsed time       ");
    SpacePadder.leftPad(buf, "["+sw.getName()+"]", MIN_SW_NAME_LENGTH);
    buf.append(" ");
    String timeStr = Util.durationInDunrationUnitsAsStr(sw.getResultInNanos(), du);
    SpacePadder.leftPad(buf, timeStr, MIN_SW_ELAPSED_TIME_NUMBER_LENGTH);
    buf.append(" ");
    Util.appendDurationUnitAsStr(buf, du);
    ps.println(buf.toString());
  }

  private static void printGlobalSW(PrintStream ps, StopWatch sw, DurationUnit du) {
    StringBuffer buf = new StringBuffer();
    buf.append("  +-");
    buf.append(" Total elapsed time ");
    SpacePadder.leftPad(buf, "["+sw.getName()+"]", MIN_SW_NAME_LENGTH);
    buf.append(" ");
    String timeStr = Util.durationInDunrationUnitsAsStr(sw.getResultInNanos(), du);
    SpacePadder.leftPad(buf, timeStr, MIN_SW_ELAPSED_TIME_NUMBER_LENGTH);
    buf.append(" ");
    Util.appendDurationUnitAsStr(buf, du);
    ps.println(buf.toString());
  }

}
