package ch.qos.logback.core.joran.ia;

import java.util.ArrayList;
import java.util.List;

import ch.qos.logback.core.ContextBase;

public class FruitContext extends ContextBase {

  List<Fruit> fruitList = new ArrayList<Fruit>();
  
  public void addFruit(Fruit fs) {
    fruitList.add(fs);
  }

  public List<Fruit> getFruitList() {
    return fruitList;
  }

  public void setFruitShellList(List<Fruit> fruitList) {
    this.fruitList = fruitList;
  }
}
