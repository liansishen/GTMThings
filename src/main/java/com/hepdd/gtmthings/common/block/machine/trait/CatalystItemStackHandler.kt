package com.hepdd.gtmthings.common.block.machine.trait

import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.crafting.Ingredient

import com.gregtechceu.gtceu.api.capability.recipe.IO
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability
import com.gregtechceu.gtceu.api.machine.MetaMachine
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler
import com.gregtechceu.gtceu.api.recipe.GTRecipe
import com.gregtechceu.gtceu.api.transfer.item.CustomItemStackHandler
import com.gregtechceu.gtceu.utils.ItemStackHashStrategy
import it.unimi.dsi.fastutil.objects.Object2IntMap
import it.unimi.dsi.fastutil.objects.Object2IntOpenCustomHashMap

import java.util.function.IntFunction

class CatalystItemStackHandler(machine: MetaMachine, slots: Int, handlerIO: IO, capabilityIO: IO) : NotifiableItemStackHandler(machine, slots, handlerIO, capabilityIO, IntFunction { size: Int -> CustomItemStackHandler(size) }) {

    constructor(machine: MetaMachine, slots: Int, handlerIO: IO) : this(machine, slots, handlerIO, handlerIO)

    override fun handleRecipeInner(io: IO?, recipe: GTRecipe, left: MutableList<Ingredient?>?, simulate: Boolean): MutableList<Ingredient?>? {
        val map: Object2IntMap<ItemStack?> =
            Object2IntOpenCustomHashMap<ItemStack?>(ItemStackHashStrategy.comparingAllButCount())
        for (i in 0..<storage.slots) {
            map.putIfAbsent(storage.getStackInSlot(i), 1)
        }
        for (content in recipe.getInputContents(ItemRecipeCapability.CAP)) {
            val ingredient = content.getContent() as Ingredient
            for (`is` in map.keys) {
                if (ingredient.test(`is`) && content.chance > 0) return left
            }
        }
        return handleRecipe(io, recipe, left, simulate, this.handlerIO, storage)
    }
}
