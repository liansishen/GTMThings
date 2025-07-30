package com.hepdd.gtmthings.client.widget;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

/**
 * Widget接口的抽象实现，提供了基本功能
 */
public abstract class AbstractWidget implements Widget {

    protected Widget parent;
    protected int relativeX;
    protected int relativeY;
    protected int width;
    protected int height;
    protected boolean visible = true;
    protected boolean enabled = true;
    protected Component tooltip;

    /**
     * 创建一个新的AbstractWidget
     *
     * @param x 相对X坐标
     * @param y 相对Y坐标
     * @param width 宽度
     * @param height 高度
     */
    public AbstractWidget(int x, int y, int width, int height) {
        this.relativeX = x;
        this.relativeY = y;
        this.width = width;
        this.height = height;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        if (!isVisible()) {
            return;
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return isEnabled() && isVisible() && isInside(mouseX, mouseY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return false;
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        return false;
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        return false;
    }

    @Override
    public Widget getParent() {
        return parent;
    }
    
    @Override
    public Widget setParent(Widget parent) {
        this.parent = parent;
        return this;
    }
    
    @Override
    public int getRelativeX() {
        return relativeX;
    }
    
    @Override
    public int getRelativeY() {
        return relativeY;
    }
    
    @Override
    public Widget setRelativePosition(int x, int y) {
        this.relativeX = x;
        this.relativeY = y;
        return this;
    }
    
    @Override
    public int getAbsoluteX() {
        int absoluteX = relativeX;
        if (parent != null) {
            absoluteX += parent.getAbsoluteX();
        }
        return absoluteX;
    }
    
    @Override
    public int getAbsoluteY() {
        int absoluteY = relativeY;
        if (parent != null) {
            absoluteY += parent.getAbsoluteY();
        }
        return absoluteY;
    }
    
    @Override
    public int getX() {
        return getAbsoluteX();
    }

    @Override
    public int getY() {
        return getAbsoluteY();
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public Widget setPosition(int x, int y) {
        // 如果有父widget，转换为相对坐标
        if (parent != null) {
            this.relativeX = x - parent.getAbsoluteX();
            this.relativeY = y - parent.getAbsoluteY();
        } else {
            this.relativeX = x;
            this.relativeY = y;
        }
        return this;
    }

    @Override
    public Widget setSize(int width, int height) {
        this.width = width;
        this.height = height;
        return this;
    }

    @Override
    public boolean isInside(double x, double y) {
        int absoluteX = getAbsoluteX();
        int absoluteY = getAbsoluteY();
        return x >= absoluteX && x < absoluteX + this.width && y >= absoluteY && y < absoluteY + this.height;
    }

    @Override
    public boolean isVisible() {
        return visible;
    }

    @Override
    public Widget setVisible(boolean visible) {
        this.visible = visible;
        return this;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public Widget setEnabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    @Override
    public Component getTooltip() {
        return tooltip;
    }

    @Override
    public Widget setTooltip(Component tooltip) {
        this.tooltip = tooltip;
        return this;
    }

    @Override
    public void update() {
        // 默认实现为空
    }

    @Override
    public void init() {
        // 默认实现为空
    }
}
