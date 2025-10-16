package com.hepdd.gtmthings.api.misc;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;

public class EnergyStat {

    public final TimeWheel minute;
    public final TimeWheel hour;
    public final TimeWheel day;
    private BigInteger lastChangedCache = BigInteger.ZERO;
    private BigInteger lastInputCache = BigInteger.ZERO;
    private BigInteger lastOutputCache = BigInteger.ZERO;

    @Getter
    private @NotNull BigDecimal avgEnergy = BigDecimal.ZERO;
    @Getter
    private @NotNull BigDecimal avgInput = BigDecimal.ZERO; // 输入平均值
    @Getter
    private @NotNull BigDecimal avgOutput = BigDecimal.ZERO; // 输出平均值

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
        int divisor = minute.slotResolution;
        avgEnergy = lastChangedCache.compareTo(BigInteger.ZERO) == 0 ?
                BigDecimal.ZERO :
                new BigDecimal(lastChangedCache).divide(BigDecimal.valueOf(divisor), RoundingMode.HALF_UP);
        avgInput = lastInputCache.compareTo(BigInteger.ZERO) == 0 ?
                BigDecimal.ZERO :
                new BigDecimal(lastInputCache).divide(BigDecimal.valueOf(divisor), RoundingMode.HALF_UP);
        avgOutput = lastOutputCache.compareTo(BigInteger.ZERO) == 0 ?
                BigDecimal.ZERO :
                new BigDecimal(lastOutputCache).divide(BigDecimal.valueOf(divisor), RoundingMode.HALF_UP);

        // 重置缓存
        lastChangedCache = BigInteger.ZERO;
        lastInputCache = BigInteger.ZERO;
        lastOutputCache = BigInteger.ZERO;
    }

    public void update(BigInteger value, int currentTick) {
        minute.update(value, currentTick);
        hour.update(value, currentTick);
        day.update(value, currentTick);
        lastChangedCache = lastChangedCache.add(value);
        if (value.compareTo(BigInteger.ZERO) > 0) {
            lastInputCache = lastInputCache.add(value);
        } else if (value.compareTo(BigInteger.ZERO) < 0) {
            lastOutputCache = lastOutputCache.add(value.negate());
        }
    }
}
