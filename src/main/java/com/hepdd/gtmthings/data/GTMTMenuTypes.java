package com.hepdd.gtmthings.data;

import com.hepdd.gtmthings.GTMThings;
import com.hepdd.gtmthings.client.menu.ExampleMenu;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;



public class GTMTMenuTypes {

    public static final DeferredRegister<MenuType<?>> MENU_TYPES =
            DeferredRegister.create(ForgeRegistries.MENU_TYPES, GTMThings.MOD_ID);

    public static final RegistryObject<MenuType<ExampleMenu>> EXAMPLE_MENU =
            MENU_TYPES.register("example_menu", () -> new MenuType<>(ExampleMenu::new,FeatureFlagSet.of()));


}
