package com.hepdd.gtmthings.common.block.machine.trait;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.utils.ItemStackHashStrategy;
import com.lowdragmc.lowdraglib.misc.ItemStackTransfer;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenCustomHashMap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class CatalystItemStackHandler extends NotifiableItemStackHandler {

    public CatalystItemStackHandler(MetaMachine machine, int slots, @NotNull IO handlerIO, @NotNull IO capabilityIO) {
        super(machine, slots, handlerIO, capabilityIO, ItemStackTransfer::new);
    }

    public CatalystItemStackHandler(MetaMachine machine, int slots, @NotNull IO handlerIO) {
        this(machine, slots, handlerIO, handlerIO);
    }

    @Override
    public List<Ingredient> handleRecipeInner(IO io, GTRecipe recipe, List<Ingredient> left, @Nullable String slotName,
                                              boolean simulate) {
        Object2IntMap<ItemStack> map = new Object2IntOpenCustomHashMap<>(ItemStackHashStrategy.comparingAllButCount());
        for (int i = 0; i < storage.getSlots(); i++) {
            map.putIfAbsent(storage.getStackInSlot(i),1);
        }
        for (Content content : recipe.getInputContents(ItemRecipeCapability.CAP)){
            var ingredient = (Ingredient) content.getContent();
            for (ItemStack is:map.keySet()) {
                if (ingredient.test(is) && content.chance > 0) return left;
            }
        }
        return handleIngredient(io, recipe, left, simulate, this.handlerIO, storage);
    }
}
