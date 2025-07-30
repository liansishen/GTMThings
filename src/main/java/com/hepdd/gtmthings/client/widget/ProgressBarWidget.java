package com.hepdd.gtmthings.client.widget;

import java.util.function.DoubleSupplier;
import java.util.function.Function;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/**
 * 进度条组件，用于显示进度、能量、流体等信息
 */
public class ProgressBarWidget extends AbstractWidget {

    // 进度条方向
    public enum Direction {
        LEFT_TO_RIGHT,
        RIGHT_TO_LEFT,
        BOTTOM_TO_TOP,
        TOP_TO_BOTTOM
    }

    // 进度值提供者
    private DoubleSupplier progressSupplier;

    // 进度条方向
    private Direction direction = Direction.LEFT_TO_RIGHT;

    // 进度条背景颜色
    private int backgroundColor = 0xFF333333;

    // 进度条填充颜色
    private int fillColor = 0xFF00AA00;

    // 进度条边框颜色
    private int borderColor = 0xFF555555;

    // 是否显示边框
    private boolean showBorder = true;

    // 是否显示进度文本
    private boolean showText = false;

    // 进度文本格式化函数
    private Function<Double, Component> textFormatter;

    // 进度条背景纹理
    private ResourceLocation backgroundTexture;

    // 进度条填充纹理
    private ResourceLocation fillTexture;

    // 纹理UV坐标
    private int backgroundU, backgroundV, fillU, fillV;

    // 悬停提示文本
    private Component hoverText;

    /**
     * 创建一个新的进度条组件
     *
     * @param x X坐标
     * @param y Y坐标
     * @param width 宽度
     * @param height 高度
     * @param progressSupplier 进度值提供者，返回0.0-1.0之间的值
     */
    public ProgressBarWidget(int x, int y, int width, int height, DoubleSupplier progressSupplier) {
        super(x, y, width, height);
        this.progressSupplier = progressSupplier;
        this.textFormatter = progress -> Component.literal(String.format("%.0f%%", progress * 100));
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        if (!isVisible()) {
            return;
        }

        // 获取当前进度值（确保在0.0-1.0范围内）
        double progress = Math.max(0.0, Math.min(1.0, progressSupplier.getAsDouble()));

        // 渲染进度条背景
        renderBackground(guiGraphics);

        // 渲染进度条填充
        renderFill(guiGraphics, progress);

        // 渲染边框
        if (showBorder) {
            renderBorder(guiGraphics);
        }

        // 渲染进度文本
        if (showText) {
            renderText(guiGraphics, progress);
        }

        // 检查悬停状态
        boolean isHovered = isEnabled() && mouseX >= getAbsoluteX() && mouseX < getAbsoluteX() + width && mouseY >= getAbsoluteY() && mouseY < getAbsoluteY() + height;
        if (isHovered && hoverText != null) {
            guiGraphics.renderTooltip(Minecraft.getInstance().font, hoverText, mouseX, mouseY);
        }
    }

    /**
     * 渲染进度条背景
     *
     * @param guiGraphics 图形上下文
     */
    protected void renderBackground(GuiGraphics guiGraphics) {
        if (backgroundTexture != null) {
            // 使用纹理渲染背景
            guiGraphics.blit(backgroundTexture, getAbsoluteX(), getAbsoluteY(), backgroundU, backgroundV, width, height, 256, 256);
        } else {
            // 使用颜色渲染背景
            guiGraphics.fill(getAbsoluteX(), getAbsoluteY(), getAbsoluteX() + width, getAbsoluteY() + height, backgroundColor);
        }
    }

    /**
     * 渲染进度条填充
     *
     * @param guiGraphics 图形上下文
     * @param progress 进度值（0.0-1.0）
     */
    protected void renderFill(GuiGraphics guiGraphics, double progress) {
        if (progress <= 0) {
            return;
        }

        int fillWidth = width;
        int fillHeight = height;
        int fillX = getAbsoluteX();
        int fillY = getAbsoluteY();

        // 根据方向计算填充区域
        switch (direction) {
            case LEFT_TO_RIGHT:
                fillWidth = (int) (width * progress);
                break;
            case RIGHT_TO_LEFT:
                fillWidth = (int) (width * progress);
                fillX = getAbsoluteX() + width - fillWidth;
                break;
            case BOTTOM_TO_TOP:
                fillHeight = (int) (height * progress);
                fillY = getAbsoluteY() + height - fillHeight;
                break;
            case TOP_TO_BOTTOM:
                fillHeight = (int) (height * progress);
                break;
        }

        if (fillTexture != null) {
            // 使用纹理渲染填充
            guiGraphics.blit(fillTexture, fillX, fillY, fillU, fillV, fillWidth, fillHeight, 256, 256);
        } else {
            // 使用颜色渲染填充
            guiGraphics.fill(fillX, fillY, fillX + fillWidth, fillY + fillHeight, fillColor);
        }
    }

