package com.hepdd.gtmthings.common.block.machine.multiblock.part

import net.minecraftforge.fluids.FluidStack

import com.gregtechceu.gtceu.api.GTValues
import com.gregtechceu.gtceu.api.capability.recipe.IO
import com.gregtechceu.gtceu.api.gui.GuiTextures
import com.gregtechceu.gtceu.api.gui.widget.PhantomFluidWidget
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity
import com.gregtechceu.gtceu.api.machine.MetaMachine
import com.gregtechceu.gtceu.api.machine.TickableSubscription
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IDistinctPart
import com.gregtechceu.gtceu.api.machine.multiblock.part.TieredIOPartMachine
import com.gregtechceu.gtceu.api.machine.trait.NotifiableFluidTank
import com.gregtechceu.gtceu.api.recipe.GTRecipe
import com.gregtechceu.gtceu.api.recipe.ingredient.FluidIngredient
import com.gregtechceu.gtceu.api.transfer.fluid.CustomFluidTank
import com.lowdragmc.lowdraglib.gui.widget.Widget
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder

import java.util.function.Consumer
import kotlin.math.sqrt

open class CreativeInputHatchPartMachine(holder: IMachineBlockEntity) :
    TieredIOPartMachine(holder, GTValues.MAX, IO.IN),
    IDistinctPart {
    companion object {
        @JvmStatic
        val MANAGED_FIELD_HOLDER: ManagedFieldHolder = ManagedFieldHolder(
            CreativeInputHatchPartMachine::class.java,
            TieredIOPartMachine.MANAGED_FIELD_HOLDER,
        )

        private const val SLOT_COUNT = 9
    }

    @Persisted
    private var tank: NotifiableFluidTank? = null
    private var slots = 0
    protected var autoIOSubs: TickableSubscription? = null
    private var fluidMap: MutableMap<Int?, FluidStack?>? = null

    @Persisted
    private val creativeTanks: Array<CustomFluidTank?>

    init {
        this.slots = SLOT_COUNT
        this.tank = createTank()
        this.fluidMap = HashMap<Int?, FluidStack?>()
        this.creativeTanks = arrayOfNulls<CustomFluidTank>(SLOT_COUNT)
        for (i in this.creativeTanks.indices) {
            this.creativeTanks[i] = CustomFluidTank(1)
        }
    }

    override fun getFieldHolder(): ManagedFieldHolder = MANAGED_FIELD_HOLDER

    protected fun createTank(): NotifiableFluidTank = InfinityFluidTank(this, SLOT_COUNT, Int.Companion.MAX_VALUE, IO.IN)

    override fun onLoad() {
        super.onLoad()
        for (i in 0..<SLOT_COUNT) {
            if (this.creativeTanks[i] != null && !this.creativeTanks[i]!!.getFluid().isEmpty) {
                fluidMap!!.put(i, this.creativeTanks[i]!!.getFluid())
            }
        }
        updateTankSubscription()
    }

    override fun onUnload() {
        super.onUnload()
    }

    protected fun updateTankSubscription() {
        if (!fluidMap!!.isEmpty()) {
            autoIOSubs = subscribeServerTick(autoIOSubs) { this.autoKeep() }
        } else if (autoIOSubs != null) {
            clearAll()
            autoIOSubs!!.unsubscribe()
            autoIOSubs = null
        }
    }

    protected fun autoKeep() {
        if (offsetTimer % 5 == 0L) {
            for (i in 0..<SLOT_COUNT) {
                if (fluidMap!!.containsKey(i)) {
                    val mFluid = this.creativeTanks[i]!!.getFluid().copy()
                    mFluid.amount = Int.Companion.MAX_VALUE
                    this.tank!!.setFluidInTank(i, mFluid)
                } else {
                    if (!this.tank!!.getFluidInTank(i).isEmpty) {
                        this.tank!!.setFluidInTank(i, FluidStack.EMPTY)
                    }
                }
            }
            updateTankSubscription()
        }
    }

    protected fun clearAll() {
        for (i in 0..<SLOT_COUNT) {
            if (!this.tank!!.getFluidInTank(i).isEmpty) {
                this.tank!!.setFluidInTank(i, FluidStack.EMPTY)
            }
        }
    }

    override fun setWorkingEnabled(workingEnabled: Boolean) {
        super.setWorkingEnabled(workingEnabled)
    }

    /**/
    // //////////////////////////////// */ // ********** GUI ***********//
    /**/
    // //////////////////////////////// */
    override fun createUIWidget(): Widget {
        var rowSize = sqrt(slots.toDouble()).toInt()
        var colSize = rowSize
        if (slots == 8) {
            rowSize = 4
            colSize = 2
        }

        val group = WidgetGroup(0, 0, 18 * rowSize + 16, 18 * colSize + 16)
        val container = WidgetGroup(4, 4, 18 * rowSize + 8, 18 * colSize + 8)

        var index = 0
        for (y in 0..<colSize) {
            for (x in 0..<rowSize) {
                val finalIndex = index++
                container.addWidget(
                    PhantomFluidWidget(
                        this.creativeTanks[finalIndex],
                        finalIndex,
                        4 + x * 18,
                        4 + y * 18,
                        18,
                        18,
                        { this.creativeTanks[finalIndex]!!.getFluid() },
                        (
                            Consumer { fluid: FluidStack? ->
                                if (fluid!!.isEmpty) {
                                    this.creativeTanks[finalIndex]!!.setFluid(fluid)
                                    if (!fluidMap!!.isEmpty() && fluidMap!!.containsKey(finalIndex)) {
                                        fluidMap!!.remove(
                                            finalIndex,
                                        )
                                    }
                                    updateTankSubscription()
                                    return@Consumer
                                }
                                for (entry in fluidMap!!.entries) {
                                    val i = entry.key as Int
                                    val f = entry.value as FluidStack
                                    if (i != finalIndex && f.fluid === fluid.fluid) {
                                        return@Consumer
                                    } else if (i == finalIndex && f.fluid !== fluid.fluid) {
                                        setFluid(finalIndex, fluid)
                                        updateTankSubscription()
                                        return@Consumer
                                    }
                                }
                                setFluid(finalIndex, fluid)
                                updateTankSubscription()
                            }
                            ),
                    ).setShowAmount(false).setBackground(GuiTextures.FLUID_SLOT),
                )
            }
        }

        container.setBackground(GuiTextures.BACKGROUND_INVERSE)
        group.addWidget(container)

        return group
    }

    private fun setFluid(index: Int, fs: FluidStack) {
        val newFluid = fs.copy()
        newFluid.amount = 1
        this.creativeTanks[index]!!.setFluid(newFluid)
        if (fluidMap!!.containsKey(index)) {
            fluidMap!!.replace(index, fs)
        } else {
            fluidMap!!.put(index, fs)
        }
    }

    override fun isDistinct(): Boolean = this.tank!!.isDistinct()

    override fun setDistinct(isDistinct: Boolean) {
        this.tank!!.setDistinct(isDistinct)
    }

    open class InfinityFluidTank(machine: MetaMachine, slots: Int, capacity: Int, io: IO) : NotifiableFluidTank(machine, slots, capacity, io) {
        override fun handleRecipeInner(io: IO, recipe: GTRecipe, left: MutableList<FluidIngredient?>, simulate: Boolean): MutableList<FluidIngredient?>? = super.handleRecipeInner(io, recipe, left, true)
    }
}
