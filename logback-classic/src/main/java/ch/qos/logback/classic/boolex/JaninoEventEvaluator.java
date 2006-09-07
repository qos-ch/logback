package ch.qos.logback.classic.boolex;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Marker;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.LoggerRemoteView;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.CoreGlobal;
import ch.qos.logback.core.boolex.JaninoEventEvaluatorBase;
import ch.qos.logback.core.boolex.Matcher;



public class JaninoEventEvaluator extends JaninoEventEvaluatorBase {

  
  public final static String IMPORT_LEVEL = "import ch.qos.logback.classic.Level;\r\n";
  
  public final static List<String> DEFAULT_PARAM_NAME_LIST = new ArrayList<String>();
  public final static List<Class> DEFAULT_PARAM_TYPE_LIST = new ArrayList<Class>();
  
  static {
    DEFAULT_PARAM_NAME_LIST.add("DEBUG");
    DEFAULT_PARAM_NAME_LIST.add("INFO");
    DEFAULT_PARAM_NAME_LIST.add("WARN");
    DEFAULT_PARAM_NAME_LIST.add("ERROR");
    
    DEFAULT_PARAM_NAME_LIST.add("event");
    DEFAULT_PARAM_NAME_LIST.add("message");
    DEFAULT_PARAM_NAME_LIST.add("logger");
    DEFAULT_PARAM_NAME_LIST.add("level");
    DEFAULT_PARAM_NAME_LIST.add("timeStamp");
    DEFAULT_PARAM_NAME_LIST.add("marker");
    

    
    DEFAULT_PARAM_TYPE_LIST.add(int.class);
    DEFAULT_PARAM_TYPE_LIST.add(int.class);
    DEFAULT_PARAM_TYPE_LIST.add(int.class);
    DEFAULT_PARAM_TYPE_LIST.add(int.class);
    
    DEFAULT_PARAM_TYPE_LIST.add(LoggingEvent.class);
    DEFAULT_PARAM_TYPE_LIST.add(String.class);
    DEFAULT_PARAM_TYPE_LIST.add(LoggerRemoteView.class);
    DEFAULT_PARAM_TYPE_LIST.add(int.class);
    DEFAULT_PARAM_TYPE_LIST.add(long.class);
    DEFAULT_PARAM_TYPE_LIST.add(Marker.class);
  }
  
  protected String getDecoratedExpression() {
    return IMPORT_LEVEL + getExpression();
  }

  protected String[] getParameterNames() {
    List<String> fullNameList = new ArrayList<String>();
    fullNameList.addAll(DEFAULT_PARAM_NAME_LIST);

    for(int i = 0; i < matcherList.size(); i++) {
      Matcher m = (Matcher) matcherList.get(i);
      fullNameList.add(m.getName());
    }
    
    return (String[]) fullNameList.toArray(CoreGlobal.EMPTY_STRING_ARRAY);
  }

  protected Class[] getParameterTypes() {
    List<Class> fullTypeList = new ArrayList<Class>();
    fullTypeList.addAll(DEFAULT_PARAM_TYPE_LIST);
    for(int i = 0; i < matcherList.size(); i++) {
      fullTypeList.add(Matcher.class);
    }
    return (Class[]) fullTypeList.toArray(CoreGlobal.EMPTY_CLASS_ARRAY);
  }

  protected Object[] getParameterValues(Object event) {
    LoggingEvent loggingEvent = (LoggingEvent) event;
    final int matcherListSize = matcherList.size();
    
    int i = 0;
    Object[] values = new Object[DEFAULT_PARAM_NAME_LIST.size()+matcherListSize];

    values[i++] = Level.DEBUG_INTEGER;
    values[i++] = Level.INFO_INTEGER;
    values[i++] = Level.WARN_INTEGER;
    values[i++] = Level.ERROR_INTEGER;
    
    values[i++] = loggingEvent;
    values[i++] = loggingEvent.getMessage();    
    values[i++] = loggingEvent.getLoggerRemoteView();
    values[i++] = loggingEvent.getLevel().toInteger();
    values[i++] = new Long(loggingEvent.getTimeStamp());
    values[i++] = loggingEvent.getMarker();
    
    
    for(int j = 0; j < matcherListSize; j++) {
      values[i++] = (Matcher) matcherList.get(j);
    }
    
    return values;
  }

}
