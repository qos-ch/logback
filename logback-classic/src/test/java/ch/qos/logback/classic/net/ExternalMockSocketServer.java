package ch.qos.logback.classic.net;

import java.io.BufferedInputStream;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ExternalMockSocketServer {

	static final String LOGGINGEVENT = "LoggingEvent";
	static final String LOGGINGEVENT2 = "LoggingEvent2";
	static final String MINIMALEXT = "MinimalExt";
	static final String MINIMALSER = "MinimalSer";

	static final int PORT = 4560;

	static int loopLen;
	static int clientNumber;

	static List<String> msgList = new ArrayList<String>();
	static boolean finished = false;

	String className = LOGGINGEVENT;

	public static void main(String[] args) {
		if (args.length == 2) {
			clientNumber = Integer.parseInt(args[0]);
			loopLen = Integer.parseInt((args[1]));
			System.out.println("Starting Server...");
			runServer();
		} else {
			usage("Wrong number of arguments.");
		}
	}

	static void usage(String msg) {
		System.err.println(msg);
		System.err.println("Usage: java "
				+ ExternalMockSocketServer.class.getName() + " clientNumber loopNumber");
		System.exit(1);
	}

	static void runServer() {
		ObjectInputStream ois;
		Object readObject;
		try {
			System.out.println("Listening on port " + PORT);
			ServerSocket serverSocket = new ServerSocket(PORT);
			for (int j = 0; j < clientNumber; j++) {
				Socket socket = serverSocket.accept();
				System.out.println("New client accepted.");
				System.out.println("Connected to client at " + socket.getInetAddress());
				ois = new ObjectInputStream(new BufferedInputStream(socket
						.getInputStream()));
				for (int i = 0; i < loopLen; i++) {
					readObject = ois.readObject();
					//msgList.add(readObject.toString());
				}
				ois.close();
				System.out.println("Finished with this client.");
			}
			serverSocket.close();
		} catch (Exception se) {
			se.printStackTrace();
		}
		System.out.println("Server finished.");
		finished = true;
	}

}
