package ch.qos.logback.core.joran.ia;

import java.util.ArrayList;
import java.util.List;

public class Fruit {

  String name;
  List<String> textList = new ArrayList<String>();
  
  public Fruit() {
  }

  public void setName(String n) {
    this.name = n;
  }

  public String getName() {
    return name;
  }
  
  public void addText(String s) {
    textList.add(s);
  }
  
}