    /**
     * 渲染进度条边框
     *
     * @param guiGraphics 图形上下文
     */
    protected void renderBorder(GuiGraphics guiGraphics) {
        // 渲染上边框
        guiGraphics.fill(getAbsoluteX(), getAbsoluteY(), getAbsoluteX() + width, getAbsoluteY() + 1, borderColor);
        // 渲染下边框
        guiGraphics.fill(getAbsoluteX(), getAbsoluteY() + height - 1, getAbsoluteX() + width, getAbsoluteY() + height, borderColor);
        // 渲染左边框
        guiGraphics.fill(getAbsoluteX(), getAbsoluteY() + 1, getAbsoluteX() + 1, getAbsoluteY() + height - 1, borderColor);
        // 渲染右边框
        guiGraphics.fill(getAbsoluteX() + width - 1, getAbsoluteY() + 1, getAbsoluteX() + width, getAbsoluteY() + height - 1, borderColor);
    }

    /**
     * 渲染进度文本
     *
     * @param guiGraphics 图形上下文
     * @param progress 进度值（0.0-1.0）
     */
    protected void renderText(GuiGraphics guiGraphics, double progress) {
        Component text = textFormatter.apply(progress);
        int textWidth = Minecraft.getInstance().font.width(text);
        int textX = getAbsoluteX() + (width - textWidth) / 2;
        int textY = getAbsoluteY() + (height - 8) / 2;

        guiGraphics.drawString(Minecraft.getInstance().font, text, textX, textY, 0xFFFFFFFF);
    }

    /**
     * 设置进度条方向
     *
     * @param direction 方向
     * @return this，用于链式调用
     */
    public ProgressBarWidget setDirection(Direction direction) {
        this.direction = direction;
        return this;
    }

    /**
     * 设置进度条背景颜色
     *
     * @param color 颜色
     * @return this，用于链式调用
     */
    public ProgressBarWidget setBackgroundColor(int color) {
        this.backgroundColor = color;
        return this;
    }

    /**
     * 设置进度条填充颜色
     *
     * @param color 颜色
     * @return this，用于链式调用
     */
    public ProgressBarWidget setFillColor(int color) {
        this.fillColor = color;
        return this;
    }

    /**
     * 设置进度条边框颜色
     *
     * @param color 颜色
     * @return this，用于链式调用
     */
    public ProgressBarWidget setBorderColor(int color) {
        this.borderColor = color;
        return this;
    }

    /**
     * 设置是否显示边框
     *
     * @param showBorder 是否显示边框
     * @return this，用于链式调用
     */
    public ProgressBarWidget setShowBorder(boolean showBorder) {
        this.showBorder = showBorder;
        return this;
    }

    /**
     * 设置是否显示进度文本
     *
     * @param showText 是否显示进度文本
     * @return this，用于链式调用
     */
    public ProgressBarWidget setShowText(boolean showText) {
        this.showText = showText;
        return this;
    }

    /**
     * 设置进度文本格式化函数
     *
     * @param textFormatter 文本格式化函数
     * @return this，用于链式调用
     */
    public ProgressBarWidget setTextFormatter(Function<Double, Component> textFormatter) {
        this.textFormatter = textFormatter;
        return this;
    }

    /**
     * 设置进度条背景纹理
     *
     * @param texture 纹理
     * @param u U坐标
     * @param v V坐标
     * @return this，用于链式调用
     */
    public ProgressBarWidget setBackgroundTexture(ResourceLocation texture, int u, int v) {
        this.backgroundTexture = texture;
        this.backgroundU = u;
        this.backgroundV = v;
        return this;
    }

    /**
     * 设置进度条填充纹理
     *
     * @param texture 纹理
     * @param u U坐标
     * @param v V坐标
     * @return this，用于链式调用
     */
    public ProgressBarWidget setFillTexture(ResourceLocation texture, int u, int v) {
        this.fillTexture = texture;
        this.fillU = u;
        this.fillV = v;
        return this;
    }

    /**
     * 设置悬停提示文本
     *
     * @param hoverText 悬停提示文本
     * @return this，用于链式调用
     */
    public ProgressBarWidget setHoverText(Component hoverText) {
        this.hoverText = hoverText;
        return this;
    }

    /**
     * 获取当前进度值
     *
     * @return 进度值（0.0-1.0）
     */
    public double getProgress() {
        return Math.max(0.0, Math.min(1.0, progressSupplier.getAsDouble()));
    }
}
