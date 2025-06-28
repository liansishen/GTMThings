package com.hepdd.gtmthings.common.cover

import com.gregtechceu.gtceu.api.capability.ICoverable
import com.gregtechceu.gtceu.api.capability.recipe.IO
import com.gregtechceu.gtceu.api.cover.CoverBehavior
import com.gregtechceu.gtceu.api.cover.CoverDefinition
import com.gregtechceu.gtceu.api.cover.IUICover
import com.gregtechceu.gtceu.api.cover.filter.FilterHandler
import com.gregtechceu.gtceu.api.cover.filter.FilterHandlers
import com.gregtechceu.gtceu.api.cover.filter.FluidFilter
import com.gregtechceu.gtceu.api.cover.filter.ItemFilter
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
import com.gregtechceu.gtceu.utils.GTTransferUtils
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget
import com.lowdragmc.lowdraglib.gui.widget.Widget
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder
import net.minecraft.MethodsReturnNonnullByDefault
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.core.registries.Registries
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.item.ItemStack
import net.minecraftforge.fluids.FluidStack
import net.minecraftforge.fluids.FluidUtil
import net.minecraftforge.fluids.capability.IFluidHandler
import net.minecraftforge.items.IItemHandler
import net.minecraftforge.items.ItemHandlerHelper
import java.util.*
import javax.annotation.ParametersAreNonnullByDefault

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
class AdvancedWirelessTransferCover(
    definition: CoverDefinition,
    coverHolder: ICoverable,
    attachedSide: Direction,
    @field:Persisted private val transferType: Int
) : CoverBehavior(definition, coverHolder, attachedSide), IUICover {
    override fun getFieldHolder(): ManagedFieldHolder {
        return MANAGED_FIELD_HOLDER
    }

    private var subscription: TickableSubscription? = null
    private var targetLever: ServerLevel? = null

    @Persisted
    private var dimensionId: String? = null

    @Persisted
    private var targetPos: BlockPos? = null

    @Persisted
    private var facing: Direction? = null

    @Persisted
    @DescSynced
    private val filterHandlerFluid: FilterHandler<FluidStack?, FluidFilter?>

    @Persisted
    @DescSynced
    private val filterHandlerItem: FilterHandler<ItemStack?, ItemFilter>

    init {
        filterHandlerFluid = FilterHandlers.fluid(this)
            .onFilterLoaded { f: FluidFilter? -> configureFilter() }
            .onFilterUpdated { f: FluidFilter? -> configureFilter() }
            .onFilterRemoved { f: FluidFilter? -> configureFilter() }

        filterHandlerItem = FilterHandlers.item(this)
            .onFilterLoaded { f: ItemFilter? -> configureFilter() }
            .onFilterUpdated { f: ItemFilter? -> configureFilter() }
            .onFilterRemoved { f: ItemFilter? -> configureFilter() }
    }

    override fun canAttach(): Boolean {
        val targetMachine = MetaMachine.getMachine(coverHolder.level, coverHolder.pos)
        return when (targetMachine) {
            is WorkableTieredMachine -> {
                (targetMachine.exportItems.slots > 0 && this.transferType == TRANSFER_ITEM) || (targetMachine.exportFluids.tanks > 0 && this.transferType == TRANSFER_FLUID)
            }

            is PumpMachine, is QuantumTankMachine, is DrumMachine -> {
                this.transferType == TRANSFER_FLUID
            }

            is QuantumChestMachine, is CrateMachine -> {
                this.transferType == TRANSFER_ITEM
            }

            else -> (targetMachine is ItemBusPartMachine && targetMachine.inventory.handlerIO != IO.IN && this.transferType == TRANSFER_ITEM) || (targetMachine is FluidHatchPartMachine && targetMachine.tank.handlerIO != IO.IN && this.transferType == TRANSFER_FLUID)
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

    override fun getAdditionalDrops(): MutableList<ItemStack?> {
        val list = super.getAdditionalDrops()
        if (!filterHandlerFluid.filterItem.isEmpty) {
            list.add(filterHandlerFluid.filterItem)
        }
        if (!filterHandlerItem.filterItem.isEmpty) {
            list.add(filterHandlerItem.filterItem)
        }
        return list
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

    private fun update() {
        val timer = coverHolder.offsetTimer
        if (timer % 5 == 0L) {
            if (transferType == TRANSFER_ITEM) {
                val adjacentItemTransfer = this.adjacentItemTransfer
                val myItemHandler = this.ownItemTransfer

                if (adjacentItemTransfer != null && myItemHandler != null) {
                    moveInventoryItems(myItemHandler, adjacentItemTransfer)
                }
            } else if (transferType == TRANSFER_FLUID) {
                val adjacentFluidTransfer = this.adjacentFluidTransfer
                val ownFluidTransfer = this.ownFluidTransfer
                if (ownFluidTransfer != null && adjacentFluidTransfer != null) {
                    transferAny(ownFluidTransfer, adjacentFluidTransfer)
                }
            }
        }
    }

    private fun moveInventoryItems(sourceInventory: IItemHandler, targetInventory: IItemHandler) {
        val filter = filterHandlerItem.getFilter()
        var itemsLeftToTransfer = Int.Companion.MAX_VALUE

        for (srcIndex in 0..<sourceInventory.slots) {
            var sourceStack = sourceInventory.extractItem(srcIndex, itemsLeftToTransfer, true)
            if (sourceStack.isEmpty) {
                continue
            }

            if (!filter.test(sourceStack)) {
                continue
            }

            val remainder = ItemHandlerHelper.insertItemStacked(targetInventory, sourceStack, true)
            val amountToInsert = sourceStack.count - remainder.count

            if (amountToInsert > 0) {
                sourceStack = sourceInventory.extractItem(srcIndex, amountToInsert, false)
                if (!sourceStack.isEmpty) {
                    ItemHandlerHelper.insertItemStacked(targetInventory, sourceStack, false)
                    itemsLeftToTransfer -= sourceStack.count

                    if (itemsLeftToTransfer == 0) {
                        break
                    }
                }
            }
        }
    }

    private fun transferAny(source: IFluidHandler, destination: IFluidHandler) {
        filterHandlerFluid.getFilter()?.let {
            GTTransferUtils.transferFluidsFiltered(
                source, destination,
                it, Int.Companion.MAX_VALUE
            )
        }
    }

    private fun getLevel() {
        if (this.dimensionId == null) return
        val resLoc = ResourceLocation.tryParse(this.dimensionId!!)
        val resKey = ResourceKey.create(Registries.DIMENSION, resLoc!!)
        this.targetLever = Objects.requireNonNull<MinecraftServer>(coverHolder.level.server).getLevel(resKey)
    }

    private fun configureFilter() {
        // Do nothing in the base implementation. This is intended to be overridden by subclasses.
    }

    private val ownItemTransfer: IItemHandler?
        get() = coverHolder.getItemHandlerCap(attachedSide, false)

    private val adjacentItemTransfer: IItemHandler?
        get() {
            if (targetLever == null || targetPos == null) return null
            return GTTransferUtils.getItemHandler(targetLever, targetPos, facing!!.opposite).resolve().orElse(null)
        }

    private val ownFluidTransfer: IFluidHandler?
        get() = coverHolder.getFluidHandlerCap(attachedSide, false)

    private val adjacentFluidTransfer: IFluidHandler?
        get() {
            if (targetLever == null || targetPos == null) return null
            return FluidUtil.getFluidHandler(targetLever, targetPos, facing!!.opposite).resolve().orElse(null)
        }

    override fun createUIWidget(): Widget {
        return if (transferType == TRANSFER_ITEM) {
            createItemUIWidget()
        } else {
            createFluidUIWidget()
        }
    }

    fun createItemUIWidget(): Widget {
        val group = WidgetGroup(0, 0, 176, 107)
        val titleLabel =
            LabelWidget(10, 5, Component.translatable("item.gtmthings.advanced_wireless_item_transfer_cover"))
        titleLabel.setText(Component.translatable("item.gtmthings.advanced_wireless_item_transfer_cover").string)
        group.addWidget(titleLabel)
        group.addWidget(filterHandlerItem.createFilterSlotUI(10, 20))
        group.addWidget(filterHandlerItem.createFilterConfigUI(10, 42, 156, 60))

        return group
    }

    fun createFluidUIWidget(): Widget {
        val group = WidgetGroup(0, 0, 176, 107)
        val titleLabel =
            LabelWidget(10, 5, Component.translatable("item.gtmthings.advanced_wireless_fluid_transfer_cover"))
        titleLabel.setText(Component.translatable("item.gtmthings.advanced_wireless_fluid_transfer_cover").string)
        group.addWidget(titleLabel)
        group.addWidget(filterHandlerFluid.createFilterSlotUI(10, 20))
        group.addWidget(filterHandlerFluid.createFilterConfigUI(10, 42, 156, 60))

        return group
    }

    companion object {
        val MANAGED_FIELD_HOLDER: ManagedFieldHolder = ManagedFieldHolder(
            AdvancedWirelessTransferCover::class.java, CoverBehavior.MANAGED_FIELD_HOLDER
        )

        const val TRANSFER_ITEM: Int = 1
        const val TRANSFER_FLUID: Int = 2
    }
}
