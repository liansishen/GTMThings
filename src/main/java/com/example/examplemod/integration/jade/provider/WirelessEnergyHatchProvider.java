package com.example.examplemod.integration.jade.provider;

import com.example.examplemod.ExampleMod;
import com.example.examplemod.api.capability.IBindable;
import com.example.examplemod.block.machine.multiblock.part.WirelessEnergyHatchPartMachine;
import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.integration.jade.provider.CapabilityBlockProvider;
import com.gregtechceu.gtceu.utils.FormattingUtil;
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

public class WirelessEnergyHatchProvider extends CapabilityBlockProvider<IBindable> {

    public WirelessEnergyHatchProvider() {
        super(new ResourceLocation(ExampleMod.MOD_ID, FormattingUtil.toLowerCaseUnder("wireless_energy_hatch_provider")));
    }

    @Override
    protected @Nullable IBindable getCapability(Level level, BlockPos pos, @Nullable Direction side) {
        if (level.getBlockEntity(pos) instanceof MetaMachineBlockEntity metaMachineBlockEntity
                && metaMachineBlockEntity.getMetaMachine() instanceof WirelessEnergyHatchPartMachine we
                && we.owner_uuid != null) {
            UUID uuid = we.owner_uuid;
            IBindable cap = new IBindable() {
                @Override
                public UUID getUUID() {
                    return uuid;
                }

                @Override
                public void setUUID(UUID uuid) {

                }
            };
            return cap;
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

        if (!(((MetaMachineBlockEntity)blockEntity).getMetaMachine() instanceof WirelessEnergyHatchPartMachine)) return;

        if (!capData.hasUUID("uuid")) {
            tooltip.add(Component.translatable("gtceu.machine.wireless_energy_hatch.tooltip.1"));
        } else {
            UUID uuid = capData.getUUID("uuid");
            Player bindPlayer = block.getLevel().getPlayerByUUID(uuid);
            if (bindPlayer != null) {
                tooltip.add(Component.translatable("gtceu.machine.wireless_energy_hatch.tooltip.2",bindPlayer.getName().getString()));
            } else {
                tooltip.add(Component.translatable("gtceu.machine.wireless_energy_hatch.tooltip.2",uuid));
            }
        }

    }
}
