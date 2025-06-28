package com.hepdd.gtmthings.common.block.machine.trait

import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability
import com.gregtechceu.gtceu.api.capability.recipe.IO
import com.gregtechceu.gtceu.api.machine.MetaMachine
import com.gregtechceu.gtceu.api.machine.trait.NotifiableFluidTank
import com.gregtechceu.gtceu.api.recipe.GTRecipe
import com.gregtechceu.gtceu.api.recipe.ingredient.FluidIngredient
import com.gregtechceu.gtceu.utils.FluidStackHashStrategy
import it.unimi.dsi.fastutil.objects.Object2IntMap
import it.unimi.dsi.fastutil.objects.Object2IntOpenCustomHashMap
import net.minecraftforge.fluids.FluidStack

class CatalystFluidStackHandler(machine: MetaMachine, slots: Int, capacity: Int, io: IO?, capabilityIO: IO?):NotifiableFluidTank(machine, slots, capacity, io, capabilityIO) {

    override fun handleRecipeInner(
        io: IO?,
        recipe: GTRecipe,
        left: MutableList<FluidIngredient?>?,
        simulate: Boolean
    ): MutableList<FluidIngredient?>? {
        val map: Object2IntMap<FluidStack?> =
            Object2IntOpenCustomHashMap<FluidStack?>(FluidStackHashStrategy.comparingAllButAmount())
        val storages = getStorages()
        for (storage in storages) {
            map.putIfAbsent(storage.getFluid(), 1)
        }

        for (content in recipe.getInputContents(FluidRecipeCapability.CAP)) {
            val ingredient = content.getContent() as FluidIngredient
            for (`is` in map.keys) {
                if (ingredient.test(`is`) && content.chance > 0) return left
            }
        }

        return super.handleRecipeInner(io, recipe, left, simulate)
    }
}