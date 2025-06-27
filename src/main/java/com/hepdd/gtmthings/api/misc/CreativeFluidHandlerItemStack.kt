package com.hepdd.gtmthings.api.misc

import com.gregtechceu.gtceu.api.capability.IThermalFluidHandlerItemStack
import net.minecraft.world.item.ItemStack
import net.minecraftforge.fluids.FluidStack
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction
import net.minecraftforge.fluids.capability.templates.FluidHandlerItemStack
import kotlin.math.min

class CreativeFluidHandlerItemStack(container: ItemStack, capacity: Int, fluidStack: FluidStack?):FluidHandlerItemStack(container, capacity),IThermalFluidHandlerItemStack {

    init {
        this.fluid = fluidStack!!
    }

    override fun drain(resource: FluidStack, action: FluidAction?): FluidStack {
        if (resource.isFluidEqual(this.fluid)) {
            if (capacity == Int.Companion.MAX_VALUE) {
                return resource.copy()
            } else {
                val fluidStack = resource.copy()
                fluidStack.amount = min(resource.amount, capacity)
                return fluidStack
            }
        } else {
            return FluidStack.EMPTY
        }
    }

    override fun fill(resource: FluidStack, doFill: FluidAction?): Int {
        return resource.amount
    }

    override fun drain(maxDrain: Int, action: FluidAction?): FluidStack {
        val contained = this.fluid
        if (contained.isEmpty) return FluidStack.EMPTY
        val drained = contained.copy()
        drained.amount = min(maxDrain, capacity)
        return drained
    }

    override fun canFillFluidType(fluid: FluidStack?): Boolean {
        return fluid?.let { this.fluid.isFluidEqual(it) }!!
    }

    override fun getMaxFluidTemperature(): Int {
        return Int.Companion.MAX_VALUE
    }

    override fun isGasProof(): Boolean {
        return true
    }

    override fun isAcidProof(): Boolean {
        return true
    }

    override fun isCryoProof(): Boolean {
        return true
    }

    override fun isPlasmaProof(): Boolean {
        return true
    }
}