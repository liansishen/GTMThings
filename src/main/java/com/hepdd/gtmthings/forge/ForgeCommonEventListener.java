package com.hepdd.gtmthings.forge;

import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import com.hepdd.gtmthings.GTMThings;
import com.hepdd.gtmthings.api.misc.WirelessEnergyContainer;
import com.hepdd.gtmthings.common.item.WirelessEnergyBindingToolBehavior;
import com.hepdd.gtmthings.data.WirelessEnergySavaedData;

@Mod.EventBusSubscriber(modid = GTMThings.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ForgeCommonEventListener {

    @SubscribeEvent
    public static void onServerTickEvent(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            WirelessEnergyContainer.observed = false;
            if (event.getServer().getTickCount() % 200 == 0) {
                WirelessEnergyContainer.GLOBAL_CACHE.values().forEach(container -> {
                    long rate = 0;
                    GlobalPos pos = container.getBindPos();
                    if (pos != null) {
                        rate = WirelessEnergyBindingToolBehavior.getRate(event.getServer().getLevel(pos.dimension()), pos.pos());
                    }
                    container.setRate(rate);
                });
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
