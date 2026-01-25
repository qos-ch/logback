/*
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2026, QOS.ch. All rights reserved.
 *
 * This program and the accompanying materials are dual-licensed under
 * either the terms of the Eclipse Public License v2.0 as published by
 * the Eclipse Foundation
 *
 *   or (per the licensee's choosing)
 *
 * under the terms of the GNU Lesser General Public License version 2.1
 * as published by the Free Software Foundation.
 */

package ch.qos.logback.core.boolex;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.ContextBase;
import ch.qos.logback.core.model.processor.ModelInterpretationContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ExpressionPropertyConditionTest {


    Context context = new ContextBase();
    ModelInterpretationContext mic = new ModelInterpretationContext(context);
    ExpressionPropertyCondition epc = new ExpressionPropertyCondition();


    static final String SMOKE_KEY = "SMOKE_KEY";
    static final String UNDEFINED_KEY = "UNDEFINED_KEY";
    static final String SMOKE_VALUE = "SMOKE_VALUE";

    @BeforeEach
    public void setUp() throws Exception {
        epc.setContext(context);
        epc.setLocalPropertyContainer(mic);
        context.putProperty(SMOKE_KEY, SMOKE_VALUE);
    }

    @Test
    public void smokeDefined() {
        String expression = String.format("isDefined(\"%s\")", SMOKE_KEY);
        check(expression, true);
    }


    @Test
    public void notSmokeDefined() {
        String expression = String.format("!isDefined(\"%s\")", UNDEFINED_KEY);
        check(expression, true);
    }

    @Test
    public void notSmokeDefined_and_nullBoo() {
        String expression = String.format("!isDefined(\"%s\") && isNull(\"%s\")", UNDEFINED_KEY, "BOO");
        check(expression, true);
    }

    @Test
    public void paranthesisSmokeDefined() {
        String expression = String.format("(isDefined(\"%s\"))", SMOKE_KEY);
        check(expression, true);
    }

    @Test
    public void SmokeDefinedAndIsNull_AND_IsNull() {
        String expression = String.format("(isDefined(\"%s\") || isNull(\"%s\")) && isNull(\"x\")", SMOKE_KEY, UNDEFINED_KEY);
        check(expression, true);
    }

    @Test
    public void NOTSmokeDefinedAndIsNull_AND_IsNull() {
        String expression = String.format("!(isDefined(\"%s\") || isNull(\"%s\")) && isNull(\"x\")", SMOKE_KEY, UNDEFINED_KEY);
        check(expression, false);
    }

    @Test
    public void smokeBiFunction() {
        String expression = String.format("propertyEquals(\"%s\", \"%s\")" , SMOKE_KEY, SMOKE_VALUE);
        check(expression, true);
    }


    @Test
    public void propertyContains() {
        String expression = String.format("propertyContains(\"%s\", \"%s\")" , SMOKE_KEY, SMOKE_VALUE.substring(0, 2));
        check(expression, true);
    }


    @Test
    public void notPropertyContainsX() {
        String expression = String.format("!propertyContains(\"%s\", \"%s\")" , SMOKE_KEY, SMOKE_VALUE+"x");
        check(expression, true);
    }

    @Test
    public void propertyEqualsOrIsNull() {
        String expression = String.format("!propertyEquals(\"%s\", \"%s\") || !isNull(\"%s\")" , SMOKE_KEY, SMOKE_VALUE, UNDEFINED_KEY);
        check(expression, false);
    }


    void check(String expression, boolean expected) {
        epc.setExpression(expression);
        epc.start();

        if(expected) {
            assertTrue(epc.evaluate());
        } else {
            assertFalse(epc.evaluate());
        }

    }
}
