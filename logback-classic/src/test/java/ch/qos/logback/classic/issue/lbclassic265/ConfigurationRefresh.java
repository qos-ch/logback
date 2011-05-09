package ch.qos.logback.classic.issue.lbclassic265;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author iwein
 */
public class ConfigurationRefresh {
    private static final Logger log = LoggerFactory.getLogger(ConfigurationRefresh.class);

    public static void main(String[] args) throws Exception {
        new ConfigurationRefresh().shouldPickupNewConfig();
    }

    public void shouldPickupNewConfig() throws Exception {
        //run this test and edit classpath:logback.xml while it is running to experiment
        while (true) {
            log.info("ping");
            Thread.sleep(2000);
        }
    }
}
