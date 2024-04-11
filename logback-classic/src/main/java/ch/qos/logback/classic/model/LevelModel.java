package ch.qos.logback.classic.model;

import java.util.Objects;

import ch.qos.logback.core.model.Model;

public class LevelModel extends Model {

    private static final long serialVersionUID = -7287549849308062148L;
    String value;

    @Override
    protected LevelModel makeNewInstance() {
        return new LevelModel();
    }
    
    @Override
    protected void mirror(Model that) {
        LevelModel actual = (LevelModel) that;
        super.mirror(actual);
        this.value = actual.value;
    }
    
    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + Objects.hash(value);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        LevelModel other = (LevelModel) obj;
        return Objects.equals(value, other.value);
    }

}
