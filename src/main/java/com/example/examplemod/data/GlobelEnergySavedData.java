package com.example.examplemod.data;

import com.example.examplemod.ExampleMod;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GlobelEnergySavedData extends SavedData {

    private final ServerLevel serverLevel;

    @Getter
    @Setter
    private Map<UUID,Long> globelEnergy;

    public static GlobelEnergySavedData getOrCreate(ServerLevel serverLevel) {
        return serverLevel.getDataStorage().computeIfAbsent(tag -> new GlobelEnergySavedData(serverLevel, tag),
                () -> new GlobelEnergySavedData(serverLevel), "gtceu_globel_energy");
    }

    public GlobelEnergySavedData(ServerLevel serverLevel) {
        this.serverLevel = serverLevel;
        globelEnergy = new HashMap<>();

    }

    public GlobelEnergySavedData(ServerLevel serverLevel,CompoundTag tag) {
        this(serverLevel);
        ListTag list = tag.getList("energylist",Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag energy = list.getCompound(i);
            globelEnergy.put(energy.getUUID("ownerUUID"),energy.getLong("energyStored"));
        }
    }

    @Override
    public @NotNull CompoundTag save(CompoundTag compoundTag) {
        ListTag list = new ListTag();
        for (Map.Entry<UUID,Long> entry:globelEnergy.entrySet()) {
            CompoundTag tag = new CompoundTag();
            list.add(tag);

            tag.putUUID("ownerUUID",entry.getKey());
            tag.putLong("energyStored",entry.getValue());
        }
        compoundTag.put("energylist",list);

        return compoundTag;
    }
}
