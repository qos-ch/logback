package ch.qos.logback.core.helpers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.testUtil.RandomUtil;
import ch.qos.logback.core.util.CoreTestConstants;
import ch.qos.logback.core.util.FileUtil;

public class MemMappedBufferedOutputStreamTest {

    int diff = RandomUtil.getPositiveInt();
    String randomOutputDir = CoreTestConstants.OUTPUT_DIR_PREFIX + diff + "/";

    @Before
    public void setUp() throws Exception {
    }

    private File createFile(String path) {
        File file = new File(path);
        FileUtil.createMissingParentDirectories(file);
        return file;
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void smoke() throws IOException {
        File file = createFile(randomOutputDir + "test");
        RandomAccessFile raf = new RandomAccessFile(file, "rw");
        MemMappedBufferedOutputStream mmbos = new MemMappedBufferedOutputStream(raf.getChannel(), 0);
        mmbos.write("hello".getBytes());
        mmbos.close();
        assertEquals(5, file.length());
    }

    @Test
    public void bufferOverflow() throws IOException {
        File file = createFile(randomOutputDir + "bufferOverflow.txt");
        RandomAccessFile raf = new RandomAccessFile(file, "rw");
        MemMappedBufferedOutputStream mmbos = new MemMappedBufferedOutputStream(raf.getChannel(), 0);

        String prefix = "2017-02-01 19:19:35,291 DEBUG [ch.qos.logback.FileAppenderBenchmark.logbackFile-jmh-worker-16] FileAppenderBenchmark  - This is a debug message";
        int runLen = 1000*1000;
        for (int i = 0; i < runLen; i++) {
            String msg = prefix + i + CoreConstants.LINE_SEPARATOR;
            mmbos.write(msg.getBytes());
        }
        mmbos.close();

        System.out.println("Reading");
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String s;
        int count = 0;
        while ((s = reader.readLine()) != null) {
            //System.out.println(s);
            assertTrue(s.startsWith(prefix));
            assertTrue(s.endsWith(Integer.valueOf(count).toString()));
            count++;
        }
        assertEquals(runLen, count);

    }

}
