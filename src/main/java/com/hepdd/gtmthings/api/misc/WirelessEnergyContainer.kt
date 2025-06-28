package com.hepdd.gtmthings.api.misc

import com.gregtechceu.gtceu.api.machine.MetaMachine
import com.hepdd.gtmthings.config.ConfigHolder
import com.hepdd.gtmthings.data.WirelessEnergySavedData
import com.hepdd.gtmthings.utils.BigIntegerUtils
import com.hepdd.gtmthings.utils.TeamUtil
import lombok.Getter
import net.minecraft.core.GlobalPos
import net.minecraft.server.MinecraftServer
import java.math.BigInteger
import java.util.*
import kotlin.math.min

@Getter
class WirelessEnergyContainer(var uuid: UUID,
                                   var storage: BigInteger? = BigInteger.ZERO,
                                   var rate: Long = 0,
                                   var bindPos: GlobalPos? = null) {
    var energyStat: EnergyStat

    init {
        val currentTick = server!!.tickCount
        this.energyStat = EnergyStat(currentTick)
    }

    companion object {
        @JvmField var observed: Boolean = false
        @JvmField val TRANSFER_DATA: WeakHashMap<MetaMachine, ITransferData> = WeakHashMap()
        @JvmField var server: MinecraftServer? = null

        @JvmStatic
        fun getOrCreateContainer(uuid: UUID?): WirelessEnergyContainer? {
            return WirelessEnergySavedData.INSTANCE?.containerMap?.computeIfAbsent(TeamUtil.getTeamUUID(uuid)) { _uuid: UUID? ->
                WirelessEnergyContainer(
                    _uuid!!
                )
            }
        }
    }

    fun addEnergy(energy: Long, machine: MetaMachine?): Long {
        var change = energy
        ConfigHolder.INSTANCE?.let { if (it.isWirelessRateEnable) change = min(rate.toDouble(), energy.toDouble()).toLong() }
        if (change <= 0) return 0
        storage = storage!!.add(BigInteger.valueOf(change))
        WirelessEnergySavedData.INSTANCE?.isDirty = true
        if (machine != null) {
            energyStat.update(BigInteger.valueOf(change), server!!.tickCount)
        }
        if (observed && machine != null) {
            TRANSFER_DATA[machine] = BasicTransferData(uuid, change, machine)
        }
        return change
    }

    fun removeEnergy(energy: Long, machine: MetaMachine?): Long {
        var change = min(BigIntegerUtils.getLongValue(storage!!).toDouble(), energy.toDouble()).toLong()
        ConfigHolder.INSTANCE?.let {
            if (it.isWirelessRateEnable) change =
                min(BigIntegerUtils.getLongValue(storage!!).toDouble(), min(rate.toDouble(), energy.toDouble())).toLong()
        }
        if (change <= 0) return 0
        storage = storage?.subtract(BigInteger.valueOf(change))
        WirelessEnergySavedData.INSTANCE?.isDirty = true
        if (machine != null) {
            energyStat.update(BigInteger.valueOf(change).negate(), server!!.tickCount)
        }
        if (observed && machine != null) {
            TRANSFER_DATA[machine] = BasicTransferData(uuid, -change, machine)
        }
        return change
    }

    fun getCapacity(): BigInteger? {
        return null
    }
}