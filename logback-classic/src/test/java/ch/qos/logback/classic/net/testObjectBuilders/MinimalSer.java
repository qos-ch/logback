package ch.qos.logback.classic.net.testObjectBuilders;

import java.io.Serializable;

public class MinimalSer implements Serializable {

    private static final long serialVersionUID = 2807646397580899815L;

    String message;

    public MinimalSer(int i) {
        message = Builder.MSG_PREFIX;
    }
}