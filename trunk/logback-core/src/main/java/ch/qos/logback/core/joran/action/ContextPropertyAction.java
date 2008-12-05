package ch.qos.logback.core.joran.action;

import org.xml.sax.Attributes;

import ch.qos.logback.core.joran.spi.ActionException;
import ch.qos.logback.core.joran.spi.InterpretationContext;

/**
 * @author Ceki Gulcu
 */
public class ContextPropertyAction extends Action {

  @Override
  public void begin(InterpretationContext ec, String name, Attributes attributes)
      throws ActionException {
    addError("The [contextProperty] element has been removed. Please use [substitutionProperty] element instead");
  }

  @Override
  public void end(InterpretationContext ec, String name) throws ActionException {
  }

}
