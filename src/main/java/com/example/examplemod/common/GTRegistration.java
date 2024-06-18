package com.example.examplemod.common;

import com.example.examplemod.ExampleMod;
import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;

public class GTRegistration {
    public static final GTRegistrate REGISTRATE = GTRegistrate.create(ExampleMod.MOD_ID);

    static {
        com.gregtechceu.gtceu.common.registry.GTRegistration.REGISTRATE.defaultCreativeTab((ResourceKey<CreativeModeTab>) null);
    }

    private GTRegistration() {/**/}
}
