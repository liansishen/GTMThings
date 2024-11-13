package com.hepdd.gtmthings.common.item.Behaviour;

import com.gregtechceu.gtceu.api.cover.CoverDefinition;
import com.gregtechceu.gtceu.api.item.component.IInteractionItem;

import com.lowdragmc.lowdraglib.side.fluid.FluidTransferHelper;
import com.lowdragmc.lowdraglib.side.item.ItemTransferHelper;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import com.hepdd.gtmthings.data.CustomItems;

public record WirelessTransferCoverPlaceBehavior(CoverDefinition coverDefinition) implements IInteractionItem {

    @Override
    public InteractionResult onItemUseFirst(ItemStack itemStack, UseOnContext context) {
        if (context.getPlayer() != null && context.getPlayer().isShiftKeyDown()) {
            Player player = context.getPlayer();
            Level level = context.getLevel();
            BlockPos blockPos = context.getClickedPos();
            var itemTransfer = ItemTransferHelper.getItemTransfer(level, blockPos, context.getClickedFace());
            var fluidTransfer = FluidTransferHelper.getFluidTransfer(level, blockPos, context.getClickedFace());
            if (((itemStack.is(CustomItems.WIRELESS_ITEM_TRANSFER_COVER.asItem()) || itemStack.is(CustomItems.ADVANCED_WIRELESS_ITEM_TRANSFER_COVER.asItem())) && itemTransfer != null && itemTransfer.getSlots() > 0) || ((itemStack.is(CustomItems.WIRELESS_FLUID_TRANSFER_COVER.asItem()) || itemStack.is(CustomItems.ADVANCED_WIRELESS_FLUID_TRANSFER_COVER.asItem())) && fluidTransfer != null && fluidTransfer.getTanks() > 0)) {
                CompoundTag tag = new CompoundTag();
                tag.putString("dimensionid", level.dimension().location().toString());
                tag.putString("blockid", level.getBlockState(blockPos).getBlock().getDescriptionId());
                tag.putString("pos", blockPos.toShortString());
                tag.putString("facing", context.getClickedFace().toString());
                tag.putInt("x", blockPos.getX());
                tag.putInt("y", blockPos.getY());
                tag.putInt("z", blockPos.getZ());
                itemStack.setTag(tag);
                if (level.isClientSide()) player.sendSystemMessage(Component.translatable("item.gtmthings.wireless_transfer.tooltip.bind.1", Component.translatable(level.getBlockState(blockPos).getBlock().getDescriptionId()), blockPos.toShortString()));
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Item item, Level level, Player player, InteractionHand usedHand) {
        if (player.isShiftKeyDown()) {
            ItemStack is = player.getItemInHand(InteractionHand.MAIN_HAND);
            is.removeTagKey("dimensionid");
            is.removeTagKey("blockid");
            is.removeTagKey("pos");
            is.removeTagKey("facing");
            is.removeTagKey("x");
            is.removeTagKey("y");
            is.removeTagKey("z");
            if (level.isClientSide()) player.sendSystemMessage(Component.translatable("item.gtmthings.wireless_transfer.tooltip.bind.2"));
        }
        return IInteractionItem.super.use(item, level, player, usedHand);
    }
}
