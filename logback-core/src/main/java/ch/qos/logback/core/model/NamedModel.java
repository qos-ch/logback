package ch.qos.logback.core.model;

public class NamedModel extends Model implements INamedModel {

    /**
     *
     */
    private static final long serialVersionUID = 3549881638769570183L;

    String name;

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(final String name) {
        this.name = name;
    }
}
