package ch.qos.logback.core.joran.conditional;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.codehaus.janino.ClassBodyEvaluator;
import org.codehaus.janino.CompileException;
import org.codehaus.janino.Parser.ParseException;
import org.codehaus.janino.Scanner.ScanException;
import org.junit.Before;

import ch.qos.logback.core.spi.ContextAwareBase;

public class PropertyEvalScriptBuilder extends ContextAwareBase {

  private static String SCRIPT_PREFIX = ""
      + "public boolean evaluate() { return ";
  private static String SCRIPT_SUFFIX = "" + "; }";

  static String SCRIPT = ""
      + "public boolean eval() { return p(\"Ka\").equals(\"Va\"); }";

  Map<String, String> map = new HashMap<String, String>();

  @Before
  public Condition build(String script) throws IllegalAccessException,
      CompileException, ParseException, ScanException, InstantiationException,
      SecurityException, NoSuchMethodException, IllegalArgumentException,
      InvocationTargetException {

    ClassBodyEvaluator cbe = new ClassBodyEvaluator();
    cbe.setImplementedTypes(new Class[] { Condition.class });
    cbe.setExtendedType(MapWrapperForScripts.class);
    cbe.cook(SCRIPT_PREFIX + script + SCRIPT_SUFFIX);

    Class<?> clazz = cbe.getClazz();
    Condition instance = (Condition) clazz.newInstance();
    Method setMapMethod = clazz.getMethod("setMap", Map.class);
    setMapMethod.invoke(instance, context.getCopyOfPropertyMap());

    Method setNameMethod = clazz.getMethod("setName", String.class);
    setNameMethod.invoke(instance, context.getName());

    return instance;
  }

}
