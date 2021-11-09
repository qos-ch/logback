package ch.qos.logback.classic.testUtil;

import java.util.Random;

public class Gaussian {

    Random random;

    double mean;
    double variance;

    public Gaussian(final double mean, final double variance) {
        random = new Random();
        this.mean = mean;
        this.variance = variance;
    }

    public Gaussian(final long seed, final double mean, final double variance) {
        random = new Random(seed);
        this.mean = mean;
        this.variance = variance;
    }

    public double getGaussian() {
        return mean + random.nextGaussian() * variance;
    }




}
