package com.hepdd.gtmthings.data;

import com.hepdd.gtmthings.api.misc.GlobalVariableStorage;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
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

    public WirelessEnergySavaedData(ServerLevel serverLevel,CompoundTag tag) {
        this(serverLevel);
        ListTag allEnergy = tag.getList("allEnergy", Tag.TAG_COMPOUND);
        for (int i = 0; i < allEnergy.size(); i++) {
            CompoundTag engTag = allEnergy.getCompound(i);
            GlobalVariableStorage.GlobalEnergy.put(engTag.getUUID("uuid"), new BigInteger(engTag.getString("energy")));
        }
    }

    @Override
    public @NotNull CompoundTag save(CompoundTag compoundTag) {
        ListTag allEnergy = new ListTag();
        for(var entry: GlobalVariableStorage.GlobalEnergy.entrySet()) {
            CompoundTag engTag = new CompoundTag();
            engTag.putUUID("uuid",entry.getKey());
            engTag.putString("energy",entry.getValue().toString());

            allEnergy.add(engTag);
        }
        compoundTag.put("allEnergy",allEnergy);
        return compoundTag;
    }
}
