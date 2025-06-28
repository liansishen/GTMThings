package com.hepdd.gtmthings.common.item

import net.minecraft.core.BlockPos
import net.minecraft.core.GlobalPos
import net.minecraft.network.chat.Component
import net.minecraft.world.InteractionResult
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.context.UseOnContext
import net.minecraft.world.level.BlockGetter

import com.gregtechceu.gtceu.api.GTValues
import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper
import com.gregtechceu.gtceu.api.item.component.IInteractionItem
import com.gregtechceu.gtceu.api.machine.MetaMachine
import com.gregtechceu.gtceu.common.machine.electric.BatteryBufferMachine
import com.gregtechceu.gtceu.common.machine.multiblock.electric.PowerSubstationMachine
import com.hepdd.gtmthings.api.misc.WirelessEnergyContainer.Companion.getOrCreateContainer
import com.hepdd.gtmthings.config.ConfigHolder

import java.math.BigInteger

class WirelessEnergyBindingToolBehavior : IInteractionItem {
    override fun onItemUseFirst(stack: ItemStack?, context: UseOnContext): InteractionResult {
        if (context.getLevel().isClientSide()) return InteractionResult.PASS
        if (ConfigHolder.INSTANCE!!.isWirelessRateEnable) {
            val pos = context.getClickedPos()
            val rate: Long = getRate(context.getLevel(), pos)
            if (rate > 0) {
                val container = getOrCreateContainer(context.getPlayer()!!.getUUID())
                container!!.rate = rate
                container.bindPos = GlobalPos.of(context.getLevel().dimension(), pos)
                context.getPlayer()!!.sendSystemMessage(
                    Component.translatable(
                        "item.gtmthings.wireless_transfer.tooltip.bind.1",
                        Component.translatable(context.getLevel().getBlockState(pos).getBlock().getDescriptionId()),
                        pos.toShortString(),
                    ),
                )
                return InteractionResult.CONSUME
            }
        }
        return InteractionResult.PASS
    }

    companion object {
        fun getRate(level: BlockGetter?, pos: BlockPos): Long {
            var rate: Long = 0
            if (level != null) {
                val machine = MetaMachine.getMachine(level, pos)
                if (machine is BatteryBufferMachine) {
                    val inv = machine.getBatteryInventory()
                    for (i in 0..<inv.getSlots()) {
                        val electricItem = GTCapabilityHelper.getElectricItem(inv.getStackInSlot(i))
                        if (electricItem != null) {
                            rate += GTValues.VEX[electricItem.getTier()]
                        }
                    }
                } else if (machine is PowerSubstationMachine && machine.isFormed()) {
                    rate = machine.getEnergyInfo().capacity().divide(BigInteger.valueOf(4096)).toLong()
                }
            }
            return rate
        }
    }
}
