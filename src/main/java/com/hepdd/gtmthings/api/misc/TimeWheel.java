package com.hepdd.gtmthings.api.misc;

import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

public class TimeWheel {

    private int firstUpdateTick = -1;
    private int lastUpdateTick = -1;

    public static class TIMESCALE {

        public final static int SECOND = 20;
        public final static int MINUTE = 20 * 60;
        public final static int HOUR = 20 * 60 * 60;
    }

    int length;
    int windowSize;
    int slotResolution;
    private final int slotNum;
    ArrayDeque<Slot> slots;
    private BigInteger sum = BigInteger.ZERO;
    private BigInteger inputSum = BigInteger.ZERO;
    private BigInteger outputSum = BigInteger.ZERO;
    private final int startIndex;
    private int currentIndex;

    public TimeWheel(int slotResolution, int slotNum, int windowStart) {
        this.length = 0;
        this.slotNum = slotNum;
        this.windowSize = slotResolution * slotNum;
        this.slotResolution = slotResolution <= 0 ? 20 : slotResolution;
        this.startIndex = (windowStart / slotResolution) % slotNum;
        this.currentIndex = startIndex;
        slots = new ArrayDeque<>(slotNum);
        slots.offer(new Slot());
    }

    public boolean tock() {
        if (slots.size() == slotNum) {
            Slot s = slots.poll();
            if (s != null) {
                sum = sum.subtract(s.inputSum).add(s.outputSum); // net = net - in + out
                inputSum = inputSum.subtract(s.inputSum);
                outputSum = outputSum.subtract(s.outputSum);
            }
        }
        slots.offer(new Slot());
        currentIndex = (currentIndex + 1) % slotNum;
        return currentIndex == startIndex;
    }

    public void update(BigInteger value, int currentTick) {
        Slot slot = slots.peekLast();
        if (slot == null) return;
        if (value.compareTo(BigInteger.ZERO) > 0) {
            slot.inputSum = slot.inputSum.add(value);
            inputSum = inputSum.add(value);
            sum = sum.add(value); // 更新净值
        } else if (value.compareTo(BigInteger.ZERO) < 0) {
            BigInteger positiveValue = value.negate();
            slot.outputSum = slot.outputSum.add(positiveValue);
            outputSum = outputSum.add(positiveValue);
            sum = sum.add(value); // 更新净值（减去）
        }
        this.lastUpdateTick = currentTick;
        if (firstUpdateTick == -1) firstUpdateTick = lastUpdateTick;
    }

    public @NotNull BigDecimal getAvgByTick() {
        return calculateAvg(this.sum);
    }

    /**
     * (新方法) 获取输入能量的平均值/刻。
     */
    public @NotNull BigDecimal getAvgInputByTick() {
        return calculateAvg(this.inputSum);
    }

    /**
     * (新方法) 获取输出能量的平均值/刻。
     */
    public @NotNull BigDecimal getAvgOutputByTick() {
        return calculateAvg(this.outputSum);
    }

    /**
     * (新方法) 获取每个时间片（Slot）的输入历史数据，用于绘制图表。
     * 列表中的每个值代表一个时间片内的总输入量。
     */
    public @NotNull List<BigInteger> getInputHistory() {
        return slots.stream().map(slot -> slot.inputSum).collect(Collectors.toList());
    }

    /**
     * (新方法) 获取每个时间片（Slot）的输出历史数据，用于绘制图表。
     * 列表中的每个值代表一个时间片内的总输出量。
     */
    public @NotNull List<BigInteger> getOutputHistory() {
        return slots.stream().map(slot -> slot.outputSum).collect(Collectors.toList());
    }

    private @NotNull BigDecimal calculateAvg(BigInteger total) {
        if (total.equals(BigInteger.ZERO)) return BigDecimal.ZERO;

        long divisor;
        int ticksElapsed = this.lastUpdateTick - this.firstUpdateTick + 1;

        if (firstUpdateTick == -1 || ticksElapsed <= 0) {
            return BigDecimal.ZERO;
        }

        if (ticksElapsed < this.windowSize) {
            // 如果时间轮未满，则使用实际经过的tick数计算
            divisor = ticksElapsed;
        } else {
            // 如果时间轮已满，则使用窗口大小计算
            divisor = (long) this.slots.size() * this.slotResolution;
        }

        if (divisor <= 0) return BigDecimal.ZERO;

        try {
            return new BigDecimal(total).divide(BigDecimal.valueOf(divisor), 2, RoundingMode.HALF_UP);
        } catch (ArithmeticException e) {
            return BigDecimal.ZERO;
        }
    }
}
