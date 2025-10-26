package com.hepdd.gtmthings.api.capability;

import com.gregtechceu.gtceu.api.capability.IWailaDisplayProvider;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;

import snownee.jade.api.BlockAccessor;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public interface IGTMTJadeIF extends IWailaDisplayProvider {

    boolean isTransmitter();

    boolean isbinded();

    String getBindPos();

    default void appendWailaTooltip(CompoundTag data, ITooltip iTooltip, BlockAccessor blockAccessor, IPluginConfig iPluginConfig) {
        if (data.getBoolean("isBinded")) {
            if (data.getBoolean("isTransmitter")) {
                iTooltip.add(Component.translatable("gtmthings.machine.transmitter_hatch.bind", data.getString("pos")));
            } else {
                iTooltip.add(Component.translatable("gtmthings.machine.receiver_hatch.bind", data.getString("pos")));
            }
        } else {
            if (data.getBoolean("isTransmitter")) {
                iTooltip.add(Component.translatable("gtmthings.machine.transmitter_hatch.unbind"));
            } else {
                iTooltip.add(Component.translatable("gtmthings.machine.receiver_hatch.unbind"));
            }
        }
    }

    default void appendWailaData(CompoundTag data, BlockAccessor blockAccessor) {
        data.putBoolean("isTransmitter", isTransmitter());
        data.putBoolean("isBinded", isbinded());
        data.putString("pos", getBindPos());
    }
}
