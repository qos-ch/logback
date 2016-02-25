/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2015, QOS.ch. All rights reserved.
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
package ch.qos.logback.core.pattern;

import java.util.List;
import java.util.regex.Pattern;

public class ReplacingCompositeConverter<E> extends CompositeConverter<E> {

    Pattern pattern;
    String regex;
    String replacement;

    public void start() {
        final List<String> optionList = getOptionList();
        if (optionList == null) {
            addError("at least two options are expected whereas you have declared none");
            return;
        }

        int numOpts = optionList.size();

        if (numOpts < 2) {
            addError("at least two options are expected whereas you have declared only " + numOpts + "as [" + optionList + "]");
            return;
        }
        regex = optionList.get(0);
        pattern = Pattern.compile(regex);
        replacement = optionList.get(1);
        super.start();
    }

    @Override
    protected String transform(E event, String in) {
        if (!started)
            return in;
        return pattern.matcher(in).replaceAll(replacement);
    }
}