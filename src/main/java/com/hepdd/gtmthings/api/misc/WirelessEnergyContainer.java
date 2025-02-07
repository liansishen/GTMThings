package com.hepdd.gtmthings.api.misc;

import com.gregtechceu.gtceu.api.machine.MetaMachine;

import net.minecraft.core.GlobalPos;

import com.hepdd.gtmthings.data.WirelessEnergySavaedData;
import com.hepdd.gtmthings.utils.BigIntegerUtils;
import com.hepdd.gtmthings.utils.TeamUtil;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.UUID;
import java.util.WeakHashMap;

@Getter
public class WirelessEnergyContainer {

    public static boolean observed;

    public static final WeakHashMap<MetaMachine, ITransferData> TRANSFER_DATA = new WeakHashMap<>();

    public static final HashMap<UUID, WirelessEnergyContainer> GLOBAL_CACHE = new HashMap<>();

    public static WirelessEnergyContainer getOrCreateContainer(UUID uuid) {
        return GLOBAL_CACHE.computeIfAbsent(TeamUtil.getTeamUUID(uuid), WirelessEnergyContainer::new);
    }

    private BigInteger storage;

    private long rate;

    private GlobalPos bindPos;

    private final UUID uuid;

    public WirelessEnergyContainer(UUID uuid, BigInteger storage, long rate, GlobalPos bindPos) {
        this.storage = storage;
        this.rate = rate;
        this.bindPos = bindPos;
        this.uuid = uuid;
    }

    private WirelessEnergyContainer(UUID uuid) {
        this.uuid = uuid;
        this.storage = BigInteger.ZERO;
    }

    public long addEnergy(long energy, @Nullable MetaMachine machine) {
        if (energy <= 0) return 0;
        long change = Math.min(rate, energy);
        storage = storage.add(BigInteger.valueOf(change));
        WirelessEnergySavaedData.INSTANCE.setDirty(true);
        if (observed && machine != null) {
            TRANSFER_DATA.put(machine, new BasicTransferData(uuid, change));
        }
        return change;
    }

    public long removeEnergy(long energy, @Nullable MetaMachine machine) {
        if (energy <= 0) return 0;
        long change = Math.min(BigIntegerUtils.getLongValue(storage), Math.min(rate, energy));
        storage = storage.subtract(BigInteger.valueOf(change));
        WirelessEnergySavaedData.INSTANCE.setDirty(true);
        if (observed && machine != null) {
            TRANSFER_DATA.put(machine, new BasicTransferData(uuid, -change));
        }
        return change;
    }

    public void setStorage(BigInteger energy) {
        storage = energy;
        WirelessEnergySavaedData.INSTANCE.setDirty(true);
    }

    public void setRate(long rate) {
        this.rate = rate;
        WirelessEnergySavaedData.INSTANCE.setDirty(true);
    }

    public void setBindPos(GlobalPos bindPos) {
        this.bindPos = bindPos;
        WirelessEnergySavaedData.INSTANCE.setDirty(true);
    }
}
