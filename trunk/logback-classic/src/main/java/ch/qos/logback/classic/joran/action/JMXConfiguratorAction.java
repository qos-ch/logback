package ch.qos.logback.classic.joran.action;

import java.lang.management.ManagementFactory;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.xml.sax.Attributes;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.jmx.Configurator;
import ch.qos.logback.classic.jmx.ConfiguratorMBean;
import ch.qos.logback.core.joran.action.Action;
import ch.qos.logback.core.joran.spi.ActionException;
import ch.qos.logback.core.joran.spi.InterpretationContext;

public class JMXConfiguratorAction extends Action {

  static final String DOMAIN = "ch.qos.logback.classic";
 
  @Override
  public void begin(InterpretationContext ec, String name, Attributes attributes)
      throws ActionException {
    register();
  }

  @Override
  public void end(InterpretationContext ec, String name) throws ActionException {

  }

  public void register() {
    ConfiguratorMBean configuratorMBean = new Configurator((LoggerContext) context);

    MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
    try {
      ObjectName on = new ObjectName(DOMAIN + ":Name=" + context.getName()
          + ",Type=" + configuratorMBean.getClass().getName());

      // StandardMBean mbean = new StandardMBean(configuratorMBean,
      // ConfiguratorMBean.class);
      if (mbs.isRegistered(on)) {
        mbs.unregisterMBean(on);
      }
      mbs.registerMBean(configuratorMBean, on);
    } catch (Exception e) {
      addError("Failed to create mbean", e);
    }
  }

}
