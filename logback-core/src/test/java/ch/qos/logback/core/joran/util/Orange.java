package ch.qos.logback.core.joran.util;

public class Orange extends Citrus<Integer> {

    @Override
    public void setPericarp(final Integer pericarp) {
        System.out.println("Orange.setPericarp");
        super.setPericarp(pericarp);
    }

    @Override
    public void foo() {
    }
}
