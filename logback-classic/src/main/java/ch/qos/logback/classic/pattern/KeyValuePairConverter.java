package ch.qos.logback.classic.pattern;

import java.util.List;

import org.slf4j.event.KeyValuePair;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.CoreConstants;

/**
 * Convert the contents of {@link KeyValuePair} list to a String.
 * 
 * Assuming the list contains the list {k1, v1}, {k2, v2}, the String
 *  output will be "k1=v1 k2=v2", without the quotes.
 *
 * 
 * @since 1.3.0
 * @author Ceki G&uuml;lc&uuml;
 *
 */
public class KeyValuePairConverter extends ClassicConverter {

	@Override
	public String convert(ILoggingEvent event) {
		
		List<KeyValuePair> kvpList = event.getKeyValuePairs();
		if(kvpList == null || kvpList.isEmpty()) {
			return CoreConstants.EMPTY_STRING;
		}
		
		StringBuilder sb = new StringBuilder();
		 for (int i = 0; i < kvpList.size(); i++) {
			 KeyValuePair kvp = kvpList.get(i);
			 if(i != 0)
				 sb.append(' ');
             sb.append(kvp.key);
             sb.append('=');
             sb.append(kvp.value);
         }
		 
		return sb.toString();
	}

}
