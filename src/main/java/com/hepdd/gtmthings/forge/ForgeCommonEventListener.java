package com.hepdd.gtmthings.forge;

import com.gregtechceu.gtceu.utils.TaskHandler;
import com.hepdd.gtmthings.GTMThings;
import com.hepdd.gtmthings.data.WirelessEnergySavaedData;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = GTMThings.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ForgeCommonEventListener {

    @SubscribeEvent
    public static void levelTick(TickEvent.LevelTickEvent event) {
        if (event.phase == TickEvent.Phase.END && event.level instanceof ServerLevel serverLevel) {
            TaskHandler.onTickUpdate(serverLevel);
            //EnvironmentalHazardSavedData.getOrCreate(serverLevel).tick();
            //LocalizedHazardSavedData.getOrCreate(serverLevel).tick();
        }
    }

    @SubscribeEvent
    public static void serverSetup(LevelEvent.Load event) {
        if (event.getLevel() instanceof ServerLevel serverLevel) {
            WirelessEnergySavaedData.INSTANCE =  WirelessEnergySavaedData.getOrCreate(serverLevel);
        }

    }
}
