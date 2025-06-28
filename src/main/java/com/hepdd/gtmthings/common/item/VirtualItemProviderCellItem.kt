package com.hepdd.gtmthings.common.item

import net.minecraft.world.item.ItemStack

import appeng.api.stacks.AEItemKey
import appeng.api.stacks.GenericStack
import appeng.items.storage.CreativeCellItem
import appeng.util.ConfigInventory
import com.hepdd.gtmthings.data.CustomItems

class VirtualItemProviderCellItem(props: Properties) : CreativeCellItem(props) {

    override fun getConfigInventory(`is`: ItemStack): ConfigInventory? = Holder(`is`).apply {
        inv = VirtualConfigInventory(63, this::save)
        load()
    }.inv

    private class Holder(private val stack: ItemStack) {
        var inv: ConfigInventory? = null

        fun load() {
            if (stack.hasTag()) {
                inv!!.readFromChildTag(stack.getOrCreateTag(), "list")
            }
        }

        fun save() {
            inv!!.writeToChildTag(stack.getOrCreateTag(), "list")
        }
    }

    private class VirtualConfigInventory(size: Int, listener: Runnable?) : ConfigInventory(null, Mode.CONFIG_TYPES, size, listener, false) {

        override fun setStack(slot: Int, stack: GenericStack?) {
            when {
                stack == null -> super.setStack(slot, null)
                stack.what() is AEItemKey &&
                    (stack.what() as AEItemKey).run {
                        item == CustomItems.VIRTUAL_ITEM_PROVIDER.asItem() && hasTag()
                    } -> {
                    val itemKey = stack.what() as AEItemKey
                    val typesOnly = mode == Mode.CONFIG_TYPES
                    itemKey.tag?.putBoolean("marked", true)

                    val adjustedStack = when {
                        typesOnly && stack.amount() != 0L -> GenericStack(itemKey, 0L)
                        !typesOnly && stack.amount() <= 0L -> null
                        else -> stack
                    }

                    super.setStack(slot, adjustedStack)
                }
            }
        }
    }
}
