package ch.qos.logback.classic.joran.sanity;

import ch.qos.logback.core.model.Model;

public class ClassicTopModel extends Model {

    private static final long serialVersionUID = 6378962040610737208L;

    @Override
    protected ClassicTopModel makeNewInstance() {
        return new ClassicTopModel();
    }

}
