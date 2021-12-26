package ch.qos.logback.core.util;

import static org.junit.Assert.*;

import javax.naming.Context;
import javax.naming.NamingException;

import org.junit.Before;
import org.junit.Test;

public class JNDIUtilTest {

	Context ctxt;

	@Before
	public void setup() {
		try {
			ctxt = JNDIUtil.getInitialContext();
		} catch (NamingException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void ensureJavaNameSpace() throws NamingException {

		try {
			JNDIUtil.lookupString(ctxt, "ldap:...");
		} catch (NamingException e) {
			String excaptionMsg = e.getMessage();
			if (excaptionMsg.startsWith(JNDIUtil.RESTRICTION_MSG))
				return;
			else {
				fail("unexpected exception " + e);
			}
		}

		fail("Should aNot yet implemented");
	}

	@Test
	public void testToStringCast() throws NamingException {
		String x = JNDIUtil.lookupString(ctxt, "java:comp:/inexistent");
		assertNull(x);
	}

	public String castToString(Object input) {
		String a = (String) input;
		return a;
	}

}
