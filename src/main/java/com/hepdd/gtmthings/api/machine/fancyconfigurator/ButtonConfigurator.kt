package com.hepdd.gtmthings.api.machine.fancyconfigurator

import com.gregtechceu.gtceu.api.gui.fancy.IFancyConfiguratorButton
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture
import com.lowdragmc.lowdraglib.gui.util.ClickData
import lombok.experimental.Accessors
import net.minecraft.network.chat.Component
import java.util.function.Consumer

@Accessors(chain = true)
open class ButtonConfigurator(texture: IGuiTexture?, onClick: Consumer<ClickData?>,var tooltips1: MutableList<Component?>):IFancyConfiguratorButton {


    private var buttonIcon: IGuiTexture? = null
    private var onClick: Consumer<ClickData?>? = null

    init {
        this.buttonIcon = texture
        this.onClick = onClick
    }

    override fun onClick(clickData: ClickData?) {
        onClick!!.accept(clickData)
    }

    override fun getIcon(): IGuiTexture? {
        return this.buttonIcon
    }

    override fun getTooltips(): MutableList<Component?> {
        return tooltips1;
    }
}