package ch.qos.logback.core.joran.conditional;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Stack;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.Attributes;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.ContextBase;
import ch.qos.logback.core.joran.TrivialConfigurator;
import ch.qos.logback.core.joran.action.Action;
import ch.qos.logback.core.joran.action.NOPAction;
import ch.qos.logback.core.joran.action.ext.StackAction;
import ch.qos.logback.core.joran.spi.ActionException;
import ch.qos.logback.core.joran.spi.ElementSelector;
import ch.qos.logback.core.joran.spi.InterpretationContext;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.status.StatusChecker;
import ch.qos.logback.core.util.CoreTestConstants;
import ch.qos.logback.core.util.StatusPrinter;

public class JavaScriptEvaluationTest {

    Context context = new ContextBase();
    StatusChecker checker = new StatusChecker(context);
    TrivialConfigurator tc;
    static final String CONDITIONAL_DIR_PREFIX = CoreTestConstants.JORAN_INPUT_PREFIX + "conditional/";

    String val1 = "val1";
    String sysKey = "sysKeyJS";

    StackAction stackAction = new StackAction();

    @Before
    public void setUp() throws Exception {
        HashMap<ElementSelector, Action> rulesMap = new HashMap<ElementSelector, Action>();
        rulesMap.put(new ElementSelector("x"), new Action() {
            @Override
            public void begin(InterpretationContext ic, String name, Attributes attributes) throws ActionException {
                // ensure JavaScript evaluation is to be used
                assertEquals("true", attributes.getValue(PropertyEvalJavaScriptBuilder.CONDITIONAL_JS_ATTR));
                // activate JavaScript evaluation
                ic.getObjectMap().put(PropertyEvalJavaScriptBuilder.CONDITIONAL_JS_ATTR, Boolean.TRUE);
            }
            @Override
            public void end(InterpretationContext ic, String name) throws ActionException {
            }            
        });
        rulesMap.put(new ElementSelector("x/stack"), stackAction);
        rulesMap.put(new ElementSelector("*/if"), new IfAction());
        rulesMap.put(new ElementSelector("*/if/then"), new ThenAction());
        rulesMap.put(new ElementSelector("*/if/then/*"), new NOPAction());

        tc = new TrivialConfigurator(rulesMap);
        tc.setContext(context);
        System.setProperty(sysKey, val1);
    }

    @After
    public void tearDown() throws Exception {
        StatusPrinter.printIfErrorsOccured(context);
        System.clearProperty(sysKey);
    }

    @Test
    public void javaScriptIsUsed() throws JoranException, Throwable {
        tc.doConfigure(CONDITIONAL_DIR_PREFIX + "javaScriptEvaluation.xml");
        verifyConfig(new String[] { "BEGIN", "a", "END" });
    }

    void verifyConfig(String[] expected) {
        Stack<String> witness = new Stack<String>();
        witness.addAll(Arrays.asList(expected));
        assertEquals(witness, stackAction.getStack());
    }

}
