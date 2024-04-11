package ch.qos.logback.core.model.processor;

import org.junit.jupiter.api.Test;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.ContextBase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class ImportModelHandlerTest {

    Context context = new ContextBase();
    ImportModelHandler imh = new ImportModelHandler(context);

    @Test
    public void testStemExtraction() {
        assertNull(imh.extractStem(null));
        assertNull(imh.extractStem(""));
        assertNull(imh.extractStem("bla."));
        assertEquals("Foo", imh.extractStem("bla.Foo"));
        assertEquals("Foo", imh.extractStem("com.titi.bla.Foo"));

    }

}
