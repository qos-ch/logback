package ch.qos.logback.core.util;

public class FixedRateInvocationGate implements InvocationGate {

    int rate;
    int invocationCount = 0;

    public FixedRateInvocationGate(int rate) {
        this.rate = rate;
    }

    @Override
    public boolean isTooSoon(long currentTime) {
        if (invocationCount++ % rate != 0)
            return true;
        return false;
    }

}
