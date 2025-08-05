package com.hepdd.gtmthings.api.misc;

import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.*;

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
                sum = sum.subtract(s.sum);
            }
        }
        slots.offer(new Slot());
        currentIndex = (currentIndex + 1) % slotNum;
        return currentIndex == startIndex;
    }

    public void update(BigInteger value, int currentTick) {
        Slot slot = slots.peekLast();
        if (slot == null) return;
        slot.sum = slot.sum.add(value);
        sum = sum.add(value);
        this.lastUpdateTick = currentTick;
        if (firstUpdateTick == -1) firstUpdateTick = lastUpdateTick;
    }

    public @NotNull BigDecimal getAvgByTick() {
        if (this.lastUpdateTick - this.firstUpdateTick < this.slotResolution * this.slotNum) {
            return (new BigDecimal(this.sum)).divide(BigDecimal.valueOf(this.lastUpdateTick - this.firstUpdateTick + 1), RoundingMode.HALF_UP);
        } else {
            try {
                return this.slots.isEmpty() ? BigDecimal.ZERO : (new BigDecimal(this.sum)).divide(BigDecimal.valueOf((long) this.slots.size() * (long) this.slotResolution + (long) (this.lastUpdateTick % this.slotResolution) - (long) this.slotResolution), RoundingMode.HALF_UP);
            } catch (ArithmeticException e) {
                return BigDecimal.ZERO;
            }
        }
    }
}
