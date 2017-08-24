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
package ch.qos.logback.core.joran.spi;

import java.util.ArrayList;
import java.util.List;

public class CaseCombinator {

    List<String> combinations(String in) {
        int length = in.length();
        List<String> permutationsList = new ArrayList<String>();

        int totalCombinations = computeTotalNumerOfCombinations(in, length);

        for (int j = 0; j < totalCombinations; j++) {
            StringBuilder newCombination = new StringBuilder();
            int pos = 0;
            for (int i = 0; i < length; i++) {
                char c = in.charAt(i);
                if (isEnglishLetter(c)) {
                    c = permute(c, j, pos);
                    pos++;
                }
                newCombination.append(c);
            }
            permutationsList.add(newCombination.toString());
        }
        return permutationsList;

    }

    private char permute(char c, int permutation, int position) {
        int mask = 1 << position;
        boolean shouldBeInUpperCase = (permutation & mask) != 0;
        boolean isEffectivelyUpperCase = isUpperCase(c);
        if (shouldBeInUpperCase && !isEffectivelyUpperCase)
            return toUpperCase(c);
        if (!shouldBeInUpperCase && isEffectivelyUpperCase)
            return toLowerCase(c);
        return c;
    }

    private int computeTotalNumerOfCombinations(String in, int length) {
        int count = 0;
        for (int i = 0; i < length; i++) {
            char c = in.charAt(i);
            if (isEnglishLetter(c))
                count++;
        }
        // return 2^count (2 to the power of count)
        return (1 << count);
    }

    private char toUpperCase(char c) {
        if ('A' <= c && c <= 'Z') {
            return c;
        }
        if ('a' <= c && c <= 'z') {
            return (char) ((int) c + 'A' - 'a');
        }
        // code should never reach this point
        return c;
    }

    private char toLowerCase(char c) {
        if ('a' <= c && c <= 'z') {
            return c;
        }
        if ('A' <= c && c <= 'Z') {
            return (char) ((int) c + 'a' - 'A');
        }
        // code should never reach this point
        return c;
    }

    private boolean isEnglishLetter(char c) {
        if ('a' <= c && c <= 'z')
            return true;

        if ('A' <= c && c <= 'Z')
            return true;
        return false;
    }

    private boolean isUpperCase(char c) {
        return ('A' <= c && c <= 'Z');
    }
}
