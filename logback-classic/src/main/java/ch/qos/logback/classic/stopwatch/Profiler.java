package ch.qos.logback.classic.stopwatch;

import java.util.ArrayList;
import java.util.List;

import ch.qos.logback.core.Layout;
import ch.qos.logback.core.pattern.SpacePadder;

// +  Profiler [BAS]
// |-- elapsed time            [doX]     0 milliseconds.
// |-- elapsed time        [doYYYYY]    56 milliseconds.
// |--+ Profiler Y
//    |-- elapsed time            [doZ]    21 milliseconds.
//    |-- elapsed time            [doZ]    21 milliseconds.
//    |-- Total elapsed time        [Y]    78 milliseconds.
// |-- elapsed time            [doZ]    21 milliseconds.
// |-- Total elapsed time      [BAS]    78 milliseconds.

public class Profiler {

  final static int MIN_SW_NAME_LENGTH = 12;
  final static int MIN_SW_ELAPSED_TIME_NUMBER_LENGTH = 6;

  final String name;
  final StopWatch globalStopWatch;

  List<StopWatch> stopwatchList = new ArrayList<StopWatch>();
  List<Object> childList = new ArrayList<Object>();

  ProfilerRegistry profilerRegistry;
  
  public Profiler(String name) {
    this.name = name;
    this.globalStopWatch = new StopWatch(name);
  }

  public ProfilerRegistry getProfilerRegistry() {
    return profilerRegistry;
  }

  public void setProfilerRegistry(ProfilerRegistry profilerRegistry) {
    this.profilerRegistry = profilerRegistry;
  }

  public void start(String name) {
    stopLastStopWatch();
    StopWatch childSW = new StopWatch(name);
    stopwatchList.add(childSW);
    childList.add(childSW);
  }

  public Profiler startNested(String name) {
    Profiler nestedProfiler = new Profiler(name);
    nestedProfiler.setProfilerRegistry(profilerRegistry);
    childList.add(nestedProfiler);
    return nestedProfiler;
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

  void stopNestedProfilers() {
    for (Object child : childList) {
      if (child instanceof Profiler)
        ((Profiler) child).stop();
    }
  }

  public Profiler stop() {
    stopLastStopWatch();
    stopNestedProfilers();
    globalStopWatch.stop();
    return this;
  }

  public void print() {
    DurationUnit du = Util.selectDurationUnitForDisplay(globalStopWatch);
    String r = buildString(du, "+", "");
    System.out.println(r);
  }

  private String buildString(DurationUnit du, String prefix, String indentation) {
    StringBuffer buf = new StringBuffer();

    
    buf.append(prefix);
    buf.append(" Profiler [");
    buf.append(name);
    buf.append("]");
    buf.append(Layout.LINE_SEP);
    for (Object child : childList) {
      if(child instanceof StopWatch) {
       buildStringForChildStopWatch(buf, indentation, (StopWatch) child, du);
      } else if(child instanceof Profiler) {
        Profiler profiler = (Profiler) child;
        profiler.stop();
        String subString = profiler.buildString(du, "|--+", indentation + "   ");
        buf.append(subString);
      }
    }
    buildStringForGlobalStopWatch(buf, indentation, globalStopWatch, du);
    return buf.toString();
  }

  private static void buildStringForChildStopWatch(StringBuffer buf,
      String indentation, StopWatch sw, DurationUnit du) {

    buf.append(indentation);
    buf.append("|--");
    buf.append(" elapsed time       ");
    SpacePadder.leftPad(buf, "[" + sw.getName() + "]", MIN_SW_NAME_LENGTH);
    buf.append(" ");
    String timeStr = Util.durationInDunrationUnitsAsStr(sw.getResultInNanos(),
        du);
    SpacePadder.leftPad(buf, timeStr, MIN_SW_ELAPSED_TIME_NUMBER_LENGTH);
    buf.append(" ");
    Util.appendDurationUnitAsStr(buf, du);
    buf.append(Layout.LINE_SEP);
  }

  private static void buildStringForGlobalStopWatch(StringBuffer buf,
      String indentation, StopWatch sw, DurationUnit du) {
    buf.append(indentation);
    buf.append("|--");
    buf.append(" Total elapsed time ");
    SpacePadder.leftPad(buf, "[" + sw.getName() + "]", MIN_SW_NAME_LENGTH);
    buf.append(" ");
    String timeStr = Util.durationInDunrationUnitsAsStr(sw.getResultInNanos(),
        du);
    SpacePadder.leftPad(buf, timeStr, MIN_SW_ELAPSED_TIME_NUMBER_LENGTH);
    buf.append(" ");
    Util.appendDurationUnitAsStr(buf, du);
    buf.append(Layout.LINE_SEP);
  }

}
