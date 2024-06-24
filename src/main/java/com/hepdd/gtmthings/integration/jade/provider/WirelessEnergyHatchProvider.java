package com.hepdd.gtmthings.integration.jade.provider;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.SimpleTieredMachine;
import com.gregtechceu.gtceu.integration.jade.provider.CapabilityBlockProvider;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import com.hepdd.gtmthings.GTMThings;
import com.hepdd.gtmthings.api.capability.IBindable;
import com.hepdd.gtmthings.common.block.machine.multiblock.part.WirelessEnergyHatchPartMachine;
import com.hepdd.gtmthings.common.cover.WirelessEnergyReceiveCover;
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

import java.util.UUID;

import static com.hepdd.gtmthings.utils.TeamUtil.GetName;
import static com.hepdd.gtmthings.utils.TeamUtil.hasOwner;

public class WirelessEnergyHatchProvider extends CapabilityBlockProvider<IBindable> {

    public WirelessEnergyHatchProvider() {
        super(new ResourceLocation(GTMThings.MOD_ID, FormattingUtil.toLowerCaseUnder("wireless_energy_hatch_provider")));
    }

    @Override
    protected @Nullable IBindable getCapability(Level level, BlockPos pos, @Nullable Direction side) {
        if (level.getBlockEntity(pos) instanceof MetaMachineBlockEntity metaMachineBlockEntity){
            var metaMachine = metaMachineBlockEntity.getMetaMachine();
            if (metaMachine instanceof WirelessEnergyHatchPartMachine we && we.owner_uuid != null) {
                UUID uuid = we.owner_uuid;
                return new IBindable() {
                    @Override
                    public UUID getUUID() {
                        return uuid;
                    }
                    @Override
                    public void setUUID(UUID uuid1) { }
                };
            } else if (metaMachine instanceof SimpleTieredMachine simpleTieredMachine) {
                var covers = simpleTieredMachine.getCoverContainer().getCovers();
                for (var cover:covers) {
                    if(cover instanceof WirelessEnergyReceiveCover wirelessEnergyReceiveCover) {
                        UUID uuid = wirelessEnergyReceiveCover.getUuid();
                        return new IBindable() {
                            @Override
                            public UUID getUUID() {
                                return uuid;
                            }
                            @Override
                            public void setUUID(UUID uuid1) { }
                        };
                    }
                }
            }
        }
        return null;
    }

    @Override
    protected void write(CompoundTag data, IBindable capability) {
        if (capability.getUUID() != null) {
            data.putUUID("uuid", capability.getUUID());
        }
    }

    @Override
    protected void addTooltip(CompoundTag capData, ITooltip tooltip, Player player, BlockAccessor block,
                              BlockEntity blockEntity, IPluginConfig config) {
        int machineType;
        if (!(blockEntity instanceof MetaMachineBlockEntity metaMachineBlockEntity)) return;
        var metaMachine = metaMachineBlockEntity.getMetaMachine();
        if (metaMachine instanceof WirelessEnergyHatchPartMachine) {
            machineType = 1;
        } else if (metaMachine instanceof SimpleTieredMachine) {
            if (!capData.hasUUID("uuid")) return;
            machineType = 2;
        } else {
            return;
        }

        if (!capData.hasUUID("uuid")) {
            if (machineType==1) {
                tooltip.add(Component.translatable("gtmthings.machine.wireless_energy_hatch.tooltip.1"));
            } else {
                tooltip.add(Component.translatable("gtmthings.machine.wireless_energy_cover.tooltip.1"));
            }
        } else {
            UUID uuid = capData.getUUID("uuid");
            if (hasOwner(block.getLevel(),uuid)) {
                if (machineType == 1) {
                    tooltip.add(Component.translatable("gtmthings.machine.wireless_energy_hatch.tooltip.2", GetName(block.getLevel(), uuid)));
                } else {
                    tooltip.add(Component.translatable("gtmthings.machine.wireless_energy_cover.tooltip.2", GetName(block.getLevel(), uuid)));
                }
            } else {
                if (machineType == 1) {
                    tooltip.add(Component.translatable("gtmthings.machine.wireless_energy_hatch.tooltip.3", uuid));
                } else {
                    tooltip.add(Component.translatable("gtmthings.machine.wireless_energy_cover.tooltip.3", uuid));
                }
            }
        }

    }
}
