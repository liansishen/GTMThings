package com.hepdd.gtmthings.integration.jade.provider;

import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.integration.jade.provider.CapabilityBlockProvider;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import com.hepdd.gtmthings.GTMThings;
import com.hepdd.gtmthings.api.capability.IBindable;
import org.jetbrains.annotations.Nullable;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

import java.util.UUID;

import static com.hepdd.gtmthings.utils.TeamUtil.GetName;
import static com.hepdd.gtmthings.utils.TeamUtil.hasOwner;
import static net.minecraft.resources.ResourceLocation.tryBuild;

public class WirelessEnergyHatchProvider extends CapabilityBlockProvider<IBindable> {

    public WirelessEnergyHatchProvider() {
        super(tryBuild(GTMThings.MOD_ID, FormattingUtil.toLowerCaseUnder("wireless_energy_hatch_provider")));
    }

    @Override
    protected @Nullable IBindable getCapability(Level level, BlockPos pos, @Nullable Direction side) {
        var metaMachine = MetaMachine.getMachine(level, pos);
        if (metaMachine != null) {
            if (metaMachine instanceof IBindable bindable && bindable.display()) {
                return bindable;
            } else {
                var covers = metaMachine.getCoverContainer().getCovers();
                for (var cover : covers) {
                    if (cover instanceof IBindable bindable && bindable.display()) {
                        return bindable;
                    }
                }
            }
        }
        return null;
    }

    @Override
    protected void write(CompoundTag data, IBindable capability) {
        if (capability.getUUID() != null) {
            data.putBoolean("isBindable", true);
            data.putUUID("uuid", capability.getUUID());
            data.putBoolean("cover", capability.cover());
        }
    }

    @Override
    protected void addTooltip(CompoundTag capData, ITooltip tooltip, Player player, BlockAccessor block, BlockEntity blockEntity, IPluginConfig config) {
        if (!capData.getBoolean("isBindable")) return;
        boolean cover = capData.getBoolean("cover");
        if (!capData.hasUUID("uuid")) {
            if (cover) {
                tooltip.add(Component.translatable("gtmthings.machine.wireless_energy_cover.tooltip.1"));
            } else {
                tooltip.add(Component.translatable("gtmthings.machine.wireless_energy_hatch.tooltip.1"));
            }
        } else {
            UUID uuid = capData.getUUID("uuid");
            if (hasOwner(block.getLevel(), uuid)) {
                if (cover) {
                    tooltip.add(Component.translatable("gtmthings.machine.wireless_energy_cover.tooltip.2", GetName(block.getLevel(), uuid)));
                } else {
                    tooltip.add(Component.translatable("gtmthings.machine.wireless_energy_hatch.tooltip.2", GetName(block.getLevel(), uuid)));
                }
            } else {
                if (cover) {
                    tooltip.add(Component.translatable("gtmthings.machine.wireless_energy_cover.tooltip.3", uuid));
                } else {
                    tooltip.add(Component.translatable("gtmthings.machine.wireless_energy_hatch.tooltip.3", uuid));
                }
            }
        }
    }
}
