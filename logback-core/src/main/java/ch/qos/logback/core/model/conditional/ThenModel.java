package ch.qos.logback.core.model.conditional;

import ch.qos.logback.core.model.Model;

public class ThenModel extends Model  {

    private static final long serialVersionUID = -3264631638136701741L;
   
    @Override
    protected ThenModel makeNewInstance() {
        return new ThenModel();
    }
}
