package com.hepdd.gtmthings.client.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

/**
 * 一个简单的文本标签widget
 */
public class LabelWidget extends AbstractWidget {

    // 标签文本
    protected Component text;

    // 文本颜色
    protected int color;

    // 是否显示阴影
    protected boolean shadow;

    // 文本对齐方式
    protected TextAlignment alignment;

    /**
     * 文本对齐方式枚举
     */
    public enum TextAlignment {
        LEFT,
        CENTER,
        RIGHT
    }

    /**
     * 创建一个新的LabelWidget
     *
     * @param x X坐标
     * @param y Y坐标
     * @param text 标签文本
     * @param color 文本颜色
     */
    public LabelWidget(int x, int y, Component text, int color) {
        this(x, y, 0, 0, text, color, false, TextAlignment.LEFT);

        // 自动计算宽度和高度
        this.width = Minecraft.getInstance().font.width(text);
        this.height = Minecraft.getInstance().font.lineHeight;
    }

    /**
     * 创建一个新的LabelWidget
     *
     * @param x X坐标
     * @param y Y坐标
     * @param width 宽度
     * @param height 高度
     * @param text 标签文本
     * @param color 文本颜色
     * @param shadow 是否显示阴影
     * @param alignment 文本对齐方式
     */
    public LabelWidget(int x, int y, int width, int height, Component text, int color, boolean shadow, TextAlignment alignment) {
        super(x, y, width, height);
        this.text = text;
        this.color = color;
        this.shadow = shadow;
        this.alignment = alignment;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        if (!isVisible()) {
            return;
        }

        // 根据对齐方式计算文本位置
        int textX = getAbsoluteX();
        if (alignment == TextAlignment.CENTER) {
            textX = getAbsoluteX() + (width - Minecraft.getInstance().font.width(text)) / 2;
        } else if (alignment == TextAlignment.RIGHT) {
            textX = getAbsoluteX() + width - Minecraft.getInstance().font.width(text);
        }

        // 渲染文本
        if (shadow) {
            guiGraphics.drawString(Minecraft.getInstance().font, text, textX, getAbsoluteY(), color, true);
        } else {
            guiGraphics.drawString(Minecraft.getInstance().font, text, textX, getAbsoluteY(), color, false);
        }
    }

    /**
     * 获取标签文本
     *
     * @return 标签文本
     */
    public Component getText() {
        return text;
    }

    /**
     * 设置标签文本
     *
     * @param text 标签文本
     * @return this，用于链式调用
     */
    public LabelWidget setText(Component text) {
        this.text = text;

        // 如果宽度为0，自动更新宽度
        if (this.width == 0) {
            this.width = Minecraft.getInstance().font.width(text);
        }

        return this;
    }

    /**
     * 获取文本颜色
     *
     * @return 文本颜色
     */
    public int getColor() {
        return color;
    }

    /**
     * 设置文本颜色
     *
     * @param color 文本颜色
     * @return this，用于链式调用
     */
    public LabelWidget setColor(int color) {
        this.color = color;
        return this;
    }

    /**
     * 获取是否显示阴影
     *
     * @return 如果显示阴影，返回true
     */
    public boolean hasShadow() {
        return shadow;
    }

    /**
     * 设置是否显示阴影
     *
     * @param shadow 是否显示阴影
     * @return this，用于链式调用
     */
    public LabelWidget setShadow(boolean shadow) {
        this.shadow = shadow;
        return this;
    }

    /**
     * 获取文本对齐方式
     *
     * @return 文本对齐方式
     */
    public TextAlignment getAlignment() {
        return alignment;
    }

    /**
     * 设置文本对齐方式
     *
     * @param alignment 文本对齐方式
     * @return this，用于链式调用
     */
    public LabelWidget setAlignment(TextAlignment alignment) {
        this.alignment = alignment;
        return this;
    }
}
