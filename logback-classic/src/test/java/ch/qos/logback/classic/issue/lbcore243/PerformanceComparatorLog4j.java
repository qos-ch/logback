package ch.qos.logback.classic.issue.lbcore243;

/**
 * Created with IntelliJ IDEA.
 * User: ceki
 * Date: 26.04.12
 * Time: 21:52
 * To change this template use File | Settings | File Templates.
 */
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import org.apache.log4j.xml.DOMConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PerformanceComparatorLog4j {

   static org.apache.log4j.Logger log4jlogger = org.apache.log4j.Logger.getLogger(PerformanceComparatorLog4j.class);

   public static void main(String[] args) throws JoranException, InterruptedException {
     initLog4jWithoutImmediateFlush();

     // Let's run once for Just In Time compiler
     log4jDirectDebugCall();


     System.out.println("###############################################");
     System.out.println("Log4j    without immediate flush: " + log4jDirectDebugCall()+ " nanos per call");
     System.out.println("###############################################");
   }

   private static long log4jDirectDebugCall() {
     Integer j = new Integer(2);
     long start = System.nanoTime();
     for (int i = 0; i < Common.loop; i++) {
       log4jlogger.debug("SEE IF THIS IS LOGGED " + j + ".");
     }
     return (System.nanoTime() - start) / Common.loop;
   }

   static String DIR_PREFIX = "src/test/java/ch/qos/logback/classic/issue/lbcore243/";

   static void initLog4jWithoutImmediateFlush() {
     DOMConfigurator domConfigurator = new DOMConfigurator();
     domConfigurator.configure(DIR_PREFIX+"log4j_without_immediateFlush.xml");
   }
   static void initLog4jWithImmediateFlush() {
     DOMConfigurator domConfigurator = new DOMConfigurator();
     domConfigurator.configure(DIR_PREFIX+"log4j_with_immediateFlush.xml");
   }
}