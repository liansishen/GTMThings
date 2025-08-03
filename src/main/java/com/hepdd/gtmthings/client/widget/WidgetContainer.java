package com.hepdd.gtmthings.client.widget;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;

/**
 * 一个可以包含其他widget的特殊widget
 */
public class WidgetContainer extends AbstractWidget {

    protected final List<Widget> children = new ArrayList<>();
    protected final int backgroundColor;

    /**
     * 创建一个新的WidgetContainer
     *
     * @param x X坐标
     * @param y Y坐标
     * @param width 宽度
     * @param height 高度
     */
    public WidgetContainer(int x, int y, int width, int height) {
        this(x, y, width, height,0);
    }

    public WidgetContainer(int x, int y, int width, int height, int backgroundColor) {
        super(x, y, width, height);
        this.backgroundColor = backgroundColor;
    }

    /**
     * 添加一个子widget
     *
     * @param widget 要添加的widget
     * @return 添加的widget，用于链式调用
     */
    public <T extends Widget> T addWidget(T widget) {
        children.add(widget);
        widget.setParent(this);
        return widget;
    }

    /**
     * 移除一个子widget
     *
     * @param widget 要移除的widget
     * @return 如果成功移除，返回true
     */
    public boolean removeWidget(Widget widget) {
        return children.remove(widget);
    }

    /**
     * 清除所有子widget
     */
    public void clearWidgets() {
        children.clear();
    }

    /**
     * 获取所有子widget
     *
     * @return 子widget列表
     */
    public List<Widget> getChildren() {
        return new ArrayList<>(children);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        if (!isVisible()) {
            return;
        }

        if (this.backgroundColor != 0) {
            guiGraphics.fill(getX(),getY(),getX() + getWidth(), getY() + getHeight(), backgroundColor);
        }

        // 渲染所有子widget
        for (Widget widget : getChildren()) {
            widget.render(guiGraphics, mouseX, mouseY, partialTicks);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!isEnabled() || !isVisible() || !isInside(mouseX, mouseY)) {
            return false;
        }

        // 从后向前遍历，这样最上面的widget会先处理事件
        for (int i = children.size() - 1; i >= 0; i--) {
            Widget widget = children.get(i);
            if (widget.mouseClicked(mouseX, mouseY, button)) {
                return true;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (!isEnabled() || !isVisible()) {
            return false;
        }

        // 从后向前遍历
        for (int i = children.size() - 1; i >= 0; i--) {
            Widget widget = children.get(i);
            if (widget.mouseReleased(mouseX, mouseY, button)) {
                return true;
            }
        }

        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (!isEnabled() || !isVisible()) {
            return false;
        }

        // 从后向前遍历
        for (int i = children.size() - 1; i >= 0; i--) {
            Widget widget = children.get(i);
            if (widget.mouseDragged(mouseX, mouseY, button, dragX, dragY)) {
                return true;
            }
        }

        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (!isEnabled() || !isVisible() || !isInside(mouseX, mouseY)) {
            return false;
        }

        // 从后向前遍历
        for (int i = children.size() - 1; i >= 0; i--) {
            Widget widget = children.get(i);
            if (widget.mouseScrolled(mouseX, mouseY, delta)) {
                return true;
            }
        }

        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (!isEnabled() || !isVisible()) {
            return false;
        }

        // 从后向前遍历
        for (int i = children.size() - 1; i >= 0; i--) {
            Widget widget = children.get(i);
            if (widget.keyPressed(keyCode, scanCode, modifiers)) {
                return true;
            }
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        if (!isEnabled() || !isVisible()) {
            return false;
        }

        // 从后向前遍历
        for (int i = children.size() - 1; i >= 0; i--) {
            Widget widget = children.get(i);
            if (widget.keyReleased(keyCode, scanCode, modifiers)) {
                return true;
            }
        }

        return super.keyReleased(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (!isEnabled() || !isVisible()) {
            return false;
        }

        // 从后向前遍历
        for (int i = children.size() - 1; i >= 0; i--) {
            Widget widget = children.get(i);
            if (widget.charTyped(codePoint, modifiers)) {
                return true;
            }
        }

        return super.charTyped(codePoint, modifiers);
    }

    @Override
    public void update() {
        super.update();

        // 更新所有子widget
        for (Widget widget : children) {
            widget.update();
        }
    }

    @Override
    public void init() {
        super.init();

        // 初始化所有子widget
        for (Widget widget : children) {
            widget.init();
        }
    }
}
