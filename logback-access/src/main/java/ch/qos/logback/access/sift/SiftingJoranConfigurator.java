package ch.qos.logback.access.sift;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import ch.qos.logback.access.spi.AccessEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.joran.action.ActionConst;
import ch.qos.logback.core.joran.action.AppenderAction;
import ch.qos.logback.core.joran.spi.Pattern;
import ch.qos.logback.core.joran.spi.RuleStore;
import ch.qos.logback.core.sift.SiftingJoranConfiguratorBase;

public class SiftingJoranConfigurator  extends SiftingJoranConfiguratorBase<AccessEvent> {

  String key;
  String value;
  
  SiftingJoranConfigurator(String key, String value) {
    this.key = key;
    this.value = value;
  }
  
  @Override
  protected Pattern initialPattern() {
    return new Pattern("configuration");
  }
  
  @Override
  protected void addInstanceRules(RuleStore rs) {
    rs.addRule(new Pattern("configuration/appender"), new AppenderAction());
  }

  @Override
  protected void buildInterpreter() {
    super.buildInterpreter();
    Map<String, Object> omap = interpreter.getInterpretationContext().getObjectMap();
    omap.put(ActionConst.APPENDER_BAG, new HashMap());
    omap.put(ActionConst.FILTER_CHAIN_BAG, new HashMap());
    Map<String, String> propertiesMap = new HashMap<String, String>();
    propertiesMap.put(key, value);
    interpreter.setInterpretationContextPropertiesMap(propertiesMap);
  }

  @SuppressWarnings("unchecked")
  public Appender<AccessEvent> getAppender() {
    Map<String, Object> omap = interpreter.getInterpretationContext().getObjectMap();
    HashMap map = (HashMap) omap.get(ActionConst.APPENDER_BAG);
    Collection values = map.values();
    return (Appender<AccessEvent>) values.iterator().next();
  }
}
