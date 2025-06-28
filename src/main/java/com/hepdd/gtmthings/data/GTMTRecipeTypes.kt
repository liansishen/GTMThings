package com.hepdd.gtmthings.data

import com.gregtechceu.gtceu.api.capability.recipe.IO
import com.gregtechceu.gtceu.api.gui.GuiTextures
import com.gregtechceu.gtceu.api.recipe.GTRecipeSerializer
import com.gregtechceu.gtceu.api.recipe.GTRecipeType
import com.gregtechceu.gtceu.api.registry.GTRegistries
import com.hepdd.gtmthings.GTMThings.Companion.id
import com.lowdragmc.lowdraglib.gui.texture.ProgressTexture.FillDirection
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.world.item.crafting.RecipeSerializer
import net.minecraft.world.item.crafting.RecipeType

object GTMTRecipeTypes {
    const val ELECTRIC: String = "electric"

    val DIGITAL_MINER_RECIPE: GTRecipeType? = register("digital_miner", ELECTRIC)
        .setMaxIOSize(0, 27, 0, 0).setEUIO(IO.IN)
        .setSlotOverlay(false, false, GuiTextures.SLOT)
        .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW, FillDirection.LEFT_TO_RIGHT)

    fun register(name: String, group: String?, vararg proxyRecipes: RecipeType<*>?): GTRecipeType {
        val recipeType = GTRecipeType(id(name), group, *proxyRecipes)
        GTRegistries.register(
            BuiltInRegistries.RECIPE_TYPE,
            recipeType.registryName,
            recipeType
        )
        GTRegistries.register(
            BuiltInRegistries.RECIPE_SERIALIZER,
            recipeType.registryName,
            GTRecipeSerializer()
        )
        GTRegistries.RECIPE_TYPES.register(recipeType.registryName, recipeType)
        return recipeType
    }

    fun init() {}
}
