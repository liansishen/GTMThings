package com.hepdd.gtmthings.utils;

import com.lowdragmc.lowdraglib.LDLib;
import dev.ftb.mods.ftbteams.api.FTBTeamsAPI;
import dev.ftb.mods.ftbteams.api.Team;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.Objects;
import java.util.UUID;

public class TeamUtil {

    public static UUID getTeamUUID(UUID playerUUID) {
        if (LDLib.isModLoaded("ftbteams")) {
            var team = FTBTeamsAPI.api().getManager().getTeamForPlayerID(playerUUID);
            return team.map(Team::getTeamId).orElse(playerUUID);
        } else{
            return playerUUID;
        }
    }

    public static Component GetName(Player player) {
        if (LDLib.isModLoaded("ftbteams")) {
            return FTBTeamsAPI.api().getManager().getTeamForPlayerID(player.getUUID()).get().getName();
        } else {
            return player.getName();
        }
    }

    public static Component GetName(Level level,UUID playerUUID) {
        if (LDLib.isModLoaded("ftbteams")) {
            Component name;
            var team = FTBTeamsAPI.api().getManager().getTeamForPlayerID(playerUUID);
            if (team.isPresent()) {
                return team.get().getName();
            } else {
                return Objects.requireNonNull(level.getPlayerByUUID(playerUUID)).getName();
            }
        } else {
            return Objects.requireNonNull(level.getPlayerByUUID(playerUUID)).getName();
        }
    }

    public static boolean hasOwner(Level level,UUID playerUUID) {
        if (LDLib.isModLoaded("ftbteams")) {
            var team = FTBTeamsAPI.api().getManager().getTeamForPlayerID(playerUUID);
            if (team.isPresent()) {
                return true;
            } else {
                return (level.getPlayerByUUID(playerUUID)!=null);
            }
        } else {
            return (level.getPlayerByUUID(playerUUID)!=null);
        }
    }
}
