package ch.qos.logback.core.joran.replay;

import java.util.ArrayList;
import java.util.List;

public class Fruit {

  String name;
  List<String> textList = new ArrayList<String>();
  
  public Fruit() {
    // System.out.println("Fruit constructor called");
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public String toString() {
    final String TAB = "    ";

    StringBuilder retValue = new StringBuilder();

    retValue.append("xFruit ( ").append("name = ").append(this.name).append(TAB).append(" )");

    return retValue.toString();
  }
  
  public void addText(String s) {
    textList.add(s);
  }
  
}
