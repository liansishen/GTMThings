package com.hepdd.gtmthings.common.block.machine.multiblock.part

import com.gregtechceu.gtceu.api.GTValues
import com.gregtechceu.gtceu.api.capability.recipe.IO
import com.gregtechceu.gtceu.api.gui.GuiTextures
import com.gregtechceu.gtceu.api.gui.fancy.ConfiguratorPanel
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity
import com.gregtechceu.gtceu.api.machine.TickableSubscription
import com.gregtechceu.gtceu.api.machine.fancyconfigurator.CircuitFancyConfigurator
import com.gregtechceu.gtceu.api.machine.feature.IMachineLife
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IDistinctPart
import com.gregtechceu.gtceu.api.machine.multiblock.part.TieredIOPartMachine
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler
import com.gregtechceu.gtceu.common.item.IntCircuitBehaviour
import com.hepdd.gtmthings.api.machine.fancyconfigurator.ButtonConfigurator
import com.hepdd.gtmthings.api.machine.fancyconfigurator.InventoryFancyConfigurator
import com.hepdd.gtmthings.api.misc.UnlimitedItemStackTransfer
import com.hepdd.gtmthings.api.transfer.UnlimitItemTransferHelper
import com.hepdd.gtmthings.common.block.machine.trait.CatalystItemStackHandler
import com.hepdd.gtmthings.utils.FormatUtil
import com.lowdragmc.lowdraglib.gui.texture.GuiTextureGroup
import com.lowdragmc.lowdraglib.gui.texture.TextTexture
import com.lowdragmc.lowdraglib.gui.util.ClickData
import com.lowdragmc.lowdraglib.gui.widget.ComponentPanelWidget
import com.lowdragmc.lowdraglib.gui.widget.DraggableScrollableWidgetGroup
import com.lowdragmc.lowdraglib.gui.widget.Widget
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup
import com.lowdragmc.lowdraglib.side.item.ItemTransferHelper
import com.lowdragmc.lowdraglib.syncdata.ISubscription
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder
import net.minecraft.ChatFormatting
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.Style
import net.minecraft.server.TickTask
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.block.Block
import java.util.function.IntFunction

open class HugeBusPartMachine(holder: IMachineBlockEntity, tier: Int, io: IO, shareSize: Int):TieredIOPartMachine(holder, tier, io),IDistinctPart, IMachineLife {

    companion object {
        @JvmStatic
        val MANAGED_FIELD_HOLDER: ManagedFieldHolder = ManagedFieldHolder(
            HugeBusPartMachine::class.java,
            TieredIOPartMachine.MANAGED_FIELD_HOLDER
        )

        const val INV_MULTIPLE: Int = 2
    }

    @Persisted
    protected var inventory: NotifiableItemStackHandler
    protected var autoIOSubs: TickableSubscription? = null
    protected var inventorySubs: ISubscription? = null

    @Persisted
    protected var circuitInventory: NotifiableItemStackHandler? = null

    @Persisted
    protected var shareInventory: CatalystItemStackHandler? = null

    init {
        this.inventory = createInventory()
        this.circuitInventory = createCircuitItemHandler(io)
        this.shareInventory = CatalystItemStackHandler(this, shareSize, IO.IN, IO.NONE)
    }

    constructor(holder: IMachineBlockEntity, tier: Int, io: IO):this(holder, tier, io, 4)

    override fun getFieldHolder():ManagedFieldHolder
    {
        return MANAGED_FIELD_HOLDER
    }

    protected fun getInventorySize(): Int {
        return if (getTier() < GTValues.EV) 1 + getTier()
        else (1 + getTier()) * INV_MULTIPLE
    }

    protected fun createInventory(): NotifiableItemStackHandler {
        return object : NotifiableItemStackHandler(
            this,
            getInventorySize(),
            io,
            io,
            IntFunction { size: Int -> UnlimitedItemStackTransfer(size) }) {
            override fun canCapOutput(): Boolean {
                return true
            }
        }
    }

    protected fun createCircuitItemHandler(vararg args: Any?): NotifiableItemStackHandler {
        return if (args.isNotEmpty() && args[0] is IO && io == IO.IN) {
            NotifiableItemStackHandler(this, 1, IO.IN, IO.NONE)
                .setFilter { itemStack: ItemStack? -> IntCircuitBehaviour.isIntegratedCircuit(itemStack) }
        } else {
            NotifiableItemStackHandler(this, 0, IO.NONE)
        }
    }

    override fun onLoad() {
        super.onLoad()
        if (level is ServerLevel) {
            val serverLevel = level as ServerLevel
            serverLevel.server.tell(TickTask(0) { this.updateInventorySubscription() })
        }
        inventorySubs = inventory.addChangedListener { this.updateInventorySubscription() }
    }

    override fun onUnload() {
        super.onUnload()
        if (inventorySubs != null) {
            inventorySubs!!.unsubscribe()
            inventorySubs = null
        }
    }

    override fun isDistinct(): Boolean {
        return inventory.isDistinct() && circuitInventory!!.isDistinct() && shareInventory!!.isDistinct()
    }

    override fun setDistinct(isDistinct: Boolean) {
        inventory.setDistinct(isDistinct)
        circuitInventory!!.setDistinct(isDistinct)
        shareInventory!!.setDistinct(isDistinct)
    }

