package com.hepdd.gtmthings.api.transfer

import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.minecraftforge.common.capabilities.ForgeCapabilities
import net.minecraftforge.items.IItemHandler
import net.minecraftforge.items.ItemHandlerHelper

import com.lowdragmc.lowdraglib.side.item.forge.ItemTransferHelperImpl
import it.unimi.dsi.fastutil.ints.IntArrayList
import it.unimi.dsi.fastutil.ints.IntList

import java.util.function.Predicate
import kotlin.math.min

open class UnlimitItemTransferHelper {
    companion object {

        @JvmStatic
        fun exportToTarget(source: IItemHandler, maxAmount: Int, predicate: Predicate<ItemStack?>, level: Level, pos: BlockPos, direction: Direction?) {
            var maxAmount = maxAmount
            if (level.getBlockState(pos).hasBlockEntity()) {
                val blockEntity = level.getBlockEntity(pos)
                if (blockEntity != null) {
                    val cap =
                        blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER, direction).resolve()
                    if (cap.isPresent) {
                        val target = cap.get()
                        for (srcIndex in 0..<source.slots) {
                            while (true) {
                                var sourceStack = source.extractItem(srcIndex, Int.Companion.MAX_VALUE, true)
                                if (sourceStack.isEmpty || !predicate.test(sourceStack)) {
                                    break
                                }
                                val remainder = insertItem(target, sourceStack, true)
                                val amountToInsert = sourceStack.count - remainder.count
                                if (amountToInsert > 0) {
                                    sourceStack = source.extractItem(srcIndex, min(maxAmount, amountToInsert), false)
                                    insertItem(target, sourceStack, false)
                                    maxAmount -= min(maxAmount, amountToInsert)
                                    if (maxAmount <= 0) return
                                } else {
                                    break
                                }
                            }
                        }
                    }
                }
            }
        }

        @JvmStatic
        fun insertItem(handler: IItemHandler?, stack: ItemStack, simulate: Boolean): ItemStack {
            var stack = stack
            if (handler == null || stack.isEmpty) {
                return stack
            }
            if (!stack.isStackable) {
                return ItemTransferHelperImpl.insertToEmpty(handler, stack, simulate)
            }

            val emptySlots: IntList = IntArrayList()
            val slots = handler.slots

            for (i in 0..<slots) {
                val slotStack = handler.getStackInSlot(i)
                if (slotStack.isEmpty) {
                    emptySlots.add(i)
                }
                if (ItemHandlerHelper.canItemStacksStackRelaxed(stack, slotStack)) {
                    stack = handler.insertItem(i, stack, simulate)
                    if (stack.isEmpty) {
                        return ItemStack.EMPTY
                    }
                }
            }

            emptySlots.forEach { slot ->
                stack = handler.insertItem(slot, stack, simulate)
                if (stack.isEmpty) {
                    return ItemStack.EMPTY
                }
            }
            return stack
        }
    }
}
