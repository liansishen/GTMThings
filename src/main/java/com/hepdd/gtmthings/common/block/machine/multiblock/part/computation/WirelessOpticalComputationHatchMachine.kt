package com.hepdd.gtmthings.common.block.machine.multiblock.part.computation

import com.gregtechceu.gtceu.api.capability.recipe.IO
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity
import com.gregtechceu.gtceu.api.machine.feature.IInteractedMachine
import com.gregtechceu.gtceu.api.machine.multiblock.part.MultiblockPartMachine
import com.gregtechceu.gtceu.common.data.GTItems
import com.hepdd.gtmthings.api.capability.IGTMTJadeIF
import com.hepdd.gtmthings.common.block.machine.trait.WirelessNotifiableComputationContainer
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder
import net.minecraft.core.BlockPos
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.chat.Component
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.BlockHitResult

open class WirelessOpticalComputationHatchMachine(holder: IMachineBlockEntity, transmitter: Boolean):MultiblockPartMachine(holder),IInteractedMachine, IGTMTJadeIF {

    companion object {
        @JvmStatic
        val MANAGED_FIELD_HOLDER: ManagedFieldHolder = ManagedFieldHolder(
            WirelessOpticalComputationHatchMachine::class.java, MultiblockPartMachine.MANAGED_FIELD_HOLDER
        )
    }


    override fun getFieldHolder(): ManagedFieldHolder {
        return MANAGED_FIELD_HOLDER
    }

    override fun isRemote(): Boolean {
        return level?.isClientSide ?: true
    }

    private var transmitter = false

    @Persisted
    var transmitterPos: BlockPos? = null

    @Persisted
    var receiverPos: BlockPos? = null
    var computationContainer: WirelessNotifiableComputationContainer? = null

    init {
        this.transmitter = transmitter
        this.computationContainer = createComputationContainer(transmitter)
    }


    protected fun createComputationContainer(vararg args: Any?): WirelessNotifiableComputationContainer {
        var io = IO.IN
        if (args.size > 1 && args[args.size - 2] is IO) {
            io = (args[args.size - 2] as IO)
        }
        if (args.isNotEmpty() && args[args.size - 1] is Boolean) {
            return WirelessNotifiableComputationContainer(this, io, transmitter)
        }
        throw IllegalArgumentException()
    }

    override fun shouldOpenUI(player: Player, hand: InteractionHand, hit: BlockHitResult): Boolean {
        return false
    }

    override fun canShared(): Boolean {
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
        val `is` = player.getItemInHand(hand)
        if (`is`.isEmpty) return InteractionResult.PASS
        if (`is`.`is`(GTItems.TOOL_DATA_STICK.asItem())) {
            if (transmitter) {
                if (this.transmitterPos == null) this.transmitterPos = pos
                var tag = `is`.tag
                if (tag != null) {
                    val posTag = CompoundTag()
                    posTag.putInt("x", this.transmitterPos!!.x)
                    posTag.putInt("y", this.transmitterPos!!.y)
                    posTag.putInt("z", this.transmitterPos!!.z)
                    tag.put("transmitterPos", posTag)
                    val bindPos = tag.get("receiverPos") as CompoundTag?
                    if (bindPos != null) {
                        val recPos = BlockPos(bindPos.getInt("x"), bindPos.getInt("y"), bindPos.getInt("z"))
                        (getMachine(level!!,recPos) as? WirelessOpticalComputationHatchMachine)?.let { woc ->
                            if (!woc.transmitter) {
                                woc.transmitterPos = this.transmitterPos
                                this.receiverPos = recPos
                                tag.remove("transmitterPos")
                                tag.remove("receiverPos")
                                if (level!!.isClientSide()) {
                                    player.sendSystemMessage(Component.translatable("gtmthings.machine.wireless_computation_hatch.binded"))
                                }
                            }
                        }
                    } else {
                        if (level!!.isClientSide()) {
                            player.sendSystemMessage(Component.translatable("gtmthings.machine.wireless_computation_transmitter_hatch.tobind"))
                        }
                    }
                    `is`.tag = tag
                } else {
                    tag = CompoundTag()
                    val posTag = CompoundTag()
                    posTag.putInt("x", this.transmitterPos!!.x)
                    posTag.putInt("y", this.transmitterPos!!.y)
                    posTag.putInt("z", this.transmitterPos!!.z)
                    tag.put("transmitterPos", posTag)
                    `is`.tag = tag
                    if (level!!.isClientSide()) {
                        player.sendSystemMessage(Component.translatable("gtmthings.machine.wireless_computation_transmitter_hatch.tobind"))
                    }
                }
                return InteractionResult.SUCCESS
            } else {
                if (this.receiverPos == null) this.receiverPos = pos
                var tag = `is`.tag
                if (tag != null) {
                    val posTag = CompoundTag()
                    posTag.putInt("x", this.receiverPos!!.x)
                    posTag.putInt("y", this.receiverPos!!.y)
                    posTag.putInt("z", this.receiverPos!!.z)
                    tag.put("receiverPos", posTag)
                    val bindPos = tag.get("transmitterPos") as CompoundTag?
                    if (bindPos != null) {
                        val tranPos = BlockPos(bindPos.getInt("x"), bindPos.getInt("y"), bindPos.getInt("z"))
                        (getMachine(level!!,tranPos) as? WirelessOpticalComputationHatchMachine)?.let { woc ->
                            if (!woc.transmitter) {
                                woc.receiverPos = this.receiverPos
                                this.transmitterPos = tranPos
                                tag.remove("transmitterPos")
                                tag.remove("receiverPos")
                                if (level!!.isClientSide()) {
                                    player.sendSystemMessage(Component.translatable("gtmthings.machine.wireless_computation_hatch.binded"))
                                }
                            }
                        }
                    } else {
                        if (level!!.isClientSide()) {
                            player.sendSystemMessage(Component.translatable("gtmthings.machine.wireless_computation_receiver_hatch.tobind"))
                        }
                    }
                    `is`.tag = tag
                } else {
                    tag = CompoundTag()
                    val posTag = CompoundTag()
                    posTag.putInt("x", this.receiverPos!!.x)
                    posTag.putInt("y", this.receiverPos!!.y)
                    posTag.putInt("z", this.receiverPos!!.z)
                    tag.put("receiverPos", posTag)
                    `is`.tag = tag
                    if (level!!.isClientSide()) {
                        player.sendSystemMessage(Component.translatable("gtmthings.machine.wireless_computation_receiver_hatch.tobind"))
                    }
                }
                return InteractionResult.SUCCESS
            }
        }
        return InteractionResult.PASS
    }

    override fun isTransmitter(): Boolean {
        return transmitter
    }

    override fun isbinded(): Boolean {
        return (this.transmitterPos != null || this.receiverPos != null)
    }

    override fun getBindPos(): String {
        if (this.isTransmitter() && this.receiverPos != null) {
            return this.receiverPos!!.toShortString()
        } else if (!this.isTransmitter() && this.transmitterPos != null) {
            return this.transmitterPos!!.toShortString()
        }
        return ""
    }
}