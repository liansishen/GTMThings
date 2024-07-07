package com.hepdd.gtmthings.data;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.common.data.GTCreativeModeTabs;
import com.hepdd.gtmthings.GTMThings;
import com.tterrag.registrate.util.entry.RegistryEntry;
import net.minecraft.world.item.CreativeModeTab;

import static com.gregtechceu.gtceu.common.data.GTMachines.CREATIVE_ENERGY;
import static com.hepdd.gtmthings.common.registry.GTMTRegistration.GTMTHINGS_REGISTRATE;
import static com.hepdd.gtmthings.data.CustomMachines.DIGITAL_MINER;
import static com.hepdd.gtmthings.data.WirelessMachines.WIRELESS_ENERGY_INPUT_HATCH;


public class CreativeModeTabs {

    public static final RegistryEntry<CreativeModeTab> CREATIVE_TAB = GTMTHINGS_REGISTRATE
            .defaultCreativeTab("creative",builder -> builder
                    .displayItems(new GTCreativeModeTabs.RegistrateDisplayItemsGenerator("creative", GTMTHINGS_REGISTRATE))
                    .title(GTMTHINGS_REGISTRATE.addLang("itemGroup", GTMThings.id("creative"), GTMThings.NAME))
                    .icon(CREATIVE_ENERGY::asStack)
                    .build())
            .register();

    public static final RegistryEntry<CreativeModeTab> WIRELESS_TAB = GTMTHINGS_REGISTRATE
            .defaultCreativeTab("wireless",builder -> builder
                    .displayItems(new GTCreativeModeTabs.RegistrateDisplayItemsGenerator("wireless", GTMTHINGS_REGISTRATE))
                    .title(GTMTHINGS_REGISTRATE.addLang("itemGroup", GTMThings.id("wireless"), GTMThings.NAME))
                    .icon(WIRELESS_ENERGY_INPUT_HATCH[GTValues.MAX-1]::asStack)
                    .build())
            .register();

    public static final RegistryEntry<CreativeModeTab> MORE_MACHINES = GTMTHINGS_REGISTRATE
            .defaultCreativeTab("more_machines",builder -> builder
                    .displayItems(new GTCreativeModeTabs.RegistrateDisplayItemsGenerator("more_machines", GTMTHINGS_REGISTRATE))
                    .title(GTMTHINGS_REGISTRATE.addLang("itemGroup", GTMThings.id("more_machines"), GTMThings.NAME))
                    .icon(DIGITAL_MINER[2]::asStack)
                    .build())
            .register();

    public static void init(){ }

}
