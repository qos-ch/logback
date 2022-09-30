package ch.qos.logback.classic.util;

import ch.qos.logback.core.util.EnvUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
public class EnvUtilTest {


    @BeforeEach
    public void setUp() throws Exception {

    }

    // this test runs fine if run from logback-classic but fails when
    // run from logback-core. This is due to the fact that package information
    // is added when creating the jar.
    @Test
    public void versionTest() {
        String versionStr = EnvUtil.logbackVersion();
        assertNotNull(versionStr);
        assertTrue(versionStr.startsWith("1.4"));
    }




}
