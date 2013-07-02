package ch.qos.logback.core.joran.event.stax;

import javax.xml.stream.Location;

/**
 * Created with IntelliJ IDEA.
 * User: ceki
 * Date: 7/2/13
 * Time: 4:07 PM
 * To change this template use File | Settings | File Templates.
 */
public class EndEvent extends StaxEvent {

  public EndEvent(String name, Location location) {
    super(name, location);
  }

  @Override
  public String toString() {
    return "EndEvent("+getName()+")  ["+location.getLineNumber()+","+location.getColumnNumber()+"]";
  }


}
