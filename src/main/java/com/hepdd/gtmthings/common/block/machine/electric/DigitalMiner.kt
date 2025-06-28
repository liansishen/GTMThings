package com.hepdd.gtmthings.common.block.machine.electric

import net.minecraft.ChatFormatting
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.Style
import net.minecraft.server.TickTask
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.block.Block
import net.minecraft.world.phys.BlockHitResult

import com.gregtechceu.gtceu.GTCEu
import com.gregtechceu.gtceu.api.GTValues
import com.gregtechceu.gtceu.api.cover.filter.ItemFilter
import com.gregtechceu.gtceu.api.gui.GuiTextures
import com.gregtechceu.gtceu.api.gui.widget.SlotWidget
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity
import com.gregtechceu.gtceu.api.machine.TickableSubscription
import com.gregtechceu.gtceu.api.machine.WorkableTieredMachine
import com.gregtechceu.gtceu.api.machine.feature.IDataInfoProvider
import com.gregtechceu.gtceu.api.machine.feature.IFancyUIMachine
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic
import com.gregtechceu.gtceu.api.transfer.item.CustomItemStackHandler
import com.gregtechceu.gtceu.common.data.GTItems
import com.gregtechceu.gtceu.common.data.machines.GTMachineUtils
import com.gregtechceu.gtceu.common.item.PortableScannerBehavior
import com.hepdd.gtmthings.api.capability.IDigitalMiner
import com.hepdd.gtmthings.api.gui.widget.SimpleNumberInputWidget
import com.hepdd.gtmthings.common.block.machine.trait.miner.DigitalMinerLogic
import com.lowdragmc.lowdraglib.gui.texture.TextTexture
import com.lowdragmc.lowdraglib.gui.widget.*
import com.lowdragmc.lowdraglib.side.item.ItemTransferHelper
import com.lowdragmc.lowdraglib.syncdata.ISubscription
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder

import java.util.function.Predicate
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.roundToLong

