package ch.qos.logback.core.joran.replay;

import java.util.List;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.ContextBase;
import ch.qos.logback.core.joran.event.SaxEvent;
import ch.qos.logback.core.joran.spi.JoranException;

public class FruitFactory {

  List<SaxEvent> eventList;
  Fruit fruit;
  
  public void setFruit(Fruit fruit) {
    this.fruit = fruit;
  }

  public Fruit buildFruit() {
    for (SaxEvent se : eventList) {
      System.out.println(se);
    }
    Context context = new ContextBase();
    this.fruit = null;
    FruitConfigurator fruitConfigurator = new FruitConfigurator(this);
    fruitConfigurator.setContext(context);
    try {
      fruitConfigurator.doConfigure(eventList);
    } catch(JoranException je) {
      je.printStackTrace();
    }
    return fruit;
  }

  public String toString() {
    final String TAB = " ";

    StringBuilder retValue = new StringBuilder();

    retValue.append("FruitFactory ( ");
    if (eventList != null && eventList.size() > 0) {
      retValue.append("event1 = ").append(eventList.get(0)).append(TAB);
    }
    retValue.append(" )");

    return retValue.toString();
  }

}
