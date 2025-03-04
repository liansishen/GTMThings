package com.hepdd.gtmthings.client;

import com.gregtechceu.gtceu.api.GTValues;

import com.lowdragmc.lowdraglib.client.utils.RenderBufferUtils;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import com.hepdd.gtmthings.GTMThings;
import com.hepdd.gtmthings.common.block.machine.electric.WirelessEnergyMonitor;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;

@Mod.EventBusSubscriber(modid = GTMThings.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
@OnlyIn(Dist.CLIENT)
public class ForgeClientEventHandler {

    @SubscribeEvent
    public static void onRenderWorldLast(RenderLevelStageEvent event) {
        var stage = event.getStage();
        if (stage == RenderLevelStageEvent.Stage.AFTER_TRIPWIRE_BLOCKS) {
            ClientLevel level = Minecraft.getInstance().level;
            if (level == null) return;

            if (WirelessEnergyMonitor.p > 0) {
                if (GTValues.CLIENT_TIME % 20 == 0) {
                    WirelessEnergyMonitor.p--;
                }
                PoseStack poseStack = event.getPoseStack();
                Camera camera = event.getCamera();
                BlockPos pose = WirelessEnergyMonitor.pPos;
                if (pose == null) return;
                highlightBlock(camera, poseStack, pose, pose);
            }
        }
    }

    private static void highlightBlock(Camera camera, PoseStack poseStack, BlockPos... poses) {
        Vec3 pos = camera.getPosition();
        poseStack.pushPose();
        poseStack.translate(-pos.x, -pos.y, -pos.z);
        RenderSystem.disableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.disableCull();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.getBuilder();
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderBufferUtils.renderCubeFace(poseStack, buffer, poses[0].getX(), poses[0].getY(), poses[0].getZ(), poses[1].getX() + 1, poses[1].getY() + 1, poses[1].getZ() + 1, 0.2f, 0.2f, 1.0f, 0.25f, true);
        tesselator.end();
        buffer.begin(VertexFormat.Mode.LINES, DefaultVertexFormat.POSITION_COLOR_NORMAL);
        RenderSystem.setShader(GameRenderer::getRendertypeLinesShader);
        RenderSystem.lineWidth(3);
        RenderBufferUtils.drawCubeFrame(poseStack, buffer, poses[0].getX(), poses[0].getY(), poses[0].getZ(), poses[1].getX() + 1, poses[1].getY() + 1, poses[1].getZ() + 1, 0.0f, 0.0f, 1.0f, 0.5f);
        tesselator.end();
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
        RenderSystem.enableDepthTest();
        poseStack.popPose();
    }
}
