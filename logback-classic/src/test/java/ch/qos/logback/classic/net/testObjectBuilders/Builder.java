package ch.qos.logback.classic.net.testObjectBuilders;

public interface Builder {

	//45 characters message
	final String MSG_PREFIX = "aaaaabbbbbcccccdddddaaaaabbbbbcccccdddddaaaa";
	//final String MSG_PREFIX = "a";
	
	Object build(int i);
}
