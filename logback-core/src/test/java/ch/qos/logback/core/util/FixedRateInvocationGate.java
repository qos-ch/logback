package ch.qos.logback.core.util;

public class FixedRateInvocationGate implements InvocationGate {

    int rate;
    int invocationCount = 0;

    public FixedRateInvocationGate(final int rate) {
        this.rate = rate;
    }

    @Override
    public boolean isTooSoon(final long currentTime) {
        if (invocationCount++ % rate != 0) {
            return true;
        }
        return false;
    }

}
