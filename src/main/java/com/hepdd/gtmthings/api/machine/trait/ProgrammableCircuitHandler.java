package com.hepdd.gtmthings.api.machine.trait;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.api.transfer.item.CustomItemStackHandler;

import net.minecraft.world.item.ItemStack;

import com.hepdd.gtmthings.common.item.VirtualItemProviderBehavior;
import com.hepdd.gtmthings.data.CustomItems;
import org.jetbrains.annotations.NotNull;

public class ProgrammableCircuitHandler extends NotifiableItemStackHandler {

    public ProgrammableCircuitHandler(MetaMachine machine) {
        super(machine, 1, IO.IN, IO.IN, ItemStackHandler::new);
    }

    private static class ItemStackHandler extends CustomItemStackHandler {

        private ItemStackHandler(int size) {
            super(size);
        }

        @Override
        public int getSlotLimit(int slot) {
            return 1;
        }

        @NotNull
        @Override
        public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            if (stack.is(CustomItems.VIRTUAL_ITEM_PROVIDER.get())) {
                setStackInSlot(slot, VirtualItemProviderBehavior.getVirtualItem(stack));
                return ItemStack.EMPTY;
            }
            return stack;
        }

        @NotNull
        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            return simulate ? super.extractItem(slot, amount, true) : ItemStack.EMPTY;
        }
    }
}
