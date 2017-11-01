package ch.qos.logback.core.joran.conditional;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.ref.WeakReference;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.spi.PropertyContainer;

public class PropertyEvalJavaScriptBuilder implements Evaluation {

    public static final String CONDITIONAL_JS_ATTR = "conditional-js";
    private static final String JS_ENGINE_NAME = "JavaScript";

    // variable names provided in JavaScript context
    static final String PROPERTIES = "props";
    static final String CONTEXT = "ctx";

    private static WeakReference<ScriptEngine> globalEngine = new WeakReference<ScriptEngine>(null);
    private Context context;
    ScriptEngine engine;

    final PropertyContainer localPropContainer;

    PropertyEvalJavaScriptBuilder(PropertyContainer localPropContainer) {
        this.localPropContainer = localPropContainer;
        
        engine = globalEngine.get();
        if (engine == null) {
            final ScriptEngineManager factory = new ScriptEngineManager();
            engine = factory.getEngineByName(JS_ENGINE_NAME);
            if (engine == null) {
                throw new IllegalStateException("JavaScript not supported");
            }
            final Reader reader = new InputStreamReader(getClass().getResourceAsStream("functions.js"));
            try {
                engine.eval(reader);
            } catch (ScriptException e) {
                throw new IllegalStateException(e);
            } finally {
                try {
                    reader.close();
                } catch (IOException e) {
                    throw new IllegalStateException(e);
                }
            }
            globalEngine = new WeakReference<ScriptEngine>(engine);
        }
    }
    
    @Override
    public Condition build(String script) {
        return new JavaScriptCondition(engine, localPropContainer, context, script);
    }

    @Override
    public void setContext(Context context) {
        this.context = context;
    }

    private static class JavaScriptCondition implements Condition {
        
        final ScriptEngine executor;
        final String script;

        JavaScriptCondition(ScriptEngine engine, PropertyContainer properties, Context context, String expression) {
            executor = engine;
            Bindings bindings = executor.getBindings(ScriptContext.ENGINE_SCOPE);
            bindings.put(PROPERTIES, properties);
            bindings.put(CONTEXT, context);
            script = expression;
        }

        @Override
        public boolean evaluate() {
            try {
                Object result = executor.eval(script);
                if (result != null && Boolean.class.equals(result.getClass())) {
                    return ((Boolean) result).booleanValue();
                }
                return false;
            } catch (ScriptException e) {
                throw new IllegalStateException(e);
            }
        }
    }
}
