package ch.qos.logback.core.status;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.ContextBase;
import ch.qos.logback.core.CoreConstants;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Ceki G&uuml;c&uuml;
 */
public class StatusUtilTest {

  Context context = new ContextBase();
  StatusUtil statusUtil = new StatusUtil(context);

  @Test
  public void emptyStatusListShouldResultInNotFound() {
    assertEquals(-1, statusUtil.timeOfLastReset());
  }

  @Test
  public void withoutResetsStatusUtilShouldReturnNotFound() {
    context.getStatusManager().add(new InfoStatus("test", this));
    assertEquals(-1, statusUtil.timeOfLastReset());
  }

  @Test
  public void statusListShouldReturnLastResetTime() {
    context.getStatusManager().add(new InfoStatus("test", this));
    long resetTime = System.currentTimeMillis();
    context.getStatusManager().add(new InfoStatus(CoreConstants.RESET_MSG_PREFIX, this));
    context.getStatusManager().add(new InfoStatus("bla", this));
    assertTrue(resetTime <= statusUtil.timeOfLastReset());
  }


}
