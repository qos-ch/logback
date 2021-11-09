package ch.qos.logback.classic.testUtil;

import org.junit.Test;

public class GaussianDump {

	@Test
	public void dump() {
		final Gaussian g = new Gaussian(1000, 100);
		for(int i = 0; i < 5000; i++) {
			final int r = (int) g.getGaussian();
			System.out.println(r);
		}
	}
}
