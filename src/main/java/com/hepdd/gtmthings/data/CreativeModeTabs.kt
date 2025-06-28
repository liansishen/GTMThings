package com.hepdd.gtmthings.data

import com.gregtechceu.gtceu.api.GTCEuAPI
import com.gregtechceu.gtceu.api.GTValues
import com.gregtechceu.gtceu.common.data.GTCreativeModeTabs.RegistrateDisplayItemsGenerator
import com.gregtechceu.gtceu.common.data.GTMachines
import com.hepdd.gtmthings.GTMThings
import com.hepdd.gtmthings.GTMThings.Companion.id
import com.hepdd.gtmthings.common.registry.GTMTRegistration
import com.tterrag.registrate.util.entry.RegistryEntry
import net.minecraft.world.item.CreativeModeTab

class CreativeModeTabs {
    companion object {
        @JvmStatic
        val CREATIVE_TAB: RegistryEntry<CreativeModeTab?> = GTMTRegistration.Companion.GTMTHINGS_REGISTRATE
            .defaultCreativeTab("creative") { builder: CreativeModeTab.Builder? ->
                builder!!
                    .displayItems(
                        RegistrateDisplayItemsGenerator(
                            "creative",
                            GTMTRegistration.Companion.GTMTHINGS_REGISTRATE
                        )
                    )
                    .title(
                        GTMTRegistration.Companion.GTMTHINGS_REGISTRATE.addLang(
                            "itemGroup",
                            id("creative"),
                            GTMThings.NAME
                        )
                    )
                    .icon { GTMachines.CREATIVE_ENERGY.asStack() }
                    .build()
            }
            .register()

        @JvmStatic
        val WIRELESS_TAB: RegistryEntry<CreativeModeTab?> = GTMTRegistration.Companion.GTMTHINGS_REGISTRATE
            .defaultCreativeTab("wireless") { builder: CreativeModeTab.Builder? ->
                builder!!
                    .displayItems(
                        RegistrateDisplayItemsGenerator(
                            "wireless",
                            GTMTRegistration.Companion.GTMTHINGS_REGISTRATE
                        )
                    )
                    .title(
                        GTMTRegistration.Companion.GTMTHINGS_REGISTRATE.addLang(
                            "itemGroup",
                            id("wireless"),
                            GTMThings.NAME
                        )
                    )
                    .icon { WirelessMachines.Companion.WIRELESS_ENERGY_INPUT_HATCH[if (GTCEuAPI.isHighTier()) GTValues.MAX else GTValues.UHV - 1]!!.asStack() }
                    .build()
            }
            .register()

        @JvmStatic
        val MORE_MACHINES: RegistryEntry<CreativeModeTab?> = GTMTRegistration.Companion.GTMTHINGS_REGISTRATE
            .defaultCreativeTab("more_machines") { builder: CreativeModeTab.Builder? ->
                builder!!
                    .displayItems(
                        RegistrateDisplayItemsGenerator(
                            "more_machines",
                            GTMTRegistration.Companion.GTMTHINGS_REGISTRATE
                        )
                    )
                    .title(
                        GTMTRegistration.Companion.GTMTHINGS_REGISTRATE.addLang(
                            "itemGroup",
                            id("more_machines"),
                            GTMThings.NAME
                        )
                    )
                    .icon { CustomMachines.DIGITAL_MINER[2]?.asStack() }
                    .build()
            }
            .register()

        fun init() {}
    }
}