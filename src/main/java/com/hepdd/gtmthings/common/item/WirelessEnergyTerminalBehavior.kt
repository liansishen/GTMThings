package com.hepdd.gtmthings.common.item

import net.minecraft.network.chat.Component
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.Level

import com.gregtechceu.gtceu.api.gui.GuiTextures
import com.gregtechceu.gtceu.api.item.component.IItemUIFactory
import com.hepdd.gtmthings.api.misc.WirelessEnergyContainer
import com.hepdd.gtmthings.common.block.machine.electric.WirelessEnergyMonitor
import com.lowdragmc.lowdraglib.gui.editor.ColorPattern
import com.lowdragmc.lowdraglib.gui.factory.HeldItemUIFactory.HeldItemHolder
import com.lowdragmc.lowdraglib.gui.modular.ModularUI
import com.lowdragmc.lowdraglib.gui.widget.*

import java.util.*

class WirelessEnergyTerminalBehavior : IItemUIFactory {
    override fun createUI(holder: HeldItemHolder, entityPlayer: Player): ModularUI = ModularUI(
        WirelessEnergyMonitor.DISPLAY_TEXT_WIDTH + 8 + 8,
        117 + 8 + 8 + 8 + 17,
        holder,
        entityPlayer,
    ).widget(createWidget(holder.getHeld().descriptionId, WirelessMonitor(entityPlayer.uuid, entityPlayer.level())))

    private fun createWidget(descriptionId: String, monitor: WirelessMonitor): Widget {
        val group = WidgetGroup(0, 0, WirelessEnergyMonitor.DISPLAY_TEXT_WIDTH + 8 + 8, 117 + 8 + 8 + 8 + 17)
        val label: Widget = LabelWidget(4, 5, descriptionId)
        label.selfPositionX = group.sizeWidth / 2 - label.sizeWidth / 2
        group.addWidget(
            DraggableScrollableWidgetGroup(4, 4, WirelessEnergyMonitor.DISPLAY_TEXT_WIDTH + 8, 117 + 8 + 8 + 17)
                .setBackground(GuiTextures.DISPLAY)
                .setYScrollBarWidth(2)
                .setYBarStyle(null, ColorPattern.T_WHITE.rectTexture().setRadius(1f))
                .addWidget(label)
                .addWidget(
                    ComponentPanelWidget(
                        8,
                        17,
                    ) { text: MutableList<Component> -> addDisplayText(text, monitor) }.setMaxWidthLimit(
                        WirelessEnergyMonitor.DISPLAY_TEXT_WIDTH,
                    ),
                ),
        )

        group.setBackground(GuiTextures.BACKGROUND_INVERSE)
        return group
    }

    private fun addDisplayText(textList: MutableList<Component>, monitor: WirelessMonitor) {
        if (monitor.isRemote) return
        if (monitor.displayTextCache == null || monitor.level.server!!.tickCount % 10 == 0) {
            monitor.displayTextCache = monitor.getDisplayText(false, WirelessEnergyMonitor.DISPLAY_TEXT_WIDTH)
        }
        textList.addAll(monitor.displayTextCache!!)
    }

    class WirelessMonitor(val uuid: UUID, val level: Level) : IWirelessMonitor {

        val isRemote: Boolean
            get() = level.isClientSide

        var displayTextCache: List<Component>? = null

        private var WirelessEnergyContainerCache: WirelessEnergyContainer? = null

        override fun getUUID(): UUID? = uuid

        override fun display(): Boolean = false

        override fun getWirelessEnergyContainerCache(): WirelessEnergyContainer? = WirelessEnergyContainerCache

        override fun setWirelessEnergyContainerCache(container: WirelessEnergyContainer) {
            WirelessEnergyContainerCache = container
        }

        override fun getMonitorLevel(): Level = this.level
    }
}
