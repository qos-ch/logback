package ch.qos.logback.core.util;

import ch.qos.logback.core.util.PropertySetter;
import junit.framework.TestCase;

public class PropertySetterTest extends TestCase {

  public void testCanContainComponent() {
    House house = new House();
    PropertySetter setter = new PropertySetter(house);
    assertEquals(PropertySetter.AS_COMPONENT, setter.canContainComponent("door"));
    
    assertEquals(PropertySetter.AS_PROPERTY, setter.canContainComponent("count"));
    assertEquals(PropertySetter.AS_PROPERTY, setter.canContainComponent("Count"));
    
    assertEquals(PropertySetter.AS_PROPERTY, setter.canContainComponent("name"));
    assertEquals(PropertySetter.AS_PROPERTY, setter.canContainComponent("Name"));
    
    assertEquals(PropertySetter.AS_PROPERTY, setter.canContainComponent("open"));
    assertEquals(PropertySetter.AS_PROPERTY, setter.canContainComponent("Open"));
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

  public void testSetComponent() {
    House house = new House();
    Door door = new Door();
    PropertySetter setter = new PropertySetter(house);
    setter.setComponent("door", door);
    assertEquals(door, house.getDoor());
  }

}

class House {
  Door door;
  int count;
  boolean open;
  String name;

  public int getCount() {
    return count;
  }

  public void setCount(int c) {
    this.count = c;
  }

  public Door getDoor() {
    return door;
  }

  public void setDoor(Door door) {
    this.door = door;
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
}

class Door {
  int handle;
}
