package ch.qos.logback.core.joran.event.stax;

import ch.qos.logback.core.joran.spi.ElementPath;

import javax.xml.stream.Location;
import javax.xml.stream.events.Attribute;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class StartEvent extends StaxEvent {

  List<Attribute> attributes;
  public ElementPath elementPath;

  StartEvent(ElementPath elementPath, String name, Iterator<Attribute> attributeIterator, Location location) {
    super(name, location);
    populateAttributes(attributeIterator);
    this.elementPath = elementPath;
  }

  private void populateAttributes(Iterator<Attribute> attributeIterator) {
    while (attributeIterator.hasNext()) {
      if (attributes == null) {
        attributes = new ArrayList<Attribute>(2);
      }
      attributes.add(attributeIterator.next());
    }
  }

  public ElementPath getElementPath() {
    return elementPath;
  }

  public List<Attribute> getAttributeList() {
    return attributes;
  }

  Attribute getAttributeByName(String name) {
    if(attributes == null)
      return null;

    for(Attribute attr: attributes) {
      if(name.equals(attr.getName().getLocalPart()))
        return attr;
    }
    return null;
  }

  @Override
  public String toString() {
    return "StartEvent(" + getName() + ")  [" + location.getLineNumber() + "," + location.getColumnNumber() + "]";
  }
}
