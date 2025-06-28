package com.hepdd.gtmthings.client

import net.minecraft.client.Minecraft
import net.minecraft.client.multiplayer.ClientLevel
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.entity.ItemRenderer
import net.minecraft.client.resources.model.BakedModel
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.ItemDisplayContext
import net.minecraft.world.item.ItemStack
import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.api.distmarker.OnlyIn

import com.lowdragmc.lowdraglib.client.renderer.IItemRendererProvider
import com.mojang.blaze3d.vertex.PoseStack

@OnlyIn(Dist.CLIENT)
class ClientUtil {
    companion object {

        @JvmStatic
        fun getItemRenderer(): ItemRenderer = Minecraft.getInstance().itemRenderer

        @JvmStatic
        fun getVanillaModel(stack: ItemStack, level: ClientLevel?, entity: LivingEntity?): BakedModel {
            val shaper = getItemRenderer().itemModelShaper
            val model = shaper.getItemModel(stack.item)
            if (model != null) {
                val bakedmodel = model.overrides.resolve(model, stack, level, entity, 0)
                if (bakedmodel != null) return bakedmodel
            }
            return shaper.modelManager.missingModel
        }

        @JvmStatic
        fun vanillaRender(stack: ItemStack, transformType: ItemDisplayContext, leftHand: Boolean, poseStack: PoseStack, buffer: MultiBufferSource, combinedLight: Int, combinedOverlay: Int, model: BakedModel) {
            IItemRendererProvider.disabled.set(true)
            getItemRenderer().render(
                stack,
                transformType,
                leftHand,
                poseStack,
                buffer,
                combinedLight,
                combinedOverlay,
                model,
            )
            IItemRendererProvider.disabled.set(false)
        }
    }
}
