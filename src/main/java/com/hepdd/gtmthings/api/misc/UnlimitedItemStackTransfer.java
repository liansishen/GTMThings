package com.hepdd.gtmthings.api.misc;

import com.lowdragmc.lowdraglib.misc.ItemStackTransfer;
import com.lowdragmc.lowdraglib.side.item.ItemTransferHelper;
import lombok.Setter;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.function.Function;

public class UnlimitedItemStackTransfer extends ItemStackTransfer {

    public UnlimitedItemStackTransfer(int size) {
        super(size);
    }

    public UnlimitedItemStackTransfer(NonNullList<ItemStack> stacks) {
        super(stacks);
    }

    public UnlimitedItemStackTransfer(ItemStack stack) {
        super(stack);
    }

    @Setter
    private Function<ItemStack, Boolean> filter;

    @Override
    public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
        return filter == null || filter.apply(stack);
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

        int toExtract = Math.min(amount, getSlotLimit(slot));

        if (existing.getCount() <= toExtract) {
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
                this.stacks.set(slot, ItemTransferHelper.copyStackWithSize(existing, existing.getCount() - toExtract));
                if (notifyChanges) {
                    onContentsChanged(slot);
                }
            }

             return ItemTransferHelper.copyStackWithSize(existing, toExtract);
        }
    }

    @Override
    public UnlimitedItemStackTransfer copy() {
        var copiedStack = NonNullList.withSize(stacks.size(),ItemStack.EMPTY);
        for (int i = 0; i < stacks.size(); i++) {
            copiedStack.set(i, stacks.get(i).copy());
        }
        var copied = new UnlimitedItemStackTransfer(copiedStack);
        copied.setFilter(filter);
        return copied;
    }

    @Override
    public int getSlotLimit(int slot) {
        return Integer.MAX_VALUE;
    }

    @Override
    protected int getStackLimit(int slot, @NotNull ItemStack stack) {
        return Integer.MAX_VALUE;
    }

    @Override
    public CompoundTag serializeNBT() {
        ListTag nbtTagList = new ListTag();
        for (int i = 0; i < stacks.size(); i++) {
            if (!stacks.get(i).isEmpty()) {
                CompoundTag itemTag = new CompoundTag();
                itemTag.putInt("Slot", i);
                var is = stacks.get(i).copy();
                itemTag.putInt("realCount",is.getCount());
                is.setCount(1);
                is.save(itemTag);
                nbtTagList.add(itemTag);
            }
        }
        CompoundTag nbt = new CompoundTag();
        nbt.put("Items", nbtTagList);
        nbt.putInt("Size", stacks.size());
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        setSize(nbt.contains("Size", Tag.TAG_INT) ? nbt.getInt("Size") : stacks.size());
        ListTag tagList = nbt.getList("Items", Tag.TAG_COMPOUND);
        for (int i = 0; i < tagList.size(); i++) {
            CompoundTag itemTags = tagList.getCompound(i);
            int slot = itemTags.getInt("Slot");

            if (slot >= 0 && slot < stacks.size()) {
                var is = ItemStack.of(itemTags).copy();
                is.setCount(itemTags.getInt("realCount"));
                stacks.set(slot, is);
            }
        }
        onLoad();
    }
}
