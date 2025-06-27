package com.hepdd.gtmthings.common.block.machine.multiblock.part

import com.gregtechceu.gtceu.api.GTValues
import com.gregtechceu.gtceu.api.capability.recipe.IO
import com.gregtechceu.gtceu.api.gui.GuiTextures
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity
import com.gregtechceu.gtceu.api.machine.TickableSubscription
import com.gregtechceu.gtceu.api.machine.feature.IDataInfoProvider
import com.gregtechceu.gtceu.api.machine.multiblock.part.TieredIOPartMachine
import com.gregtechceu.gtceu.api.machine.trait.NotifiableLaserContainer
import com.gregtechceu.gtceu.common.item.PortableScannerBehavior
import com.gregtechceu.gtceu.utils.GTUtil
import com.lowdragmc.lowdraglib.gui.editor.ColorPattern
import com.lowdragmc.lowdraglib.gui.modular.ModularUI
import com.lowdragmc.lowdraglib.gui.texture.GuiTextureGroup
import com.lowdragmc.lowdraglib.gui.texture.ResourceBorderTexture
import com.lowdragmc.lowdraglib.gui.texture.TextTexture
import com.lowdragmc.lowdraglib.gui.util.ClickData
import com.lowdragmc.lowdraglib.gui.widget.ButtonWidget
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget
import com.lowdragmc.lowdraglib.gui.widget.SelectorWidget
import com.lowdragmc.lowdraglib.gui.widget.TextFieldWidget
import com.lowdragmc.lowdraglib.syncdata.ISubscription
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component
import net.minecraft.world.entity.player.Player
import org.apache.commons.lang3.ArrayUtils
import java.util.*

open class CreativeLaserHatchPartMachine(holder: IMachineBlockEntity):TieredIOPartMachine(holder, GTValues.MAX, IO.IN),IDataInfoProvider {

    companion object {
        @JvmStatic
        val MANAGED_FIELD_HOLDER: ManagedFieldHolder =
            ManagedFieldHolder(CreativeLaserHatchPartMachine::class.java, TieredIOPartMachine.MANAGED_FIELD_HOLDER)

        @JvmStatic
        val VNF: Array<String> = arrayOf<String>(
            ChatFormatting.BLUE.toString() + "IV",
            ChatFormatting.LIGHT_PURPLE.toString() + "LuV",
            ChatFormatting.RED.toString() + "ZPM",
            ChatFormatting.DARK_AQUA.toString() + "UV",
            ChatFormatting.DARK_RED.toString() + "UHV",
            ChatFormatting.GREEN.toString() + "UEV",
            ChatFormatting.DARK_GREEN.toString() + "UIV",
            ChatFormatting.YELLOW.toString() + "UXV",
            ChatFormatting.BLUE.toString() + ChatFormatting.BOLD + "OpV",
            ChatFormatting.RED.toString() + ChatFormatting.BOLD + "MAX"
        )
    }

    @Persisted
    private var buffer: NotifiableLaserContainer? = null
    private var laserListener: ISubscription? = null
    private var explosionSubs: TickableSubscription? = null
    private var maxEnergy: Long? = null

    @Persisted
    private var voltage: Long = 0

    @Persisted
    private var amps = 256

    @Persisted
    private var setTier = GTValues.IV

    init {
        this.voltage = GTValues.VEX[setTier]
        this.maxEnergy = voltage * 64L * amps
        this.buffer = NotifiableLaserContainer.receiverContainer(this, this.maxEnergy!!, voltage, amps.toLong())
    }


    override fun onUnload() {
        super.onUnload()
        if (laserListener != null) {
            laserListener!!.unsubscribe()
            laserListener = null
        }
    }

    override fun onLoad() {
        super.onLoad()
        laserListener = buffer!!.addChangedListener { this.addEngerySubscription() }
        addEngerySubscription()
    }

    protected fun addEngerySubscription() {
        explosionSubs = subscribeServerTick(explosionSubs) { this.addEng() }
    }

    protected fun addEng() {
        if (buffer!!.inputVoltage != voltage || buffer!!.inputAmperage != amps.toLong()) {
            maxEnergy = voltage * 64L * amps
            buffer!!.resetBasicInfo(maxEnergy!!, voltage, amps.toLong(), 0, 0)
            buffer!!.setEnergyStored(0)
        }
        if (buffer!!.getEnergyStored() < this.maxEnergy!!) {
            buffer!!.setEnergyStored(this.maxEnergy!!)
        }
    }

    override fun canShared(): Boolean {
        return false
    }

    override fun getFieldHolder(): ManagedFieldHolder {
        return MANAGED_FIELD_HOLDER
    }

    override fun createUI(entityPlayer: Player): ModularUI {
        return ModularUI(176, 136, this, entityPlayer)
            .background(GuiTextures.BACKGROUND)
            .widget(LabelWidget(7, 32, "gtceu.creative.energy.voltage"))
            .widget(
                TextFieldWidget(
                    9, 47, 152, 16, { voltage.toString() },
                    { value: String? ->
                        voltage = value!!.toLong()
                        setTier = GTUtil.getTierByVoltage(voltage).toInt()
                    }).setNumbersOnly(8192L, Long.Companion.MAX_VALUE)
            )
            .widget(LabelWidget(7, 74, "gtceu.creative.energy.amperage"))
            .widget(
                ButtonWidget(
                    7, 87, 20, 20,
                    GuiTextureGroup(ResourceBorderTexture.BUTTON_COMMON, TextTexture("-"))
                ) { cd: ClickData? -> amps = if (--amps == -1) 0 else amps }
            )
            .widget(
                TextFieldWidget(
                    31, 89, 114, 16, { amps.toString() },
                    { value: String? -> amps = value!!.toInt() }).setNumbersOnly(256, 67108864)
            )
            .widget(
                ButtonWidget(
                    149, 87, 20, 20,
                    GuiTextureGroup(ResourceBorderTexture.BUTTON_COMMON, TextTexture("+"))
                ) { cd: ClickData? ->
                    if (amps < Int.Companion.MAX_VALUE) {
                        amps++
                    }
                }
            )

            .widget(
                SelectorWidget(7, 7, 30, 20, Arrays.stream(VNF).toList(), -1)
                    .setOnChanged { tier: String? ->
                        setTier = ArrayUtils.indexOf(VNF, tier) + 5
                        voltage = GTValues.VEX[setTier]
                    }
                    .setSupplier { VNF[setTier - 5] }
                    .setButtonBackground(ResourceBorderTexture.BUTTON_COMMON)
                    .setBackground(ColorPattern.BLACK.rectTexture())
                    .setValue(VNF[setTier - 5])
            )
    }

    override fun getDataInfo(mode: PortableScannerBehavior.DisplayMode): MutableList<Component?> {
        if (mode == PortableScannerBehavior.DisplayMode.SHOW_ALL || mode == PortableScannerBehavior.DisplayMode.SHOW_ELECTRICAL_INFO) {
            return mutableListOf(
                Component.literal(
                    String.format("%d/%d EU", buffer!!.getEnergyStored(), buffer!!.energyCapacity)
                )
            )
        }
        return ArrayList<Component?>()
    }
}