package com.hepdd.gtmthings.common.block.machine.multiblock.part

import net.minecraft.core.Direction
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.player.Player

import com.gregtechceu.gtceu.api.GTValues
import com.gregtechceu.gtceu.api.capability.IEnergyContainer
import com.gregtechceu.gtceu.api.capability.recipe.IO
import com.gregtechceu.gtceu.api.gui.GuiTextures
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity
import com.gregtechceu.gtceu.api.machine.MetaMachine
import com.gregtechceu.gtceu.api.machine.feature.IDataInfoProvider
import com.gregtechceu.gtceu.api.machine.multiblock.part.TieredIOPartMachine
import com.gregtechceu.gtceu.api.machine.trait.NotifiableEnergyContainer
import com.gregtechceu.gtceu.api.pattern.MultiblockWorldSavedData
import com.gregtechceu.gtceu.api.recipe.GTRecipe
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
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder
import org.apache.commons.lang3.ArrayUtils

import java.util.*
import kotlin.math.min

open class CreativeEnergyHatchPartMachine(holder: IMachineBlockEntity) :
    TieredIOPartMachine(holder, GTValues.MAX, IO.IN),
    IDataInfoProvider {

    companion object {
        @JvmStatic
        val MANAGED_FIELD_HOLDER: ManagedFieldHolder =
            ManagedFieldHolder(CreativeEnergyHatchPartMachine::class.java, TieredIOPartMachine.MANAGED_FIELD_HOLDER)
    }

    @Persisted
    var energyContainer: NotifiableEnergyContainer? = null

    @Persisted
    private var maxEnergy: Long = 0

    @Persisted
    private var voltage: Long = 0

    @Persisted
    private var amps = 1

    @Persisted
    private var setTier = 0

    init {
        this.energyContainer = createEnergyContainer()
    }

    override fun getFieldHolder(): ManagedFieldHolder = MANAGED_FIELD_HOLDER

    protected fun createEnergyContainer(): NotifiableEnergyContainer {
        this.voltage = GTValues.VEX[setTier]
        this.maxEnergy = this.voltage * this.amps
        val container = InfinityEnergyContainer(this, this.maxEnergy, this.voltage, this.amps.toLong(), 0L, 0L)
        return container
    }

    override fun loadCustomPersistedData(tag: CompoundTag) {
        super.loadCustomPersistedData(tag)
        updateEnergyContainer()
    }

    override fun createUI(entityPlayer: Player): ModularUI = ModularUI(176, 136, this, entityPlayer)
        .background(GuiTextures.BACKGROUND)
        .widget(LabelWidget(7, 32, "gtceu.creative.energy.voltage"))
        .widget(
            TextFieldWidget(
                9,
                47,
                152,
                16,
                { voltage.toString() },
                { value: String? ->
                    setVoltage(value!!.toLong())
                    setTier = GTUtil.getTierByVoltage(this.voltage).toInt()
                },
            ).setNumbersOnly(8L, Long.Companion.MAX_VALUE),
        )
        .widget(LabelWidget(7, 74, "gtceu.creative.energy.amperage"))
        .widget(
            ButtonWidget(
                7,
                87,
                20,
                20,
                GuiTextureGroup(ResourceBorderTexture.BUTTON_COMMON, TextTexture("-")),
            ) { cd: ClickData? -> setAmps(if (--amps == -1) 0 else amps) },
        )
        .widget(
            TextFieldWidget(
                31,
                89,
                114,
                16,
                { amps.toString() },
                { value: String? -> setAmps(value!!.toInt()) },
            ).setNumbersOnly(1, 67108864),
        )
        .widget(
            ButtonWidget(
                149,
                87,
                20,
                20,
                GuiTextureGroup(ResourceBorderTexture.BUTTON_COMMON, TextTexture("+")),
            ) { cd: ClickData? ->
                if (amps < Int.Companion.MAX_VALUE) {
                    setAmps(++amps)
                }
            },
        )
        .widget(
            SelectorWidget(7, 7, 50, 20, Arrays.stream(GTValues.VNF).toList(), -1)
                .setOnChanged { tier: String? ->
                    setTier = ArrayUtils.indexOf(GTValues.VNF, tier)
                    setVoltage(GTValues.VEX[setTier])
                }
                .setSupplier { GTValues.VNF[setTier] }
                .setButtonBackground(ResourceBorderTexture.BUTTON_COMMON)
                .setBackground(ColorPattern.BLACK.rectTexture())
                .setValue(GTValues.VNF[setTier]),
        )

    private fun setVoltage(voltage: Long) {
        this.voltage = voltage
        this.maxEnergy = this.voltage * this.amps
        updateMachine()
    }

    private fun setAmps(amps: Int) {
        this.amps = amps
        this.maxEnergy = this.voltage * this.amps
        updateMachine()
    }

    private fun updateEnergyContainer() {
        this.energyContainer!!.resetBasicInfo(this.maxEnergy, this.voltage, this.amps.toLong(), 0, 0)
        this.energyContainer!!.setEnergyStored(this.maxEnergy)
    }

    private fun updateMachine() {
        updateEnergyContainer()
        if (level is ServerLevel) {
            val serverLevel = level as ServerLevel
            serverLevel.server.execute {
                for (c in getControllers()) {
                    if (c.isFormed) {
                        c.patternLock.lock()
                        try {
                            c.onStructureInvalid()
                            val mwsd = MultiblockWorldSavedData.getOrCreate(serverLevel)
                            mwsd.removeMapping(c.multiblockState)
                            mwsd.addAsyncLogic(c)
                        } finally {
                            c.patternLock.unlock()
                        }
                    }
                }
            }
        }
    }

    /**/
    // //////////////////////////////// */ // ********** Misc **********//
    /**/
    // //////////////////////////////// */
    override fun tintColor(index: Int): Int {
        if (index == 2) {
            return GTValues.VC[getTier()]
        }
        return super.tintColor(index)
    }

    override fun getDataInfo(mode: PortableScannerBehavior.DisplayMode): MutableList<Component?> {
        if (mode == PortableScannerBehavior.DisplayMode.SHOW_ALL || mode == PortableScannerBehavior.DisplayMode.SHOW_ELECTRICAL_INFO) {
            return mutableListOf(
                Component.literal(
                    String.format(
                        "%d/%d EU",
                        energyContainer!!.getEnergyStored(),
                        energyContainer!!.energyCapacity,
                    ),
                ),
            )
        }
        return ArrayList<Component?>()
    }

    open class InfinityEnergyContainer(machine: MetaMachine, maxCapacity: Long, maxInputVoltage: Long, maxInputAmperage: Long, maxOutputVoltage: Long, maxOutputAmperage: Long) :
        NotifiableEnergyContainer(
            machine,
            maxCapacity,
            maxInputVoltage,
            maxInputAmperage,
            maxOutputVoltage,
            maxOutputAmperage,
        ) {
        override fun handleRecipeInner(io: IO, recipe: GTRecipe, left: MutableList<Long?>, simulate: Boolean): MutableList<Long?>? {
            val capability: IEnergyContainer = this
            var sum: Long = left.stream().reduce(0L) { a: Long?, b: Long? -> java.lang.Long.sum(a!!, b!!) }!!
            if (io == IO.IN) {
                val canOutput = capability.energyStored
                if (!simulate) {
                    capability.addEnergy(-min(canOutput, sum))
                }
                sum = sum - canOutput
            } else if (io == IO.OUT) {
                val canInput = capability.getEnergyCapacity() - capability.getEnergyStored()
                if (!simulate) {
                    capability.addEnergy(min(canInput, sum))
                }
                sum = sum - canInput
            }
            return (if (sum <= 0) null else mutableListOf(sum))
        }

        override fun changeEnergy(energyToAdd: Long): Long {
            val oldEnergyStored = getEnergyStored()
            var newEnergyStored =
                if (energyCapacity - oldEnergyStored < energyToAdd) energyCapacity else (oldEnergyStored + energyToAdd)
            if (newEnergyStored < 0) newEnergyStored = 0
            return newEnergyStored - oldEnergyStored
        }

        override fun checkOutputSubscription() {}

        override fun serverTick() {}

        override fun acceptEnergyFromNetwork(side: Direction, voltage: Long, amperage: Long): Long = 0

        override fun outputsEnergy(side: Direction): Boolean = false

        override fun inputsEnergy(side: Direction): Boolean = false
    }
}
