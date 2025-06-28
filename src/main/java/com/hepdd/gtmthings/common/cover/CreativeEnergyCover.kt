package com.hepdd.gtmthings.common.cover

import net.minecraft.MethodsReturnNonnullByDefault
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
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder

import javax.annotation.ParametersAreNonnullByDefault
import kotlin.math.min

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
class CreativeEnergyCover(definition: CoverDefinition, coverHolder: ICoverable, attachedSide: Direction) : CoverBehavior(definition, coverHolder, attachedSide) {
    private var subscription: TickableSubscription? = null

    @Persisted
    private var energyPerTick: Long

    @Persisted
    private var tier: Int

    @Persisted
    private val amperage = 1

    @Persisted
    private var machineMaxEnergy: Long = 0

    override fun getFieldHolder(): ManagedFieldHolder = MANAGED_FIELD_HOLDER

    init {
        this.tier = GTValues.LV
        this.energyPerTick = GTValues.VEX[tier] * amperage
    }

    override fun canAttach(): Boolean {
        val machine = this.machine
        if (machine is TieredEnergyMachine && machine.energyContainer.getHandlerIO() == IO.IN) {
            val covers = machine.getCoverContainer().covers
            for (cover in covers) {
                if (cover is CreativeEnergyCover) return false
            }
            return true
        } else {
            return false
        }
    }

    override fun onLoad() {
        super.onLoad()
        updateCoverSub()
    }

    override fun onRemoved() {
        super.onRemoved()
        if (subscription != null) {
            subscription!!.unsubscribe()
        }
    }

    override fun onAttached(itemStack: ItemStack, player: ServerPlayer) {
        super.onAttached(itemStack, player)
        val machine = this.machine
        if (machine is TieredEnergyMachine) {
            this.tier = machine.getTier()
            this.energyPerTick = GTValues.VEX[this.tier] * amperage
            this.machineMaxEnergy = GTValues.VEX[machine.getTier()] shl 6
        }
        updateCoverSub()
    }

    private fun updateCoverSub() {
        subscription = coverHolder.subscribeServerTick(subscription) { this.updateEnergy() }
    }

    private fun updateEnergy() {
        val energyContainer =
            GTCapabilityHelper.getEnergyContainer(coverHolder.level, coverHolder.pos, attachedSide)
        if (energyContainer != null) {
            val changeStored = min(this.machineMaxEnergy - energyContainer.energyStored, this.energyPerTick)
            if (changeStored <= 0) return
            energyContainer.addEnergy(changeStored)
        }
        updateCoverSub()
    }

    private val machine: MetaMachine?
        get() = MetaMachine.getMachine(coverHolder.level, coverHolder.pos)

    companion object {
        private val MANAGED_FIELD_HOLDER: ManagedFieldHolder = ManagedFieldHolder(
            CreativeEnergyCover::class.java,
            CoverBehavior.MANAGED_FIELD_HOLDER,
        )
    }
}
