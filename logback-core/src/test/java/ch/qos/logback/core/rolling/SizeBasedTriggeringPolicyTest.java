package ch.qos.logback.core.rolling;

import ch.qos.logback.core.util.FileSize;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SizeBasedTriggeringPolicyTest {
    @Test
    public void constructor() {
        SizeBasedTriggeringPolicy sizeBasedTriggeringPolicy = new SizeBasedTriggeringPolicy();

        FileSize expected = new FileSize(SizeBasedTriggeringPolicy.DEFAULT_MAX_FILE_SIZE);
        assertEquals(expected.getSize(), sizeBasedTriggeringPolicy.maxFileSize.getSize());
    }

    @Test
    public void setMaxFileSize() throws Exception {
        FileSize fileSize = new FileSize(300);

        SizeBasedTriggeringPolicy sizeBasedTriggeringPolicy = new SizeBasedTriggeringPolicy();
        sizeBasedTriggeringPolicy.setMaxFileSize(fileSize);

        assertEquals(fileSize.getSize(), sizeBasedTriggeringPolicy.maxFileSize.getSize());
    }

    @Test
    public void getMaxFileSize() throws Exception {
        FileSize fileSize = new FileSize(300);

        SizeBasedTriggeringPolicy sizeBasedTriggeringPolicy = new SizeBasedTriggeringPolicy();
        sizeBasedTriggeringPolicy.setMaxFileSize(fileSize);

        assertEquals(fileSize.getSize(), sizeBasedTriggeringPolicy.getMaxFileSize().getSize());
    }
}