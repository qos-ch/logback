package ch.qos.logback.core.joran.ia;

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
      StatusPrinter.print(fruitContext);
      List<Fruit> fList = fruitContext.getFruitList();
      assertNotNull(fList);
      assertEquals(1, fList.size());
      
      Fruit f0 = fList.get(0);
      assertEquals("blue", f0.getName());
      assertEquals(1, f0.textList.size());
    } catch (Exception je) {
      StatusPrinter.print(fruitContext);
      throw je;
    }
  }
  
  
}
