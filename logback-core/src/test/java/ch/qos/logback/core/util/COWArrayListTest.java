package ch.qos.logback.core.util;

import static org.junit.Assert.assertArrayEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class COWArrayListTest {

    Integer[] model = {};
    COWArrayList<Integer> cowaList = new COWArrayList<>(model);

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void basicToArray() {
        cowaList.add(1);
        final Object[] result = cowaList.toArray();
        assertArrayEquals(new Integer[] { 1 }, result);
    }

    @Test
    public void basicToArrayWithModel() {
        cowaList.add(1);
        final Integer[] result = cowaList.toArray(model);
        assertArrayEquals(new Integer[] { 1 }, result);
    }


    @Test
    public void basicToArrayTyped() {
        cowaList.add(1);
        final Integer[] result = cowaList.asTypedArray();
        assertArrayEquals(new Integer[] { 1 }, result);
    }

}
