package ch.qos.logback.core.net;

import java.io.IOException;
import java.io.InputStream;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * HardenedObjectInputStream restricts the set of classes that can be deserialized to a set of
 * explicitly whitelisted classes. This prevents certain type of attacks from being successful.
 *
 * <p>It is assumed that classes in the "java.lang" and  "java.util" packages are
 * always authorized.</p>
 *
 * @author Ceki G&uuml;lc&uuml;
 * @since 1.2.0
 */
public class HardenedObjectInputStream extends ObjectInputStream {

	final List<String> whitelistedClassNames;
	final static String[] JAVA_PACKAGES = { "java.lang", "java.util" };

	public HardenedObjectInputStream(final InputStream in, final String[] whilelist) throws IOException {
		super(in);

		whitelistedClassNames = new ArrayList<>();
		if (whilelist != null) {
			Collections.addAll(whitelistedClassNames, whilelist);
		}
	}

	public HardenedObjectInputStream(final InputStream in, final List<String> whitelist) throws IOException {
		super(in);

		whitelistedClassNames = new ArrayList<>();
		whitelistedClassNames.addAll(whitelist);
	}

	@Override
	protected Class<?> resolveClass(final ObjectStreamClass anObjectStreamClass) throws IOException, ClassNotFoundException {

		final String incomingClassName = anObjectStreamClass.getName();

		if (!isWhitelisted(incomingClassName)) {
			throw new InvalidClassException("Unauthorized deserialization attempt", anObjectStreamClass.getName());
		}

		return super.resolveClass(anObjectStreamClass);
	}

	private boolean isWhitelisted(final String incomingClassName) {
		for (final String element : JAVA_PACKAGES) {
			if (incomingClassName.startsWith(element)) {
				return true;
			}
		}
		for (final String whiteListed : whitelistedClassNames) {
			if (incomingClassName.equals(whiteListed)) {
				return true;
			}
		}
		return false;
	}

	protected void addToWhitelist(final List<String> additionalAuthorizedClasses) {
		whitelistedClassNames.addAll(additionalAuthorizedClasses);
	}
}
