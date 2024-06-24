package com.hepdd.gtmthings.common.registry;

import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate;
import com.hepdd.gtmthings.GTMThings;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;

public class GTMTRegistration {

    public static GTRegistrate GTMTHINGS_REGISTRATE = GTRegistrate.create(GTMThings.MOD_ID);

    static {
        GTMTRegistration.GTMTHINGS_REGISTRATE.defaultCreativeTab((ResourceKey<CreativeModeTab>) null);
    }

    private GTMTRegistration() {/**/}
}