    protected open fun refundAll(clickData: ClickData) {
        if (ItemTransferHelper.getItemTransfer(
                level, pos.relative(getFrontFacing()),
                getFrontFacing().opposite
            ) != null
        ) {
            setWorkingEnabled(false)
            exportToNearby(inventory, getFrontFacing())
        }
    }


    /**/////////////////////////////////// */ // ******** Auto IO *********//
    /**/////////////////////////////////// */
    override fun onNeighborChanged(block: Block, fromPos: BlockPos, isMoving: Boolean) {
        super.onNeighborChanged(block, fromPos, isMoving)
        updateInventorySubscription()
    }

    override fun onRotated(oldFacing: Direction, newFacing: Direction) {
        super.onRotated(oldFacing, newFacing)
        updateInventorySubscription()
    }

    override fun onMachineRemoved() {
        clearInventory(shareInventory!!)
    }

    protected open fun updateInventorySubscription() {
        if (isWorkingEnabled && ((io == IO.OUT && !inventory.isEmpty()) || io == IO.IN) && ItemTransferHelper.getItemTransfer(
                level, pos.relative(getFrontFacing()),
                getFrontFacing().opposite
            ) != null
        ) {
            autoIOSubs = subscribeServerTick(autoIOSubs) { this.autoIO() }
        } else if (autoIOSubs != null) {
            autoIOSubs!!.unsubscribe()
            autoIOSubs = null
        }
    }

    protected open fun autoIO() {
        if (offsetTimer % 5 == 0L) {
            if (isWorkingEnabled) {
                if (io == IO.OUT) {
                    exportToNearby(inventory, getFrontFacing())
                } else if (io == IO.IN) {
                    inventory.importFromNearby(getFrontFacing())
                }
            }
            updateInventorySubscription()
        }
    }

    fun exportToNearby(handler: NotifiableItemStackHandler, vararg facings: Direction) {
        if (handler.isEmpty()) return
        val level = getLevel()
        val pos = getPos()
        for (facing in facings) {
            UnlimitItemTransferHelper.exportToTarget(
                handler, Int.Companion.MAX_VALUE, { f: ItemStack? -> true }, level!!, pos.relative(facing),
                facing.opposite
            )
        }
    }

    override fun setWorkingEnabled(workingEnabled: Boolean) {
        super.setWorkingEnabled(workingEnabled)
        updateInventorySubscription()
    }


    /**/////////////////////////////////// */ // ********** GUI ***********//
    /**/////////////////////////////////// */
    override fun attachConfigurators(configuratorPanel: ConfiguratorPanel) {
        super<TieredIOPartMachine>.attachConfigurators(configuratorPanel)
        if (this.io == IO.IN) {
            configuratorPanel.attachConfigurators(CircuitFancyConfigurator(circuitInventory!!.storage))
            configuratorPanel.attachConfigurators(
                ButtonConfigurator(
                    GuiTextureGroup(GuiTextures.BUTTON, TextTexture("🔙")), { clickData: ClickData? ->
                        this.refundAll(
                            clickData!!
                        )
                    },
                    mutableListOf(Component.translatable("gtmthings.machine.huge_item_bus.tooltip.1"))
                )
            )
            configuratorPanel.attachConfigurators(
                InventoryFancyConfigurator(
                    shareInventory!!.storage, Component.translatable("gui.gtmthings.share_inventory.title"),
                    mutableListOf(
                        Component.translatable("gui.gtmthings.share_inventory.desc.0"),
                        Component.translatable("gui.gtmthings.share_inventory.desc.1"),
                        Component.translatable("gui.gtmthings.share_inventory.desc.2")
                    )
                )
            )
        }
    }

    override fun createUIWidget(): Widget {
        val height = 117
        val width = 178
        val group = WidgetGroup(0, 0, width + 8, height + 4)

        val componentPanel = ComponentPanelWidget(8, 5) { textList: MutableList<Component?>? ->
            this.addDisplayText(
                textList!!
            )
        }.setMaxWidthLimit(width - 16)
        val screen = DraggableScrollableWidgetGroup(4, 4, width, height)
            .setBackground(GuiTextures.DISPLAY)
            .addWidget(componentPanel)
        group.addWidget(screen)

        return group
    }

    private fun addDisplayText(textList: MutableList<Component?>) {
        var itemCount = 0
        for (i in 0..<getInventorySize()) {
            val `is` = inventory.getStackInSlot(i)
            if (!`is`.isEmpty) {
                textList.add(
                    `is`.displayName.copy()
                        .setStyle(Style.EMPTY.withColor(ChatFormatting.YELLOW))
                        .append(
                            Component.literal(FormatUtil.formatNumber(`is`.count.toLong()))
                                .setStyle(Style.EMPTY.withColor(ChatFormatting.AQUA))
                        )
                )
                itemCount++
            }
        }
        if (textList.isEmpty()) {
            textList.add(Component.translatable("gtmthings.machine.huge_item_bus.tooltip.3"))
        }
        textList.add(
            0, Component.translatable("gtmthings.machine.huge_item_bus.tooltip.2", itemCount, getInventorySize())
                .setStyle(Style.EMPTY.withColor(ChatFormatting.GREEN))
        )
    }

}