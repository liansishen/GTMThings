package com.hepdd.gtmthings.api.machine.fancyconfigurator

import net.minecraft.network.chat.Component

import com.gregtechceu.gtceu.api.gui.fancy.IFancyConfiguratorButton
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture
import com.lowdragmc.lowdraglib.gui.util.ClickData
import lombok.experimental.Accessors

import java.util.function.Consumer

@Accessors(chain = true)
open class ButtonConfigurator(texture: IGuiTexture?, onClick: Consumer<ClickData?>, var tooltips1: MutableList<Component?>) : IFancyConfiguratorButton {

    private var buttonIcon: IGuiTexture? = null
    private var onClick: Consumer<ClickData?>? = null

    init {
        this.buttonIcon = texture
        this.onClick = onClick
    }

    override fun onClick(clickData: ClickData?) {
        onClick!!.accept(clickData)
    }

    override fun getIcon(): IGuiTexture? = this.buttonIcon

    override fun getTooltips(): MutableList<Component?> = tooltips1
}
