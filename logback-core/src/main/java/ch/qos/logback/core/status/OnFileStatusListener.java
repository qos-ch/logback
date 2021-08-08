package ch.qos.logback.core.status;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;

public class OnFileStatusListener extends OnPrintStreamStatusListenerBase {

	String filename;
	PrintStream ps;
	
	@Override
	public void start() {
		if(filename ==  null) {
			addInfo("File option not set. Defaulting to \"status.txt\"");
			filename = "status.txt";
		}
		
		try {
			FileOutputStream fos = new FileOutputStream(filename, true);
			ps = new PrintStream(fos, true);
		} catch (FileNotFoundException e) {
			addError("Failed to open ["+filename+"]", e);
			return;
		}
		
		super.start();
		
	}
	
	@Override
	public void stop() {
		if(!isStarted) {
			return;
		}
		if(ps != null)
			ps.close();
		super.stop();
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	
	@Override
	protected PrintStream getPrintStream() {
		return ps;
	}

	
	
}
