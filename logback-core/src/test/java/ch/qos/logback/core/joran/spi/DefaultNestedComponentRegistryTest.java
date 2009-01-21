package ch.qos.logback.core.joran.spi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class DefaultNestedComponentRegistryTest {

  DefaultNestedComponentRegistry registry = new DefaultNestedComponentRegistry();

  @Before
  public void setUp() throws Exception {

  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void smoke() {
    String propertyName = "window";
    registry.add(House.class, propertyName, Window.class);
    Class result = registry.findDefaultComponentType(House.class, propertyName);
    assertEquals(Window.class, result);
  }

  @Test
  public void absent() {
    registry.add(House.class, "a", Window.class);
    Class result = registry.findDefaultComponentType(House.class, "other");
    assertNull(result);
  }
}
