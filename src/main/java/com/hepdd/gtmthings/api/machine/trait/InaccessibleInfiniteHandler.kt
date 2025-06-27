package com.hepdd.gtmthings.api.machine.trait

import appeng.api.stacks.AEItemKey
import com.gregtechceu.gtceu.api.capability.recipe.IO
import com.gregtechceu.gtceu.api.machine.MetaMachine
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler
import com.gregtechceu.gtceu.api.recipe.GTRecipe
import com.gregtechceu.gtceu.api.recipe.ingredient.SizedIngredient
import com.gregtechceu.gtceu.api.transfer.item.CustomItemStackHandler
import com.gregtechceu.gtceu.integration.ae2.utils.KeyStorage
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.crafting.Ingredient
import java.util.function.IntFunction
import kotlin.math.min

class InaccessibleInfiniteHandler(holder: MetaMachine, internalBuffer: KeyStorage):NotifiableItemStackHandler(
    holder, 1, IO.OUT, IO.NONE, IntFunction { i: Int -> ItemStackHandlerDelegate(internalBuffer) }) {

    private var delegate: ItemStackHandlerDelegate? = null

    init {
        internalBuffer.setOnContentsChanged { this.onContentsChanged() }
        delegate = (storage as ItemStackHandlerDelegate)
    }

    fun getFirstSized(sizedIngredient: SizedIngredient): ItemStack {
        val inner = sizedIngredient.getInner()
        if (inner is SizedIngredient) {
            return getFirstSized(inner)
        }
        return getFirst(inner)
    }

    fun getFirst(ingredient: Ingredient): ItemStack {
        for (stack in ingredient.items) {
            if (!stack.isEmpty) {
                return stack
            }
        }
        return ItemStack.EMPTY
    }

    override fun handleRecipe(
        io: IO?,
        recipe: GTRecipe?,
        left: MutableList<*>,
        simulate: Boolean
    ): MutableList<Ingredient?>? {
        if (!simulate && io == IO.OUT) {
            for (ingredient in left) {
                if ((ingredient as Ingredient).isEmpty) continue
                val item: ItemStack
                val count: Int
                if (ingredient is SizedIngredient) {
                    item = getFirstSized(ingredient)
                    count = ingredient.getAmount()
                } else {
                    item = getFirst(ingredient)
                    count = item.count
                }
                if (item.isEmpty) continue
                delegate!!.insertItem(item, count)
            }
            delegate?.internalBuffer?.onChanged()
            return null
        }
        return null
    }

    override fun getContents(): MutableList<Any?> {
        return mutableListOf()
    }

    override fun getTotalContentAmount(): Double {
        return 0.0
    }

    override fun isEmpty(): Boolean {
        return true
    }

    open class ItemStackHandlerDelegate(val internalBuffer: KeyStorage) :
        CustomItemStackHandler() {
        fun insertItem(stack: ItemStack, count: Int) {
            val key = AEItemKey.of(stack)
            val oldValue = internalBuffer.storage.getOrDefault(key, 0)
            val changeValue = min(Long.Companion.MAX_VALUE - oldValue, count.toLong())
            internalBuffer.storage.put(key, oldValue + changeValue)
        }

        override fun getSlots(): Int {
            return Short.Companion.MAX_VALUE.toInt()
        }

        override fun getSlotLimit(slot: Int): Int {
            return Int.Companion.MAX_VALUE
        }

        override fun getStackInSlot(slot: Int): ItemStack {
            return ItemStack.EMPTY
        }

        override fun setStackInSlot(slot: Int, stack: ItemStack) {}

        override fun insertItem(slot: Int, stack: ItemStack, simulate: Boolean): ItemStack {
            val key = AEItemKey.of(stack)
            val count = stack.count
            val oldValue = internalBuffer.storage.getOrDefault(key, 0)
            val changeValue = min(Long.Companion.MAX_VALUE - oldValue, count.toLong())
            if (!simulate) {
                internalBuffer.storage.put(key, oldValue + changeValue)
            } else if (count.toLong() != changeValue) {
                return stack.copyWithCount((count - changeValue).toInt())
            }
            return ItemStack.EMPTY
        }
    }

    override fun extractItem(slot: Int, amount: Int, simulate: Boolean): ItemStack {
        return ItemStack.EMPTY
    }
}