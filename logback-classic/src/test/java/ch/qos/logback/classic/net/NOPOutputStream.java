package ch.qos.logback.classic.net;

import java.io.IOException;
import java.io.OutputStream;

public class NOPOutputStream extends OutputStream {

	@Override
	public void write(int b) throws IOException {
		// do nothing

	}

}
