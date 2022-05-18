package ch.qos.logback.classic.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import ch.qos.logback.core.model.Model;

public class ModelDuplicationTest {

    @Test
    public void smoke() {
        ConfigurationModel cm = new ConfigurationModel();
        cm.setDebugStr("x");
        Model copy = Model.duplicate(cm);
        assertEquals(cm, copy);
    }
}
