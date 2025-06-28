package com.hepdd.gtmthings.api.misc

import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.nbt.Tag
import net.minecraft.world.item.ItemStack

import com.gregtechceu.gtceu.api.transfer.item.CustomItemStackHandler
import com.lowdragmc.lowdraglib.side.item.ItemTransferHelper

import java.util.function.Function
import kotlin.math.min

class UnlimitedItemStackTransfer(size: Int) : CustomItemStackHandler(size) {

    private val filter: Function<ItemStack?, Boolean?>? = null

    override fun isItemValid(slot: Int, stack: ItemStack): Boolean = filter == null || filter.apply(stack) == true

    override fun extractItem(slot: Int, amount: Int, simulate: Boolean): ItemStack {
        if (amount == 0) return ItemStack.EMPTY

        validateSlotIndex(slot)

        val existing = this.stacks[slot]

        if (existing.isEmpty) return ItemStack.EMPTY

        val toExtract = min(amount, getSlotLimit(slot))

        if (existing.count <= toExtract) {
            if (!simulate) {
                this.stacks[slot] = ItemStack.EMPTY
                onContentsChanged(slot)
                return existing
            } else {
                return existing.copy()
            }
        } else {
            if (!simulate) {
                this.stacks[slot] = ItemTransferHelper.copyStackWithSize(existing, existing.count - toExtract)
                onContentsChanged(slot)
            }

            return ItemTransferHelper.copyStackWithSize(existing, toExtract)
        }
    }

    override fun getSlotLimit(slot: Int): Int = Int.Companion.MAX_VALUE

    override fun getStackLimit(slot: Int, stack: ItemStack): Int = Int.Companion.MAX_VALUE

    override fun serializeNBT(): CompoundTag {
        val nbtTagList = ListTag()
        for (i in stacks.indices) {
            if (!stacks[i].isEmpty) {
                val itemTag = CompoundTag()
                itemTag.putInt("Slot", i)
                val `is` = stacks[i].copy()
                itemTag.putInt("realCount", `is`.count)
                `is`.count = 1
                `is`.save(itemTag)
                nbtTagList.add(itemTag)
            }
        }
        val nbt = CompoundTag()
        nbt.put("Items", nbtTagList)
        nbt.putInt("Size", stacks.size)
        return nbt
    }

    override fun deserializeNBT(nbt: CompoundTag) {
        setSize(if (nbt.contains("Size", Tag.TAG_INT.toInt())) nbt.getInt("Size") else stacks.size)
        val tagList = nbt.getList("Items", Tag.TAG_COMPOUND.toInt())
        for (i in tagList.indices) {
            val itemTags = tagList.getCompound(i)
            val slot = itemTags.getInt("Slot")

            if (slot >= 0 && slot < stacks.size) {
                val `is` = ItemStack.of(itemTags).copy()
                `is`.count = itemTags.getInt("realCount")
                stacks[slot] = `is`
            }
        }
        onLoad()
    }
}
