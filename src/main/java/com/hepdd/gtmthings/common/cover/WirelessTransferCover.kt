package com.hepdd.gtmthings.common.cover

import net.minecraft.MethodsReturnNonnullByDefault
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.item.ItemStack

import com.gregtechceu.gtceu.api.capability.ICoverable
import com.gregtechceu.gtceu.api.capability.recipe.IO
import com.gregtechceu.gtceu.api.cover.CoverBehavior
import com.gregtechceu.gtceu.api.cover.CoverDefinition
import com.gregtechceu.gtceu.api.machine.MetaMachine
import com.gregtechceu.gtceu.api.machine.SimpleTieredMachine
import com.gregtechceu.gtceu.api.machine.TickableSubscription
import com.gregtechceu.gtceu.api.machine.WorkableTieredMachine
import com.gregtechceu.gtceu.common.machine.electric.PumpMachine
import com.gregtechceu.gtceu.common.machine.multiblock.part.FluidHatchPartMachine
import com.gregtechceu.gtceu.common.machine.multiblock.part.ItemBusPartMachine
import com.gregtechceu.gtceu.common.machine.storage.CrateMachine
import com.gregtechceu.gtceu.common.machine.storage.DrumMachine
import com.gregtechceu.gtceu.common.machine.storage.QuantumChestMachine
import com.gregtechceu.gtceu.common.machine.storage.QuantumTankMachine
import com.lowdragmc.lowdraglib.side.fluid.FluidStack
import com.lowdragmc.lowdraglib.side.fluid.FluidTransferHelper
import com.lowdragmc.lowdraglib.side.item.ItemTransferHelper
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder

import java.util.*
import javax.annotation.ParametersAreNonnullByDefault

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
class WirelessTransferCover(definition: CoverDefinition, coverHolder: ICoverable, attachedSide: Direction, @field:Persisted private val transferType: Int) : CoverBehavior(definition, coverHolder, attachedSide) {
    private var subscription: TickableSubscription? = null
    private var targetLever: ServerLevel? = null

    @Persisted
    private var dimensionId: String? = null

    @Persisted
    private var targetPos: BlockPos? = null

    @Persisted
    private var facing: Direction? = null

    override fun getFieldHolder(): ManagedFieldHolder = MANAGED_FIELD_HOLDER

    override fun canAttach(): Boolean {
        val targetMachine = MetaMachine.getMachine(coverHolder.level, coverHolder.pos)
        if (targetMachine is WorkableTieredMachine) {
            if ((targetMachine.exportItems.slots > 0 && this.transferType == TRANSFER_ITEM) || (targetMachine.exportFluids.tanks > 0 && this.transferType == TRANSFER_FLUID)) {
                for (cover in targetMachine.getCoverContainer().covers) {
                    if (cover is WirelessTransferCover && cover.transferType == this.transferType) return false
                }
                return true
            }
            return false
        } else if (targetMachine is PumpMachine || targetMachine is QuantumTankMachine || targetMachine is DrumMachine) {
            if (this.transferType == TRANSFER_FLUID) {
                for (cover in targetMachine.getCoverContainer().covers) {
                    if (cover is WirelessTransferCover) return false
                }
                return true
            }
            return false
        } else if (targetMachine is QuantumChestMachine || targetMachine is CrateMachine) {
            if (this.transferType == TRANSFER_ITEM) {
                for (cover in targetMachine.getCoverContainer().covers) {
                    if (cover is WirelessTransferCover) return false
                }
                return true
            }
            return false
        } else if ((targetMachine is ItemBusPartMachine && targetMachine.inventory.handlerIO != IO.IN && this.transferType == TRANSFER_ITEM) || (targetMachine is FluidHatchPartMachine && targetMachine.tank.handlerIO != IO.IN && this.transferType == TRANSFER_FLUID)) {
            for (cover in targetMachine.getCoverContainer().covers) {
                if (cover is WirelessTransferCover && cover.transferType == this.transferType) return false
            }
            return true
        }
        return false
    }

    override fun onLoad() {
        super.onLoad()
        getLevel()
        subscription = coverHolder.subscribeServerTick(subscription) { this.update() }
    }

    override fun onRemoved() {
        super.onRemoved()
        if (subscription != null) {
            subscription!!.unsubscribe()
        }
    }

    override fun onAttached(itemStack: ItemStack, player: ServerPlayer) {
        val tag = itemStack.tag
        if (tag != null) {
            this.dimensionId = tag.getString("dimensionid")
            val intX = tag.getInt("x")
            val intY = tag.getInt("y")
            val intZ = tag.getInt("z")
            this.targetPos = BlockPos(intX, intY, intZ)
            this.facing = Direction.byName(tag.getString("facing"))
            getLevel()
        }
        val targetMachine = MetaMachine.getMachine(coverHolder.level, coverHolder.pos)
        if (targetMachine is SimpleTieredMachine) {
            if (this.transferType == TRANSFER_ITEM) targetMachine.isAutoOutputItems = false
            if (this.transferType == TRANSFER_FLUID) targetMachine.isAutoOutputFluids = false
        } else if (targetMachine is ItemBusPartMachine && this.transferType == TRANSFER_ITEM) {
            targetMachine.setWorkingEnabled(false)
        } else if (targetMachine is FluidHatchPartMachine && this.transferType == TRANSFER_FLUID) {
            targetMachine.setWorkingEnabled(false)
        }
        super.onAttached(itemStack, player)
    }

    private fun getLevel() {
        if (this.dimensionId == null) return
        val resLoc = ResourceLocation.tryParse(this.dimensionId!!)
        val resKey = ResourceKey.create(Registries.DIMENSION, resLoc!!)
        this.targetLever = Objects.requireNonNull<MinecraftServer>(coverHolder.level.server).getLevel(resKey)
    }

    private fun update() {
        if (coverHolder.offsetTimer % 5 == 0L) {
            if (this.targetLever == null || this.targetPos == null || coverHolder.level.isClientSide()) return
            if (this.transferType == TRANSFER_ITEM) {
                val itemTransfer =
                    ItemTransferHelper.getItemTransfer(coverHolder.level, coverHolder.pos, attachedSide)
                if (itemTransfer == null) return
                ItemTransferHelper.exportToTarget(
                    itemTransfer,
                    Int.Companion.MAX_VALUE,
                    { f: ItemStack? -> true },
                    this.targetLever,
                    this.targetPos,
                    this.facing,
                )
            } else {
                val fluidTransfer =
                    FluidTransferHelper.getFluidTransfer(coverHolder.level, coverHolder.pos, attachedSide)
                if (fluidTransfer == null) return
                FluidTransferHelper.exportToTarget(
                    fluidTransfer,
                    Int.Companion.MAX_VALUE,
                    { f: FluidStack? -> true },
                    this.targetLever,
                    this.targetPos,
                    this.facing,
                )
            }
        }
    }

    companion object {
        private val MANAGED_FIELD_HOLDER: ManagedFieldHolder = ManagedFieldHolder(
            WirelessTransferCover::class.java,
            CoverBehavior.MANAGED_FIELD_HOLDER,
        )

        const val TRANSFER_ITEM: Int = 1
        const val TRANSFER_FLUID: Int = 2
    }
}
