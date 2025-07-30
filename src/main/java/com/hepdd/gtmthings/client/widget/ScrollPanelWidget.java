package com.hepdd.gtmthings.client.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * 一个可滚动的面板widget，可以包含其他widget并支持滚动查看
 */
public class ScrollPanelWidget extends WidgetContainer {

    // 滚动条纹理
    private static final ResourceLocation SCROLLER_TEXTURE = ResourceLocation.parse("textures/gui/container/creative_inventory/tabs.png");

    // 内容高度
    protected int contentHeight;

    // 当前滚动位置 (0.0 - 1.0)
    protected double scrollPosition;

    // 是否正在拖动滚动条
    protected boolean isDragging;

    // 滚动条宽度
    protected static final int SCROLLBAR_WIDTH = 6;

    // 滚动条背景颜色
    protected int scrollbarBackgroundColor = 0x33000000;

    // 滚动条颜色
    protected int scrollbarColor = 0x66FFFFFF;

    /**
     * 创建一个新的ScrollPanelWidget
     *
     * @param x X坐标
     * @param y Y坐标
     * @param width 宽度
     * @param height 高度
     * @param contentHeight 内容高度
     */
    public ScrollPanelWidget(int x, int y, int width, int height, int contentHeight) {
        super(x, y, width, height);
        this.contentHeight = contentHeight;
        this.scrollPosition = 0.0;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        if (!isVisible()) {
            return;
        }

        int absoluteX = getAbsoluteX();
        int absoluteY = getAbsoluteY();

        // 保存当前的裁剪状态
        guiGraphics.enableScissor(absoluteX, absoluteY, absoluteX + width, absoluteY + height);

        // 计算内容的Y偏移
        int contentOffset = getContentOffset();

        // 渲染内容
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0, -contentOffset, 0);

        // 渲染所有子widget
        for (Widget widget : children) {
            // 只渲染可见区域内的widget
            if (isWidgetVisible(widget, contentOffset)) {
                widget.render(guiGraphics, mouseX, mouseY + contentOffset, partialTicks);
            }
        }

        guiGraphics.pose().popPose();

        // 恢复裁剪状态
        guiGraphics.disableScissor();

