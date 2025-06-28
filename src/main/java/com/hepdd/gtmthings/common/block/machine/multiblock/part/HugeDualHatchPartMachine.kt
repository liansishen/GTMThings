package com.hepdd.gtmthings.common.block.machine.multiblock.part

import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.Style

import com.gregtechceu.gtceu.api.capability.recipe.IO
import com.gregtechceu.gtceu.api.gui.GuiTextures
import com.gregtechceu.gtceu.api.gui.fancy.ConfiguratorPanel
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity
import com.gregtechceu.gtceu.api.machine.fancyconfigurator.FancyTankConfigurator
import com.gregtechceu.gtceu.api.machine.trait.NotifiableFluidTank
import com.hepdd.gtmthings.common.block.machine.trait.CatalystFluidStackHandler
import com.hepdd.gtmthings.utils.FormatUtil
import com.lowdragmc.lowdraglib.gui.util.ClickData
import com.lowdragmc.lowdraglib.gui.widget.ComponentPanelWidget
import com.lowdragmc.lowdraglib.gui.widget.DraggableScrollableWidgetGroup
import com.lowdragmc.lowdraglib.gui.widget.Widget
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup
import com.lowdragmc.lowdraglib.side.fluid.FluidTransferHelper
import com.lowdragmc.lowdraglib.side.item.ItemTransferHelper
import com.lowdragmc.lowdraglib.syncdata.ISubscription
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder

open class HugeDualHatchPartMachine(holder: IMachineBlockEntity, tier: Int, io: IO) : HugeBusPartMachine(holder, tier, io, 9) {

    companion object {
        @JvmStatic
        val MANAGED_FIELD_HOLDER: ManagedFieldHolder = ManagedFieldHolder(
            HugeDualHatchPartMachine::class.java,
            HugeBusPartMachine.MANAGED_FIELD_HOLDER,
        )
    }

    override fun getFieldHolder(): ManagedFieldHolder = MANAGED_FIELD_HOLDER

    @Persisted
    protected var tank: NotifiableFluidTank? = null
    protected var tankSubs: ISubscription? = null

    @Persisted
    protected var shareTank: CatalystFluidStackHandler? = null

    private var hasFluidTransfer = false
    private var hasItemTransfer = false

    init {
        this.tank = createTank()
        this.shareTank = CatalystFluidStackHandler(this, 9, 16000, IO.IN, IO.NONE)
    }

    protected fun createTank(): NotifiableFluidTank = object : NotifiableFluidTank(this, this.getTankInventorySize(), Int.Companion.MAX_VALUE, io) {
        override fun canCapOutput(): Boolean = true
    }

    protected fun getTankInventorySize(): Int = this.getTier() + 1

    override fun attachConfigurators(configuratorPanel: ConfiguratorPanel) {
        super.attachConfigurators(configuratorPanel)
        configuratorPanel.attachConfigurators(
            FancyTankConfigurator(
                shareTank!!.getStorages(),
                Component.translatable("gui.gtceu.share_tank.title"),
            ).setTooltips(
                mutableListOf<Component?>(
                    Component.translatable("gui.gtceu.share_tank.desc.0"),
                    Component.translatable("gui.gtceu.share_inventory.desc.1"),
                ),
            ),
        )
    }

    override fun onLoad() {
        super.onLoad()
        this.tankSubs = this.tank!!.addChangedListener { this.updateInventorySubscription() }
    }

    override fun onUnload() {
        super.onUnload()
        if (this.tankSubs != null) {
            this.tankSubs!!.unsubscribe()
            this.tankSubs = null
        }
    }

    override fun refundAll(clickData: ClickData) {
        // 退不回去流体
        super.refundAll(clickData)
        if (hasFluidTransfer) {
            this.tank!!.exportToNearby(this.getFrontFacing())
        }
    }

