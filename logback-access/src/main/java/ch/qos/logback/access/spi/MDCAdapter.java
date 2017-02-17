package ch.qos.logback.access.spi;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

/**
 * Adapts MDC context map access to real provider (slf4j, log4j2 or log4j1)
 */
public class MDCAdapter {

    private static final Class<?>[] ADAPTERS = {Slf4jAdapter.class, Log4j2Adapter.class, Log4jAdapter.class};

    public interface MDCAccessAdapter {
        Map<String, String> getCopyOfContextMap();
    }

    static MDCAccessAdapter adapter;

    private MDCAdapter() {
    }

    public static Map<String, String> getCopyOfContextMap(){
        assert adapter != null;
        return adapter.getCopyOfContextMap();
    }

    static MDCAccessAdapter getAdapter() {

        for( int i = 0; i< ADAPTERS.length; i++){
            try{
                Constructor<MDCAccessAdapter> ctor = (Constructor<MDCAccessAdapter>) ADAPTERS[i].getConstructor();
                return ctor.newInstance();
            }
            catch( NoSuchMethodException e ){
                // NOP: try next
            }
            catch( ReflectiveOperationException e ){
                // NOP: try next
            }
        }
        return new NopAdapter();
    }

    static {
        MDCAdapter.adapter = getAdapter();
    }

    /**
     * Common adapter part
     */
    private static class CommonAdapter implements MDCAccessAdapter {

        protected final Method method;

        public CommonAdapter(String klass, String method) throws ReflectiveOperationException {
            Method _method = Class.forName(klass).getDeclaredMethod(method);
            if( ! Map.class.isAssignableFrom(_method.getReturnType()) ){
                throw new IllegalArgumentException(String.format("Expected %s.%s to return a map type not: %s",
                        klass, method, _method.getReturnType().getTypeName()));
            }
            this.method = _method;
        }

        /**
         *
         * @return
         */
        public Map<String,String> getCopyOfContextMap() {
            try{
                return (Map<String,String>) method.invoke(null);
            }
            catch( IllegalAccessException e ){
                throw new IllegalStateException(e);
            }
            catch( InvocationTargetException e ){
                throw new IllegalStateException(e);
            }
        }
    }

    /**
     * Adapter for slf4j-api
     */
    private static class Slf4jAdapter extends CommonAdapter {

        public Slf4jAdapter() throws ReflectiveOperationException {
            super("org.slf4j.MDC", "getCopyOfContextMap");
        }
    }

    /**
     * Adapter for log4j version 2
     */
    private static class Log4j2Adapter extends CommonAdapter {

        public Log4j2Adapter() throws ReflectiveOperationException {
            super("org.apache.logging.log4j.ThreadContext", "getContext");
        }
    }

    /**
     * Adapter for log4j version 1
     */
    private static class Log4jAdapter extends CommonAdapter {

        public Log4jAdapter() throws ReflectiveOperationException {
            super("org.apache.log4j.MDC", "getContext");
        }

        @Override
        public Map<String,String> getCopyOfContextMap() {
            try{
                Hashtable<String, Object> table = (Hashtable<String, Object>) method.invoke(null);
                Map<String, String> res = new HashMap<String, String>(table.size());
                for( Map.Entry<String, Object> kv: table.entrySet()){
                    Object value = kv.getValue();
                    if( value != null ){
                        res.put(kv.getKey(), value.toString());
                    }
                }
                return res;
            }
            catch( IllegalAccessException e ){
                throw new IllegalStateException(e);
            }
            catch( InvocationTargetException e ){
                throw new IllegalStateException(e);
            }
        }
    }

    /**
     * fallback adapter
     */
    private static class NopAdapter implements MDCAccessAdapter {

        public NopAdapter() {
        }

        public Map<String,String> getCopyOfContextMap() {
            return Collections.<String,String>emptyMap();
        }
    }
}
