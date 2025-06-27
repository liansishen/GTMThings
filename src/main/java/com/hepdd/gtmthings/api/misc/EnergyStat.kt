package com.hepdd.gtmthings.api.misc;

import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;

public class EnergyStat {

    private final TimeWheel minute;
    private final TimeWheel hour;
    private final TimeWheel day;
    private BigInteger lastChangedCache = BigInteger.ZERO;

    @NotNull
    BigDecimal avgEnergy = BigDecimal.ZERO;

    public EnergyStat(int windowStart) {
        minute = new TimeWheel(TimeWheel.TIMESCALE.SECOND, 60, windowStart);
        hour = new TimeWheel(TimeWheel.TIMESCALE.MINUTE, 60, windowStart);
        day = new TimeWheel(TimeWheel.TIMESCALE.HOUR, 24, windowStart);
    }

    public void tick() {
        if (minute.tock()) {
            if (hour.tock()) {
                day.tock();
            }
        }
        avgEnergy = lastChangedCache.compareTo(BigInteger.ZERO) == 0 ?
                BigDecimal.ZERO :
                new BigDecimal(lastChangedCache).divide(BigDecimal.valueOf(minute.slotResolution), RoundingMode.HALF_UP);
        lastChangedCache = BigInteger.ZERO;
    }

    public void update(BigInteger value, int currentTick) {
        minute.update(value, currentTick);
        hour.update(value, currentTick);
        day.update(value, currentTick);
        lastChangedCache = lastChangedCache.add(value);
    }

    public @NotNull BigDecimal getMinuteAvg() {
        return minute.getAvgByTick();
    }

    public @NotNull BigDecimal getHourAvg() {
        return hour.getAvgByTick();
    }

    public @NotNull BigDecimal getDayAvg() {
        return day.getAvgByTick();
    }

    public @NotNull BigDecimal getAvgEnergy() {
        return avgEnergy;
    }
}
