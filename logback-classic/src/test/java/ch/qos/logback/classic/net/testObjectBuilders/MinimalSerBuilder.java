package ch.qos.logback.classic.net.testObjectBuilders;

import java.io.Serializable;


public class MinimalSerBuilder implements Builder{

	public Object build(int i) {
		return new MinimalSer(i);
	}

}

class MinimalSer implements Serializable {

	private static final long serialVersionUID = 2807646397580899815L;

	String message;

	public MinimalSer(int i) {
		// 45 characters message
		message = Builder.MSG_PREFIX + i;
	}
}