package ch.qos.logback.classic.issue.github867;

import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.util.FileSize;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.ClassicTestConstants;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class Github867Test {

    LoggerContext lc;
    Logger logger;

    private static final String ONE_KB_STRING;

    static {

        StringBuilder sb = new StringBuilder();
        for (int j = 0; j < 100; j++) {
            String message = "1234567890";
            sb.append(message);
        }
        ONE_KB_STRING = sb.toString();
    }

    @BeforeEach
    void setUp() throws JoranException {

        this.clearLogsFolder();

        lc = (LoggerContext) LoggerFactory.getILoggerFactory();
        lc.reset();
        logger = lc.getLogger(Github867Test.class);

        JoranConfigurator configurator = new JoranConfigurator();
        configurator.setContext(lc);
        configurator.doConfigure(ClassicTestConstants.INPUT_PREFIX + "issue/logback_github867.xml");
    }

    @AfterEach
    void clear() {

        this.clearLogsFolder();
    }

    @Test
    void testGenerateFiles() throws InterruptedException, IOException {

        List<FileSize> fileSizes = this.returnFileSizesAfterLogging();
        assertThat(fileSizes).hasSize(2)
                .contains(new FileSize(512934));

        fileSizes = this.returnFileSizesAfterLogging();
        assertThat(fileSizes).hasSize(4)
                .contains(new FileSize(512934), new FileSize(512934), new FileSize(512934));
    }

    private void clearLogsFolder() {

        if (lc != null) {
            lc.stop();
        }

        Path logPath = Paths.get("logs");
        try (Stream<Path> logs = Files.walk(logPath)) {
            logs.filter(Files::isRegularFile)
                    .map(Path::toFile)
                    .forEach(File::delete);

            logPath.toFile().delete();
        } catch (IOException ignored) {

        }
    }

    private List<FileSize> returnFileSizesAfterLogging() throws InterruptedException, IOException {

        for (int i = 0; i < 1000; i++) {
            logger.info("{} - {}", i, ONE_KB_STRING);
            Thread.sleep(1);
        }

        try (Stream<Path> logs = Files.walk(Paths.get("logs"))) {

            return logs
                    .filter(Files::isRegularFile)
                    .map(file -> new FileSize(file.toFile().length()))
                    .collect(Collectors.toList());
        }
    }

}
