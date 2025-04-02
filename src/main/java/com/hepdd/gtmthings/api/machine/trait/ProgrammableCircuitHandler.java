package com.hepdd.gtmthings.api.machine.trait;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.cover.CoverBehavior;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.SimpleTieredMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.api.transfer.item.CustomItemStackHandler;

import net.minecraft.world.item.ItemStack;

import com.hepdd.gtmthings.common.cover.ProgrammableCover;
import com.hepdd.gtmthings.common.item.VirtualItemProviderBehavior;
import com.hepdd.gtmthings.data.CustomItems;
import org.jetbrains.annotations.NotNull;

public class ProgrammableCircuitHandler extends NotifiableItemStackHandler {

    public ProgrammableCircuitHandler(Object machine) {
        super((MetaMachine) machine, 1, IO.IN, IO.IN, size -> new ItemStackHandler(size, machine));
    }

    private static class ItemStackHandler extends CustomItemStackHandler {

        private final Object machine;

        private ItemStackHandler(int size, Object machine) {
            super(size);
            this.machine = machine;
        }

        @Override
        public int getSlotLimit(int slot) {
            return 1;
        }

        @NotNull
        @Override
        public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            if (stack.is(CustomItems.VIRTUAL_ITEM_PROVIDER.get())) {
                boolean allow = true;
                if (machine instanceof SimpleTieredMachine tieredMachine) {
                    allow = false;
                    for (CoverBehavior cover : tieredMachine.getCoverContainer().getCovers()) {
                        if (cover instanceof ProgrammableCover) {
                            allow = true;
                            break;
                        }
                    }
                }
                if (allow) {
                    setStackInSlot(slot, VirtualItemProviderBehavior.getVirtualItem(stack));
                    return ItemStack.EMPTY;
                }
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
