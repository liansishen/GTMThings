package com.hepdd.gtmthings.common.item.Render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

import com.mojang.blaze3d.vertex.PoseStack;

public class ProgrammingCircuitItemRenderer extends BlockEntityWithoutLevelRenderer {

    public ProgrammingCircuitItemRenderer() {
        super(null, null);
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext displayContext, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        // super.renderByItem(stack, displayContext, poseStack, buffer, packedLight, packedOverlay);
        // stack.getItem();
        // new ResourceLoadStateTracker();
        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
        BakedModel bakedModel = itemRenderer.getModel(stack, null, null, 1);

        poseStack.pushPose();
        poseStack.translate(0.5F, 0.5F, 0.5F);
        float xOffset = -1 / 32f;
        float zOffset = 0;
        poseStack.translate(-xOffset, 0, -zOffset);
        // poseStack.mulPose(Axis.YP.rotationDegrees(degree));
        poseStack.translate(xOffset, 0, zOffset);
        itemRenderer.render(stack, ItemDisplayContext.NONE, false, poseStack, buffer, packedLight, packedOverlay, bakedModel);
        poseStack.popPose();
    }
}
