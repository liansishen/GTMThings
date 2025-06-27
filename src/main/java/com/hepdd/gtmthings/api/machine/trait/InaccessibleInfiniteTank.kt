package com.hepdd.gtmthings.api.machine.trait

import appeng.api.stacks.AEFluidKey
import com.gregtechceu.gtceu.api.capability.recipe.IO
import com.gregtechceu.gtceu.api.machine.MetaMachine
import com.gregtechceu.gtceu.api.machine.trait.NotifiableFluidTank
import com.gregtechceu.gtceu.api.recipe.GTRecipe
import com.gregtechceu.gtceu.api.recipe.ingredient.FluidIngredient
import com.gregtechceu.gtceu.api.transfer.fluid.CustomFluidTank
import com.gregtechceu.gtceu.integration.ae2.utils.KeyStorage
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.level.material.Fluid
import net.minecraftforge.fluids.FluidStack
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction
import kotlin.math.min

class InaccessibleInfiniteTank(holder: MetaMachine, internalBuffer: KeyStorage): NotifiableFluidTank(holder,
    mutableListOf<CustomFluidTank?>(FluidStorageDelegate(internalBuffer)), IO.OUT, IO.NONE) {
    private var storage: FluidStorageDelegate? = null

    init {
        internalBuffer.setOnContentsChanged { this.onContentsChanged() }
        storage = getStorages()[0] as FluidStorageDelegate
        allowSameFluids = true
    }

    fun getFirst(fluidIngredient: FluidIngredient): Fluid? {
        for (value in fluidIngredient.values) {
            for (fluid in value.fluids) {
                return fluid
            }
        }
        return null
    }

    override fun handleRecipe(
        io: IO?,
        recipe: GTRecipe?,
        left: MutableList<*>,
        simulate: Boolean
    ): MutableList<FluidIngredient?>? {
        if (!simulate && io == IO.OUT) {
            for (ingredient in left) {
                if ((ingredient as FluidIngredient).isEmpty) continue
                val fluid: Fluid? = getFirst(ingredient)
                if (fluid != null) {
                    storage!!.fill(fluid, ingredient.amount, ingredient.nbt)
                }
            }
            storage?.internalBuffer?.onChanged()
            return null
        }
        return null
    }

    override fun getTanks(): Int {
        return 128
    }

    override fun getContents(): MutableList<Any?> {
        return mutableListOf()
    }

    override fun getTotalContentAmount(): Double {
        return 0.0
    }

    override fun isEmpty(): Boolean {
        return true
    }

    override fun getFluidInTank(tank: Int): FluidStack {
        return FluidStack.EMPTY
    }

    override fun setFluidInTank(tank: Int, fluidStack: FluidStack) {}

    override fun getTankCapacity(tank: Int): Int {
        return Int.Companion.MAX_VALUE
    }

    override fun isFluidValid(tank: Int, stack: FluidStack): Boolean {
        return true
    }

    override fun handleRecipeInner(
        io: IO?,
        recipe: GTRecipe?,
        left: MutableList<FluidIngredient>,
        simulate: Boolean
    ): MutableList<FluidIngredient>? {
        if (io != IO.OUT) return left
        val action = if (simulate) FluidAction.SIMULATE else FluidAction.EXECUTE
        val it: MutableIterator<FluidIngredient?> = left.iterator()
        while (it.hasNext()) {
            val ingredient: FluidIngredient = it.next()!!
            if (ingredient.isEmpty) {
                it.remove()
                continue
            }

            val fluids = ingredient.getStacks()
            if (fluids.size == 0 || fluids[0]!!.isEmpty) {
                it.remove()
                continue
            }

            val output: FluidStack = fluids[0]!!
            ingredient.shrink(storage!!.fill(output, action))
            if (ingredient.amount <= 0) it.remove()
        }
        return if (left.isEmpty()) null else left
    }

    open class FluidStorageDelegate(val internalBuffer: KeyStorage) : CustomFluidTank(0) {
        fun fill(fluid: Fluid?, amount: Int, tag: CompoundTag?) {
            val key = AEFluidKey.of(fluid, tag)
            val oldValue = internalBuffer.storage.getOrDefault(key, 0)
            val changeValue = min(Long.Companion.MAX_VALUE - oldValue, amount.toLong())
            if (changeValue > 0) {
                internalBuffer.storage.put(key, oldValue + changeValue)
            }
        }

        override fun getCapacity(): Int {
            return Int.Companion.MAX_VALUE
        }

        override fun setFluid(fluid: FluidStack?) {}

        override fun fill(resource: FluidStack, action: FluidAction): Int {
            val key = AEFluidKey.of(resource.fluid, resource.tag)
            val amount = resource.amount
            val oldValue = internalBuffer.storage.getOrDefault(key, 0)
            val changeValue = min(Long.Companion.MAX_VALUE - oldValue, amount.toLong())
            if (changeValue > 0 && action.execute()) {
                internalBuffer.storage.put(key, oldValue + changeValue)
            }
            return changeValue.toInt()
        }

        override fun supportsFill(tank: Int): Boolean {
            return false
        }

        override fun supportsDrain(tank: Int): Boolean {
            return false
        }
    }
}