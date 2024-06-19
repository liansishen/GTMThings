package com.hepdd.gtmthings.api.misc;

import com.lowdragmc.lowdraglib.misc.ItemStackTransfer;
import com.lowdragmc.lowdraglib.side.item.ItemTransferHelper;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class UnlimitedItemStackTransfer extends ItemStackTransfer {

    public UnlimitedItemStackTransfer() {
    }

    public UnlimitedItemStackTransfer(int size) {
        super(size);
    }

    public UnlimitedItemStackTransfer(NonNullList<ItemStack> stacks) {
        super(stacks);
    }

    public UnlimitedItemStackTransfer(ItemStack stack) {
        super(stack);
    }


    @Override
    @NotNull
    public ItemStack extractItem(int slot, int amount, boolean simulate, boolean notifyChanges) {
        if (amount == 0)
            return ItemStack.EMPTY;

        validateSlotIndex(slot);

        ItemStack existing = this.stacks.get(slot);

        if (existing.isEmpty())
            return ItemStack.EMPTY;

        if (existing.getCount() <= amount) {
            if (!simulate) {
                this.stacks.set(slot, ItemStack.EMPTY);
                if (notifyChanges) {
                    onContentsChanged(slot);
                }
                return existing;
            } else {
                return existing.copy();
            }
        } else {
            if (!simulate) {
                this.stacks.set(slot, ItemTransferHelper.copyStackWithSize(existing, existing.getCount() - amount));
                if (notifyChanges) {
                    onContentsChanged(slot);
                }
            }

            return ItemTransferHelper.copyStackWithSize(existing, amount);
        }
    }

    public UnlimitedItemStackTransfer copy() {
        var copy = new UnlimitedItemStackTransfer(getSlots());
        for (int i = 0; i < stacks.size(); i++) {
            copy.setStackInSlot(i, getStackInSlot(i));
        }
        return copy;
    }

    @Override
    public int getSlotLimit(int slot) {
        return Integer.MAX_VALUE;
    }

    @Override
    protected int getStackLimit(int slot, @NotNull ItemStack stack) {
        return Integer.MAX_VALUE;
    }
}
