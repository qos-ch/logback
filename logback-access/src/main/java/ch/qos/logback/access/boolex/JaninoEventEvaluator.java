package ch.qos.logback.access.boolex;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ch.qos.logback.access.spi.AccessEvent;
import ch.qos.logback.core.CoreGlobal;
import ch.qos.logback.core.boolex.JaninoEventEvaluatorBase;
import ch.qos.logback.core.boolex.Matcher;



public class JaninoEventEvaluator extends JaninoEventEvaluatorBase {

  public final static List<String> DEFAULT_PARAM_NAME_LIST = new ArrayList<String>();
  public final static List<Class> DEFAULT_PARAM_TYPE_LIST = new ArrayList<Class>();
  
  static {
    DEFAULT_PARAM_NAME_LIST.add("event");
    
    DEFAULT_PARAM_TYPE_LIST.add(AccessEvent.class);
  }
  
  
  public JaninoEventEvaluator() {
    
  }
  
  protected String getDecoratedExpression() {
    return getExpression();
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
    AccessEvent loggingEvent = (AccessEvent) event;
    final int matcherListSize = matcherList.size();
    
    int i = 0;
    Object[] values = new Object[DEFAULT_PARAM_NAME_LIST.size()+matcherListSize];

    values[i++] = loggingEvent;
    
    for(int j = 0; j < matcherListSize; j++) {
      values[i++] = (Matcher) matcherList.get(j);
    }
    
    return values;
  }

}
