package com.hepdd.gtmthings.api.machine.trait;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.ingredient.SizedIngredient;
import com.gregtechceu.gtceu.api.transfer.item.CustomItemStackHandler;
import com.gregtechceu.gtceu.integration.ae2.utils.KeyStorage;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import appeng.api.stacks.AEItemKey;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class InaccessibleInfiniteHandler extends NotifiableItemStackHandler {

    private final ItemStackHandlerDelegate delegate;

    public InaccessibleInfiniteHandler(MetaMachine holder, KeyStorage internalBuffer) {
        super(holder, 1, IO.OUT, IO.NONE, i -> new ItemStackHandlerDelegate(internalBuffer));
        internalBuffer.setOnContentsChanged(this::onContentsChanged);
        delegate = ((ItemStackHandlerDelegate) storage);
    }

    public static ItemStack getFirstSized(SizedIngredient sizedIngredient) {
        Ingredient inner = sizedIngredient.getInner();
        if (inner instanceof SizedIngredient ingredient) {
            return getFirstSized(ingredient);
        }
        return getFirst(inner);
    }

    public static ItemStack getFirst(Ingredient ingredient) {
        for (var stack : ingredient.getItems()) {
            if (!stack.isEmpty()) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public List<Ingredient> handleRecipe(IO io, GTRecipe recipe, List<?> left, boolean simulate) {
        if (!simulate && io == IO.OUT) {
            for (Object ingredient : left) {
                if (((Ingredient) ingredient).isEmpty()) continue;
                ItemStack item;
                int count;
                if (ingredient instanceof SizedIngredient sizedIngredient) {
                    item = getFirstSized(sizedIngredient);
                    count = sizedIngredient.getAmount();
                } else {
                    item = getFirst((Ingredient) ingredient);
                    count = item.getCount();
                }
                if (item.isEmpty()) continue;
                delegate.insertItem(item, count);
            }
            delegate.internalBuffer.onChanged();
            return null;
        }
        return null;
    }

    @Override
    public List<Object> getContents() {
        return Collections.emptyList();
    }

    @Override
    public double getTotalContentAmount() {
        return 0;
    }

    @Override
    public boolean isEmpty() {
        return true;
    }

    private static class ItemStackHandlerDelegate extends CustomItemStackHandler {

        private final KeyStorage internalBuffer;

        private ItemStackHandlerDelegate(KeyStorage internalBuffer) {
            super();
            this.internalBuffer = internalBuffer;
        }

        private void insertItem(ItemStack stack, int count) {
            var key = AEItemKey.of(stack);
            long oldValue = internalBuffer.storage.getOrDefault(key, 0);
            long changeValue = Math.min(Long.MAX_VALUE - oldValue, count);
            internalBuffer.storage.put(key, oldValue + changeValue);
        }

        @Override
        public int getSlots() {
            return Short.MAX_VALUE;
        }

        @Override
        public int getSlotLimit(int slot) {
            return Integer.MAX_VALUE;
        }

        @Override
        public @NotNull ItemStack getStackInSlot(int slot) {
            return ItemStack.EMPTY;
        }

        @Override
        public void setStackInSlot(int slot, @NotNull ItemStack stack) {}

        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            var key = AEItemKey.of(stack);
            int count = stack.getCount();
            long oldValue = internalBuffer.storage.getOrDefault(key, 0);
            long changeValue = Math.min(Long.MAX_VALUE - oldValue, count);
            if (!simulate) {
                internalBuffer.storage.put(key, oldValue + changeValue);
            } else if (count != changeValue) {
                return stack.copyWithCount((int) (count - changeValue));
            }
            return ItemStack.EMPTY;
        }
    }

    @Override
    public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
        return ItemStack.EMPTY;
    }
}
