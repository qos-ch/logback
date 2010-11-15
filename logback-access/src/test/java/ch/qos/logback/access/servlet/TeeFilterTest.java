package ch.qos.logback.access.servlet;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

public class TeeFilterTest {


    @Test
    public void extractNameList() {
        assertEquals(Arrays.asList(new String[]{"a"}), TeeFilter.extractNameList("a"));
        assertEquals(Arrays.asList(new String[]{"a", "b"}), TeeFilter.extractNameList("a, b"));
        assertEquals(Arrays.asList(new String[]{"a", "b"}), TeeFilter.extractNameList("a; b"));
        assertEquals(Arrays.asList(new String[]{"a", "b", "c"}), TeeFilter.extractNameList("a; b, c"));
    }

    @Test
    public void defaultCase() {
        assertTrue(TeeFilter.computeActivation("somehost", "", ""));
        assertTrue(TeeFilter.computeActivation("somehost", null, null));
    }

    @Test
    public void withIncludesOnly() {
        assertTrue(TeeFilter.computeActivation("a", "a", null));
        assertTrue(TeeFilter.computeActivation("a", "a, b", null));
        assertFalse(TeeFilter.computeActivation("a", "b", null));
        assertFalse(TeeFilter.computeActivation("a", "b, c", null));
    }


    @Test
    public void withExcludesOnly() {
        assertFalse(TeeFilter.computeActivation("a", null, "a"));
        assertFalse(TeeFilter.computeActivation("a", null, "a, b"));
        assertTrue(TeeFilter.computeActivation("a", null, "b"));
        assertTrue(TeeFilter.computeActivation("a", null, "b, c"));
    }


    @Test
    public void withIncludesAndExcludes() {
        assertFalse(TeeFilter.computeActivation("a", "a", "a"));
        assertTrue(TeeFilter.computeActivation("a", "a", "b"));
        assertFalse(TeeFilter.computeActivation("a", "b", "a"));
        assertFalse(TeeFilter.computeActivation("a", "b", "b"));

    }

}
