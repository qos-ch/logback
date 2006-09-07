package ch.qos.logback.core.rolling;

import junit.framework.TestCase;
import ch.qos.logback.core.Context;
import ch.qos.logback.core.ContextBase;

public class SizeBasedTriggeringPolicyTest extends TestCase {

	public void testStringToLong() {
		Context context = new ContextBase();
		SizeBasedTriggeringPolicy policy = new SizeBasedTriggeringPolicy();
		policy.setContext(context);

		Long result;

		{
			result = policy.toFileSize("123");
			assertEquals(new Long("123"), result);
		}
		{
			result = policy.toFileSize("123KB");
			// = 123 * 1024
			assertEquals(new Long("125952"), result);
		}
		{
			result = policy.toFileSize("123MB");
			// = 123 * 1024 * 1024
			assertEquals(new Long("128974848"), result);
		}
		{
			result = policy.toFileSize("123GB");
			// = 123 * 1024 * 1024 * 1024
			assertEquals(new Long("132070244352"), result);
		}

		{
			result = policy.toFileSize("123xxxx");
			// = 123 * 1024 * 1024 * 1024
			assertEquals(new Long(SizeBasedTriggeringPolicy.DEFAULT_MAX_FILE_SIZE), result);
			assertEquals(2, context.getStatusManager().getCount());
		}

	}
}
