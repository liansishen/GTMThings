package com.hepdd.gtmthings.data;

import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;

import com.hepdd.gtmthings.api.misc.WirelessEnergyContainer;
import org.jetbrains.annotations.NotNull;

import java.math.BigInteger;
import java.util.UUID;

public class WirelessEnergySavaedData extends SavedData {

    public static WirelessEnergySavaedData INSTANCE;

    public static WirelessEnergySavaedData getOrCreate(ServerLevel serverLevel) {
        return serverLevel.getDataStorage().computeIfAbsent(WirelessEnergySavaedData::new, WirelessEnergySavaedData::new, "gtceu_wireless_energy");
    }

    public WirelessEnergySavaedData() {}

    public WirelessEnergySavaedData(CompoundTag tag) {
        ListTag allEnergy = tag.getList("allEnergy", Tag.TAG_COMPOUND);
        for (int i = 0; i < allEnergy.size(); i++) {
            CompoundTag engTag = allEnergy.getCompound(i);
            UUID uuid = engTag.getUUID("uuid");
            BigInteger energy = new BigInteger(engTag.getString("energy").isEmpty() ? "0" : engTag.getString("energy"));
            long rate = engTag.getLong("rate");
            GlobalPos bindPos = readGlobalPos(engTag.getString("dimension"), engTag.getLong("pos"));
            WirelessEnergyContainer.GLOBAL_CACHE.put(uuid, new WirelessEnergyContainer(uuid, energy, rate, bindPos));
        }
    }

    @Override
    public @NotNull CompoundTag save(@NotNull CompoundTag compoundTag) {
        ListTag allEnergy = new ListTag();
        for (var entry : WirelessEnergyContainer.GLOBAL_CACHE.entrySet()) {
            CompoundTag engTag = new CompoundTag();
            engTag.putUUID("uuid", entry.getKey());
            WirelessEnergyContainer container = entry.getValue();
            engTag.putString("energy", container.getStorage().toString());
            engTag.putLong("rate", container.getRate());
            engTag.putString("dimension", container.getBindPos().dimension().location().toString());
            engTag.putLong("pos", container.getBindPos().pos().asLong());
            allEnergy.add(engTag);
        }
        compoundTag.put("allEnergy", allEnergy);
        return compoundTag;
    }

    private static GlobalPos readGlobalPos(String dimension, long pos) {
        ResourceLocation key = ResourceLocation.tryParse(dimension);
        if (key == null) {
            return null;
        }
        BlockPos pos1 = BlockPos.of(pos);
        ResourceKey<Level> level = ResourceKey.create(Registries.DIMENSION, key);
        return GlobalPos.of(level, pos1);
    }
}
