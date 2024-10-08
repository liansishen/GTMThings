package com.hepdd.gtmthings.common.block.machine.trait;

import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableFluidTank;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.recipe.ingredient.FluidIngredient;
import com.gregtechceu.gtceu.utils.FluidStackHashStrategy;
import com.lowdragmc.lowdraglib.misc.FluidStorage;
import com.lowdragmc.lowdraglib.side.fluid.FluidStack;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenCustomHashMap;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * @author EasterFG on 2024/10/3
 */
public class CatalystFluidStackHandler extends NotifiableFluidTank {
    public CatalystFluidStackHandler(MetaMachine machine, int slots, long capacity, IO io, IO capabilityIO) {
        super(machine, slots, capacity, io, capabilityIO);
    }

    @Override
    public List<FluidIngredient> handleRecipeInner(IO io, GTRecipe recipe, List<FluidIngredient> left, @Nullable String slotName, boolean simulate) {
        Object2IntMap<FluidStack> map = new Object2IntOpenCustomHashMap<>(FluidStackHashStrategy.comparingAllButAmount());
        FluidStorage[] storages = getStorages();
        for (FluidStorage storage : storages) {
            map.putIfAbsent(storage.getFluid(), 1);
        }

        for (Content content : recipe.getInputContents(FluidRecipeCapability.CAP)) {
            var ingredient = (FluidIngredient) content.getContent();
            for (FluidStack is : map.keySet()) {
                if (ingredient.test(is) && content.chance > 0) return left;
            }
        }

        return handleIngredient(io, recipe, left, simulate, this.handlerIO, storages);
    }
}
