package com.hepdd.gtmthings.client.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;

/**
 * 屏幕管理器，用于打开各种屏幕
 */
public class ScreenManager {

    /**
     * 打开演示widget屏幕
     *
     * @param player 玩家
     */
    public static void openDemoWidgetScreen(Player player) {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.player == null) return;

        mc.execute(() -> {
            try {
                mc.setScreen(new DemoWidgetScreen(mc,500,500));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

    }
}
