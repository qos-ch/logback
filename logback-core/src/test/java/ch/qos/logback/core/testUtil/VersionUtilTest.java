package ch.qos.logback.core.testUtil;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class VersionUtilTest {

    
    @Test
    public void test() {
        System.out.println(System.getProperty("java.version"));
        assertEquals(6, VersionUtil.getJavaMajorVersion("1.6"));
        assertEquals(7, VersionUtil.getJavaMajorVersion("1.7.0_21-b11"));
        assertEquals(8, VersionUtil.getJavaMajorVersion("1.8.0_25"));
    }

    @Test 
    public void testJava9() {
        assertEquals(9, VersionUtil.getJavaMajorVersion("9"));
        assertEquals(9, VersionUtil.getJavaMajorVersion("9.12"));
        assertEquals(9, VersionUtil.getJavaMajorVersion("9ea"));
        
    }

    @Test 
    public void testJava11() {
        assertEquals(11, VersionUtil.getJavaMajorVersion("11"));
        assertEquals(11, VersionUtil.getJavaMajorVersion("11.612"));

    }

}
