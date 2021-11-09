package ch.qos.logback.core.rolling.helper;

import static org.junit.Assert.assertArrayEquals;

import java.io.File;
import java.util.Date;

import org.junit.Test;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.ContextBase;

public class SizeAndTimeBasedArchiveRemoverTest {

    Context context = new ContextBase();

    @Test
    public void smoke() {
        final FileNamePattern fileNamePattern = new FileNamePattern("smoke-%d-%i.gz", context);
        final SizeAndTimeBasedArchiveRemover remover = new SizeAndTimeBasedArchiveRemover(fileNamePattern, null);
        final File[] fileArray = new File[2];
        final File[] expected = new File[2];

        fileArray[0] = expected[1] = new File("/tmp/smoke-1970-01-01-0.gz");
        fileArray[1] = expected[0] = new File("/tmp/smoke-1970-01-01-1.gz");

        remover.descendingSort(fileArray, new Date(0));

        assertArrayEquals(expected, fileArray);
    }

    @Test
    public void badFilenames() {
        final FileNamePattern fileNamePattern = new FileNamePattern("smoke-%d-%i.gz", context);
        final SizeAndTimeBasedArchiveRemover remover = new SizeAndTimeBasedArchiveRemover(fileNamePattern, null);
        final File[] fileArray = new File[2];
        final File[] expected = new File[2];

        fileArray[0] = expected[0] = new File("/tmp/smoke-1970-01-01-b.gz");
        fileArray[1] = expected[1] = new File("/tmp/smoke-1970-01-01-c.gz");

        remover.descendingSort(fileArray, new Date(0));

        assertArrayEquals(expected, fileArray);
    }
}
