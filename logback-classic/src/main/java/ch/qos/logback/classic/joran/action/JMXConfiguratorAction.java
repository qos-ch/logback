package ch.qos.logback.classic.joran.action;

import javax.management.ObjectName;

import org.xml.sax.Attributes;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.jmx.JMXConfigurator;
import ch.qos.logback.classic.jmx.MBeanUtil;
import ch.qos.logback.core.joran.action.Action;
import ch.qos.logback.core.joran.spi.ActionException;
import ch.qos.logback.core.joran.spi.InterpretationContext;
import ch.qos.logback.core.util.OptionHelper;

public class JMXConfiguratorAction extends Action {

  static final String OBJECT_NAME_ATTRIBUTE_NAME = "objectName";
  static final String SUFFIX_ATTRIBUTE_NAME = "suffix";
  static final char JMX_NAME_SEPARATOR = ',';
  
  @Override
  public void begin(InterpretationContext ec, String name, Attributes attributes)
      throws ActionException {
    addInfo("begin");

    String objectNameAsStr;
    String objectNameAttributeVal = attributes
        .getValue(OBJECT_NAME_ATTRIBUTE_NAME);
    String suffixAttributeVal = attributes
    .getValue(SUFFIX_ATTRIBUTE_NAME);

    if (OptionHelper.isEmpty(objectNameAttributeVal)) {
      objectNameAsStr = MBeanUtil.getObjectNameFor((LoggerContext) context,
          JMXConfigurator.class);
    } else {
      objectNameAsStr = objectNameAttributeVal;
    }

    if(!OptionHelper.isEmpty(suffixAttributeVal)) {
      if(suffixAttributeVal.indexOf(0) != JMX_NAME_SEPARATOR) {
        objectNameAsStr += JMX_NAME_SEPARATOR;
      }
      objectNameAsStr += suffixAttributeVal;
    }
    
    ObjectName objectName = MBeanUtil.string2ObjectName(context, this,
        objectNameAsStr);

    if (objectName != null) {
      MBeanUtil.register((LoggerContext) context, objectName, this);
    }

  }

  @Override
  public void end(InterpretationContext ec, String name) throws ActionException {

  }

}
