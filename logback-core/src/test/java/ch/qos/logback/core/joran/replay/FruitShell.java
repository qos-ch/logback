package ch.qos.logback.core.joran.replay;

import ch.qos.logback.core.spi.ContextAwareBase;

public class FruitShell extends ContextAwareBase {

  FruitFactory fruitFactory;
  String name;
  
  public void setFruitFactory(FruitFactory fruitFactory) {
    this.fruitFactory = fruitFactory;
  }

  void testFruit() {
    
    Fruit fruit = fruitFactory.buildFruit();
    System.out.println(fruit);
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  /**
   * Constructs a <code>String</code> with all attributes
   * in name = value format.
   *
   * @return a <code>String</code> representation 
   * of this object.
   */
  public String toString()
  {
      final String TAB = " ";
      
      String retValue = "";
      
      retValue = "FruitShell ( "
          + "fruitFactory = " + this.fruitFactory + TAB
          + "name = " + this.name + TAB
          + " )";
      
      return retValue;
  }
  
}
