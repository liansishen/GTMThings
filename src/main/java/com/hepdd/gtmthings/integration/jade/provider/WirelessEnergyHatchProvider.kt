package com.hepdd.gtmthings.integration.jade.provider

import com.gregtechceu.gtceu.api.machine.MetaMachine
import com.gregtechceu.gtceu.integration.jade.provider.CapabilityBlockProvider
import com.gregtechceu.gtceu.utils.FormattingUtil
import com.hepdd.gtmthings.GTMThings
import com.hepdd.gtmthings.api.capability.IBindable
import com.hepdd.gtmthings.utils.TeamUtil.Companion.getName
import com.hepdd.gtmthings.utils.TeamUtil.Companion.hasOwner
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.entity.BlockEntity
import snownee.jade.api.BlockAccessor
import snownee.jade.api.ITooltip
import snownee.jade.api.config.IPluginConfig

class WirelessEnergyHatchProvider():CapabilityBlockProvider<IBindable>(
        ResourceLocation.tryBuild(
            GTMThings.MOD_ID,
            FormattingUtil.toLowerCaseUnder("wireless_energy_hatch_provider")
        )
    ) {

    override fun getCapability(level: Level, pos: BlockPos, side: Direction?): IBindable? {
        val metaMachine = MetaMachine.getMachine(level, pos)
        if (metaMachine != null) {
            if (metaMachine is IBindable && metaMachine.display()) {
                return metaMachine
            } else {
                val covers = metaMachine.getCoverContainer().covers
                for (cover in covers) {
                    if (cover is IBindable && cover.display()) {
                        return cover
                    }
                }
            }
        }
        return null
    }

    override fun write(data: CompoundTag, capability: IBindable) {
        if (capability.getUUID() != null) {
            data.putBoolean("isBindable", true)
            data.putUUID("uuid", capability.getUUID())
            data.putBoolean("cover", capability.cover())
        }
    }

    override fun addTooltip(
        capData: CompoundTag,
        tooltip: ITooltip,
        player: Player?,
        block: BlockAccessor,
        blockEntity: BlockEntity?,
        config: IPluginConfig?
    ) {
        if (!capData.getBoolean("isBindable")) return
        val cover = capData.getBoolean("cover")
        if (!capData.hasUUID("uuid")) {
            if (cover) {
                tooltip.add(Component.translatable("gtmthings.machine.wireless_energy_cover.tooltip.1"))
            } else {
                tooltip.add(Component.translatable("gtmthings.machine.wireless_energy_hatch.tooltip.1"))
            }
        } else {
            val uuid = capData.getUUID("uuid")
            if (hasOwner(block.level, uuid)) {
                if (cover) {
                    tooltip.add(
                        Component.translatable(
                            "gtmthings.machine.wireless_energy_cover.tooltip.2",
                            getName(block.level, uuid)
                        )
                    )
                } else {
                    tooltip.add(
                        Component.translatable(
                            "gtmthings.machine.wireless_energy_hatch.tooltip.2",
                            getName(block.level, uuid)
                        )
                    )
                }
            } else {
                if (cover) {
                    tooltip.add(Component.translatable("gtmthings.machine.wireless_energy_cover.tooltip.3", uuid))
                } else {
                    tooltip.add(Component.translatable("gtmthings.machine.wireless_energy_hatch.tooltip.3", uuid))
                }
            }
        }
    }
}