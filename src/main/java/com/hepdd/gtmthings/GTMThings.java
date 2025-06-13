package com.hepdd.gtmthings;

import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.cover.CoverDefinition;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import com.hepdd.gtmthings.config.ConfigHolder;
import com.hepdd.gtmthings.data.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static com.hepdd.gtmthings.common.registry.GTMTRegistration.GTMTHINGS_REGISTRATE;
import static net.minecraft.resources.ResourceLocation.tryBuild;

@Mod(GTMThings.MOD_ID)
public class GTMThings {

    public static final String MOD_ID = "gtmthings";
    public static final String NAME = "GTM Things";
    public static final Logger LOGGER = LogManager.getLogger();

    public static ResourceLocation id(String name) {
        return tryBuild(MOD_ID, name);
    }

    public GTMThings() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        ConfigHolder.init();
        GTMTHINGS_REGISTRATE.registerEventListeners(modEventBus);
        modEventBus.addGenericListener(GTRecipeType.class, this::registerRecipeTypes);
        modEventBus.addGenericListener(MachineDefinition.class, this::registerMachines);
        modEventBus.addGenericListener(CoverDefinition.class, this::registerCovers);
    }

    private void registerRecipeTypes(GTCEuAPI.RegisterEvent<ResourceLocation, GTRecipeType> event) {
        GTMTRecipeTypes.init();
    }

    private void registerMachines(GTCEuAPI.RegisterEvent<ResourceLocation, MachineDefinition> event) {
        CreativeMachines.init();
        WirelessMachines.init();
        CustomMachines.init();
    }

    private void registerCovers(GTCEuAPI.RegisterEvent<ResourceLocation, CoverDefinition> event) {
        CreativeModeTabs.init();
        GTMTCovers.init();
        CustomItems.init();
    }
}
