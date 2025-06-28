package com.hepdd.gtmthings.common.block.machine.electric

import com.gregtechceu.gtceu.api.GTValues
import com.gregtechceu.gtceu.api.capability.recipe.IO
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity
import com.gregtechceu.gtceu.api.machine.MetaMachine
import com.gregtechceu.gtceu.api.machine.TickableSubscription
import com.gregtechceu.gtceu.api.machine.feature.IInteractedMachine
import com.gregtechceu.gtceu.api.machine.feature.IMachineLife
import com.gregtechceu.gtceu.api.machine.multiblock.part.TieredIOPartMachine
import com.gregtechceu.gtceu.api.machine.trait.NotifiableEnergyContainer
import com.gregtechceu.gtceu.common.data.GTItems
import com.hepdd.gtmthings.api.machine.IWirelessEnergyContainerHolder
import com.hepdd.gtmthings.api.misc.WirelessEnergyContainer
import com.hepdd.gtmthings.utils.TeamUtil
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.network.chat.Component
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.BlockHitResult
import java.util.*

class WirelessEnergyInterface(holder: IMachineBlockEntity): TieredIOPartMachine(holder, GTValues.MAX, IO.IN), IInteractedMachine, IMachineLife, IWirelessEnergyContainerHolder {

    companion object {
        @JvmStatic
        val MANAGED_FIELD_HOLDER: ManagedFieldHolder = ManagedFieldHolder(
            WirelessEnergyInterface::class.java,
            MetaMachine.MANAGED_FIELD_HOLDER
        )

        fun getFieldHolder(): ManagedFieldHolder {
            return MANAGED_FIELD_HOLDER
        }
    }

    private var updEnergySubs: TickableSubscription? = null

    private var wirelessEnergyContainerCache: WirelessEnergyContainer? = null

    private var energyContainer: NotifiableEnergyContainer

    init {
        this.energyContainer = createEnergyContainer()
    }

    private fun createEnergyContainer(): NotifiableEnergyContainer {
        val container =
            NotifiableEnergyContainer.receiverContainer(
                this, Long.MAX_VALUE,
                GTValues.VEX[tier], 67108864
            )
        container.setSideInputCondition { s: Direction -> s == frontFacing && isWorkingEnabled }
        container.setCapabilityValidator { s: Direction? -> s == null || s == frontFacing }

        return container
    }

    override fun onLoad() {
        super.onLoad()
        updateEnergySubscription()
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
        val currentStored = energyContainer.energyStored
        if (currentStored <= 0) return
        val container = getWirelessEnergyContainer() ?: return
        val changeEnergy = container.addEnergy(currentStored, this)
        if (changeEnergy > 0) energyContainer.energyStored = currentStored - changeEnergy
    }

    override fun shouldOpenUI(player: Player?, hand: InteractionHand?, hit: BlockHitResult?): Boolean {
        return false
    }

    override fun onUse(
        state: BlockState?,
        world: Level?,
        pos: BlockPos?,
        player: Player,
        hand: InteractionHand?,
        hit: BlockHitResult?
    ): InteractionResult {
        if (isRemote) return InteractionResult.PASS
        val `is` = player.getItemInHand(hand!!)
        if (`is`.isEmpty) return InteractionResult.PASS
        if (`is`.`is`(GTItems.TOOL_DATA_STICK.asItem())) {
            ownerUUID = player.uuid
            wirelessEnergyContainerCache = null
            player.sendSystemMessage(
                Component.translatable(
                    "gtmthings.machine.wireless_energy_hatch.tooltip.bind",
                    TeamUtil.getName(player)
                )
            )
            updateEnergySubscription()
            return InteractionResult.SUCCESS
        }
        return InteractionResult.PASS
    }

    override fun onLeftClick(
        player: Player,
        world: Level?,
        hand: InteractionHand?,
        pos: BlockPos?,
        direction: Direction?
    ): Boolean {
        if (isRemote) return false
        val `is` = player.getItemInHand(hand!!)
        if (`is`.isEmpty) return false
        if (`is`.`is`(GTItems.TOOL_DATA_STICK.asItem())) {
            ownerUUID = null
            wirelessEnergyContainerCache = null
            player.sendSystemMessage(Component.translatable("gtmthings.machine.wireless_energy_hatch.tooltip.unbind"))
            updateEnergySubscription()
            return true
        }
        return false
    }

    override fun onMachinePlaced(player: LivingEntity?, stack: ItemStack?) {
        if (player != null) {
            ownerUUID = player.uuid
            updateEnergySubscription()
        }
    }

    override fun getWirelessEnergyContainerCache(): WirelessEnergyContainer? {
        return this.wirelessEnergyContainerCache
    }

    override fun setWirelessEnergyContainerCache(container: WirelessEnergyContainer) {
        this.wirelessEnergyContainerCache = container
    }

    override fun getUUID(): UUID? {
        return this.ownerUUID
    }


    /**/////////////////////////////////// */ // ********** Misc **********//
    /**/////////////////////////////////// */
    override fun tintColor(index: Int): Int {
        if (index == 2) {
            return GTValues.VC[getTier()]
        }
        return super.tintColor(index)
    }
}