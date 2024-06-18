package com.example.examplemod.data;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.jetbrains.annotations.NotNull;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.UUID;

import static com.example.examplemod.api.misc.GlobalVariableStorage.GlobalEnergy;

public class WirelessEnergySavaedData extends SavedData {

    public static WirelessEnergySavaedData INSTANCE;
    private ServerLevel serverLevel;
    //private HashMap<UUID,Long> wirelessEnergy;

    public static WirelessEnergySavaedData getOrCreate(ServerLevel serverLevel) {
        return serverLevel.getDataStorage().computeIfAbsent(tag -> new WirelessEnergySavaedData(serverLevel, tag),
                () -> new WirelessEnergySavaedData(serverLevel), "gtceu_wireless_energy");
    }

    public WirelessEnergySavaedData(ServerLevel serverLevel) {
        this.serverLevel = serverLevel;
        //this.wirelessEnergy = new HashMap<>();
    }

    public WirelessEnergySavaedData(ServerLevel serverLevel,CompoundTag tag) {
        this(serverLevel);
        ListTag allEnergy = tag.getList("allEnergy", Tag.TAG_COMPOUND);
        for (int i = 0; i < allEnergy.size(); i++) {
            CompoundTag engTag = allEnergy.getCompound(i);
            GlobalEnergy.put(engTag.getUUID("uuid"), BigInteger.valueOf(engTag.getLong("energy")));
        }
    }

    @Override
    public @NotNull CompoundTag save(CompoundTag compoundTag) {
        ListTag allEnergy = new ListTag();
        for(var entry:GlobalEnergy.entrySet()) {
            CompoundTag engTag = new CompoundTag();
            engTag.putUUID("uuid",entry.getKey());
            engTag.putLong("energy",entry.getValue().longValue());

            allEnergy.add(engTag);
        }
        compoundTag.put("allEnergy",allEnergy);
        return compoundTag;
    }

//    public void updateEnergy(UUID owner,Long EU) {
//        this.wirelessEnergy.put(owner,EU);
//        setDirty();
//    }
}
