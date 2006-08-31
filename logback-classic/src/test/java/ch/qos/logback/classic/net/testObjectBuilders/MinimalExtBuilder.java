package ch.qos.logback.classic.net.testObjectBuilders;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;


public class MinimalExtBuilder implements Builder {

	public Object build(int i) {
		return new MinimalExt(i);
	}
	
}

class MinimalExt implements Externalizable {

	private static final long serialVersionUID = -1367146218373963709L;

	String message;

	//public no-args constructor is needed for Externalization
	public MinimalExt() {		
	}
	
	public MinimalExt(int i) {
		// 45 characters message
		message = "aaaaabbbbbcccccdddddaaaaabbbbbcccccdddddaaaa" + i;
	}

	public void readExternal(ObjectInput in) throws IOException,
			ClassNotFoundException {
		message = (String) in.readObject();

	}

	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(message);
	}
}
