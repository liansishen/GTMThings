package com.hepdd.gtmthings.api.machine.trait

import com.gregtechceu.gtceu.api.capability.recipe.IO
import com.gregtechceu.gtceu.api.machine.MetaMachine
import com.gregtechceu.gtceu.api.machine.SimpleTieredMachine
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler
import com.gregtechceu.gtceu.api.transfer.item.CustomItemStackHandler
import com.hepdd.gtmthings.common.cover.ProgrammableCover
import com.hepdd.gtmthings.common.item.VirtualItemProviderBehavior
import com.hepdd.gtmthings.data.CustomItems
import net.minecraft.world.item.ItemStack
import java.util.function.IntFunction

class ProgrammableCircuitHandler(machine: Any?):NotifiableItemStackHandler(
    machine as MetaMachine?, 1, IO.IN, IO.IN, IntFunction { size: Int -> ItemStackHandler(size, machine) }) {

    open class ItemStackHandler(size: Int, private val machine: Any?) : CustomItemStackHandler(size) {
        override fun getSlotLimit(slot: Int): Int {
            return 1
        }

        override fun insertItem(slot: Int, stack: ItemStack, simulate: Boolean): ItemStack {
            if (stack.`is`(CustomItems.VIRTUAL_ITEM_PROVIDER.get())) {
                var allow = true
                if (machine is SimpleTieredMachine) {
                    allow = false
                    for (cover in machine.getCoverContainer().covers) {
                        if (cover is ProgrammableCover) {
                            allow = true
                            break
                        }
                    }
                }
                if (allow) {
                    setStackInSlot(slot, VirtualItemProviderBehavior.getVirtualItem(stack))
                    return ItemStack.EMPTY
                }
            }
            return stack
        }

        override fun extractItem(slot: Int, amount: Int, simulate: Boolean): ItemStack {
            return if (simulate) super.extractItem(slot, amount, true) else ItemStack.EMPTY
        }
    }
}