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

    public static UUID GetUUID(Player player) {
        if (LDLib.isModLoaded("ftbteams")) {
            return FTBTeamsAPI.api().getManager().getTeamForPlayerID(player.getUUID()).get().getTeamId();
        } else {
            return player.getUUID();
        }
    }
    public static UUID GetUUID(Level level,UUID playerUUID) {
        if (!hasOwner(level,playerUUID)) return null;
        if (LDLib.isModLoaded("ftbteams")) {
            var team = FTBTeamsAPI.api().getManager().getTeamForPlayerID(playerUUID);
            if (team.isEmpty()) {
                return playerUUID;
            } else {
                return team.get().getTeamId();
            }
        } else {
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
            if (team.isEmpty()) {
                try {
                    team = FTBTeamsAPI.api().getManager().getTeamByID(playerUUID);
                    name = team.get().getName();
                } catch (Exception e) {
                    name = Objects.requireNonNull(level.getPlayerByUUID(playerUUID)).getName();
                }
            } else {
                name = team.get().getName();
            }
            return name;
        } else {
            return Objects.requireNonNull(level.getPlayerByUUID(playerUUID)).getName();
        }
    }

    public static boolean hasOwner(Level level,UUID playerUUID) {
        if (LDLib.isModLoaded("ftbteams")) {
            var team = FTBTeamsAPI.api().getManager().getTeamForPlayerID(playerUUID);
            if (team.isEmpty()) {
                try {
                    team = FTBTeamsAPI.api().getManager().getTeamByID(playerUUID);
                    return team.isPresent();
                } catch (Exception e) {
                    return false;
                }
            } else {
                return true;
            }
        } else {
            return (level.getPlayerByUUID(playerUUID)!=null);
        }
    }
}
