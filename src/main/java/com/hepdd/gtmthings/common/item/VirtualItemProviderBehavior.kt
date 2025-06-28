package com.hepdd.gtmthings.common.item

import com.gregtechceu.gtceu.api.gui.GuiTextures
import com.gregtechceu.gtceu.api.gui.fancy.FancyMachineUIWidget
import com.gregtechceu.gtceu.api.gui.fancy.IFancyUIProvider
import com.gregtechceu.gtceu.api.gui.fancy.TabsWidget
import com.gregtechceu.gtceu.api.gui.widget.SlotWidget
import com.gregtechceu.gtceu.api.item.component.IAddInformation
import com.gregtechceu.gtceu.api.item.component.IItemUIFactory
import com.hepdd.gtmthings.data.CustomItems
import com.lowdragmc.lowdraglib.gui.factory.HeldItemUIFactory.HeldItemHolder
import com.lowdragmc.lowdraglib.gui.modular.ModularUI
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture
import com.lowdragmc.lowdraglib.gui.texture.ItemStackTexture
import com.lowdragmc.lowdraglib.gui.widget.Widget
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResultHolder
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.TooltipFlag
import net.minecraft.world.level.Level
import net.minecraftforge.items.IItemHandlerModifiable

class VirtualItemProviderBehavior : IAddInformation, IItemUIFactory, IFancyUIProvider {
    private var player: Player? = null
    private var hand: InteractionHand? = null

    override fun appendHoverText(
        itemstack: ItemStack,
        world: Level?,
        list: MutableList<Component?>,
        flag: TooltipFlag
    ) {
        if (itemstack.hasTag()) {
            list.add(
                Component.translatable("gui.ae2.Items").append(": ").append(getVirtualItem(itemstack).displayName)
            )
        }
    }

    override fun use(
        item: Item?,
        level: Level?,
        player: Player?,
        usedHand: InteractionHand?
    ): InteractionResultHolder<ItemStack?>? {
        this.player = player
        hand = usedHand
        return super<IItemUIFactory>.use(item, level, player, usedHand)
    }

    override fun createUI(holder: HeldItemHolder?, entityPlayer: Player?): ModularUI {
        return ModularUI(176, 166, holder, entityPlayer).widget(FancyMachineUIWidget(this, 176, 166))
    }

    override fun createMainPage(widget: FancyMachineUIWidget?): Widget {
        val group = WidgetGroup(0, 0, 18 + 16, 18 + 16)
        val container = WidgetGroup(4, 4, 18 + 8, 18 + 8)
        container.addWidget(SlotWidget(ItemHandler(player, hand!!), 0, 4, 4, true, true).setBackground(GuiTextures.SLOT))
        group.addWidget(container)
        return group
    }

    override fun attachSideTabs(sideTabs: TabsWidget) {
        sideTabs.setMainTab(this)
    }

    override fun getTabIcon(): IGuiTexture {
        return ItemStackTexture(CustomItems.VIRTUAL_ITEM_PROVIDER.get())
    }

    override fun getTitle(): Component {
        return CustomItems.VIRTUAL_ITEM_PROVIDER.get().description
    }

    @JvmRecord
    private data class ItemHandler(val entityPlayer: Player?, val hand: InteractionHand) : IItemHandlerModifiable {
        val item: ItemStack
            get() = entityPlayer!!.getItemInHand(hand)

        override fun setStackInSlot(i: Int, arg: ItemStack) {}

        override fun getSlots(): Int {
            return 1
        }

        override fun getStackInSlot(i: Int): ItemStack {
            return getVirtualItem(this.item)
        }

        override fun insertItem(i: Int, arg: ItemStack, bl: Boolean): ItemStack {
            if (arg.`is`(CustomItems.VIRTUAL_ITEM_PROVIDER.get())) return arg
            entityPlayer!!.setItemInHand(hand, setVirtualItem(this.item, arg.copyWithCount(1)))
            return arg.copyWithCount(arg.count - 1)
        }

        override fun extractItem(i: Int, j: Int, bl: Boolean): ItemStack {
            if (this.item.getOrCreateTag().getBoolean("marked")) return ItemStack.EMPTY
            setVirtualItem(this.item, ItemStack.EMPTY)
            return getStackInSlot(0)
        }

        override fun getSlotLimit(i: Int): Int {
            return 1
        }

        override fun isItemValid(i: Int, arg: ItemStack): Boolean {
            return true
        }
    }

    companion object {
        val INSTANCE: VirtualItemProviderBehavior = VirtualItemProviderBehavior()

        private fun setVirtualItem(stack: ItemStack, virtualItem: ItemStack): ItemStack {
            val tag = stack.getOrCreateTag()
            tag.remove("t")
            val id = BuiltInRegistries.ITEM.getKey(virtualItem.item)
            tag.putString("m", id.namespace)
            tag.putString("n", id.path)
            val itemTag = virtualItem.tag
            if (itemTag != null) tag.put("t", itemTag)
            return stack
        }

        fun getVirtualItem(item: ItemStack): ItemStack {
            val tag = item.getOrCreateTag()
            val mod = tag.getString("m")
            if (mod.isEmpty()) {
                return ItemStack.EMPTY
            }
            val stack =
                BuiltInRegistries.ITEM.get(ResourceLocation.tryBuild(mod, tag.getString("n"))).defaultInstance
            if (tag.contains("t")) stack.tag = tag.get("t") as CompoundTag?
            return stack
        }
    }
}
