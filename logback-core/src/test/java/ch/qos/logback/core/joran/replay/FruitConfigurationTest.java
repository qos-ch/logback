package ch.qos.logback.core.joran.replay;

import java.util.HashMap;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import ch.qos.logback.core.joran.SimpleConfigurator;
import ch.qos.logback.core.joran.action.Action;
import ch.qos.logback.core.joran.action.NOPAction;
import ch.qos.logback.core.joran.spi.Pattern;
import ch.qos.logback.core.util.Constants;
import ch.qos.logback.core.util.StatusPrinter;

public class FruitConfigurationTest extends TestCase {

  FruitContext fruitContext = new FruitContext();

  public FruitConfigurationTest(String arg0) {
    super(arg0);
  }

  protected void setUp() throws Exception {
    super.setUp();
  }

  protected void tearDown() throws Exception {
    super.tearDown();
  }

  public List<FruitShell> doFirstPart(String filename) throws Exception {

    try {
      HashMap<Pattern, Action> rulesMap = new HashMap<Pattern, Action>();
      rulesMap.put(new Pattern("group/fruitShell"), new FruitShellAction());
      rulesMap.put(new Pattern("group/fruitShell/fruit"),
          new FruitFactoryAction());
      rulesMap.put(new Pattern("group/fruitShell/fruit/*"), new NOPAction());
      SimpleConfigurator simpleConfigurator = new SimpleConfigurator(rulesMap);

      simpleConfigurator.setContext(fruitContext);

      simpleConfigurator.doConfigure(Constants.TEST_DIR_PREFIX + "input/joran/"
          + filename);

      return fruitContext.getFruitShellList();
    } catch (Exception je) {
      StatusPrinter.print(fruitContext);
      throw je;
    }
  }

  public void test1() throws Exception {
    List<FruitShell> fsList = doFirstPart("fruit1.xml");
    assertNotNull(fsList);
    assertEquals(1, fsList.size());

    FruitShell fs0 = fsList.get(0);
    assertNotNull(fs0);
    assertEquals("fs0", fs0.getName());
    Fruit fruit0 = fs0.fruitFactory.buildFruit();
    assertTrue(fruit0 instanceof Fruit);
    assertEquals("blue", fruit0.getName());
  }

  public void test2() throws Exception {
    List<FruitShell> fsList = doFirstPart("fruit2.xml");
    assertNotNull(fsList);
    assertEquals(2, fsList.size());

    FruitShell fs0 = fsList.get(0);
    assertNotNull(fs0);
    assertEquals("fs0", fs0.getName());
    Fruit fruit0 = fs0.fruitFactory.buildFruit();
    assertTrue(fruit0 instanceof Fruit);
    assertEquals("blue", fruit0.getName());

    FruitShell fs1 = fsList.get(1);
    assertNotNull(fs1);
    assertEquals("fs1", fs1.getName());
    Fruit fruit1 = fs1.fruitFactory.buildFruit();
    assertTrue(fruit1 instanceof WeightytFruit);
    assertEquals("orange", fruit1.getName());
    assertEquals(1.2, ((WeightytFruit) fruit1).getWeight());
  }

  public void testWithSubst() throws Exception {
    List<FruitShell> fsList = doFirstPart("fruitWithSubst.xml");
    assertNotNull(fsList);
    assertEquals(1, fsList.size());

    FruitShell fs0 = fsList.get(0);
    assertNotNull(fs0);
    assertEquals("fs0", fs0.getName());
    int oldCount = FruitFactory.count;
    Fruit fruit0 = fs0.fruitFactory.buildFruit();
    assertTrue(fruit0 instanceof WeightytFruit);
    assertEquals("orange-" + oldCount, fruit0.getName());
    assertEquals(1.2, ((WeightytFruit) fruit0).getWeight());
  }

  public static Test suite() {
    TestSuite suite = new TestSuite();
    // suite.addTest(new FruitConfigurationTest("testWithSubst"));
    suite.addTestSuite(FruitConfigurationTest.class);
    return suite;
  }
}
