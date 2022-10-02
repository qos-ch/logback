package ch.qos.logback.core.blackbox.model;

import ch.qos.logback.core.model.Model;

public class BlackboxTopModel extends Model {

    private static final long serialVersionUID = 6378962040610737208L;

    @Override
    protected BlackboxTopModel makeNewInstance() {
        return new BlackboxTopModel();
    }
    
}
