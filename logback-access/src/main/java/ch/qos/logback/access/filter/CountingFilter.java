 package ch.qos.logback.access.filter;

import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.StandardMBean;
import java.lang.management.ManagementFactory;

public class CountingFilter extends Filter {

  long total = 0;
  final StatisticalViewImpl accessStatsImpl;
  
  String domain = "ch.qos.logback.access";
  
  public CountingFilter() {
    accessStatsImpl = new StatisticalViewImpl(this);
  }
  
  @Override
  public FilterReply decide(Object event) {
    total++;
    accessStatsImpl.update();
    return FilterReply.NEUTRAL;
  }

  public long getTotal() {
    return total;
  }
  
  
  @Override
  public void start() {
    MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
    try {
      ObjectName on = new ObjectName(domain+":Name="+getName());
      StandardMBean mbean = new StandardMBean(accessStatsImpl, StatisticalView.class);
      if (mbs.isRegistered(on)) {
          mbs.unregisterMBean(on);
      }
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

  public String getDomain() {
    return domain;
  }

  public void setDomain(String domain) {
    this.domain = domain;
  }
  
}
