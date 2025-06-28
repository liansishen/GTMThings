package com.hepdd.gtmthings.common.registry

import net.minecraft.resources.ResourceKey
import net.minecraft.world.item.CreativeModeTab

import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate
import com.hepdd.gtmthings.GTMThings

open class GTMTRegistration {

    companion object {
        @JvmStatic
        var GTMTHINGS_REGISTRATE: GTRegistrate = GTRegistrate.create(GTMThings.MOD_ID)

        init {
            GTMTHINGS_REGISTRATE.defaultCreativeTab(null as ResourceKey<CreativeModeTab?>?)
        }
    }

    private fun GTMTRegistration() {
        /**/
    }
}
