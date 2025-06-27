package com.hepdd.gtmthings.common.block.machine.multiblock.part

import com.gregtechceu.gtceu.api.GTValues
import com.gregtechceu.gtceu.api.capability.recipe.IO
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity
import com.gregtechceu.gtceu.api.machine.TickableSubscription
import com.gregtechceu.gtceu.api.machine.feature.IExplosionMachine
import com.gregtechceu.gtceu.api.machine.feature.IInteractedMachine
import com.gregtechceu.gtceu.api.machine.feature.IMachineLife
import com.gregtechceu.gtceu.api.machine.multiblock.part.TieredIOPartMachine
import com.gregtechceu.gtceu.api.machine.trait.NotifiableEnergyContainer
import com.gregtechceu.gtceu.common.data.GTItems
import com.hepdd.gtmthings.api.machine.IWirelessEnergyContainerHolder
import com.hepdd.gtmthings.api.misc.WirelessEnergyContainer
import com.hepdd.gtmthings.utils.TeamUtil
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.network.chat.Component
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.BlockHitResult
import java.util.*
import kotlin.math.min

open class WirelessEnergyHatchPartMachine(holder: IMachineBlockEntity, tier: Int,io: IO,amperage:Int):TieredIOPartMachine(holder,tier,io),IInteractedMachine, IExplosionMachine, IMachineLife, IWirelessEnergyContainerHolder {
    companion object {
        @JvmStatic
        val MANAGED_FIELD_HOLDER: ManagedFieldHolder = ManagedFieldHolder(
            WirelessEnergyHatchPartMachine::class.java, TieredIOPartMachine.MANAGED_FIELD_HOLDER
        )

        fun getFieldHolder(): ManagedFieldHolder? {
            return MANAGED_FIELD_HOLDER
        }
    }

    private var wirelessEnergyContainerCache: WirelessEnergyContainer? = null

    @Persisted
    var energyContainer: NotifiableEnergyContainer? = null

    private var amperage: Int = 0
    private var updEnergySubs: TickableSubscription? = null

    init {
        this.amperage = amperage
        this.energyContainer = createEnergyContainer()
    }

    private fun createEnergyContainer(): NotifiableEnergyContainer {
        val container: NotifiableEnergyContainer
        if (io == IO.OUT) {
            container = NotifiableEnergyContainer.emitterContainer(
                this, GTValues.VEX[tier] * 64L * amperage,
                GTValues.VEX[tier], amperage.toLong()
            )
        } else {
            container = NotifiableEnergyContainer.receiverContainer(
                this, GTValues.VEX[tier] * 16L * amperage,
                GTValues.VEX[tier], amperage.toLong()
            )
        }
        return container
    }

    override fun onLoad() {
        super.onLoad()
        updateEnergySubscription()
    }

    override fun isRemote(): Boolean {
        return level?.isClientSide ?: true
    }

    override fun onUnload() {
        super.onUnload()
        if (updEnergySubs != null) {
            updEnergySubs!!.unsubscribe()
            updEnergySubs = null
        }
    }

    private fun updateEnergySubscription() {
        if (this.getUUID() != null) {
            updEnergySubs = subscribeServerTick(updEnergySubs) { this.updateEnergy() }
        } else if (updEnergySubs != null) {
            updEnergySubs!!.unsubscribe()
            updEnergySubs = null
        }
    }

    private fun updateEnergy() {
        if (this.getUUID() == null) return
        if (io == IO.IN) {
            useEnergy()
        } else {
            addEnergy()
        }
    }

    private fun useEnergy() {
        val currentStored = energyContainer!!.getEnergyStored()
        val maxStored = energyContainer!!.energyCapacity
        var changeStored =
            min(maxStored - currentStored, energyContainer!!.inputVoltage * energyContainer!!.inputAmperage)
        if (changeStored <= 0) return
        val container = getWirelessEnergyContainer()
        if (container == null) return
        changeStored = container.removeEnergy(changeStored, this)
        if (changeStored > 0) energyContainer!!.setEnergyStored(currentStored + changeStored)
    }

    private fun addEnergy() {
        val currentStored = energyContainer!!.getEnergyStored()
        if (currentStored <= 0) return
        var changeStored =
            min(energyContainer!!.outputVoltage * energyContainer!!.outputAmperage, currentStored)
        val container = getWirelessEnergyContainer()
        if (container == null) return
        changeStored = container.addEnergy(changeStored, this)
        if (changeStored > 0) energyContainer!!.setEnergyStored(currentStored - changeStored)
    }

    override fun shouldOpenUI(player: Player, hand: InteractionHand, hit: BlockHitResult): Boolean {
        return false
    }

    override fun onUse(
        state: BlockState,
        world: Level,
        pos: BlockPos,
        player: Player,
        hand: InteractionHand,
        hit: BlockHitResult
    ): InteractionResult {
        if (isRemote) return InteractionResult.PASS
        val `is` = player.getItemInHand(hand)
        if (`is`.isEmpty) return InteractionResult.PASS
        if (`is`.`is`(GTItems.TOOL_DATA_STICK.asItem())) {
            ownerUUID = player.getUUID()
            this.wirelessEnergyContainerCache = null
            player.sendSystemMessage(
                Component.translatable(
                    "gtmthings.machine.wireless_energy_hatch.tooltip.bind",
                    TeamUtil.GetName(player)
                )
            )
            updateEnergySubscription()
            return InteractionResult.SUCCESS
        } else if (`is`.`is`(Items.STICK)) {
            if (io == IO.OUT) energyContainer!!.setEnergyStored(GTValues.VEX[tier] * 64L * amperage)
            return InteractionResult.SUCCESS
        }
        return InteractionResult.PASS
    }

    override fun onLeftClick(
        player: Player,
        world: Level,
        hand: InteractionHand,
        pos: BlockPos,
        direction: Direction
    ): Boolean {
        if (isRemote) return false
        val `is` = player.getItemInHand(hand)
        if (`is`.isEmpty) return false
        if (`is`.`is`(GTItems.TOOL_DATA_STICK.asItem())) {
            ownerUUID = null
            this.wirelessEnergyContainerCache = null
            player.sendSystemMessage(Component.translatable("gtmthings.machine.wireless_energy_hatch.tooltip.unbind"))
            updateEnergySubscription()
            return true
        }
        return false
    }

    override fun onMachinePlaced(player: LivingEntity?, stack: ItemStack) {
        if (player != null) {
            ownerUUID = player.getUUID()
            updateEnergySubscription()
        }
    }

    override fun getUUID(): UUID? {
        return ownerUUID
    }


    /**/////////////////////////////////// */ // ********** Misc **********//
    /**/////////////////////////////////// */
    override fun tintColor(index: Int): Int {
        if (index == 2) {
            return GTValues.VC[getTier()]
        }
        return super.tintColor(index)
    }

    override fun getWirelessEnergyContainerCache(): WirelessEnergyContainer? {
        return this.wirelessEnergyContainerCache
    }

    override fun setWirelessEnergyContainerCache(container: WirelessEnergyContainer) {
        this.wirelessEnergyContainerCache = container
    }
}