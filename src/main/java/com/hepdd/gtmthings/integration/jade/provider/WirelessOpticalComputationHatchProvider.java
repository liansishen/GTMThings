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
import com.hepdd.gtmthings.api.capability.IGTMTJadeIF;
import org.jetbrains.annotations.Nullable;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

import static net.minecraft.resources.ResourceLocation.tryBuild;

public class WirelessOpticalComputationHatchProvider extends CapabilityBlockProvider<IGTMTJadeIF> {

    public WirelessOpticalComputationHatchProvider() {
        super(tryBuild(GTMThings.MOD_ID, FormattingUtil.toLowerCaseUnder("wireless_energy_hatch_provider")));
    }

    @Override
    protected @Nullable IGTMTJadeIF getCapability(Level level, BlockPos pos, @Nullable Direction side) {
        if (MetaMachine.getMachine(level, pos) instanceof IGTMTJadeIF jadeIF) {
            return jadeIF;
        }
        return null;
    }

    @Override
    protected void write(CompoundTag data, IGTMTJadeIF capability) {
        if (capability == null) return;
        data.putBoolean("isGTMTJadeIF", true);
        data.putBoolean("isTransmitter", capability.isTransmitter());
        data.putBoolean("isBinded", capability.isbinded());
        data.putString("pos", capability.getBindPos());
    }

    @Override
    protected void addTooltip(CompoundTag capData, ITooltip tooltip, Player player, BlockAccessor block, BlockEntity blockEntity, IPluginConfig config) {
        if (!capData.getBoolean("isGTMTJadeIF")) return;
        if (capData.getBoolean("isBinded")) {
            if (capData.getBoolean("isTransmitter")) {
                tooltip.add(Component.translatable("gtmthings.machine.transmitter_hatch.bind", capData.getString("pos")));
            } else {
                tooltip.add(Component.translatable("gtmthings.machine.receiver_hatch.bind", capData.getString("pos")));
            }
        } else {
            if (capData.getBoolean("isTransmitter")) {
                tooltip.add(Component.translatable("gtmthings.machine.transmitter_hatch.unbind"));
            } else {
                tooltip.add(Component.translatable("gtmthings.machine.receiver_hatch.unbind"));
            }
        }
    }
}
