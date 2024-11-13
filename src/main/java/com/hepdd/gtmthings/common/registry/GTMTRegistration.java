package com.hepdd.gtmthings.common.registry;

import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;

import com.hepdd.gtmthings.GTMThings;

public class GTMTRegistration {

    public static GTRegistrate GTMTHINGS_REGISTRATE = GTRegistrate.create(GTMThings.MOD_ID);

    static {
        GTMTRegistration.GTMTHINGS_REGISTRATE.defaultCreativeTab((ResourceKey<CreativeModeTab>) null);
    }

    private GTMTRegistration() {/**/}
}
