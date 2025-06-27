package com.hepdd.gtmthings.client

import com.hepdd.gtmthings.GTMThings.Companion.id
import com.hepdd.gtmthings.common.item.VirtualItemProviderBehavior
import com.lowdragmc.lowdraglib.client.model.ModelFactory
import com.lowdragmc.lowdraglib.client.renderer.IRenderer
import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.DefaultVertexFormat
import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.blaze3d.vertex.Tesselator
import com.mojang.blaze3d.vertex.VertexFormat
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.GameRenderer
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.resources.model.BakedModel
import net.minecraft.world.inventory.InventoryMenu
import net.minecraft.world.item.ItemDisplayContext
import net.minecraft.world.item.ItemStack
import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.api.distmarker.OnlyIn

class VirtualItemProviderRenderer:IRenderer {

    companion object {
        @JvmStatic
        var INSTANCE = VirtualItemProviderRenderer()
    }

    @OnlyIn(Dist.CLIENT)
    override fun renderItem(
        stack: ItemStack,
        transformType: ItemDisplayContext,
        leftHand: Boolean,
        poseStack: PoseStack,
        buffer: MultiBufferSource,
        combinedLight: Int,
        combinedOverlay: Int,
        model: BakedModel?
    ) {
        val item = VirtualItemProviderBehavior.getVirtualItem(stack)
        poseStack.pushPose()
        if (!item.isEmpty) {
            val mc = Minecraft.getInstance()
            val bakedModel = mc.itemRenderer.getModel(item, mc.level, mc.player, 0)
            mc.itemRenderer
                .render(item, transformType, leftHand, poseStack, buffer, combinedLight, combinedOverlay, bakedModel)
        }
        if (transformType == ItemDisplayContext.GUI) {
            poseStack.translate(-0.5f, -0.5f, -0.5f)
            val tess = Tesselator.getInstance()
            val builder = tess.builder
            builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX)
            RenderSystem.setShader { GameRenderer.getPositionTexShader() }
            val sprite = ModelFactory.getBlockSprite(id("item/virtual_item_provider"))
            RenderSystem.setShaderTexture(0, InventoryMenu.BLOCK_ATLAS)
            val minU = sprite.u0
            val maxU = sprite.u1
            val minV = sprite.v0
            val maxV = sprite.v1
            val pos = poseStack.last().pose()
            builder.vertex(pos, 1f, 1f, 0f).uv(maxU, minV).endVertex()
            builder.vertex(pos, 0f, 1f, 0f).uv(minU, minV).endVertex()
            builder.vertex(pos, 0f, 0f, 0f).uv(minU, maxV).endVertex()
            builder.vertex(pos, 1f, 0f, 0f).uv(maxU, maxV).endVertex()
            tess.end()
        }
        poseStack.popPose()
    }
}