package com.hepdd.gtmthings.common.cover

import net.minecraft.core.Direction
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.item.ItemStack

import com.gregtechceu.gtceu.api.GTValues
import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper
import com.gregtechceu.gtceu.api.capability.ICoverable
import com.gregtechceu.gtceu.api.capability.recipe.IO
import com.gregtechceu.gtceu.api.cover.CoverBehavior
import com.gregtechceu.gtceu.api.cover.CoverDefinition
import com.gregtechceu.gtceu.api.machine.MetaMachine
import com.gregtechceu.gtceu.api.machine.TickableSubscription
import com.gregtechceu.gtceu.api.machine.TieredEnergyMachine
import com.gregtechceu.gtceu.common.machine.electric.BatteryBufferMachine
import com.gregtechceu.gtceu.common.machine.electric.HullMachine
import com.hepdd.gtmthings.api.machine.IWirelessEnergyContainerHolder
import com.hepdd.gtmthings.api.machine.WirelessEnergyReceiveCoverHolder
import com.hepdd.gtmthings.api.misc.WirelessEnergyContainer

import java.util.*
import kotlin.math.min

open class WirelessEnergyReceiveCover(definition: CoverDefinition, coverHolder: ICoverable, attachedSide: Direction, tier: Int, amperage: Int) :
    CoverBehavior(definition, coverHolder, attachedSide),
    IWirelessEnergyContainerHolder {

    private var subscription: TickableSubscription? = null

    private var wirelessEnergyContainerCache: WirelessEnergyContainer? = null

    private var machine: MetaMachine? = null

    private var energyPerTick: Long = 0
    private var tier = 0
    private var amperage = 0
    private var machineMaxEnergy: Long = 0

    init {
        this.tier = tier
        this.amperage = amperage
        this.energyPerTick = GTValues.VEX[tier] * amperage
    }

    override fun canAttach(): Boolean {
        val machine = getMachine()
        if (machine is TieredEnergyMachine && machine.energyContainer.getHandlerIO() == IO.IN && machine.getTier() >= this.tier) {
            val covers = machine.getCoverContainer().covers
            for (cover in covers) {
                if (cover is WirelessEnergyReceiveCover) return false
            }
            return true
        } else if (machine is BatteryBufferMachine) {
            return machine.getTier() >= this.tier
        } else if (machine is HullMachine) {
            return machine.getTier() >= this.tier
        } else if (machine is WirelessEnergyReceiveCoverHolder) {
            return machine.getTier() >= this.tier
        } else {
            return false
        }
    }

    override fun onAttached(itemStack: ItemStack, player: ServerPlayer) {
        super.onAttached(itemStack, player)
        val machine = getMachine()
        if (machine != null && getUUID() == null) {
            machine.ownerUUID = player.getUUID()
        }
        updateCoverSub()
    }

    override fun onLoad() {
        super.onLoad()
        updateCoverSub()
    }

    override fun onRemoved() {
        super.onRemoved()
        machine = null
        wirelessEnergyContainerCache = null
        if (subscription != null) {
            subscription!!.unsubscribe()
            subscription = null
        }
    }

    private fun updateCoverSub() {
        subscription = coverHolder.subscribeServerTick(subscription) { this.updateEnergy() }
    }

    private fun updateEnergy() {
        if (getUUID() == null) return
        val energyContainer =
            GTCapabilityHelper.getEnergyContainer(coverHolder.level, coverHolder.pos, attachedSide)
        if (energyContainer != null) {
            val machine = getMachine()
            if (machine is BatteryBufferMachine || machine is HullMachine || machine is WirelessEnergyReceiveCoverHolder) {
                val changeStored =
                    min(energyContainer.energyCapacity - energyContainer.energyStored, this.energyPerTick)
                if (changeStored <= 0) return
                val container = getWirelessEnergyContainer()
                if (container == null) return
                val changeenergy = container.removeEnergy(changeStored, machine)
                if (changeenergy > 0) {
                    energyContainer.acceptEnergyFromNetwork(
                        null,
                        changeenergy / this.amperage,
                        this.amperage.toLong(),
                    )
                }
            } else {
                val changeStored = min(this.machineMaxEnergy - energyContainer.energyStored, this.energyPerTick)
                if (changeStored <= 0) return
                val container = getWirelessEnergyContainer()
                if (container == null) return
                val changeenergy = container.removeEnergy(changeStored, machine)
                if (changeenergy > 0) energyContainer.addEnergy(changeenergy)
            }
        }
        updateCoverSub()
    }

    override fun getUUID(): UUID? {
        val machine = getMachine()
        if (machine != null) return machine.ownerUUID
        return null
    }

    override fun cover(): Boolean = true

    private fun getMachine(): MetaMachine? {
        if (machine == null) machine = MetaMachine.getMachine(coverHolder.level, coverHolder.pos)
        if (machine is TieredEnergyMachine) {
            this.machineMaxEnergy = GTValues.VEX[(machine as TieredEnergyMachine).getTier()] shl 6
        }
        return machine
    }

    override fun getWirelessEnergyContainerCache(): WirelessEnergyContainer? = this.wirelessEnergyContainerCache

    override fun setWirelessEnergyContainerCache(container: WirelessEnergyContainer) {
        this.wirelessEnergyContainerCache = container
    }
}
