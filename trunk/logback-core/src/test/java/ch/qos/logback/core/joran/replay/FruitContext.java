package ch.qos.logback.core.joran.replay;

import java.util.ArrayList;
import java.util.List;

import ch.qos.logback.core.ContextBase;

public class FruitContext extends ContextBase {

  List<FruitShell> fruitShellList = new ArrayList<FruitShell>();
  
  public void addFruitShell(FruitShell fs) {
    fruitShellList.add(fs);
  }

  public List<FruitShell> getFruitShellList() {
    return fruitShellList;
  }

  public void setFruitShellList(List<FruitShell> fruitShellList) {
    this.fruitShellList = fruitShellList;
  }
}
