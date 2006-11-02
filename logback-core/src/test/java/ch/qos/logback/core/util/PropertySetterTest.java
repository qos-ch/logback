package ch.qos.logback.core.util;

import java.util.ArrayList;
import java.util.List;

import ch.qos.logback.core.util.PropertySetter;
import junit.framework.TestCase;

public class PropertySetterTest extends TestCase {

  public void testCanContainComponent() {
    House house = new House();
    PropertySetter setter = new PropertySetter(house);
    assertEquals(ContainmentType.AS_SINGLE_COMPONENT, setter.canContainComponent("door"));
    
    assertEquals(ContainmentType.AS_SINGLE_PROPERTY, setter.canContainComponent("count"));
    assertEquals(ContainmentType.AS_SINGLE_PROPERTY, setter.canContainComponent("Count"));
    
    assertEquals(ContainmentType.AS_SINGLE_PROPERTY, setter.canContainComponent("name"));
    assertEquals(ContainmentType.AS_SINGLE_PROPERTY, setter.canContainComponent("Name"));
    
    assertEquals(ContainmentType.AS_SINGLE_PROPERTY, setter.canContainComponent("open"));
    assertEquals(ContainmentType.AS_SINGLE_PROPERTY, setter.canContainComponent("Open"));
    
    assertEquals(ContainmentType.AS_COMPONENT_COLLECTION, setter.canContainComponent("Window"));
    assertEquals(ContainmentType.AS_PROPERTY_COLLECTION, setter.canContainComponent("adjective"));
    
  }

  public void testSetProperty() {
    {
      House house = new House();
      PropertySetter setter = new PropertySetter(house);
      setter.setProperty("count", "10");
      setter.setProperty("name", "jack");
      setter.setProperty("open", "true");

      assertEquals(10, house.getCount());
      assertEquals("jack", house.getName());
      assertTrue(house.isOpen());
    }
    
    {
      House house = new House();
      PropertySetter setter = new PropertySetter(house);
      setter.setProperty("Count", "10");
      setter.setProperty("Name", "jack");
      setter.setProperty("Open", "true");

      assertEquals(10, house.getCount());
      assertEquals("jack", house.getName());
      assertTrue(house.isOpen());
    }
  }

  public void testSetCamelProperty() {
    House house = new House();
    PropertySetter setter = new PropertySetter(house);
    
    setter.setProperty("camelCase", "trot");
    assertEquals("trot", house.getCamelCase());
    
    setter.setProperty("camelCase", "gh");
    assertEquals("gh", house.getCamelCase());
  }
  
  public void testSetComponent() {
    House house = new House();
    Door door = new Door();
    PropertySetter setter = new PropertySetter(house);
    setter.setComponent("door", door);
    assertEquals(door, house.getDoor());
  }

  public void testPropertyCollection() {
    House house = new House();
    PropertySetter setter = new PropertySetter(house);
    setter.addProperty("adjective", "nice");
    setter.addProperty("adjective", "big");
    assertEquals(2, house.adjectiveList.size());
    assertEquals("nice", house.adjectiveList.get(0));
    assertEquals("big", house.adjectiveList.get(1));
  }

  public void testComponentCollection() {
    House house = new House();
    PropertySetter setter = new PropertySetter(house);
    Window w1 = new Window();
    w1.handle=10;
    Window w2 = new Window();
    w2.handle=20;
    
    setter.addComponent("window", w1);
    setter.addComponent("window", w2);
    assertEquals(2, house.windowList.size());
    assertEquals(10, house.windowList.get(0).handle);
    assertEquals(20, house.windowList.get(1).handle);
  }
  
  public void testSetComponentWithCamelCaseName() {
    House house = new House();
    SwimmingPool pool = new SwimmingPool();
    PropertySetter setter = new PropertySetter(house);
    setter.setComponent("swimmingPool", pool);
    assertEquals(pool, house.getSwimmingPool());
  }

}

class House {
  Door mainDoor;
  int count;
  boolean open;
  String name;
  String camelCase;
  SwimmingPool pool;
  
  List<String> adjectiveList = new ArrayList<String>();
  List<Window> windowList = new ArrayList<Window>();
  
  public String getCamelCase() {
    return camelCase;
  }

  public void setCamelCase(String camelCase) {
    this.camelCase = camelCase;
  }

  public int getCount() {
    return count;
  }

  public void setCount(int c) {
    this.count = c;
  }

  public Door getDoor() {
    return mainDoor;
  }

  public void setDoor(Door door) {
    this.mainDoor = door;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public boolean isOpen() {
    return open;
  }

  public void setOpen(boolean open) {
    this.open = open;
  }
  
  public void setSwimmingPool(SwimmingPool pool) {
    this.pool = pool;
  }
  
  public SwimmingPool getSwimmingPool() {
    return pool;
  }
  
  public void addWindow(Window w) {
    windowList.add(w);
  }
  

  public void addAdjective(String s) {
    adjectiveList.add(s);
  }
}

class Door {
  int handle;
}

class Window {
  int handle;
}

class SwimmingPool {
  int length;
  int width;
  int depth;
}
