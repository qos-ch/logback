package ch.qos.logback.core.model.processor.conditional;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class IfModelHandlerTest {


    @Test
    public void testBlacklistedRefs() throws Exception {
        assertFalse(IfModelHandler.hasBlacklistedReferences("true"));
        assertTrue(IfModelHandler.hasBlacklistedReferences("new Integer(1)"));
        assertTrue(IfModelHandler.hasBlacklistedReferences("org.springframework"));
        assertTrue(IfModelHandler.hasBlacklistedReferences("java.lang.Runtime"));
    }
}
