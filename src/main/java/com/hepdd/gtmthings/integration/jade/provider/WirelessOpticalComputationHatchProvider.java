package com.hepdd.gtmthings.integration.jade.provider;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.integration.jade.provider.CapabilityBlockProvider;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import com.hepdd.gtmthings.GTMThings;
import com.hepdd.gtmthings.api.capability.IGTMTJadeIF;
import com.hepdd.gtmthings.common.block.machine.multiblock.part.computation.WirelessOpticalComputationHatchMachine;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public class WirelessOpticalComputationHatchProvider extends CapabilityBlockProvider<IGTMTJadeIF> {

    public WirelessOpticalComputationHatchProvider() {
        super(new ResourceLocation(GTMThings.MOD_ID, FormattingUtil.toLowerCaseUnder("wireless_energy_hatch_provider")));
    }

    @Override
    protected @Nullable IGTMTJadeIF getCapability(Level level, BlockPos pos, @Nullable Direction side) {
        if (level.getBlockEntity(pos) instanceof MetaMachineBlockEntity metaMachineBlockEntity){
            var metaMachine = metaMachineBlockEntity.getMetaMachine();
            if (metaMachine instanceof WirelessOpticalComputationHatchMachine woc) {
                if( woc.getTransmitterPos() != null && woc.getReceiverPos() != null) {
                    return new IGTMTJadeIF() {
                        @Override
                        public boolean isbinded() {
                            return true;
                        }

                        @Override
                        public String getPos() {
                            return woc.isTransmitter()?woc.getTransmitterPos().toShortString():woc.getReceiverPos().toShortString();
                        }
                    };
                } else {
                    return new IGTMTJadeIF() {
                        @Override
                        public boolean isbinded() {
                            return false;
                        }

                        @Override
                        public String getPos() {
                            return "";
                        }
                    };
                }
            }
        }
        return null;
    }

    @Override
    protected void write(CompoundTag data, IGTMTJadeIF capability) {
        data.putBoolean("isBinded",capability.isbinded());
        data.putString("pos",capability.getPos());
    }

    @Override
    protected void addTooltip(CompoundTag capData, ITooltip tooltip, Player player, BlockAccessor block, BlockEntity blockEntity, IPluginConfig config) {
        if (!(blockEntity instanceof MetaMachineBlockEntity metaMachineBlockEntity)) return;
        var metaMachine = metaMachineBlockEntity.getMetaMachine();
        if (metaMachine instanceof WirelessOpticalComputationHatchMachine woc) {
            if (capData.getBoolean("isBinded")) {
                if (woc.isTransmitter()) {
                    tooltip.add(Component.translatable("gtmthings.machine.wireless_computation_transmitter_hatch.bind"
                            ,capData.getString("pos")));
                } else {
                    tooltip.add(Component.translatable("gtmthings.machine.wireless_computation_receiver_hatch.bind"
                            ,capData.getString("pos")));
                }
            } else {
                if (woc.isTransmitter()) {
                    tooltip.add(Component.translatable("gtmthings.machine.wireless_computation_transmitter_hatch.unbind"));
                } else {
                    tooltip.add(Component.translatable("gtmthings.machine.wireless_computation_receiver_hatch.unbind"));
                }
            }
        }
    }
}
