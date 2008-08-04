package ch.qos.logback.classic.util;

import java.util.Arrays;
import java.util.Random;

import junit.framework.AssertionFailedError;

public class BogoPerf {

  private static long NANOS_IN_ONE_SECOND = 1000 * 1000 * 1000;
  private static int INITIAL_N = 1000;
  private static int LAST_N = 100;
  private static double INITIAL_BOGO_IPS = computeBogoIPS(INITIAL_N);

  private static int SLACK = 2;

  static {
    System.out.println("Host runs at "+INITIAL_BOGO_IPS + " BIPS");
  }
  
  /**
   * Compute bogoInstructions per second
   * <p>
   * on a 3.2 Ghz Pentium D CPU (around 2007), we obtain about 10'000 bogoIPS.
   * 
   * @param N
   *                number of bogoInstructions to average over in order to
   *                compute the result
   * @return bogo Instructions Per Second
   */
  private static double computeBogoIPS(int N) {
    long begin = System.nanoTime();

    for (int i = 0; i < N; i++) {
      bogoInstruction();
    }
    long end = System.nanoTime();

    // duration
    double D = end - begin;
    // average duration per instruction
    double avgDPIS = D / N;
    // System.out.println(D + " nanos for " + N + " instructions");
    // System.out.println(avgD + " nanos per instruction");

    double bogoIPS = NANOS_IN_ONE_SECOND / avgDPIS;
    // System.out.println(bogoIPS + " bogoIPS");

    return bogoIPS;
  }

  private static void bogoInstruction() {
    Random random = new Random(100);
    int len = 500;
    int[] intArray = new int[len];
    for (int i = 0; i < len; i++) {
      intArray[i] = random.nextInt();
    }
    Arrays.sort(intArray);
  }

  /**
   * Computed the BogoIPS for this host CPU.
   * 
   * @return
   */
  public static double currentBIPS() {
    double lastBogos = computeBogoIPS(LAST_N);
    return (2 * lastBogos + INITIAL_BOGO_IPS) / 3;
  }

  /**
   * Assertion used for values that <b>decrease</b> with faster CPUs, 
   * typically the time (duration) needed to perform a task.
   * 
   * @param currentDuration
   * @param referenceDuraion
   * @param referenceBIPS
   * @throws AssertionFailedError
   */
  public static void assertDuration(double currentDuration,
      long referenceDuraion, double referenceBIPS)
      throws AssertionFailedError {
    double ajustedDuration = adjustExpectedDuration(referenceDuraion,
        referenceBIPS);
    if (currentDuration > ajustedDuration * SLACK) {
      throw new AssertionFailedError(currentDuration + " exceeded expected "
          + ajustedDuration + " (adjusted), " + referenceDuraion + " (raw)");
    }
  }
  /**
   * Assertion used for values that <b>increase<b> with faster CPUs, typically 
   * the number of operations accomplished per unit of time.
   * 
   * @param currentPerformance
   * @param referencePerformance
   * @param referenceBIPS
   * @throws AssertionFailedError
   */
  public static void assertPerformance(double currentPerformance,
      long referencePerformance, double referenceBIPS)
      throws AssertionFailedError {
    double ajustedPerf = adjustExpectedPerformance(referencePerformance,
        referenceBIPS);
    if (currentPerformance*SLACK < ajustedPerf) {
      throw new AssertionFailedError(currentPerformance + " below expected "
          + ajustedPerf + " (adjusted), " + referencePerformance + " (raw)");
    }
  }
  
  private static double adjustExpectedPerformance(long referenceDuration,
      double referenceBIPS) {
    double currentBIPS = currentBIPS();
    return referenceDuration * (currentBIPS/referenceBIPS);
  }
  
  private static double adjustExpectedDuration(long referenceDuration,
      double referenceBIPS) {
    double currentBIPS = currentBIPS();
    return referenceDuration * (referenceBIPS / currentBIPS);
  }
}
