package com.hepdd.gtmthings.api.misc

import java.math.BigDecimal
import java.math.BigInteger
import java.math.RoundingMode

class EnergyStat(windowStart: Int) {

    private var minute: TimeWheel? = null
    private var hour: TimeWheel? = null
    private var day: TimeWheel? = null
    private var lastChangedCache: BigInteger = BigInteger.ZERO

    var averageEnergy: BigDecimal = BigDecimal.ZERO

    init {
        minute = TimeWheel(TimeWheel.TIMESCALE.SECOND, 60, windowStart)
        hour = TimeWheel(TimeWheel.TIMESCALE.MINUTE, 60, windowStart)
        day = TimeWheel(TimeWheel.TIMESCALE.HOUR, 24, windowStart)
    }

    fun tick() {
        if (minute!!.tock()) {
            if (hour!!.tock()) {
                day!!.tock()
            }
        }
        averageEnergy =
            if (lastChangedCache.compareTo(BigInteger.ZERO) == 0) {
                BigDecimal.ZERO
            } else {
                BigDecimal(lastChangedCache).divide(
                    BigDecimal.valueOf(minute!!.slotResolution.toLong()),
                    RoundingMode.HALF_UP,
                )
            }
        lastChangedCache = BigInteger.ZERO
    }

    fun update(value: BigInteger?, currentTick: Int) {
        minute!!.update(value, currentTick)
        hour!!.update(value, currentTick)
        day!!.update(value, currentTick)
        lastChangedCache = lastChangedCache.add(value)
    }

    fun getMinuteAvg(): BigDecimal = minute!!.getAvgByTick()

    fun getHourAvg(): BigDecimal = hour!!.getAvgByTick()

    fun getDayAvg(): BigDecimal = day!!.getAvgByTick()

    fun getAvgEnergy(): BigDecimal = averageEnergy
}
