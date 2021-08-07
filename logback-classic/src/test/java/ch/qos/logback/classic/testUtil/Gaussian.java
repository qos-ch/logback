package ch.qos.logback.classic.testUtil;

import java.util.Random;

public class Gaussian {

	Random random;
	
	double mean;
	double variance;
	
	public Gaussian(double mean, double variance) {
		this.random = new Random();
		this.mean = mean;
		this.variance = variance;
	}
	
	public Gaussian(long seed, double mean, double variance) {
		this.random = new Random(seed);
		this.mean = mean;
		this.variance = variance;
	}
	
	public double getGaussian() {
		return mean + random.nextGaussian() * variance;
	}

	
	
	
}
