package com.hepdd.gtmthings.common;

import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate;
import com.hepdd.gtmthings.GTMThings;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;

public class GTRegistration {
    public static final GTRegistrate REGISTRATE = GTRegistrate.create(GTMThings.MOD_ID);

    static {
        com.gregtechceu.gtceu.common.registry.GTRegistration.REGISTRATE.defaultCreativeTab((ResourceKey<CreativeModeTab>) null);
    }

    private GTRegistration() {/**/}
}
