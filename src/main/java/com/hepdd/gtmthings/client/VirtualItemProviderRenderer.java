package com.hepdd.gtmthings.client;

import com.lowdragmc.lowdraglib.client.model.ModelFactory;
import com.lowdragmc.lowdraglib.client.renderer.IRenderer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import com.hepdd.gtmthings.GTMThings;
import com.hepdd.gtmthings.common.item.VirtualItemProviderBehavior;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import org.joml.Matrix4f;

public final class VirtualItemProviderRenderer implements IRenderer {

    public static final VirtualItemProviderRenderer INSTANCE = new VirtualItemProviderRenderer();

    @Override
    @OnlyIn(Dist.CLIENT)
    public void renderItem(ItemStack stack, ItemDisplayContext transformType, boolean leftHand, PoseStack poseStack, MultiBufferSource buffer, int combinedLight, int combinedOverlay, BakedModel model) {
        ItemStack item = VirtualItemProviderBehavior.getVirtualItem(stack);
        poseStack.pushPose();
        if (!item.isEmpty()) {
            Minecraft mc = Minecraft.getInstance();
            BakedModel bakedModel = mc.getItemRenderer().getModel(item, mc.level, mc.player, 0);
            mc.getItemRenderer().render(item, transformType, leftHand, poseStack, buffer, combinedLight, combinedOverlay, bakedModel);
        }
        if (transformType == ItemDisplayContext.GUI) {
            poseStack.translate(-0.5F, -0.5F, -0.5F);
            Tesselator tess = Tesselator.getInstance();
            BufferBuilder builder = tess.getBuilder();
            builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            TextureAtlasSprite sprite = ModelFactory.getBlockSprite(GTMThings.id("item/virtual_item_provider"));
            RenderSystem.setShaderTexture(0, InventoryMenu.BLOCK_ATLAS);
            float minU = sprite.getU0();
            float maxU = sprite.getU1();
            float minV = sprite.getV0();
            float maxV = sprite.getV1();
            Matrix4f pos = poseStack.last().pose();
            builder.vertex(pos, 1, 1, 0).uv(maxU, minV).endVertex();
            builder.vertex(pos, 0, 1, 0).uv(minU, minV).endVertex();
            builder.vertex(pos, 0, 0, 0).uv(minU, maxV).endVertex();
            builder.vertex(pos, 1, 0, 0).uv(maxU, maxV).endVertex();
            tess.end();
        }
        poseStack.popPose();
    }
}
