package com.hepdd.gtmthings.client.widget;

import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

/**
 * 一个简单的按钮widget
 */
public class ButtonWidget extends AbstractWidget {

    // 按钮纹理
    private static final ResourceLocation BUTTON_TEXTURES = ResourceLocation.parse("textures/gui/widgets.png");

    // 按钮状态
    protected boolean isHovered;
    protected boolean isPressed;

    // 按钮文本
    protected Component message;

    // 点击事件处理器
    protected Consumer<ButtonWidget> onPress;

    /**
     * 创建一个新的ButtonWidget
     *
     * @param x X坐标
     * @param y Y坐标
     * @param width 宽度
     * @param height 高度
     * @param message 按钮文本
     * @param onPress 点击事件处理器
     */
    public ButtonWidget(int x, int y, int width, int height, Component message, Consumer<ButtonWidget> onPress) {
        super(x, y, width, height);
        this.message = message;
        this.onPress = onPress;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        if (!isVisible()) {
            return;
        }

        // 更新悬停状态
        this.isHovered = isEnabled() && isInside(mouseX, mouseY);

        // 渲染按钮背景
        renderButton(guiGraphics);

        // 渲染按钮文本
        renderText(guiGraphics);
    }

    /**
     * 渲染按钮背景
     *
     * @param guiGraphics 图形上下文
     */
    protected void renderButton(GuiGraphics guiGraphics) {
        int i = getYImage();
        guiGraphics.blit(BUTTON_TEXTURES, getAbsoluteX(), getAbsoluteY(), 0, 46 + i * 20, width / 2, height);
        guiGraphics.blit(BUTTON_TEXTURES, getAbsoluteX() + width / 2, getAbsoluteY(), 200 - width / 2, 46 + i * 20, width / 2, height);
    }

    /**
     * 获取按钮的Y纹理偏移，基于当前状态
     *
     * @return Y纹理偏移
     */
    protected int getYImage() {
        int i = 1; // 默认状态

        if (!isEnabled()) {
            i = 0; // 禁用状态
        } else if (isPressed) {
            i = 2; // 按下状态
        } else if (isHovered) {
            i = 2; // 悬停状态
        }

        return i;
    }

    /**
     * 渲染按钮文本
     *
     * @param guiGraphics 图形上下文
     */
    protected void renderText(GuiGraphics guiGraphics) {
        int textColor = isEnabled() ? 0xFFFFFF : 0xA0A0A0;

        if (isHovered && isEnabled()) {
            textColor = 0xFFFFA0; // 悬停时文本颜色
        }

        guiGraphics.drawCenteredString(
            Minecraft.getInstance().font,
            message,
                getAbsoluteX() + width / 2,
                getAbsoluteY() + (height - 8) / 2,
            textColor
        );
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (isEnabled() && isVisible() && isInside(mouseX, mouseY) && button == 0) {
            this.isPressed = true;
            playPressSound();
            if (onPress != null) {
                onPress.accept(this);
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (isPressed && button == 0) {
            this.isPressed = false;
            return true;
        }
        return false;
    }

    /**
     * 播放按钮按下的声音
     */
    protected void playPressSound() {
        Minecraft.getInstance().getSoundManager().play(
            net.minecraft.client.resources.sounds.SimpleSoundInstance.forUI(
                SoundEvents.UI_BUTTON_CLICK,
                1.0F
            )
        );
    }

    /**
     * 获取按钮文本
     *
     * @return 按钮文本
     */
    public Component getMessage() {
        return message;
    }

    /**
     * 设置按钮文本
     *
     * @param message 按钮文本
     * @return this，用于链式调用
     */
    public ButtonWidget setMessage(Component message) {
        this.message = message;
        return this;
    }

    /**
     * 设置点击事件处理器
     *
     * @param onPress 点击事件处理器
     * @return this，用于链式调用
     */
    public ButtonWidget setOnPress(Consumer<ButtonWidget> onPress) {
        this.onPress = onPress;
        return this;
    }
}
