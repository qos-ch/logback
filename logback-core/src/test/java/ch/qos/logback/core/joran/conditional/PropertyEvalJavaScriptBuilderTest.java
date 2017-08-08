package ch.qos.logback.core.joran.conditional;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

public class PropertyEvalJavaScriptBuilderTest extends PropertyEvalScriptBuilderTest {

    PropertyEvalJavaScriptBuilder builder = new PropertyEvalJavaScriptBuilder(localPropContainer);

    @Before
    public void setUp() {
        super.setUp();
        builder.setContext(context);
    }

    @Override
    void buildAndAssertTrue(String script) throws Exception {
        Condition condition = builder.build(script);
        assertNotNull(condition);
        assertTrue(condition.evaluate());
    }

    @Override
    void buildAndAssertFalse(String script) throws Exception {
        Condition condition = builder.build(script);
        assertNotNull(condition);
        assertFalse(condition.evaluate());
    }

    @Test
    public void javaScriptEngineIsReused() throws Exception {
        PropertyEvalJavaScriptBuilder anotherBuilder = new PropertyEvalJavaScriptBuilder(localPropContainer);
        assertSame(builder.engine, anotherBuilder.engine);
    }
    
}
