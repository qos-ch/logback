package ch.qos.logback.classic;

import java.util.concurrent.CyclicBarrier;

import org.junit.Ignore;
import org.junit.Test;

import ch.qos.logback.core.contention.AbstractMultiThreadedHarness;
import ch.qos.logback.core.contention.RunnableWithCounterAndDone;
import ch.qos.logback.core.testUtil.StatusChecker;

@Ignore
public class LoggerContextConcurrentResetTest {
    static int CONCURRENT_RESET_THREAD_COUNT = 10;

    // see http://jira.qos.ch/browse/LOGBACK-397
    @Test(timeout = 1000)
    public void concurrentReset() throws InterruptedException {
        final LoggerContext loggerContext = new LoggerContext();
        final CyclicBarrier cyclicBarrier = new CyclicBarrier(CONCURRENT_RESET_THREAD_COUNT);
        final StatusChecker statusChecker = new StatusChecker(loggerContext);
        final int desiredResetCount = 100;
        final RunnableWithCounterAndDone[] runnableArray = buildRunnableArray(loggerContext, cyclicBarrier);
        final Harness harness = new Harness((Resetter) runnableArray[0], desiredResetCount);
        harness.execute(runnableArray);
        statusChecker.assertIsErrorFree();
    }

    static class Harness extends AbstractMultiThreadedHarness {
        int desiredResetCount;
        Resetter resetter;

        Harness(final Resetter resetter, final int desiredResetCount) {
            this.resetter = resetter;
            this.desiredResetCount = desiredResetCount;
        }

        @Override
        public void waitUntilEndCondition() throws InterruptedException {
            while (resetter.getCounter() < desiredResetCount) {
                Thread.yield();
            }
        }
    }

    static class GetLoggerRunnable extends RunnableWithCounterAndDone {

        final int burstLength = 30;
        LoggerContext loggerContext;
        CyclicBarrier cyclicBarrier;
        String nameSuffix;

        GetLoggerRunnable(final LoggerContext loggerContext, final CyclicBarrier cyclicBarrier, final String nameSuffix) {
            this.loggerContext = loggerContext;
            this.cyclicBarrier = cyclicBarrier;
            this.nameSuffix = nameSuffix;
        }

        @Override
        public void run() {
            try {
                cyclicBarrier.await();
            } catch (final Exception e) {
            }

            while (!isDone()) {
                final long i = counter % burstLength;
                loggerContext.getLogger("org.bla." + nameSuffix + ".x" + i);
                counter++;
                if (i == 0) {
                    Thread.yield();
                }
            }
        }
    }

    static class Resetter extends RunnableWithCounterAndDone {
        LoggerContext loggerContext;
        CyclicBarrier cyclicBarrier;
        public int resetCount = 0;

        Resetter(final LoggerContext loggerContext, final CyclicBarrier cyclicBarrier) {
            this.loggerContext = loggerContext;
            this.cyclicBarrier = cyclicBarrier;
        }

        @Override
        public void run() {
            try {
                cyclicBarrier.await();
            } catch (final Exception e) {
            }
            while (!isDone()) {
                loggerContext.reset();
                counter++;
                Thread.yield();
            }
        }
    }

    private RunnableWithCounterAndDone[] buildRunnableArray(final LoggerContext loggerContext, final CyclicBarrier cyclicBarrier) {
        final RunnableWithCounterAndDone[] rArray = new RunnableWithCounterAndDone[CONCURRENT_RESET_THREAD_COUNT];
        rArray[0] = new Resetter(loggerContext, cyclicBarrier);
        for (int i = 1; i < CONCURRENT_RESET_THREAD_COUNT; i++) {
            rArray[i] = new GetLoggerRunnable(loggerContext, cyclicBarrier, "mouse-" + i);
        }
        return rArray;
    }
}
