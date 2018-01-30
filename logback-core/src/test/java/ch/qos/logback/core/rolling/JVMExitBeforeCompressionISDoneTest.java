package ch.qos.logback.core.rolling;

import java.util.Date;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.encoder.EchoEncoder;
import ch.qos.logback.core.hook.DefaultShutdownHook;
import ch.qos.logback.core.rolling.testUtil.ScaffoldingForRollingTests;
import ch.qos.logback.core.status.OnConsoleStatusListener;
import ch.qos.logback.core.testUtil.RandomUtil;
import ch.qos.logback.core.util.StatusListenerConfigHelper;
import ch.qos.logback.core.util.StatusPrinter;
@Ignore
public class JVMExitBeforeCompressionISDoneTest extends ScaffoldingForRollingTests {

    RollingFileAppender<Object> rfa = new RollingFileAppender<Object>();
    TimeBasedRollingPolicy<Object> tbrp = new TimeBasedRollingPolicy<Object>();
    DefaultShutdownHook delayingShutdownHook = new DefaultShutdownHook();
    
    static final long FRI_2016_05_13_T_170415_GMT = 1463159055630L;
                                                    
    EchoEncoder<Object> encoder = new EchoEncoder<Object>();

    @Before
    @Override
    public void setUp() {
        super.setUp();
        StatusListenerConfigHelper.addOnConsoleListenerInstance(context, new OnConsoleStatusListener());
        delayingShutdownHook.setContext(context);
        initRFA(rfa);
    }

    void initRFA(RollingFileAppender<Object> rfa) {
        rfa.setContext(context);
        rfa.setEncoder(encoder);
    }

    void initTRBP(RollingFileAppender<Object> rfa, TimeBasedRollingPolicy<Object> tbrp, String filenamePattern, long givenTime) {
        tbrp.setContext(context);
        tbrp.setFileNamePattern(filenamePattern);
        tbrp.setParent(rfa);
        tbrp.timeBasedFileNamingAndTriggeringPolicy = new DefaultTimeBasedFileNamingAndTriggeringPolicy<Object>();
        tbrp.timeBasedFileNamingAndTriggeringPolicy.setCurrentTime(givenTime);
        rfa.setRollingPolicy(tbrp);
        tbrp.start();
        rfa.start();
    }

    @After
    public void tearDown() throws Exception {
        StatusPrinter.print(context);
    }

    @Ignore
    @Test
    public void test1() {
        Thread shutdownThread = new Thread(delayingShutdownHook);
        Runtime.getRuntime().addShutdownHook(shutdownThread);
        
        String patternPrefix = "test1";
        String compressionSuffix = ".zip";

        this.currentTime = FRI_2016_05_13_T_170415_GMT;
        
        Date d = new Date(FRI_2016_05_13_T_170415_GMT); //WED_2016_03_23_T_230705_CET);
        System.out.println(d);
        System.out.print(d.getTime());
        
        int ticksPerHour = 100;
        int hours = 7;
        int totalTicks = ticksPerHour*hours;
        long singleTickDuration = CoreConstants.MILLIS_IN_ONE_HOUR/ticksPerHour;
        
        String fileNamePatternStr = randomOutputDir + patternPrefix + "-%d{" + DATE_PATTERN_BY_DAY + ", GMT}" + compressionSuffix;
        initTRBP(rfa, tbrp, fileNamePatternStr, currentTime);

        incCurrentTime(singleTickDuration);
        tbrp.timeBasedFileNamingAndTriggeringPolicy.setCurrentTime(currentTime);

        for (int i = 0; i < totalTicks; i++) {
            StringBuilder sb = new StringBuilder(1000);
            sb.append("Hello");
            for(int j = 0; j < 100; j++) {
                sb.append(RandomUtil.getPositiveInt());
            }
            sb.append(i);
            
            rfa.doAppend(sb.toString());
            addExpectedFileNamedIfItsTime_ByDate(fileNamePatternStr);
            incCurrentTime(singleTickDuration);
            tbrp.timeBasedFileNamingAndTriggeringPolicy.setCurrentTime(currentTime);
        }
        

            
        
        // String nameOfExpectedZipFile = randomOutputDir + patternPrefix+"-2016-05-13.zip";;
        
        // File expectedZipFile = new File(nameOfExpectedZipFile);
        // assertTrue("expecting file ["+nameOfExpectedZipFile+"] to exist", expectedZipFile.exists());
        // File[] files = getFilesInDirectory(randomOutputDir);
        // assertEquals(2, files.length);
    }

}
