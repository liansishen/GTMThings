package com.hepdd.gtmthings.client.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

/**
 * 一个简单的图像widget，用于显示纹理
 */
public class ImageWidget extends AbstractWidget {

    // 纹理资源位置
    protected ResourceLocation texture;

    // 纹理UV坐标
    protected int u;
    protected int v;

    // 纹理区域大小
    protected int textureWidth;
    protected int textureHeight;

    // 纹理文件大小
    protected int fileWidth = 256;
    protected int fileHeight = 256;

    // 渲染颜色 (ARGB)
    protected int color = 0xFFFFFFFF;

    /**
     * 创建一个新的ImageWidget
     *
     * @param x X坐标
     * @param y Y坐标
     * @param width 渲染宽度
     * @param height 渲染高度
     * @param texture 纹理资源位置
     */
    public ImageWidget(int x, int y, int width, int height, ResourceLocation texture) {
        this(x, y, width, height, texture, 0, 0, width, height);
    }

    /**
     * 创建一个新的ImageWidget
     *
     * @param x X坐标
     * @param y Y坐标
     * @param width 渲染宽度
     * @param height 渲染高度
     * @param texture 纹理资源位置
     * @param u 纹理U坐标
     * @param v 纹理V坐标
     * @param textureWidth 纹理区域宽度
     * @param textureHeight 纹理区域高度
     */
    public ImageWidget(int x, int y, int width, int height, ResourceLocation texture, int u, int v, int textureWidth, int textureHeight) {
        super(x, y, width, height);
        this.texture = texture;
        this.u = u;
        this.v = v;
        this.textureWidth = textureWidth;
        this.textureHeight = textureHeight;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        if (!isVisible()) {
            return;
        }

        // 设置渲染颜色
        float a = ((color >> 24) & 0xFF) / 255.0F;
        float r = ((color >> 16) & 0xFF) / 255.0F;
        float g = ((color >> 8) & 0xFF) / 255.0F;
        float b = (color & 0xFF) / 255.0F;

        RenderSystem.setShaderColor(r, g, b, a);

        // 渲染纹理
        guiGraphics.blit(texture, getAbsoluteX(), getAbsoluteY(), width, height, u, v, textureWidth, textureHeight, textureWidth, textureHeight);

        // 重置渲染颜色
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    /**
     * 获取纹理资源位置
     *
     * @return 纹理资源位置
     */
    public ResourceLocation getTexture() {
        return texture;
    }

    /**
     * 设置纹理资源位置
     *
     * @param texture 纹理资源位置
     * @return this，用于链式调用
     */
    public ImageWidget setTexture(ResourceLocation texture) {
        this.texture = texture;
        return this;
    }

    /**
     * 获取纹理U坐标
     *
     * @return 纹理U坐标
     */
    public int getU() {
        return u;
    }

    /**
     * 获取纹理V坐标
     *
     * @return 纹理V坐标
     */
    public int getV() {
        return v;
    }

    /**
     * 设置纹理UV坐标
     *
     * @param u 纹理U坐标
     * @param v 纹理V坐标
     * @return this，用于链式调用
     */
    public ImageWidget setUV(int u, int v) {
        this.u = u;
        this.v = v;
        return this;
    }

    /**
     * 获取纹理区域宽度
     *
     * @return 纹理区域宽度
     */
    public int getTextureWidth() {
        return textureWidth;
    }

    /**
     * 获取纹理区域高度
     *
     * @return 纹理区域高度
     */
    public int getTextureHeight() {
        return textureHeight;
    }

    /**
     * 设置纹理区域大小
     *
     * @param width 纹理区域宽度
     * @param height 纹理区域高度
     * @return this，用于链式调用
     */
    public ImageWidget setTextureSize(int width, int height) {
        this.textureWidth = width;
        this.textureHeight = height;
        return this;
    }

    /**
     * 获取纹理文件宽度
     *
     * @return 纹理文件宽度
     */
    public int getFileWidth() {
        return fileWidth;
    }

    /**
     * 获取纹理文件高度
     *
     * @return 纹理文件高度
     */
    public int getFileHeight() {
        return fileHeight;
    }

    /**
     * 设置纹理文件大小
     *
     * @param width 纹理文件宽度
     * @param height 纹理文件高度
     * @return this，用于链式调用
     */
    public ImageWidget setFileSize(int width, int height) {
        this.fileWidth = width;
        this.fileHeight = height;
        return this;
    }

    /**
     * 获取渲染颜色
     *
     * @return 渲染颜色 (ARGB)
     */
    public int getColor() {
        return color;
    }

    /**
     * 设置渲染颜色
     *
     * @param color 渲染颜色 (ARGB)
     * @return this，用于链式调用
     */
    public ImageWidget setColor(int color) {
        this.color = color;
        return this;
    }

    /**
     * 设置渲染颜色
     *
     * @param a 透明度 (0-255)
     * @param r 红色 (0-255)
     * @param g 绿色 (0-255)
     * @param b 蓝色 (0-255)
     * @return this，用于链式调用
     */
    public ImageWidget setColor(int a, int r, int g, int b) {
        this.color = (a << 24) | (r << 16) | (g << 8) | b;
        return this;
    }
}
