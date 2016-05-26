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

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;

public class CaseCombinatorTest {

    CaseCombinator p = new CaseCombinator();

    @Test
    public void smoke() {
        CaseCombinator p = new CaseCombinator();

        List<String> result = p.combinations("a-B=");

        List<String> witness = new ArrayList<String>();
        witness.add("a-b=");
        witness.add("A-b=");
        witness.add("a-B=");
        witness.add("A-B=");
        assertEquals(witness, result);
    }

    @Test
    public void other() {
        List<String> result = p.combinations("aBCd");
        assertEquals(16, result.size());
        Set<String> witness = new HashSet<String>(result);
        // check that there are no duplicates
        assertEquals(16, witness.size());
    }
}
