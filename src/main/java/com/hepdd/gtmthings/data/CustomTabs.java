package com.hepdd.gtmthings.data;

import com.gregtechceu.gtceu.common.data.GTCreativeModeTabs;
import com.hepdd.gtmthings.GTMThings;
import com.tterrag.registrate.util.entry.RegistryEntry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;

import static com.hepdd.gtmthings.GTMThings.EXAMPLE_REGISTRATE;
import static net.minecraft.world.item.Items.OAK_SIGN;


public class CustomTabs {

    public static RegistryEntry<CreativeModeTab> GTADDON = EXAMPLE_REGISTRATE.defaultCreativeTab("gt_addon",
                    builder -> builder.displayItems(new GTCreativeModeTabs.RegistrateDisplayItemsGenerator("gt_addon", EXAMPLE_REGISTRATE))
                            .icon(() -> OAK_SIGN.getDefaultInstance())
                            .title(EXAMPLE_REGISTRATE.addLang("itemGroup", new ResourceLocation(GTMThings.MOD_ID, "gt_addon"),
                                    "GTAddon" + " all items"))
                            .build())
            .register();

    public static void init(){
        GTMThings.LOGGER.info("CustomTabs init");
    }

}
