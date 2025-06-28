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
        if (context.level.isClientSide()) return InteractionResult.PASS
        if (ConfigHolder.INSTANCE!!.isWirelessRateEnable) {
            val pos = context.clickedPos
            val rate: Long = getRate(context.level, pos)
            if (rate > 0) {
                val container = getOrCreateContainer(context.player!!.getUUID())
                container!!.rate = rate
                container.bindPos = GlobalPos.of(context.level.dimension(), pos)
                context.player!!.sendSystemMessage(
                    Component.translatable(
                        "item.gtmthings.wireless_transfer.tooltip.bind.1",
                        Component.translatable(context.level.getBlockState(pos).block.descriptionId),
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
                    for (i in 0..<inv.slots) {
                        val electricItem = GTCapabilityHelper.getElectricItem(inv.getStackInSlot(i))
                        if (electricItem != null) {
                            rate += GTValues.VEX[electricItem.tier]
                        }
                    }
                } else if (machine is PowerSubstationMachine && machine.isFormed()) {
                    rate = machine.energyInfo.capacity().divide(BigInteger.valueOf(4096)).toLong()
                }
            }
            return rate
        }
    }
}
