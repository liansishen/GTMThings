package com.example.examplemod.data;

import com.example.examplemod.ExampleMod;
import com.gregtechceu.gtceu.common.data.GTCreativeModeTabs;
import com.tterrag.registrate.util.entry.RegistryEntry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;

import static com.example.examplemod.ExampleMod.EXAMPLE_REGISTRATE;
import static net.minecraft.world.item.Items.OAK_SIGN;


public class CustomTabs {

    public static RegistryEntry<CreativeModeTab> GTADDON = EXAMPLE_REGISTRATE.defaultCreativeTab("gt_addon",
                    builder -> builder.displayItems(new GTCreativeModeTabs.RegistrateDisplayItemsGenerator("gt_addon", EXAMPLE_REGISTRATE))
                            .icon(() -> OAK_SIGN.getDefaultInstance())
                            .title(EXAMPLE_REGISTRATE.addLang("itemGroup", new ResourceLocation(ExampleMod.MOD_ID, "gt_addon"),
                                    "GTAddon" + " all items"))
                            .build())
            .register();

    public static void init(){
        ExampleMod.LOGGER.info("CustomTabs init");
    }

}
