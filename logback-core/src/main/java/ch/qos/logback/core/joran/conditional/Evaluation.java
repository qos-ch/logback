package ch.qos.logback.core.joran.conditional;

import ch.qos.logback.core.Context;

public interface Evaluation {

    Condition build(String script);
    void setContext(Context context);

}
