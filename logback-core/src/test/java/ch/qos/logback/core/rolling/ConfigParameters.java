package ch.qos.logback.core.rolling;

class ConfigParameters {

    long simulatedTime;
    int maxHistory;
    int simulatedNumberOfPeriods;
    int startInactivity = -1;
    int numInactivityPeriods;
    String fileNamePattern;
    long periodDurationInMillis = TimeBasedRollingWithArchiveRemoval_Test.MILLIS_IN_DAY;
    long sizeCap;

    ConfigParameters(final long simulatedTime) {
        this.simulatedTime = simulatedTime;
    }

    ConfigParameters maxHistory(final int maxHistory) {
        this.maxHistory = maxHistory;
        return this;
    }

    ConfigParameters simulatedNumberOfPeriods(final int simulatedNumberOfPeriods) {
        this.simulatedNumberOfPeriods = simulatedNumberOfPeriods;
        return this;
    }

    ConfigParameters startInactivity(final int startInactivity) {
        this.startInactivity = startInactivity;
        return this;
    }

    ConfigParameters numInactivityPeriods(final int numInactivityPeriods) {
        this.numInactivityPeriods = numInactivityPeriods;
        return this;
    }

    ConfigParameters fileNamePattern(final String fileNamePattern) {
        this.fileNamePattern = fileNamePattern;
        return this;
    }

    ConfigParameters periodDurationInMillis(final long periodDurationInMillis) {
        this.periodDurationInMillis = periodDurationInMillis;
        return this;
    }

    ConfigParameters sizeCap(final long sizeCap) {
        this.sizeCap = sizeCap;
        return this;
    }
}