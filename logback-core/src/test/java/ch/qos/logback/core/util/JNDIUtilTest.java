package ch.qos.logback.core.util;

import static org.junit.Assert.*;

import javax.naming.Context;
import javax.naming.NamingException;

import org.junit.Test;

public class JNDIUtilTest {

	@Test
	public void ensureJavaNameSpace() throws NamingException {
		Context ctxt = JNDIUtil.getInitialContext();

		try {
			JNDIUtil.lookup(ctxt, "ldap:...");
		} catch (NamingException e) {
			String excaptionMsg = e.getMessage();
			if(excaptionMsg.startsWith(JNDIUtil.RESTRICTION_MSG)) 
				return;
			else {
				fail("unexpected exception " + e);
			}
		}

		fail("Should aNot yet implemented");
	}

}