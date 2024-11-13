package com.hepdd.gtmthings.api.transfer;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;

import java.util.function.Predicate;

import javax.annotation.Nullable;

import static com.lowdragmc.lowdraglib.side.item.forge.ItemTransferHelperImpl.insertToEmpty;

public class UnlimitItemTransferHelper {

    public static void exportToTarget(IItemHandler source, int maxAmount, Predicate<ItemStack> predicate, Level level, BlockPos pos, @Nullable Direction direction) {
        if (level.getBlockState(pos).hasBlockEntity()) {
            var blockEntity = level.getBlockEntity(pos);
            if (blockEntity != null) {
                var cap = blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER, direction).resolve();
                if (cap.isPresent()) {
                    var target = cap.get();
                    for (int srcIndex = 0; srcIndex < source.getSlots(); srcIndex++) {
                        while (true) {
                            ItemStack sourceStack = source.extractItem(srcIndex, Integer.MAX_VALUE, true);
                            if (sourceStack.isEmpty() || !predicate.test(sourceStack)) {
                                break;
                            }
                            ItemStack remainder = insertItem(target, sourceStack, true);
                            int amountToInsert = sourceStack.getCount() - remainder.getCount();
                            if (amountToInsert > 0) {
                                sourceStack = source.extractItem(srcIndex, Math.min(maxAmount, amountToInsert), false);
                                insertItem(target, sourceStack, false);
                                maxAmount -= Math.min(maxAmount, amountToInsert);
                                if (maxAmount <= 0) return;
                            } else {
                                break;
                            }
                        }
                    }
                }
            }
        }
    }

    public static ItemStack insertItem(IItemHandler handler, ItemStack stack, boolean simulate) {
        if (handler == null || stack.isEmpty()) {
            return stack;
        }
        if (!stack.isStackable()) {
            return insertToEmpty(handler, stack, simulate);
        }

        IntList emptySlots = new IntArrayList();
        int slots = handler.getSlots();

        for (int i = 0; i < slots; i++) {
            ItemStack slotStack = handler.getStackInSlot(i);
            if (slotStack.isEmpty()) {
                emptySlots.add(i);
            }
            if (ItemHandlerHelper.canItemStacksStackRelaxed(stack, slotStack)) {
                stack = handler.insertItem(i, stack, simulate);
                if (stack.isEmpty()) {
                    return ItemStack.EMPTY;
                }
            }
        }

        for (int slot : emptySlots) {
            stack = handler.insertItem(slot, stack, simulate);
            if (stack.isEmpty()) {
                return ItemStack.EMPTY;
            }
        }
        return stack;
    }
}
