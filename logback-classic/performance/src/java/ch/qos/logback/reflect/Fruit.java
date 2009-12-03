package ch.qos.logback.reflect;

public class Fruit {

  String color;
  boolean sweet;
  
  Fruit(  String color, boolean sweet) {
    this.color = color;
    this.sweet = sweet;
  }
  
  public String getColor() {
    return color;
  }
  public void setColor(String color) {
    this.color = color;
  }
  public boolean isSweet() {
    return sweet;
  }
  public void setSweet(boolean sweet) {
    this.sweet = sweet;
  }
  

}
