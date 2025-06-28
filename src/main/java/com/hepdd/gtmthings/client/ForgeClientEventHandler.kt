package com.hepdd.gtmthings.client

import net.minecraft.client.Camera
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.GameRenderer
import net.minecraft.core.BlockPos
import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.api.distmarker.OnlyIn
import net.minecraftforge.client.event.RenderLevelStageEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.common.Mod.EventBusSubscriber

import com.gregtechceu.gtceu.api.GTValues
import com.hepdd.gtmthings.GTMThings
import com.hepdd.gtmthings.common.block.machine.electric.WirelessEnergyMonitor
import com.lowdragmc.lowdraglib.client.utils.RenderBufferUtils
import com.mojang.blaze3d.platform.GlStateManager
import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.DefaultVertexFormat
import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.blaze3d.vertex.Tesselator
import com.mojang.blaze3d.vertex.VertexFormat

@EventBusSubscriber(modid = GTMThings.MOD_ID, bus = EventBusSubscriber.Bus.FORGE, value = [Dist.CLIENT])
@OnlyIn(Dist.CLIENT)
class ForgeClientEventHandler {
    companion object {
        @SubscribeEvent @JvmStatic
        fun onRenderWorldLast(event: RenderLevelStageEvent) {
            val stage = event.stage
            if (stage === RenderLevelStageEvent.Stage.AFTER_TRIPWIRE_BLOCKS) {
                val level = Minecraft.getInstance().level
                if (level == null) return

                if (WirelessEnergyMonitor.p > 0) {
                    if (GTValues.CLIENT_TIME % 20 == 0L) {
                        WirelessEnergyMonitor.p--
                    }
                    val poseStack = event.poseStack
                    val camera = event.camera
                    val pose = WirelessEnergyMonitor.pPos
                    if (pose == null) return
                    highlightBlock(camera, poseStack, pose, pose)
                }
            }
        }

        @JvmStatic
        private fun highlightBlock(camera: Camera, poseStack: PoseStack, vararg poses: BlockPos?) {
            val pos = camera.position
            poseStack.pushPose()
            poseStack.translate(-pos.x, -pos.y, -pos.z)
            RenderSystem.disableDepthTest()
            RenderSystem.enableBlend()
            RenderSystem.disableCull()
            RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA)
            val tesselator = Tesselator.getInstance()
            val buffer = tesselator.builder
            buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR)
            RenderSystem.setShader { GameRenderer.getPositionColorShader() }
            RenderBufferUtils.renderCubeFace(
                poseStack,
                buffer,
                poses[0]!!.x.toFloat(),
                poses[0]!!.y.toFloat(),
                poses[0]!!.z.toFloat(),
                (poses[1]!!.x + 1).toFloat(),
                (poses[1]!!.y + 1).toFloat(),
                (poses[1]!!.z + 1).toFloat(),
                0.2f,
                0.2f,
                1.0f,
                0.25f,
                true,
            )
            tesselator.end()
            buffer.begin(VertexFormat.Mode.LINES, DefaultVertexFormat.POSITION_COLOR_NORMAL)
            RenderSystem.setShader { GameRenderer.getRendertypeLinesShader() }
            RenderSystem.lineWidth(3f)
            RenderBufferUtils.drawCubeFrame(
                poseStack,
                buffer,
                poses[0]!!.x.toFloat(),
                poses[0]!!.y.toFloat(),
                poses[0]!!.z.toFloat(),
                (poses[1]!!.x + 1).toFloat(),
                (poses[1]!!.y + 1).toFloat(),
                (poses[1]!!.z + 1).toFloat(),
                0.0f,
                0.0f,
                1.0f,
                0.5f,
            )
            tesselator.end()
            RenderSystem.enableCull()
            RenderSystem.disableBlend()
            RenderSystem.enableDepthTest()
            poseStack.popPose()
        }
    }
}
