package ch.qos.logback.core.joran.ia;

import java.util.ArrayList;
import java.util.List;

public class Fruit {

  String name;
  List<String> textList = new ArrayList<String>();
  
  public Fruit() {
    System.out.println("Fruit constructor called");
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }
  
  public void addText(String s) {
    textList.add(s);
  }
  
}
