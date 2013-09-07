package ch.qos.logback.core.joran.event.stax;


import javax.xml.stream.Location;

public class StaxEvent {

  final String name;
  final Location location;

  StaxEvent(String name, Location location) {
    this.name = name;
    this.location = location;

  }

  public String getName() {
    return name;
  }

  public Location getLocation() {
    return location;
  }

}
