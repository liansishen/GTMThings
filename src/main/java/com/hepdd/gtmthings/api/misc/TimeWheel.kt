package com.hepdd.gtmthings.api.misc

import java.math.BigDecimal
import java.math.BigInteger
import java.math.RoundingMode
import java.util.*

class TimeWheel(slotResolution: Int, slotNum: Int, windowStart: Int) {

    private var firstUpdateTick = -1
    private var lastUpdateTick = -1

    object TIMESCALE {
        const val SECOND: Int = 20
        const val MINUTE: Int = 20 * 60
        const val HOUR: Int = 20 * 60 * 60
    }

    var length: Int = 0
    var windowSize: Int = 0
    var slotResolution: Int = 0
    var slots: ArrayDeque<Slot?>? = null
    private var sum: BigInteger = BigInteger.ZERO
    private var slotNum = 0
    private var startIndex = 0

    private var currentIndex = 0

    init {
        this.length = 0
        this.slotNum = slotNum
        this.windowSize = slotResolution * slotNum
        this.slotResolution = if (slotResolution <= 0) 20 else slotResolution
        this.startIndex = (windowStart / slotResolution) % slotNum
        this.currentIndex = startIndex
        slots = ArrayDeque<Slot?>(slotNum)
        slots!!.offer(Slot())
    }

    fun tock(): Boolean {
        if (slots!!.size == slotNum) {
            val s = slots!!.poll()
            if (s != null) {
                sum = sum.subtract(s.sum)
            }
        }
        slots!!.offer(Slot())
        currentIndex = (currentIndex + 1) % slotNum
        return currentIndex == startIndex
    }

    fun update(value: BigInteger?, currentTick: Int) {
        val slot = slots!!.peekLast()
        if (slot == null) return
        slot.sum = slot.sum.add(value)
        sum = sum.add(value)
        this.lastUpdateTick = currentTick
        if (firstUpdateTick == -1) firstUpdateTick = lastUpdateTick
    }

    fun getAvgByTick(): BigDecimal {
        if (lastUpdateTick - firstUpdateTick < slotResolution * slotNum) {
            return BigDecimal(sum).divide(
                BigDecimal.valueOf(
                    (lastUpdateTick - firstUpdateTick + 1).toLong(),
                ),
                RoundingMode.HALF_UP,
            )
        }
        return if (slots!!.isEmpty()) {
            BigDecimal.ZERO
        } else {
            BigDecimal(sum).divide(
                BigDecimal.valueOf(slots!!.size.toLong() * slotResolution + lastUpdateTick % slotResolution - slotResolution),
                RoundingMode.HALF_UP,
            )
        }
    }
}
