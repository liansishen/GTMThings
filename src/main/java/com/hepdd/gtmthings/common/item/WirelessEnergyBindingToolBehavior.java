package com.hepdd.gtmthings.common.item;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.IElectricItem;
import com.gregtechceu.gtceu.api.item.component.IInteractionItem;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.common.machine.electric.BatteryBufferMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.PowerSubstationMachine;
import com.hepdd.gtmthings.api.misc.GlobalVariableStorage;
import com.hepdd.gtmthings.utils.TeamUtil;
import com.lowdragmc.lowdraglib.misc.ItemStackTransfer;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;

import java.math.BigInteger;

public class WirelessEnergyBindingToolBehavior implements IInteractionItem {

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        BlockPos pos = context.getClickedPos();
        Pair<Boolean, Long> rate = getRate(context.getLevel(), pos);
        if (rate.getFirst()) {
            GlobalVariableStorage.GlobalRate.put(TeamUtil.getTeamUUID(context.getPlayer().getUUID()), Pair.of(GlobalPos.of(context.getLevel().dimension(), pos), rate.getSecond()));
            context.getPlayer().sendSystemMessage(Component.translatable("item.gtmthings.wireless_transfer.tooltip.bind.1",Component.translatable(context.getLevel().getBlockState(pos).getBlock().getDescriptionId()),pos.toShortString()));
           return InteractionResult.CONSUME;
        }
        return InteractionResult.PASS;
    }

    public static Pair<Boolean, Long> getRate(BlockGetter level, BlockPos pos) {
        long rate = 0;
        boolean effective = false;
        MetaMachine machine = MetaMachine.getMachine(level, pos);
        if (machine instanceof BatteryBufferMachine batteryBufferMachine) {
            effective = true;
            ItemStackTransfer inv = batteryBufferMachine.getBatteryInventory();
            for (int i = 0; i < inv.getSlots(); i++) {
                IElectricItem electricItem = GTCapabilityHelper.getElectricItem(inv.getStackInSlot(i));
                if (electricItem != null) {
                    rate += GTValues.V[electricItem.getTier()];
                }
            }
        } else if (machine instanceof PowerSubstationMachine powerSubstationMachine) {
            effective = true;
            rate = powerSubstationMachine.getEnergyInfo().capacity().divide(BigInteger.valueOf(4096)).longValue();
        }
        return Pair.of(effective, rate);
    }

}
