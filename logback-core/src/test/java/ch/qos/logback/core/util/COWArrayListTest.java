package ch.qos.logback.core.util;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

public class COWArrayListTest {

    Integer[] model = new Integer[0];
    COWArrayList<Integer> cowaList = new COWArrayList<Integer>(model);

    @BeforeEach
    public void setUp() throws Exception {
    }

    @AfterEach
    public void tearDown() throws Exception {
    }

    @Test
    public void basicToArray() {
        cowaList.add(1);
        Object[] result = cowaList.toArray();
        assertArrayEquals(new Integer[] { 1 }, result);
    }

    @Test
    public void basicToArrayWithModel() {
        cowaList.add(1);
        Integer[] result = cowaList.toArray(model);
        assertArrayEquals(new Integer[] { 1 }, result);
    }

    @Test
    public void basicToArrayTyped() {
        cowaList.add(1);
        Integer[] result = cowaList.asTypedArray();
        assertArrayEquals(new Integer[] { 1 }, result);
    }

}
