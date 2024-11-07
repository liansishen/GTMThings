package com.hepdd.gtmthings.data;

import com.hepdd.gtmthings.api.misc.GlobalVariableStorage;
import com.mojang.datafixers.util.Pair;
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
import org.jetbrains.annotations.NotNull;

import java.math.BigInteger;

public class WirelessEnergySavaedData extends SavedData {

    public static WirelessEnergySavaedData INSTANCE;
    private ServerLevel serverLevel;

    public static WirelessEnergySavaedData getOrCreate(ServerLevel serverLevel) {
        return serverLevel.getDataStorage().computeIfAbsent(tag -> new WirelessEnergySavaedData(serverLevel, tag),
                () -> new WirelessEnergySavaedData(serverLevel), "gtceu_wireless_energy");
    }

    public WirelessEnergySavaedData(ServerLevel serverLevel) {
        this.serverLevel = serverLevel;
    }

    public WirelessEnergySavaedData(ServerLevel serverLevel, CompoundTag tag) {
        this(serverLevel);
        ListTag allEnergy = tag.getList("allEnergy", Tag.TAG_COMPOUND);
        ListTag allRate = tag.getList("allRate", Tag.TAG_COMPOUND);
        for (int i = 0; i < allEnergy.size(); i++) {
            CompoundTag engTag = allEnergy.getCompound(i);
            GlobalVariableStorage.GlobalEnergy.put(engTag.getUUID("uuid"),
                    new BigInteger(engTag.getString("energy").isEmpty()?"0":engTag.getString("energy")));
        }
        for (int i = 0; i < allRate.size(); i++) {
            CompoundTag rateTag = allRate.getCompound(i);
            GlobalVariableStorage.GlobalRate.put(rateTag.getUUID("uuid"),
                    Pair.of(readGlobalPos(rateTag.getString("dimension"), rateTag.getLong("pos")), rateTag.getLong("rate")));
        }
    }

    @Override
    public @NotNull CompoundTag save(@NotNull CompoundTag compoundTag) {
        ListTag allEnergy = new ListTag();
        ListTag allRate = new ListTag();
        for(var entry: GlobalVariableStorage.GlobalEnergy.entrySet()) {
            CompoundTag engTag = new CompoundTag();
            engTag.putUUID("uuid",entry.getKey());
            engTag.putString("energy",entry.getValue().toString());

            allEnergy.add(engTag);
        }
        for(var entry: GlobalVariableStorage.GlobalRate.entrySet()) {
            CompoundTag rateTag = new CompoundTag();
            rateTag.putUUID("uuid", entry.getKey());
            rateTag.putString("dimension", entry.getValue().getFirst().dimension().location().toString());
            rateTag.putLong("pos", entry.getValue().getFirst().pos().asLong());
            rateTag.putLong("rate", entry.getValue().getSecond());

            allRate.add(rateTag);
        }
        compoundTag.put("allEnergy",allEnergy);
        compoundTag.put("allRate",allRate);
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
