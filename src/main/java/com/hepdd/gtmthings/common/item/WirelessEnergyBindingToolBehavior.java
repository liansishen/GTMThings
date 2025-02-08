package com.hepdd.gtmthings.common.item;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.IElectricItem;
import com.gregtechceu.gtceu.api.item.component.IInteractionItem;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.transfer.item.CustomItemStackHandler;
import com.gregtechceu.gtceu.common.machine.electric.BatteryBufferMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.PowerSubstationMachine;

import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;

import com.hepdd.gtmthings.api.misc.WirelessEnergyContainer;

import java.math.BigInteger;

public class WirelessEnergyBindingToolBehavior implements IInteractionItem {

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        BlockPos pos = context.getClickedPos();
        long rate = getRate(context.getLevel(), pos);
        if (rate > 0) {
            WirelessEnergyContainer container = WirelessEnergyContainer.getOrCreateContainer(context.getPlayer().getUUID());
            container.setRate(rate);
            container.setBindPos(GlobalPos.of(context.getLevel().dimension(), pos));
            if (context.getLevel().isClientSide()) context.getPlayer().sendSystemMessage(Component.translatable("item.gtmthings.wireless_transfer.tooltip.bind.1", Component.translatable(context.getLevel().getBlockState(pos).getBlock().getDescriptionId()), pos.toShortString()));
            return InteractionResult.CONSUME;
        }
        return InteractionResult.PASS;
    }

    public static long getRate(BlockGetter level, BlockPos pos) {
        long rate = 0;
        if (level != null) {
            MetaMachine machine = MetaMachine.getMachine(level, pos);
            if (machine instanceof BatteryBufferMachine batteryBufferMachine) {
                CustomItemStackHandler inv = batteryBufferMachine.getBatteryInventory();
                for (int i = 0; i < inv.getSlots(); i++) {
                    IElectricItem electricItem = GTCapabilityHelper.getElectricItem(inv.getStackInSlot(i));
                    if (electricItem != null) {
                        rate += GTValues.VEX[electricItem.getTier()];
                    }
                }
            } else if (machine instanceof PowerSubstationMachine powerSubstationMachine) {
                rate = powerSubstationMachine.getEnergyInfo().capacity().divide(BigInteger.valueOf(4096)).longValue();
            }
        }
        return rate;
    }
}