        // 渲染滚动条
        renderScrollbar(guiGraphics);
    }

    /**
     * 检查widget是否在可见区域内
     *
     * @param widget 要检查的widget
     * @param contentOffset 内容Y偏移
     * @return 如果widget在可见区域内，返回true
     */
    protected boolean isWidgetVisible(Widget widget, int contentOffset) {
        int widgetY = widget.getRelativeY();
        int widgetHeight = widget.getHeight();

        // 检查widget是否与可见区域相交
        return (widgetY + widgetHeight > contentOffset) && (widgetY < contentOffset + height);
    }

    /**
     * 渲染滚动条
     *
     * @param guiGraphics 图形上下文
     */
    protected void renderScrollbar(GuiGraphics guiGraphics) {
        // 只有当内容高度大于面板高度时才渲染滚动条
        if (contentHeight <= height) {
            return;
        }

        // 计算滚动条位置和大小
        int scrollbarX = getAbsoluteX() + width - SCROLLBAR_WIDTH;
        int scrollbarY = getAbsoluteY();
        int scrollbarHeight = height;

        // 渲染滚动条背景
        guiGraphics.fill(scrollbarX, scrollbarY, scrollbarX + SCROLLBAR_WIDTH, scrollbarY + scrollbarHeight, scrollbarBackgroundColor);

        // 计算滑块位置和大小
        int thumbHeight = Math.max(20, scrollbarHeight * height / contentHeight);
        int thumbY = scrollbarY + (int)((scrollbarHeight - thumbHeight) * scrollPosition);

        // 渲染滑块
        guiGraphics.fill(scrollbarX, thumbY, scrollbarX + SCROLLBAR_WIDTH, thumbY + thumbHeight, scrollbarColor);
    }

    /**
     * 获取内容的Y偏移
     *
     * @return Y偏移
     */
    protected int getContentOffset() {
        return (int)((contentHeight - height) * scrollPosition);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!isEnabled() || !isVisible() || !isInside(mouseX, mouseY)) {
            return false;
        }

        // 检查是否点击了滚动条
        if (button == 0 && isScrollbarVisible() && mouseX >= getAbsoluteX() + width - SCROLLBAR_WIDTH && mouseX < getAbsoluteX() + width) {
            this.isDragging = true;
            updateScrollPosition(mouseY);
            return true;
        }

        // 计算内容的Y偏移
        int contentOffset = getContentOffset();

        // 从后向前遍历，这样最上面的widget会先处理事件
        for (int i = children.size() - 1; i >= 0; i--) {
            Widget widget = children.get(i);
            if (isWidgetVisible(widget, contentOffset) &&
                widget.mouseClicked(mouseX, mouseY + contentOffset, button)) {
                return true;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (isDragging && button == 0) {
            this.isDragging = false;
            return true;
        }

        // 计算内容的Y偏移
        int contentOffset = getContentOffset();

        // 从后向前遍历
        for (int i = children.size() - 1; i >= 0; i--) {
            Widget widget = children.get(i);
            if (isWidgetVisible(widget, contentOffset) &&
                widget.mouseReleased(mouseX, mouseY + contentOffset, button)) {
                return true;
            }
        }

        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (isDragging) {
            updateScrollPosition(mouseY);
            return true;
        }

        // 计算内容的Y偏移
        int contentOffset = getContentOffset();

        // 从后向前遍历
        for (int i = children.size() - 1; i >= 0; i--) {
            Widget widget = children.get(i);
            if (isWidgetVisible(widget, contentOffset) &&
                widget.mouseDragged(mouseX, mouseY + contentOffset, button, dragX, dragY)) {
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

        // 处理滚轮滚动
        if (isScrollbarVisible()) {
            // 计算滚动步长
            double scrollStep = 0.1;

            // 更新滚动位置
            setScrollPosition(scrollPosition - delta * scrollStep);
            return true;
        }

        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    /**
     * 更新滚动位置
     *
     * @param mouseY 鼠标Y坐标
     */
    protected void updateScrollPosition(double mouseY) {
        // 计算相对于滚动条的位置
        double relativeY = (mouseY - getAbsoluteY()) / height;

        // 更新滚动位置
        setScrollPosition(relativeY);
    }

    /**
     * 设置滚动位置
     *
     * @param position 滚动位置 (0.0 - 1.0)
     */
    public void setScrollPosition(double position) {
        // 限制滚动位置在有效范围内
        this.scrollPosition = Math.max(0.0, Math.min(1.0, position));
    }

    /**
     * 获取滚动位置
     *
     * @return 滚动位置 (0.0 - 1.0)
     */
    public double getScrollPosition() {
        return scrollPosition;
    }

    /**
     * 检查滚动条是否可见
     *
     * @return 如果滚动条可见，返回true
     */
    public boolean isScrollbarVisible() {
        return contentHeight > height;
    }

    /**
     * 获取内容高度
     *
     * @return 内容高度
     */
    public int getContentHeight() {
        return contentHeight;
    }

    /**
     * 设置内容高度
     *
     * @param contentHeight 内容高度
     * @return this，用于链式调用
     */
    public ScrollPanelWidget setContentHeight(int contentHeight) {
        this.contentHeight = contentHeight;

        // 如果内容高度变小，可能需要调整滚动位置
        if (contentHeight <= height) {
            this.scrollPosition = 0.0;
        } else if (getContentOffset() > contentHeight - height) {
            this.scrollPosition = 1.0;
        }

        return this;
    }

    /**
     * 设置滚动条背景颜色
     *
     * @param color 颜色
     * @return this，用于链式调用
     */
    public ScrollPanelWidget setScrollbarBackgroundColor(int color) {
        this.scrollbarBackgroundColor = color;
        return this;
    }

    /**
     * 设置滚动条颜色
     *
     * @param color 颜色
     * @return this，用于链式调用
     */
    public ScrollPanelWidget setScrollbarColor(int color) {
        this.scrollbarColor = color;
        return this;
    }
}
