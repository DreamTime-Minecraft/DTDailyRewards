package su.dreamtime.dailyrewards.util;

public enum DRTimeUnit {
    SECOND(1000),
    MINUTE(60000),
    HOUR(3600000),
    DAY(86400000),
    WEEK(604800000);

    private long factor;

    DRTimeUnit(long factor) {
        this.factor = factor;
    }

    public long getFactor() {
        return factor;
    }
}
