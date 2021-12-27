package ch.qos.logback.core.util;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

import javax.naming.Context;
import javax.naming.NamingException;

import ch.qos.logback.core.CoreConstants;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

public class JNDIUtilTest {

	private static final String ALLOWED_JNDI_NAME = "java:...";

	@Rule
	public MockitoRule mockitoRule = MockitoJUnit.rule();

	@Mock
	private Object mockObject;
	@Mock
	private Context mockContext;

	@Test
	public void shouldNotFailIfContextNull() throws NamingException {
		Object result = JNDIUtil.lookup(null, ALLOWED_JNDI_NAME);

		assertNull(result);
	}

	@Test
	public void shouldNotFailIfNameNull() throws NamingException {
		Object result = JNDIUtil.lookup(mockContext, null);

		assertNull(result);
	}

	@Test
	public void allowJavaNameSpace() throws NamingException {
		when(mockContext.lookup(ALLOWED_JNDI_NAME)).thenReturn(mockObject);
		when(mockObject.toString()).thenReturn("toStringResult");

		Object result = JNDIUtil.lookup(mockContext, ALLOWED_JNDI_NAME);

		assertEquals("toStringResult", result);
	}

	@Test
	public void ensureJavaNameSpace() throws NamingException {
		Context ctxt = JNDIUtil.getInitialContext();

		try {
			JNDIUtil.lookupObject(ctxt, "ldap:...");
		} catch (NamingException e) {
			String exceptionMsg = e.getMessage();
			if(exceptionMsg.startsWith(JNDIUtil.RESTRICTION_MSG))
				return;
			else {
				fail("unexpected exception " + e);
			}
		}

		fail("Should fail if JNDI namespace is not java");
	}

}