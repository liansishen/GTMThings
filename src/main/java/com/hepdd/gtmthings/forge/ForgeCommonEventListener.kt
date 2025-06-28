package com.hepdd.gtmthings.forge

import com.hepdd.gtmthings.GTMThings
import com.hepdd.gtmthings.api.misc.WirelessEnergyContainer
import com.hepdd.gtmthings.common.item.WirelessEnergyBindingToolBehavior
import com.hepdd.gtmthings.data.WirelessEnergySavedData
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.Level
import net.minecraftforge.event.TickEvent
import net.minecraftforge.event.TickEvent.ServerTickEvent
import net.minecraftforge.event.level.LevelEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.common.Mod.EventBusSubscriber

@EventBusSubscriber(modid = GTMThings.MOD_ID, bus = EventBusSubscriber.Bus.FORGE)
class ForgeCommonEventListener {

    companion object {
        @JvmStatic @SubscribeEvent
        fun onServerTickEvent(event: ServerTickEvent) {
            if (event.phase == TickEvent.Phase.END) {
                if (event.server.tickCount % 20 == 0) {
                    val refreshBinding = event.server.tickCount % 200 == 0
                    for (container in WirelessEnergySavedData.INSTANCE?.containerMap?.values!!) {
                        if (refreshBinding) {
                            var rate: Long = 0
                            val pos = container.bindPos
                            if (pos != null) {
                                rate = WirelessEnergyBindingToolBehavior.getRate(
                                    event.server.getLevel(pos.dimension()),
                                    pos.pos()
                                )
                            }
                            container.rate = rate
                        }
                        container.energyStat.tick()
                    }
                }
            } else {
                WirelessEnergyContainer.observed = false
            }
        }

        @JvmStatic @SubscribeEvent
        fun serverSetup(event: LevelEvent.Load) {
            (event.level as? ServerLevel)?.let { level ->
                val serverLevel: ServerLevel? = level.server.getLevel(Level.OVERWORLD)
                if (serverLevel == null) return
                WirelessEnergySavedData.INSTANCE = WirelessEnergySavedData.getOrCreate(serverLevel)
                WirelessEnergyContainer.server = level.server
            }
        }
    }
}