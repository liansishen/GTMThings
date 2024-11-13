package com.hepdd.gtmthings.api.misc;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;

import com.hepdd.gtmthings.data.WirelessEnergySavaedData;
import com.mojang.datafixers.util.Pair;

import java.math.BigInteger;
import java.util.UUID;
import java.util.WeakHashMap;

import static com.hepdd.gtmthings.utils.TeamUtil.getTeamUUID;

public class WirelessEnergyManager {

    public static final WeakHashMap<Pair<UUID, IMachineBlockEntity>, Long> MachineData = new WeakHashMap<>();

    public static void strongCheckOrAddUser(UUID user_uuid) {
        if (!GlobalVariableStorage.GlobalEnergy.containsKey(user_uuid)) {
            GlobalVariableStorage.GlobalEnergy.put(user_uuid, BigInteger.ZERO);
        }
    }

    /**
     * @return The actual energy of change
     */
    public static long addEUToGlobalEnergyMap(UUID user_uuid, long EU, MetaMachine machine) {
        // Mark the data as dirty and in need of saving.
        try {
            WirelessEnergySavaedData.INSTANCE.setDirty(true);

        } catch (Exception exception) {
            System.out.println("COULD NOT MARK GLOBAL ENERGY AS DIRTY IN ADD EU");
            exception.printStackTrace();
        }
        // Get the team UUID. Users are by default in a team with a UUID equal to their player UUID.
        UUID teamUUID = getTeamUUID(user_uuid);
        long rate = GlobalVariableStorage.GlobalRate.getOrDefault(teamUUID, Pair.of(null, 0L)).getSecond();
        if (rate > 0 && EU != 0) {
            EU = EU > 0 ? Math.min(EU, rate) : Math.max(EU, -rate);
            MachineData.put(Pair.of(user_uuid, machine.holder), EU);

            // Get the teams total energy stored. If they are not in the map, return 0 EU.
            BigInteger totalEU = GlobalVariableStorage.GlobalEnergy.getOrDefault(teamUUID, BigInteger.ZERO);
            // When totalEU < 0,set it to 0 and save.
            if (totalEU.signum() < 0) {
                totalEU = BigInteger.ZERO;
                GlobalVariableStorage.GlobalEnergy.put(getTeamUUID(user_uuid), totalEU);
            }

            totalEU = totalEU.add(BigInteger.valueOf(EU));

            // Get personal energy,when > 0, add to team energy and clear personal energy.
            // BigInteger userTotalEU = GlobalVariableStorage.GlobalEnergy.getOrDefault(user_uuid, BigInteger.ZERO);
            // if (userTotalEU.signum() > 0) {
            // totalEU = totalEU.add(userTotalEU);
            // GlobalVariableStorage.GlobalEnergy.put(user_uuid, BigInteger.ZERO);
            // }

            // If there is sufficient EU then complete the operation and return.
            if (totalEU.signum() >= 0) {
                GlobalVariableStorage.GlobalEnergy.put(teamUUID, totalEU);
                // WirelessEnergySavaedData.INSTANCE.updateEnergy(user_uuid,totalEU.longValue());
                return EU;
            }
        }

        // There is insufficient EU so cancel the operation and return 0.
        return 0;
    }

    // ------------------------------------------------------------------------------------

    public static BigInteger getUserEU(UUID user_uuid) {
        BigInteger totalEU = GlobalVariableStorage.GlobalEnergy.getOrDefault(getTeamUUID(user_uuid), BigInteger.ZERO);
        if (totalEU.signum() < 0) {
            WirelessEnergySavaedData.INSTANCE.setDirty(true);
            totalEU = BigInteger.ZERO;
            GlobalVariableStorage.GlobalEnergy.put(getTeamUUID(user_uuid), totalEU);
        }
        return totalEU;
    }

    // This overwrites the EU in the network. Only use this if you are absolutely sure you know what you are doing.
    public static void setUserEU(UUID user_uuid, BigInteger EU) {
        // Mark the data as dirty and in need of saving.
        try {
            WirelessEnergySavaedData.INSTANCE.setDirty(true);
        } catch (Exception exception) {
            System.out.println("COULD NOT MARK GLOBAL ENERGY AS DIRTY IN SET EU");
            exception.printStackTrace();
        }

        GlobalVariableStorage.GlobalEnergy.put(getTeamUUID(user_uuid), EU);
    }

    public static void clearGlobalEnergyInformationMaps() {
        // Do not use this unless you are 100% certain you know what you are doing.
        GlobalVariableStorage.GlobalEnergy.clear();
    }
}
