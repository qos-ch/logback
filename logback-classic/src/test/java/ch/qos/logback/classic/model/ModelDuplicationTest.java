package ch.qos.logback.classic.model;

import ch.qos.logback.core.model.ImportModel;
import ch.qos.logback.core.model.Model;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ModelDuplicationTest {

    @Test
    public void smoke() {
        ConfigurationModel cm = new ConfigurationModel();
        cm.setDebugStr("x");
        Model copy = Model.duplicate(cm);
        assertEquals(cm, copy);
    }

    @Test
    public void test() {
        ConfigurationModel cm = new ConfigurationModel();
        cm.setDebugStr("x");
        
        ImportModel importModel = new ImportModel();
        importModel.setClassName("a");

        cm.addSubModel(importModel);
        
        Model copy = Model.duplicate(cm);
        assertEquals(cm, copy);
    }
    
    
    
}
