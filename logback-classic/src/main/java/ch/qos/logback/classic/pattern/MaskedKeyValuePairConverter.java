/*
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2024, QOS.ch. All rights reserved.
 *
 * This program and the accompanying materials are dual-licensed under
 * either the terms of the Eclipse Public License v1.0 as published by
 * the Eclipse Foundation
 *
 *   or (per the licensee's choosing)
 *
 * under the terms of the GNU Lesser General Public License version 2.1
 * as published by the Free Software Foundation.
 */

package ch.qos.logback.classic.pattern;


import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.CoreConstants;
import org.slf4j.event.KeyValuePair;

import java.util.ArrayList;
import java.util.List;

import static ch.qos.logback.classic.pattern.KeyValuePairConverter.*;

/**
 * Similar to  {@link KeyValuePairConverter} with the added ability to mask the values of specified keys.
 * <p>
 * Assuming the specified key is k2, and the kvp list of an event contains {k1, v1}, {k2, v2}, the String output
 * will be "k1=v1 k2=XXX", without the quotes.
 *
 * Value quotes can be specified as the first option, e.g %maskedKvp{SINGLE, k1}
 *
 * @author Ceki G&uuml;lc&uuml;
 * @since 1.5.7
 */


public class MaskedKeyValuePairConverter extends ClassicConverter {
    public static final String MASK = "XXX";
    List<String> optionList;
    List<String> maskList = new ArrayList<>();
    KeyValuePairConverter.ValueQuoteSpecification valueQuoteSpec = KeyValuePairConverter.ValueQuoteSpecification.DOUBLE;

    public void start() {
        this.optionList = getOptionList();
        KeyValuePairConverter.ValueQuoteSpecification extractedSpec = extractSpec(this.optionList);
        if (extractedSpec == null) {
            maskList = optionList;
        } else {
            valueQuoteSpec = extractedSpec;
            maskList = optionList.subList(1, optionList.size());
        }

        checkMaskListForExtraQuoteSpecs(maskList);

        super.start();
    }

    private void checkMaskListForExtraQuoteSpecs(List<String> maskList) {
        if(maskList == null || maskList.isEmpty())
            return;
        if(maskList.contains(DOUBLE_OPTION_STR)) {
            addWarn("quote spec "+DOUBLE_OPTION_STR+ " found in the wrong order");
        }
        if(maskList.contains(SINGLE_OPTION_STR)) {
            addWarn("extra quote spec "+SINGLE_OPTION_STR+ " found in the wrong order");
        }
        if(maskList.contains(NONE_OPTION_STR)) {
            addWarn("extra quote spec "+NONE_OPTION_STR+ " found in the wrong order");
        }
    }


    KeyValuePairConverter.ValueQuoteSpecification extractSpec(List<String> optionList) {

        if (optionList == null || optionList.isEmpty()) {
            return null;
        }

        String firstOption = optionList.get(0);

        if (DOUBLE_OPTION_STR.equalsIgnoreCase(firstOption)) {
            return KeyValuePairConverter.ValueQuoteSpecification.DOUBLE;
        } else if (SINGLE_OPTION_STR.equalsIgnoreCase(firstOption)) {
            return KeyValuePairConverter.ValueQuoteSpecification.SINGLE;
        } else if (NONE_OPTION_STR.equalsIgnoreCase(firstOption)) {
            return KeyValuePairConverter.ValueQuoteSpecification.NONE;
        } else {
            return null;
        }
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
            Character quoteChar = valueQuoteSpec.asChar();
            if (quoteChar != null)
                sb.append(quoteChar);
            if (maskList.contains(kvp.key))
                sb.append(MASK);
            else
                sb.append(String.valueOf(kvp.value));
            if (quoteChar != null)
                sb.append(quoteChar);
        }

        return sb.toString();
    }
}
