package com.hepdd.gtmthings.client.widget;

import com.hepdd.gtmthings.client.menu.ExampleMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/**
 * 一个基础的屏幕类，用于使用widget系统创建GUI屏幕
 */
public class WidgetScreen extends AbstractContainerScreen<ExampleMenu> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.parse("textures/gui/container/example.png");
    // 根widget容器
    protected final WidgetContainer rootContainer;

    // 屏幕背景颜色
    protected int backgroundColor = 0xFF000000;
    int screenWidth;
    int screenHeight;

    /**
     * 创建一个新的WidgetScreen
     *
     * @param title 屏幕标题
     */
    public WidgetScreen(ExampleMenu menu, Inventory playerInventory, Component title) {
        super(menu,playerInventory,title);
        screenWidth = 500;
        screenHeight = 500;
//        int gameWidth = Minecraft.getInstance().getWindow().getWidth();
//        int gameHeight = Minecraft.getInstance().getWindow().getHeight();
//        int x = (gameWidth - screenWidth) / 2;
//        int y = Math.max((gameHeight - screenHeight) / 2, 0);
//        this.rootContainer = new WidgetContainer(x, y, screenWidth, screenHeight);
        this.rootContainer = new WidgetContainer(0, 0, screenWidth, screenHeight);
    }



    @Override
    protected void init() {
        super.init();

        // 设置根容器大小为屏幕大小
        //rootContainer.setSize(width, Math.min(height,500));

        // 初始化widgets
        initWidgets();
    }

    @Override
    public void resize(Minecraft minecraft, int width, int height) {
        super.resize(minecraft, screenWidth, screenHeight);
        this.repositionElements();
    }

    /**
     * 初始化widgets，子类应该重写此方法来添加widgets
     */
    protected void initWidgets() {
        // 子类应该重写此方法来添加widgets
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        // 渲染背景
        renderBackground(guiGraphics);

        // 渲染根容器
        rootContainer.render(guiGraphics, mouseX, mouseY, partialTicks);

        // 渲染其他元素
        super.render(guiGraphics, mouseX, mouseY, partialTicks);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float v, int i, int i1) {
        guiGraphics.blit(TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics) {
        // 渲染屏幕背景
        guiGraphics.fill(0, 0, width, height, backgroundColor);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // 将事件传递给根容器
        if (rootContainer.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        // 将事件传递给根容器
        if (rootContainer.mouseReleased(mouseX, mouseY, button)) {
            return true;
        }

        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        // 将事件传递给根容器
        if (rootContainer.mouseDragged(mouseX, mouseY, button, dragX, dragY)) {
            return true;
        }

        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        // 将事件传递给根容器
        if (rootContainer.mouseScrolled(mouseX, mouseY, delta)) {
            return true;
        }

        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // 将事件传递给根容器
        if (rootContainer.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        // 将事件传递给根容器
        if (rootContainer.keyReleased(keyCode, scanCode, modifiers)) {
            return true;
        }

        return super.keyReleased(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        // 将事件传递给根容器
        if (rootContainer.charTyped(codePoint, modifiers)) {
            return true;
        }

        return super.charTyped(codePoint, modifiers);
    }

    /**
     * 添加widget到根容器
     *
     * @param widget 要添加的widget
     * @return 添加的widget
     */
    public <T extends Widget> T addWidget(T widget) {
        rootContainer.addWidget(widget);
        return widget;
    }

    /**
     * 移除widget从根容器
     *
     * @param widget 要移除的widget
     * @return 如果移除成功，返回true
     */
    public boolean removeWidget(Widget widget) {
        return rootContainer.removeWidget(widget);
    }

    /**
     * 清除所有widgets
     */
    public void clearWidgets() {
        rootContainer.clearWidgets();
    }

    /**
     * 获取根容器
     *
     * @return 根容器
     */
    public WidgetContainer getRootContainer() {
        return rootContainer;
    }

    /**
     * 设置屏幕背景颜色
     *
     * @param color 颜色
     * @return this，用于链式调用
     */
    public WidgetScreen setBackgroundColor(int color) {
        this.backgroundColor = color;
        return this;
    }
}