open class DigitalMiner(holder: IMachineBlockEntity, tier: Int, vararg args: Any) :
    WorkableTieredMachine(holder, tier, GTMachineUtils.defaultTankSizeFunction, args),
    IDigitalMiner,
    IFancyUIMachine,
    IDataInfoProvider {

    companion object {
        @JvmStatic
        val MANAGED_FIELD_HOLDER: ManagedFieldHolder = ManagedFieldHolder(
            DigitalMiner::class.java,
            WorkableTieredMachine.MANAGED_FIELD_HOLDER,
        )

        const val BORDER_WIDTH: Int = 3
    }

    private var fortuneLevel: Int
    private var energyPerTick: Long = 0
    private var autoOutputSubs: TickableSubscription? = null
    private var exportItemSubs: ISubscription? = null
    private var energySubs: ISubscription? = null

    @Persisted
    private var filterInventory: CustomItemStackHandler? = null

    private var itemFilter: ItemFilter? = null

    // widget
    private var filterSlot: SlotWidget? = null
    private var resetButton: ButtonWidget? = null
    private var silkButton: ButtonWidget? = null
    // protected var fortuneButton: ButtonWidget? = null
    // protected var overClockButton: ButtonWidget? = null

    @Persisted
    private var minerRadius = 0

    @Persisted
    private var minHeight = 0

    @Persisted
    private var maxHeight = 0
    private var silkLevel = 0

    init {
        this.energyPerTick = GTValues.VEX[tier - 1]
        this.filterInventory = createFilterItemHandler()
        this.fortuneLevel = 1
        this.silkLevel = 0
        this.minHeight = 0
        this.maxHeight = 64
        this.minerRadius = 32
    }

    override fun getFieldHolder(): ManagedFieldHolder = MANAGED_FIELD_HOLDER

    override fun isRemote(): Boolean = level?.isClientSide ?: GTCEu.isClientThread()

    protected fun createFilterItemHandler(): CustomItemStackHandler {
        val transfer = CustomItemStackHandler()
        transfer.filter =
            Predicate { item: ItemStack -> item.`is`(GTItems.ITEM_FILTER.asItem()) || item.`is`(GTItems.TAG_FILTER.asItem()) }
        return transfer
    }

    override fun createRecipeLogic(vararg args: Any?): RecipeLogic = DigitalMinerLogic(
        this,
        minerRadius,
        minHeight,
        maxHeight,
        silkLevel,
        itemFilter,
        (40 / 2.0.pow(getTier().toDouble())).toInt(),
    )

    override fun onMachineRemoved() {
        clearInventory(exportItems.storage)
        filterInventory?.let { clearInventory(it) }
    }

    override fun getRecipeLogic(): DigitalMinerLogic = super.getRecipeLogic() as DigitalMinerLogic

    override fun onNeighborChanged(block: Block, fromPos: BlockPos, isMoving: Boolean) {
        super.onNeighborChanged(block, fromPos, isMoving)
        updateAutoOutputSubscription()
    }

    override fun onLoad() {
        super.onLoad()
        if (!isRemote) {
            filterChange()
            (level as? ServerLevel)?.server?.tell(TickTask(0) { this.updateAutoOutputSubscription() })
            exportItemSubs = exportItems.addChangedListener { this.updateAutoOutputSubscription() }
        }
    }

    override fun onUnload() {
        super.onUnload()
        if (exportItemSubs != null) {
            exportItemSubs!!.unsubscribe()
            exportItemSubs = null
        }

        if (energySubs != null) {
            energySubs!!.unsubscribe()
            energySubs = null
        }
    }

    /**/
    // //////////////////////////////// */ // ********** LOGIC **********//
    /**/
    // //////////////////////////////// */
    protected fun updateAutoOutputSubscription() {
        val outputFacingItems = frontFacing
        if (!exportItems.isEmpty &&
            ItemTransferHelper.getItemTransfer(
                level,
                pos.relative(outputFacingItems),
                outputFacingItems.opposite,
            ) != null
        ) {
            autoOutputSubs = subscribeServerTick(autoOutputSubs) { this.autoOutput() }
        } else if (autoOutputSubs != null) {
            autoOutputSubs!!.unsubscribe()
            autoOutputSubs = null
        }
    }

    protected fun autoOutput() {
        if (offsetTimer % 5 == 0L) {
            exportItems.exportToNearby(frontFacing)
        }
        updateAutoOutputSubscription()
    }

    override fun drainInput(simulate: Boolean): Boolean {
        val resultEnergy = energyContainer.energyStored - energyPerTick
        if (resultEnergy >= 0L && resultEnergy <= energyContainer.energyCapacity) {
            if (!simulate) energyContainer.removeEnergy(energyPerTick)
            return true
        }
        return false
    }

    override fun createUIWidget(): Widget {
        val rowSize = 3
        val colSize = 9
        val width = colSize * 18 + 16
        val height = rowSize * 18 + 76 + 4
        var index = 0

        val group = WidgetGroup(0, 0, width, height)

        // infomation screen
        val componentPanel = ComponentPanelWidget(
            4,
            5,
        ) { textList: MutableList<Component>? ->
            this.addDisplayText(
                textList,
            )
        }.setMaxWidthLimit(110)
        val container = WidgetGroup(8, 0, 87, 76)
        container.addWidget(
            DraggableScrollableWidgetGroup(
                4,
                4,
                container.size.width - 8,
                container.size.height - 8,
            )
                .setBackground(GuiTextures.DISPLAY)
                .addWidget(componentPanel),
        )
        container.setBackground(GuiTextures.BACKGROUND_INVERSE)
        group.addWidget(container)

        // output slots
        val slots = WidgetGroup(8, 76 + 4 / 2, colSize * 18, rowSize * 18)
        for (y in 0..<rowSize) {
            for (x in 0..<colSize) {
                val slot = SlotWidget(exportItems, index++, x * 18, y * 18, true, false)
                    .setBackground(GuiTextures.SLOT)
                slots.addWidget(slot)
            }
        }
        group.addWidget(slots)

        // filter slot
        this.filterSlot = SlotWidget(this.filterInventory, 0, 117, 4, true, true)
        filterSlot!!.setChangeListener { this.filterChange() }
            .setBackground(GuiTextures.SLOT, GuiTextures.FILTER_SLOT_OVERLAY)
        group.addWidget(filterSlot)

        // Radius
        group.addWidget(LabelWidget(99, 26, "水平范围:"))
        group.addWidget(
            SimpleNumberInputWidget(
                140,
                24,
                24,
                12,
                { this.minerRadius },
                { radius: Int? -> this.minerRadius = radius!! },
            )
                .setMin(1).setMax((8 * 2.0.pow(getTier().toDouble())).toInt()),
        )

        // Min height
        group.addWidget(LabelWidget(99, 44, "最小高度:"))
        group.addWidget(
            SimpleNumberInputWidget(
                140,
                42,
                24,
                12,
                { this.minHeight },
                { mheight: Int? -> this.minHeight = mheight!! },
            )
                .setMin(level!!.minBuildHeight).setMax(level!!.maxBuildHeight),
        )

        // Max height
        group.addWidget(LabelWidget(99, 62, "最大高度:"))
        group.addWidget(
            SimpleNumberInputWidget(
                140,
                60,
                24,
                12,
                { this.maxHeight },
                { maxHeight: Int? -> this.maxHeight = maxHeight!! },
            )
                .setMin(level!!.minBuildHeight).setMax(level!!.maxBuildHeight),
        )

        // reset button
        this.resetButton = ButtonWidget(
            16,
            46 + BORDER_WIDTH,
            18,
            16 - BORDER_WIDTH,
            TextTexture("重置").setDropShadow(false).setColor(ChatFormatting.GRAY.color!!),
        ) { this.reset() }
        resetButton!!.setHoverTooltips(Component.literal("修改配置后必须重置才能生效。"))
        group.addWidget(this.resetButton)

        // silk button
        this.silkButton = ButtonWidget(
            36,
            46 + BORDER_WIDTH,
            18,
            16 - BORDER_WIDTH,
            TextTexture("精准")
                .setDropShadow(false)
                .setColor((if (silkLevel == 0) ChatFormatting.GRAY.color else ChatFormatting.GREEN.color)!!),
        ) { this.setSilk() }
        silkButton!!.setHoverTooltips(Component.literal("开启精准采集模式，4倍耗电。"))
        group.addWidget(this.silkButton)

        return group
    }

    private fun resetRecipe() {
        isWorkingEnabled = false
        getRecipeLogic().resetRecipeLogic(this.minerRadius, this.minHeight, this.maxHeight, this.silkLevel, itemFilter)
    }

    private fun filterChange() {
        this.itemFilter = null
        if (!filterInventory!!.getStackInSlot(0).isEmpty) {
            this.itemFilter = ItemFilter.loadFilter(
                filterInventory!!.getStackInSlot(0),
            )
        }
        resetRecipe()
    }

    private fun reset() {
        resetRecipe()
    }

    private fun setSilk() {
        if (silkLevel == 0) {
            silkLevel = 1
            silkButton!!.setButtonTexture(
                TextTexture("精准").setDropShadow(false).setColor(ChatFormatting.GREEN.color!!),
            )
            energyPerTick = GTValues.VEX[getTier() - 1] * 4
        } else {
            silkLevel = 0
            silkButton!!.setButtonTexture(
                TextTexture("精准").setDropShadow(false).setColor(ChatFormatting.GRAY.color!!),
            )
            energyPerTick = GTValues.VEX[getTier() - 1]
        }
        resetRecipe()
    }

    private fun addDisplayText(textList: MutableList<Component>?) {
        textList!!.add(Component.literal("挖掘: ").append(getRecipeLogic().oreAmount.toString()))
        if (getRecipeLogic().isDone) {
            textList.add(
                Component.translatable("gtceu.multiblock.large_miner.done")
                    .setStyle(Style.EMPTY.withColor(ChatFormatting.GREEN)),
            )
        } else if (getRecipeLogic().isWorking) {
            textList.add(
                Component.translatable("gtceu.multiblock.large_miner.working")
                    .setStyle(Style.EMPTY.withColor(ChatFormatting.GOLD)),
            )
        } else if (!this.isWorkingEnabled) {
            textList.add(Component.translatable("gtceu.multiblock.work_paused"))
        }
        if (getRecipeLogic().isInventoryFull) {
            textList.add(
                Component.translatable("gtceu.multiblock.large_miner.invfull")
                    .setStyle(Style.EMPTY.withColor(ChatFormatting.RED)),
            )
        }
        if (!drainInput(true)) {
            textList.add(
                Component.translatable("gtceu.multiblock.large_miner.needspower")
                    .setStyle(Style.EMPTY.withColor(ChatFormatting.RED)),
            )
        }
    }

    /**/
    // //////////////////////////////// */ // ******* Interaction *******//
    /**/
    // //////////////////////////////// */
    override fun onScrewdriverClick(playerIn: Player, hand: InteractionHand, gridSide: Direction, hitResult: BlockHitResult): InteractionResult {
        if (isRemote) return InteractionResult.SUCCESS

        if (!this.isActive) {
            val currentRadius = getRecipeLogic().currentRadius
            if (currentRadius == 1) {
                getRecipeLogic().currentRadius = getRecipeLogic().maximumRadius
            } else if (playerIn.isShiftKeyDown) {
                getRecipeLogic().currentRadius =
                    max(1.0, (currentRadius / 2.0f).roundToLong().toDouble()).toInt()
            } else {
                getRecipeLogic().currentRadius = max(1.0, (currentRadius - 1).toDouble()).toInt()
            }

            getRecipeLogic().resetArea(true)

            val workingArea: Int = IDigitalMiner.getWorkingArea(getRecipeLogic().currentRadius)
            playerIn.sendSystemMessage(
                Component.translatable("gtceu.universal.tooltip.working_area", workingArea, workingArea),
            )
        } else {
            playerIn.sendSystemMessage(Component.translatable("gtceu.multiblock.large_miner.errorradius"))
        }
        return InteractionResult.SUCCESS
    }

    override fun getDataInfo(mode: PortableScannerBehavior.DisplayMode): List<Component> {
        if (mode == PortableScannerBehavior.DisplayMode.SHOW_ALL ||
            mode == PortableScannerBehavior.DisplayMode.SHOW_MACHINE_INFO
        ) {
            val workingArea: Int = IDigitalMiner.getWorkingArea(getRecipeLogic().currentRadius)
            return listOf<Component>(
                Component.translatable("gtceu.universal.tooltip.working_area", workingArea, workingArea),
            )
        }
        return ArrayList()
    }
}
