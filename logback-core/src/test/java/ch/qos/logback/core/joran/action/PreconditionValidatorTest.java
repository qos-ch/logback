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
package ch.qos.logback.core.joran.action;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.ContextBase;
import ch.qos.logback.core.joran.spi.SaxEventInterpretationContext;

/**
 * Test {@link PreconditionValidator}.
 */
public class PreconditionValidatorTest {

    IncludeAction includeAction = new IncludeAction();
    Context context;
    SaxEventInterpretationContext interpretationContext;
    DummyAttributes attributes;
    String tagName = "testTag";

    @BeforeEach
    public void setUp() {
        context = new ContextBase();
        includeAction.setContext(context);
        interpretationContext = new SaxEventInterpretationContext(context, null);
        attributes = new DummyAttributes();
    }

    @Test
    public void validateOneAndOnlyOneAttributeProvided_withExactlyOneValidAttribute() {
        attributes.setValue("file", "test.txt");

        PreconditionValidator validator = new PreconditionValidator(includeAction, interpretationContext, tagName, attributes);
        validator.validateOneAndOnlyOneAttributeProvided("file", "url", "resource");

        Assertions.assertTrue(validator.isValid());
    }

    @Test
    public void validateOneAndOnlyOneAttributeProvided_withTwoValidAttributes() {
        attributes.setValue("file", "test.txt");
        attributes.setValue("url", "http://example.com");

        PreconditionValidator validator = new PreconditionValidator(includeAction, interpretationContext, tagName, attributes);
        validator.validateOneAndOnlyOneAttributeProvided("file", "url", "resource");

        Assertions.assertFalse(validator.isValid());
        Assertions.assertEquals(1, context.getStatusManager().getCount());
    }

    @Test
    public void validateOneAndOnlyOneAttributeProvided_withNoValidAttributes() {
        PreconditionValidator validator = new PreconditionValidator(includeAction, interpretationContext, tagName, attributes);
        validator.validateOneAndOnlyOneAttributeProvided("file", "url", "resource");

        Assertions.assertFalse(validator.isValid());
        Assertions.assertEquals(1, context.getStatusManager().getCount());
    }

    @Test
    public void validateOneAndOnlyOneAttributeProvided_withEmptyStringAttribute() {
        attributes.setValue("file", "");

        PreconditionValidator validator = new PreconditionValidator(includeAction, interpretationContext, tagName, attributes);
        validator.validateOneAndOnlyOneAttributeProvided("file", "url", "resource");

        Assertions.assertFalse(validator.isValid());
    }

    @Test
    public void validateOneAndOnlyOneAttributeProvided_withWhitespaceOnlyAttribute() {
        attributes.setValue("file", "   ");

        PreconditionValidator validator = new PreconditionValidator(includeAction, interpretationContext, tagName, attributes);
        validator.validateOneAndOnlyOneAttributeProvided("file", "url", "resource");

        Assertions.assertFalse(validator.isValid());
    }

    @Test
    public void validateOneAndOnlyOneAttributeProvided_withOneValidAndOneInvalidAttribute() {
        attributes.setValue("file", "test.txt");
        attributes.setValue("url", "");

        PreconditionValidator validator = new PreconditionValidator(includeAction, interpretationContext, tagName, attributes);
        validator.validateOneAndOnlyOneAttributeProvided("file", "url", "resource");

        Assertions.assertTrue(validator.isValid());
    }

    @Test
    public void validateOneAndOnlyOneAttributeProvided_withThreeValidAttributes() {
        attributes.setValue("file", "test.txt");
        attributes.setValue("url", "http://example.com");
        attributes.setValue("resource", "config.xml");

        PreconditionValidator validator = new PreconditionValidator(includeAction, interpretationContext, tagName, attributes);
        validator.validateOneAndOnlyOneAttributeProvided("file", "url", "resource");

        Assertions.assertFalse(validator.isValid());
        Assertions.assertEquals(1, context.getStatusManager().getCount());
    }

    @Test
    public void validateOneAndOnlyOneAttributeProvided_withSingleAttributeOption() {
        attributes.setValue("name", "testName");

        PreconditionValidator validator = new PreconditionValidator(includeAction, interpretationContext, tagName, attributes);
        validator.validateOneAndOnlyOneAttributeProvided("name");

        Assertions.assertTrue(validator.isValid());
    }

    @Test
    public void validateOneAndOnlyOneAttributeProvided_withNullAttribute() {
        attributes.setValue("file", null);

        PreconditionValidator validator = new PreconditionValidator(includeAction, interpretationContext, tagName, attributes);
        validator.validateOneAndOnlyOneAttributeProvided("file", "url");

        Assertions.assertFalse(validator.isValid());
    }
}
