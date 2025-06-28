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
import net.minecraft.world.level.saveddata.SavedData;

import com.hepdd.gtmthings.api.misc.WirelessEnergyContainer;
import org.jetbrains.annotations.NotNull;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class WirelessEnergySavaedData extends SavedData {

    public static WirelessEnergySavaedData INSTANCE;

    public static WirelessEnergySavaedData getOrCreate(ServerLevel serverLevel) {
        return serverLevel.getDataStorage().computeIfAbsent(WirelessEnergySavaedData::new, WirelessEnergySavaedData::new, "gtceu_wireless_energy");
    }

    public final Map<UUID, WirelessEnergyContainer> containerMap = new HashMap<>();

    public WirelessEnergySavaedData() {}

    public WirelessEnergySavaedData(CompoundTag tag) {
        ListTag allEnergy = tag.getList("allEnergy", Tag.TAG_COMPOUND);
        for (int i = 0; i < allEnergy.size(); i++) {
            WirelessEnergyContainer container = readTag(allEnergy.getCompound(i));
            containerMap.put(container.getUuid(), container);
        }
    }

    @Override
    public @NotNull CompoundTag save(@NotNull CompoundTag compoundTag) {
        ListTag allEnergy = new ListTag();
        for (WirelessEnergyContainer container : containerMap.values()) {
            CompoundTag engTag = toTag(container);
            if (engTag.isEmpty()) continue;
            allEnergy.add(engTag);
        }
        compoundTag.put("allEnergy", allEnergy);
        return compoundTag;
    }

    protected WirelessEnergyContainer readTag(CompoundTag engTag) {
        UUID uuid = engTag.getUUID("uuid");
        String en = engTag.getString("energy");
        BigInteger energy = new BigInteger(en.isEmpty() ? "0" : en);
        long rate = engTag.getLong("rate");
        GlobalPos bindPos = readGlobalPos(engTag.getString("dimension"), engTag.getLong("pos"));
        return new WirelessEnergyContainer(uuid, energy, rate, bindPos);
    }

    protected CompoundTag toTag(WirelessEnergyContainer container) {
        CompoundTag engTag = new CompoundTag();
        BigInteger storage = container.getStorage();
        if (!Objects.equals(storage, BigInteger.ZERO)) {
            engTag.putString("energy", storage.toString());
        }
        long rate = container.getRate();
        if (rate != 0) {
            engTag.putLong("rate", rate);
        }
        GlobalPos bindPos = container.getBindPos();
        if (bindPos != null) {
            engTag.putString("dimension", bindPos.dimension().location().toString());
            engTag.putLong("pos", bindPos.pos().asLong());
        }
        if (!engTag.isEmpty()) engTag.putUUID("uuid", container.getUuid());
        return engTag;
    }

    private static GlobalPos readGlobalPos(String dimension, long pos) {
        if (dimension.isEmpty()) return null;
        if (pos == 0) return null;
        ResourceLocation key = ResourceLocation.tryParse(dimension);
        if (key == null) return null;
        return GlobalPos.of(ResourceKey.create(Registries.DIMENSION, key), BlockPos.of(pos));
    }
}
