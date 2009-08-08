package ch.qos.logback.core.joran.action;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.xml.sax.Attributes;

import ch.qos.logback.core.joran.spi.ActionException;
import ch.qos.logback.core.joran.spi.InterpretationContext;
import ch.qos.logback.core.util.OptionHelper;

public class TimestampAction extends Action {
  static String DATE_PATTERN_ATTRIBUTE = "datePattern";
  
  boolean inError = false;
  
  @Override
  public void begin(InterpretationContext ec, String name, Attributes attributes) throws ActionException {
    String nameStr = attributes.getValue(NAME_ATTRIBUTE);
    if(OptionHelper.isEmpty(nameStr)) {
      addError("["+NAME_ATTRIBUTE+"] attribute cannot be empty");
      inError = true;
    }
    String datePatternStr = attributes.getValue(DATE_PATTERN_ATTRIBUTE);
    if(OptionHelper.isEmpty(datePatternStr)) {
      addError("["+DATE_PATTERN_ATTRIBUTE+"] attribute cannot be empty");
      inError = true;
    }
    
    if(inError)
      return;
    
    SimpleDateFormat sdf = new SimpleDateFormat(datePatternStr);
    String val = sdf.format(new Date());

    addInfo("Adding property ["+nameStr+"] with value ["+val+"] to the context");
    context.putProperty(nameStr, val);
  }

  @Override
  public void end(InterpretationContext ec, String name) throws ActionException {
  }

}
