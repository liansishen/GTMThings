package com.hepdd.gtmthings

import com.gregtechceu.gtceu.api.GTCEuAPI
import com.gregtechceu.gtceu.api.cover.CoverDefinition
import com.gregtechceu.gtceu.api.machine.MachineDefinition
import com.gregtechceu.gtceu.api.recipe.GTRecipeType
import com.hepdd.gtmthings.common.registry.GTMTRegistration
import com.hepdd.gtmthings.config.ConfigHolder
import com.hepdd.gtmthings.data.*
import net.minecraft.resources.ResourceLocation
import net.minecraft.resources.ResourceLocation.tryBuild
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext

@Mod(GTMThings.MOD_ID)
class GTMThings(context: FMLJavaModLoadingContext) {

    companion object {
        const val MOD_ID: String = "gtmthings"
        const val NAME: String = "GTM Things"

        @JvmStatic
        fun id(name: String): ResourceLocation {
            return tryBuild(MOD_ID, name)!!
        }
    }

    init {
        val modEventBus = context.modEventBus
        ConfigHolder.init()
        GTMTRegistration.GTMTHINGS_REGISTRATE.registerEventListeners(modEventBus)
        modEventBus.addGenericListener(
            GTRecipeType::class.java
        ) { event: GTCEuAPI.RegisterEvent<ResourceLocation, GTRecipeType> -> this.registerRecipeTypes(event) }
        modEventBus.addGenericListener(
            MachineDefinition::class.java
        ) { event: GTCEuAPI.RegisterEvent<ResourceLocation, MachineDefinition> ->
            this.registerMachines(
                event
            )
        }
        modEventBus.addGenericListener(
            CoverDefinition::class.java
        ) { event: GTCEuAPI.RegisterEvent<ResourceLocation, CoverDefinition> -> this.registerCovers(event) }
    }

    private fun registerRecipeTypes(event: GTCEuAPI.RegisterEvent<ResourceLocation, GTRecipeType>) {
        GTMTRecipeTypes.init()
    }

    private fun registerMachines(event: GTCEuAPI.RegisterEvent<ResourceLocation, MachineDefinition>) {
        CreativeMachines.init()
        WirelessMachines.init()
        CustomMachines.init()
    }

    private fun registerCovers(event: GTCEuAPI.RegisterEvent<ResourceLocation, CoverDefinition>) {
        CreativeModeTabs.init()
        GTMTCovers.init()
        CustomItems.init()
    }

}