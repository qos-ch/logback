package ch.qos.logback.core.joran.event.stax;

import javax.xml.stream.Location;

public class BodyEvent extends StaxEvent {


  private String text;

  BodyEvent(String text, Location location) {
    super(null, location);
    this.text = text;
  }

  public String getText() {
    return text;
  }

  void append(String txt) {
    text += txt;
  }

  @Override
  public String toString() {
    return "BodyEvent(" + getText() + ")" + location.getLineNumber() + ","
            + location.getColumnNumber();
  }
}
