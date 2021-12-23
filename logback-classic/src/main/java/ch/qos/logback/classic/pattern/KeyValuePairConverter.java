package ch.qos.logback.classic.pattern;

import java.util.List;

import org.slf4j.event.KeyValuePair;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.CoreConstants;

/**
 * Convert the contents of {@link KeyValuePair} list to a String.
 * 
 * Assuming the list contains the list {k1, v1}, {k2, v2}, the String output
 * will be "k1=v1 k2=v2", without the quotes.
 *
 * 
 * @since 1.3.0
 * @author Ceki G&uuml;lc&uuml;
 *
 */
public class KeyValuePairConverter extends ClassicConverter {

	static final String DOUBLE_OPTION_STR = "DOUBLE";
	static final String SINGLE_OPTION_STR = "SINGLE";
	static final String NONE_OPTION_STR = "NONE";

	enum ValueQuoteSpecification {
		NONE, SINGLE, DOUBLE;

		Character asChar() {
			switch (this) {
			case NONE:
				return null;
			case DOUBLE:
				return '"';
			case SINGLE:
				return '\'';
			default:
				throw new IllegalStateException();
			}
		}
	}

	ValueQuoteSpecification valueQuoteSpec = ValueQuoteSpecification.DOUBLE;

	public void start() {
		String optStr = getFirstOption();
		valueQuoteSpec = optionStrToSpec(optStr);
		super.start();
	}

	private ValueQuoteSpecification optionStrToSpec(String optStr) {
		if (optStr == null)
			return ValueQuoteSpecification.DOUBLE;
		if (DOUBLE_OPTION_STR.equalsIgnoreCase(optStr))
			return ValueQuoteSpecification.DOUBLE;
		if (SINGLE_OPTION_STR.equalsIgnoreCase(optStr))
			return ValueQuoteSpecification.SINGLE;
		if (NONE_OPTION_STR.equalsIgnoreCase(optStr))
			return ValueQuoteSpecification.NONE;
		return ValueQuoteSpecification.DOUBLE;
	}

	@Override
	public String convert(ILoggingEvent event) {

		List<KeyValuePair> kvpList = event.getKeyValuePairs();
		if (kvpList == null || kvpList.isEmpty()) {
			return CoreConstants.EMPTY_STRING;
		}

		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < kvpList.size(); i++) {
			KeyValuePair kvp = kvpList.get(i);
			if (i != 0)
				sb.append(' ');
			sb.append(String.valueOf(kvp.key));
			sb.append('=');
			Character c = valueQuoteSpec.asChar();
			if (c != null)
				sb.append(c);
			sb.append(String.valueOf(kvp.value));
			if (c != null)
				sb.append(c);
		}

		return sb.toString();
	}

}
