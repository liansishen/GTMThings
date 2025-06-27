package com.hepdd.gtmthings.api.machine.fancyconfigurator

import com.gregtechceu.gtceu.api.gui.GuiTextures
import com.gregtechceu.gtceu.api.gui.fancy.IFancyConfigurator
import com.gregtechceu.gtceu.api.gui.widget.SlotWidget
import com.gregtechceu.gtceu.api.transfer.item.CustomItemStackHandler
import com.hepdd.gtmthings.GTMThings
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture
import com.lowdragmc.lowdraglib.gui.texture.ResourceTexture
import com.lowdragmc.lowdraglib.gui.widget.Widget
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup
import com.lowdragmc.lowdraglib.jei.IngredientIO
import lombok.Getter
import lombok.Setter
import lombok.experimental.Accessors
import net.minecraft.network.chat.Component
import kotlin.collections.mutableListOf
import kotlin.math.sqrt

@Accessors(chain = true)
open class InventoryFancyConfigurator(inventory: CustomItemStackHandler, title: Component?,val tooltips1:MutableList<Component?>):IFancyConfigurator {

    private var inventory: CustomItemStackHandler? = null

    private var title: Component? = null

    init {
        this.inventory = inventory
        this.title = title
    }

    override fun getIcon(): IGuiTexture {
        return ResourceTexture("%s:textures/overlay/inventory_configurator.png".format(GTMThings.MOD_ID))
    }

    override fun createConfigurator(): Widget {
        var rowSize = sqrt(inventory!!.getSlots().toDouble()).toInt()
        var colSize = rowSize
        if (inventory!!.getSlots() == 8) {
            rowSize = 4
            colSize = 2
        }
        val group = WidgetGroup(0, 0, 18 * rowSize + 16, 18 * colSize + 16)
        val container = WidgetGroup(4, 4, 18 * rowSize + 8, 18 * colSize + 8)
        var index = 0
        for (y in 0..<colSize) {
            for (x in 0..<rowSize) {
                container.addWidget(
                    SlotWidget(inventory, index++, 4 + x * 18, 4 + y * 18, true, true)
                        .setBackgroundTexture(GuiTextures.SLOT)
                        .setIngredientIO(IngredientIO.INPUT)
                )
            }
        }

        container.setBackground(GuiTextures.BACKGROUND_INVERSE)
        group.addWidget(container)

        return group
    }

    override fun getTitle(): Component? {
        return title
    }

    override fun getTooltips(): List<Component?>? {
        return tooltips1
    }
}