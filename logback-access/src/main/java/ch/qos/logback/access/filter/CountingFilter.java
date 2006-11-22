 package ch.qos.logback.access.filter;

import java.lang.management.ManagementFactory;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.StandardMBean;

import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;

public class CountingFilter extends Filter {

  long total = 0;
  final StatisticalViewImpl accessStatsImpl;
  
  CountingFilter() {
    accessStatsImpl = new StatisticalViewImpl(this);
  }
  
  @Override
  public FilterReply decide(Object event) {
    total++;
    return FilterReply.NEUTRAL;
  }

  public long getTotal() {
    return total;
  }
  
  
  @Override
  public void start() {
    MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
    try {
      ObjectName on = new ObjectName("totp:Filter=1");
      StandardMBean mbean = new StandardMBean(accessStatsImpl, StatisticalView.class);
      mbs.registerMBean(mbean, on);
      super.start();
    } catch (Exception e) {
      addError("Failed to create mbean", e);
    }
  }
  
  @Override
  public void stop() {
    super.stop();
    try {
      MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
      ObjectName on = new ObjectName("totp:Filter=1");
      mbs.unregisterMBean(on);
    } catch(Exception e) {
      addError("Failed to unregister mbean", e);
    }
  }
  
}
