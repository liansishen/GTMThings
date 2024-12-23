package com.hepdd.gtmthings.forge;

import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import com.hepdd.gtmthings.GTMThings;
import com.hepdd.gtmthings.api.misc.GlobalVariableStorage;
import com.hepdd.gtmthings.common.item.WirelessEnergyBindingToolBehavior;
import com.hepdd.gtmthings.data.WirelessEnergySavaedData;
import com.mojang.datafixers.util.Pair;

import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = GTMThings.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ForgeCommonEventListener {

    @SubscribeEvent
    public static void onServerTickEvent(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END && event.getServer().getTickCount() % 200 == 0) {
            Iterator<Map.Entry<UUID, Pair<GlobalPos, Long>>> iterator = GlobalVariableStorage.GlobalRate.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<UUID, Pair<GlobalPos, Long>> entry = iterator.next();
                UUID uuid = entry.getKey();
                GlobalPos globalPos = entry.getValue().getFirst();
                Pair<Boolean, Long> rate = WirelessEnergyBindingToolBehavior.getRate(event.getServer().getLevel(globalPos.dimension()), globalPos.pos());

                if (rate.getFirst()) {
                    GlobalVariableStorage.GlobalRate.put(uuid, Pair.of(globalPos, rate.getSecond()));
                } else {
                    iterator.remove();
                }
            }
        }
    }

    @SubscribeEvent
    public static void serverSetup(LevelEvent.Load event) {
        if (event.getLevel() instanceof ServerLevel serverLevel) {
            WirelessEnergySavaedData.INSTANCE = WirelessEnergySavaedData.getOrCreate(serverLevel);
        }
    }
}
