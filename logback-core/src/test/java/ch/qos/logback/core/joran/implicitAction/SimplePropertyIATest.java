package ch.qos.logback.core.joran.implicitAction;

import java.util.HashMap;
import java.util.List;

import junit.framework.TestCase;
import ch.qos.logback.core.joran.SimpleConfigurator;
import ch.qos.logback.core.joran.action.Action;
import ch.qos.logback.core.joran.spi.Pattern;
import ch.qos.logback.core.util.Constants;
import ch.qos.logback.core.util.StatusPrinter;

public class SimplePropertyIATest extends TestCase {

  FruitContext fruitContext = new FruitContext();
  
  public SimplePropertyIATest(String arg0) {
    super(arg0);
  }

  protected void setUp() throws Exception {
    super.setUp();
    fruitContext.setName("fruits");
  }

  protected void tearDown() throws Exception {
    super.tearDown();
  }

  public void test() throws Exception {
      try {
      HashMap<Pattern, Action> rulesMap = new HashMap<Pattern, Action>();
      rulesMap.put(new Pattern("/context/"), new FruitContextAction());
      SimpleConfigurator simpleConfigurator = new SimpleConfigurator(rulesMap);

      simpleConfigurator.setContext(fruitContext);

      simpleConfigurator.doConfigure(Constants.TEST_DIR_PREFIX + "input/joran/"
          + "simplePropertyIA1.xml");
      List<Fruit> fList = fruitContext.getFruitList();
      assertNotNull(fList);
      assertEquals(1, fList.size());
      
      Fruit f0 = fList.get(0);
      assertEquals("blue", f0.getName());
      assertEquals(2, f0.textList.size());
      assertEquals("hello", f0.textList.get(0));
      assertEquals("world", f0.textList.get(1));
    } catch (Exception je) {
      StatusPrinter.print(fruitContext);
      throw je;
    }
  }
  
  
}
