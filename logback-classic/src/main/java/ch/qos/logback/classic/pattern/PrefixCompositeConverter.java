package ch.qos.logback.classic.pattern;

import static ch.qos.logback.core.util.OptionHelper.extractDefaultReplacement;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.pattern.CompositeConverter;
import ch.qos.logback.core.pattern.Converter;

public class PrefixCompositeConverter extends CompositeConverter<ILoggingEvent> {

    public String convert(ILoggingEvent event) {
        StringBuilder buf = new StringBuilder();
        Converter<ILoggingEvent> childConverter = this.getChildConverter();

        for (Converter<ILoggingEvent> c = childConverter; c != null; c = c.getNext()) {
            if (c instanceof MDCConverter) {
                MDCConverter mdcConverter = (MDCConverter) c;

                String firstOption = mdcConverter.getFirstOption();
                String[] keyInfo = extractDefaultReplacement(firstOption);
                String key = keyInfo[0];
                if (key != null) {
                    buf.append(key).append("=");
                }
            } else {
                
            }
            c.write(buf, event);
        }
        String intermediary = buf.toString();

        return null;
    }

    protected String transform(ILoggingEvent event, String in) {
        throw new UnsupportedOperationException();
    }

}
