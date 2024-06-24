package com.hepdd.gtmthings.data;

import com.gregtechceu.gtceu.common.data.GTCreativeModeTabs;
import com.hepdd.gtmthings.GTMThings;
import com.tterrag.registrate.util.entry.RegistryEntry;
import net.minecraft.world.item.CreativeModeTab;

import static com.gregtechceu.gtceu.common.data.GTMachines.CREATIVE_ENERGY;
import static com.hepdd.gtmthings.common.registry.GTMTRegistration.GTMTHINGS_REGISTRATE;


public class CustomTabs {

    public static final RegistryEntry<CreativeModeTab> GTMTHINGS_TAB = GTMTHINGS_REGISTRATE
            .defaultCreativeTab("main",builder -> builder
                    .displayItems(new GTCreativeModeTabs.RegistrateDisplayItemsGenerator("main", GTMTHINGS_REGISTRATE))
                    .title(GTMTHINGS_REGISTRATE.addLang("itemGroup", GTMThings.id("machines"), GTMThings.NAME))
                    .icon(CREATIVE_ENERGY::asStack)
                    .build())
            .register();

    public static void init(){
        GTMThings.LOGGER.info("CustomTabs init");
    }

}
