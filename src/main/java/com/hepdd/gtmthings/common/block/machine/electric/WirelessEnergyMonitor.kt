package com.hepdd.gtmthings.common.block.machine.electric

import com.gregtechceu.gtceu.GTCEu
import com.gregtechceu.gtceu.api.gui.GuiTextures
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity
import com.gregtechceu.gtceu.api.machine.MetaMachine
import com.gregtechceu.gtceu.api.machine.feature.IFancyUIMachine
import com.hepdd.gtmthings.api.misc.WirelessEnergyContainer
import com.hepdd.gtmthings.common.item.IWirelessMonitor
import com.lowdragmc.lowdraglib.gui.util.ClickData
import com.lowdragmc.lowdraglib.gui.widget.*
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder
import net.minecraft.core.BlockPos
import net.minecraft.network.chat.Component
import net.minecraft.world.level.Level
import java.util.*

open class WirelessEnergyMonitor(holder: IMachineBlockEntity) : MetaMachine(holder), IFancyUIMachine, IWirelessMonitor {

    companion object {
        val MANAGED_FIELD_HOLDER: ManagedFieldHolder = ManagedFieldHolder(
            WirelessEnergyMonitor::class.java,
            MetaMachine.MANAGED_FIELD_HOLDER
        )
        @JvmField var p: Int = 0
        @JvmField var pPos: BlockPos? = null
        @JvmField var DISPLAY_TEXT_WIDTH: Int = 220
    }


    override fun getFieldHolder(): ManagedFieldHolder {
        return MANAGED_FIELD_HOLDER
    }

    var cache: WirelessEnergyContainer? = null

    private var textListCache: List<Component?>? = null

    @Persisted
    private var all = false


    /**/////////////////////////////////// */ // *********** GUI ***********//
    /**/////////////////////////////////// */
    private fun handleDisplayClick(componentData: String, clickData: ClickData) {
        if (componentData == "all") {
            if (!clickData.isRemote) {
                all = !all
            }
        } else if (clickData.isRemote) {
            p = 100
            val parts = componentData.split(", ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            pPos = BlockPos(parts[0].toInt(), parts[1].toInt(), parts[2].toInt())
        }
    }

    override fun isRemote(): Boolean {
        return if (holder.level() == null)  GTCEu.isClientThread() else holder.level().isClientSide
    }

    override fun createUIWidget(): Widget {
        val group = WidgetGroup(0, 0, DISPLAY_TEXT_WIDTH + 8 + 8, 117 + 8)

        group.addWidget(
            DraggableScrollableWidgetGroup(4, 4, DISPLAY_TEXT_WIDTH + 8, 117).setBackground(GuiTextures.DISPLAY)
                .addWidget(LabelWidget(4, 5, self().blockState.block.descriptionId))
                .addWidget(
                    ComponentPanelWidget(
                        4, 17
                    ) { textList: MutableList<Component?> ->
                        this.addDisplayText(
                            textList
                        )
                    }
                        .setMaxWidthLimit(DISPLAY_TEXT_WIDTH)
                        .clickHandler { componentData: String, clickData: ClickData ->
                            this.handleDisplayClick(
                                componentData,
                                clickData
                            )
                        })
        )
        group.setBackground(GuiTextures.BACKGROUND_INVERSE)
        return group
    }

    fun addDisplayText(textList: MutableList<Component?>) {
        if (isRemote) return
        if (textListCache == null || offsetTimer % 10 == 0L) {
            textListCache = getDisplayText(all, DISPLAY_TEXT_WIDTH)
        }
        textList.addAll(textListCache!!)
    }

    override fun getUUID(): UUID? {
        return this.ownerUUID
    }

    override fun display(): Boolean {
        return false
    }

    override fun getMonitorLevel(): Level? {
        return holder.level()
    }

    override fun getWirelessEnergyContainerCache(): WirelessEnergyContainer? {

        return this.cache
    }

    override fun setWirelessEnergyContainerCache(container: WirelessEnergyContainer) {
        this.cache  = container
    }
}