package com.hepdd.gtmthings.integration.jade.provider

import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.entity.BlockEntity

import com.gregtechceu.gtceu.api.machine.MetaMachine
import com.gregtechceu.gtceu.integration.jade.provider.CapabilityBlockProvider
import com.gregtechceu.gtceu.utils.FormattingUtil
import com.hepdd.gtmthings.GTMThings
import com.hepdd.gtmthings.api.capability.IGTMTJadeIF
import snownee.jade.api.BlockAccessor
import snownee.jade.api.ITooltip
import snownee.jade.api.config.IPluginConfig

class WirelessOpticalComputationHatchProvider :
    CapabilityBlockProvider<IGTMTJadeIF>(
        ResourceLocation.tryBuild(
            GTMThings.MOD_ID,
            FormattingUtil.toLowerCaseUnder("wireless_energy_hatch_provider"),
        ),
    ) {

    override fun getCapability(level: Level, pos: BlockPos, side: Direction?): IGTMTJadeIF? {
        (MetaMachine.getMachine(level, pos) as? IGTMTJadeIF)?.let { jadeIf ->
            return jadeIf
        }
        return null
    }

    override fun write(data: CompoundTag, capability: IGTMTJadeIF) {
        data.putBoolean("isGTMTJadeIF", true)
        data.putBoolean("isTransmitter", capability.isTransmitter())
        data.putBoolean("isBinded", capability.isbinded())
        data.putString("pos", capability.getBindPos())
    }

    override fun addTooltip(capData: CompoundTag, tooltip: ITooltip, player: Player?, block: BlockAccessor?, blockEntity: BlockEntity?, config: IPluginConfig?) {
        if (!capData.getBoolean("isGTMTJadeIF")) return
        if (capData.getBoolean("isBinded")) {
            if (capData.getBoolean("isTransmitter")) {
                tooltip.add(Component.translatable("gtmthings.transmitter_hatch.bind", capData.getString("pos")))
            } else {
                tooltip.add(Component.translatable("gtmthings.machine.receiver_hatch.bind", capData.getString("pos")))
            }
        } else {
            if (capData.getBoolean("isTransmitter")) {
                tooltip.add(Component.translatable("gtmthings.machine.transmitter_hatch.unbind"))
            } else {
                tooltip.add(Component.translatable("gtmthings.machine.receiver_hatch.unbind"))
            }
        }
    }
}
