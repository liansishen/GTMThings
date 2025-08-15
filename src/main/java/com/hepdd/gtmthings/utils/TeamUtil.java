package com.hepdd.gtmthings.utils;

import com.gregtechceu.gtceu.GTCEu;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import dev.ftb.mods.ftbteams.api.FTBTeamsAPI;
import dev.ftb.mods.ftbteams.api.Team;

import java.util.Optional;
import java.util.UUID;

public class TeamUtil {

    private static final boolean isFTBTeamsLoaded = GTCEu.isModLoaded("ftbteams");

    public static UUID getTeamUUID(UUID playerUUID) {
        if (isFTBTeamsLoaded && FTBTeamsAPI.api().isManagerLoaded()) {
            var team = FTBTeamsAPI.api().getManager().getTeamForPlayerID(playerUUID);
            return team.map(Team::getTeamId).orElse(playerUUID);
        } else {
            return playerUUID;
        }
    }

    public static Component GetName(Player player) {
        if (isFTBTeamsLoaded && FTBTeamsAPI.api().isManagerLoaded()) {
            Optional<Team> team = FTBTeamsAPI.api().getManager().getTeamForPlayerID(player.getUUID());
            if (team.isPresent()) return team.get().getName();
        }
        return player.getName();
    }

    public static Component GetName(Level level, UUID playerUUID) {
        if (isFTBTeamsLoaded && FTBTeamsAPI.api().isManagerLoaded()) {
            var team = FTBTeamsAPI.api().getManager().getTeamForPlayerID(playerUUID);
            if (team.isPresent()) {
                return team.get().getName();
            }
        }
        Player player = level.getPlayerByUUID(playerUUID);
        if (player != null) return player.getName();
        return Component.literal(playerUUID.toString());
    }

    public static boolean hasOwner(Level level, UUID playerUUID) {
        if (isFTBTeamsLoaded && FTBTeamsAPI.api().isManagerLoaded()) {
            var team = FTBTeamsAPI.api().getManager().getTeamForPlayerID(playerUUID);
            if (team.isPresent()) {
                return true;
            } else {
                return (level.getPlayerByUUID(playerUUID) != null);
            }
        } else {
            return (level.getPlayerByUUID(playerUUID) != null);
        }
    }
}