    override fun updateInventorySubscription() {
        val canOutput = this.io == IO.OUT && (!this.tank!!.isEmpty() || !this.inventory.isEmpty())
        val level = this.level
        if (level != null) {
            this.hasItemTransfer = ItemTransferHelper.getItemTransfer(
                level,
                this.pos.relative(this.getFrontFacing()),
                this.getFrontFacing().opposite,
            ) != null
            this.hasFluidTransfer = FluidTransferHelper.getFluidTransfer(
                level,
                this.pos.relative(this.getFrontFacing()),
                this.getFrontFacing().opposite,
            ) != null
        } else {
            this.hasItemTransfer = false
            this.hasFluidTransfer = false
        }

        if (!this.isWorkingEnabled || !canOutput && this.io != IO.IN || !this.hasItemTransfer && !this.hasFluidTransfer) {
            if (this.autoIOSubs != null) {
                this.autoIOSubs!!.unsubscribe()
                this.autoIOSubs = null
            }
        } else {
            this.autoIOSubs = this.subscribeServerTick(this.autoIOSubs) { this.autoIO() }
        }
    }

    override fun autoIO() {
        if (this.offsetTimer % 5 == 0L) {
            if (isWorkingEnabled) {
                if (this.io == IO.OUT) {
                    if (this.hasItemTransfer) {
                        this.inventory.exportToNearby(this.getFrontFacing())
                    }

                    if (this.hasFluidTransfer) {
                        this.tank!!.exportToNearby(this.getFrontFacing())
                    }
                } else if (this.io == IO.IN) {
                    if (this.hasItemTransfer) {
                        this.inventory.importFromNearby(this.getFrontFacing())
                    }

                    if (this.hasFluidTransfer) {
                        this.tank!!.importFromNearby(this.getFrontFacing())
                    }
                }
            }
        }
    }

    override fun createUIWidget(): Widget {
        val height = 117
        val width = 178
        val group = WidgetGroup(0, 0, width + 8, height + 4)
        val componentPanel = (
            ComponentPanelWidget(8, 5) { textList: MutableList<Component?>? ->
                this.addDisplayText(
                    textList!!,
                )
            }
            ).setMaxWidthLimit(width - 16)
        val screen = (DraggableScrollableWidgetGroup(4, 4, width, height)).setBackground(GuiTextures.DISPLAY)
            .addWidget(componentPanel)
        group.addWidget(screen)
        return group
    }

    private fun addDisplayText(textList: MutableList<Component?>) {
        var itemCount = 0
        var tankCount = 0

        // item
        for (i in 0..<super.getInventorySize() - 1) {
            val `is` = super.inventory.getStackInSlot(i)
            if (!`is`.isEmpty) {
                textList.add(
                    `is`.displayName.copy().setStyle(Style.EMPTY.withColor(ChatFormatting.YELLOW))
                        .append(
                            Component.literal(FormatUtil.formatNumber(`is`.count.toLong()))
                                .setStyle(Style.EMPTY.withColor(ChatFormatting.AQUA)),
                        ),
                )
                ++itemCount
            }
        }

        // tank
        for (i in 0..<this.getTankInventorySize()) {
            val fs = this.tank!!.getFluidInTank(i)
            if (!fs.isEmpty) {
                textList.add(
                    fs.displayName.copy().setStyle(Style.EMPTY.withColor(ChatFormatting.GOLD))
                        .append(
                            Component.literal(
                                if (fs.amount < 1000) {
                                    fs.amount
                                        .toString() + "mB"
                                } else {
                                    FormatUtil.formatNumber(fs.amount / 1000L) + "B"
                                },
                            )
                                .setStyle(Style.EMPTY.withColor(ChatFormatting.AQUA)),
                        ),
                )
                ++tankCount
            }
        }

        if (textList.isEmpty()) {
            textList.add(Component.translatable("gtmthings.machine.huge_item_bus.tooltip.3"))
        }

        textList.add(
            0,
            Component.translatable("gtmthings.machine.huge_item_bus.tooltip.2", itemCount, super.getInventorySize())
                .setStyle(Style.EMPTY.withColor(ChatFormatting.GREEN)),
        )
        textList.add(
            1,
            Component.translatable(
                "gtmthings.machine.huge_dual_hatch.tooltip.2",
                tankCount,
                this.getTankInventorySize(),
            )
                .setStyle(Style.EMPTY.withColor(ChatFormatting.GREEN)),
        )
    }
}
