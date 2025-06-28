package com.hepdd.gtmthings.common.item.behaviour

import com.gregtechceu.gtceu.api.cover.CoverDefinition
import com.gregtechceu.gtceu.api.item.component.IInteractionItem
import com.hepdd.gtmthings.data.CustomItems
import com.lowdragmc.lowdraglib.side.fluid.FluidTransferHelper
import com.lowdragmc.lowdraglib.side.item.ItemTransferHelper
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.chat.Component
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.InteractionResultHolder
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.context.UseOnContext
import net.minecraft.world.level.Level

@JvmRecord
data class WirelessTransferCoverPlaceBehavior(val coverDefinition: CoverDefinition?) : IInteractionItem {
    override fun onItemUseFirst(itemStack: ItemStack, context: UseOnContext): InteractionResult {
        if (context.player != null && context.player!!.isShiftKeyDown) {
            val player = context.player
            val level = context.level
            val blockPos = context.clickedPos
            val itemTransfer = ItemTransferHelper.getItemTransfer(level, blockPos, context.clickedFace)
            val fluidTransfer = FluidTransferHelper.getFluidTransfer(level, blockPos, context.clickedFace)
            if (((itemStack.`is`(CustomItems.WIRELESS_ITEM_TRANSFER_COVER.asItem()) || itemStack.`is`(CustomItems.ADVANCED_WIRELESS_ITEM_TRANSFER_COVER.asItem())) && itemTransfer != null && itemTransfer.slots > 0) || ((itemStack.`is`(
                    CustomItems.WIRELESS_FLUID_TRANSFER_COVER.asItem()
                ) || itemStack.`is`(CustomItems.ADVANCED_WIRELESS_FLUID_TRANSFER_COVER.asItem())) && fluidTransfer != null && fluidTransfer.tanks > 0)
            ) {
                val tag = CompoundTag()
                tag.putString("dimensionid", level.dimension().location().toString())
                tag.putString("blockid", level.getBlockState(blockPos).block.descriptionId)
                tag.putString("pos", blockPos.toShortString())
                tag.putString("facing", context.clickedFace.toString())
                tag.putInt("x", blockPos.x)
                tag.putInt("y", blockPos.y)
                tag.putInt("z", blockPos.z)
                itemStack.tag = tag
                if (level.isClientSide()) player!!.sendSystemMessage(
                    Component.translatable(
                        "item.gtmthings.wireless_transfer.tooltip.bind.1",
                        Component.translatable(level.getBlockState(blockPos).block.descriptionId),
                        blockPos.toShortString()
                    )
                )
            }
            return InteractionResult.SUCCESS
        }
        return InteractionResult.PASS
    }

    override fun use(
        item: Item?,
        level: Level,
        player: Player,
        usedHand: InteractionHand?
    ): InteractionResultHolder<ItemStack?>? {
        if (player.isShiftKeyDown) {
            val `is` = player.getItemInHand(InteractionHand.MAIN_HAND)
            `is`.removeTagKey("dimensionid")
            `is`.removeTagKey("blockid")
            `is`.removeTagKey("pos")
            `is`.removeTagKey("facing")
            `is`.removeTagKey("x")
            `is`.removeTagKey("y")
            `is`.removeTagKey("z")
            if (level.isClientSide()) player.sendSystemMessage(Component.translatable("item.gtmthings.wireless_transfer.tooltip.bind.2"))
        }
        return super.use(item, level, player, usedHand)
    }
}
