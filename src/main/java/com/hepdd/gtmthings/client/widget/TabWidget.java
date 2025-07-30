package com.hepdd.gtmthings.client.widget;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/**
 * 一个标签页widget，允许用户在多个内容页面之间切换
 */
public class TabWidget extends AbstractWidget {

    // 标签页列表
    protected final List<Tab> tabs = new ArrayList<>();

    // 当前选中的标签页索引
    protected int selectedTabIndex = -1;

    // 标签页切换事件处理器
    protected Consumer<Integer> onTabChanged;

    // 标签页高度
    protected int tabHeight = 20;

    // 标签页间距
    protected int tabSpacing = 2;

    // 标签页背景颜色
    protected int tabBackgroundColor = 0xFF333333;

    // 选中标签页背景颜色
    protected int selectedTabBackgroundColor = 0xFF666666;

    // 悬停标签页背景颜色
    protected int hoverTabBackgroundColor = 0xFF555555;

    // 标签页文本颜色
    protected int tabTextColor = 0xFFFFFFFF;

    // 选中标签页文本颜色
    protected int selectedTabTextColor = 0xFFFFFFFF;

    // 标签页内容区域背景颜色
    protected int contentBackgroundColor = 0xFF222222;

    /**
     * 标签页类，表示一个标签页
     */
    public static class Tab {
        // 标签页标题
        protected final Component title;

        // 标签页图标（可选）
        protected final ResourceLocation icon;

        // 标签页内容
        protected final WidgetContainer content;

        // 是否悬停
        protected boolean isHovered;

        /**
         * 创建一个新的标签页
         *
         * @param title 标签页标题
         * @param content 标签页内容
         */
        public Tab(Component title, WidgetContainer content) {
            this(title, null, content);
        }

        /**
         * 创建一个新的标签页
         *
         * @param title 标签页标题
         * @param icon 标签页图标
         * @param content 标签页内容
         */
        public Tab(Component title, ResourceLocation icon, WidgetContainer content) {
            this.title = title;
            this.icon = icon;
            this.content = content;
        }

        /**
         * 获取标签页标题
         *
         * @return 标签页标题
         */
        public Component getTitle() {
            return title;
        }

        /**
         * 获取标签页图标
         *
         * @return 标签页图标
         */
        public ResourceLocation getIcon() {
            return icon;
        }

        /**
         * 获取标签页内容
         *
         * @return 标签页内容
         */
        public WidgetContainer getContent() {
            return content;
        }

        /**
         * 获取是否悬停
         *
         * @return 如果悬停，返回true
         */
        public boolean isHovered() {
            return isHovered;
        }

        /**
         * 设置是否悬停
         *
         * @param hovered 是否悬停
         */
        public void setHovered(boolean hovered) {
            this.isHovered = hovered;
        }
    }

    /**
     * 创建一个新的TabWidget
     *
     * @param x X坐标
     * @param y Y坐标
     * @param width 宽度
     * @param height 高度
     */
    public TabWidget(int x, int y, int width, int height) {
        super(x, y, width, height);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        if (!isVisible()) {
            return;
        }

        // 渲染标签页
        renderTabs(guiGraphics, mouseX, mouseY);

        // 渲染内容区域背景
        renderContentBackground(guiGraphics);

        // 渲染当前标签页内容
        renderContent(guiGraphics, mouseX, mouseY, partialTicks);
    }

    /**
     * 渲染标签页
     *
     * @param guiGraphics 图形上下文
     * @param mouseX 鼠标X坐标
     * @param mouseY 鼠标Y坐标
     */
    protected void renderTabs(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        int tabX = getAbsoluteX();

        // 更新标签页悬停状态并渲染标签页
        for (int i = 0; i < tabs.size(); i++) {
            Tab tab = tabs.get(i);
            int tabWidth = getTabWidth(tab);

            // 检查鼠标是否悬停在标签页上
            boolean isHovered = isEnabled() && mouseX >= tabX && mouseX < tabX + tabWidth && mouseY >= getAbsoluteY() && mouseY < getAbsoluteY() + tabHeight;
            tab.setHovered(isHovered);

            // 渲染标签页
            renderTab(guiGraphics, tab, tabX, i == selectedTabIndex);

            // 更新下一个标签页的X坐标
            tabX += tabWidth + tabSpacing;
        }
    }

    /**
     * 渲染单个标签页
     *
     * @param guiGraphics 图形上下文
     * @param tab 标签页
     * @param tabX 标签页X坐标
     * @param isSelected 是否选中
     */
    protected void renderTab(GuiGraphics guiGraphics, Tab tab, int tabX, boolean isSelected) {
        int tabWidth = getTabWidth(tab);

        // 选择背景颜色
        int backgroundColor;
        if (isSelected) {
            backgroundColor = selectedTabBackgroundColor;
        } else if (tab.isHovered()) {
            backgroundColor = hoverTabBackgroundColor;
        } else {
            backgroundColor = tabBackgroundColor;
        }

        // 渲染标签页背景
        guiGraphics.fill(tabX, getAbsoluteY(), tabX + tabWidth, getAbsoluteY() + tabHeight, backgroundColor);

        // 渲染标签页图标
        if (tab.getIcon() != null) {
            guiGraphics.blit(tab.getIcon(), tabX + 2, getAbsoluteY() + 2, 0, 0, 16, 16, 16, 16);
        }

        // 渲染标签页标题
        int textX = tabX + (tab.getIcon() != null ? 20 : 4);
        int textColor = isSelected ? selectedTabTextColor : tabTextColor;

        guiGraphics.drawString(
            Minecraft.getInstance().font,
            tab.getTitle(),
            textX,
                getAbsoluteY() + (tabHeight - 8) / 2,
            textColor
        );
    }

    /**
     * 渲染内容区域背景
     *
     * @param guiGraphics 图形上下文
     */
    protected void renderContentBackground(GuiGraphics guiGraphics) {
        // 渲染内容区域背景
        guiGraphics.fill(getAbsoluteX(), getAbsoluteY() + tabHeight, getAbsoluteX() + width, getAbsoluteY() + height, contentBackgroundColor);
    }

    /**
     * 渲染当前标签页内容
     *
     * @param guiGraphics 图形上下文
     * @param mouseX 鼠标X坐标
     * @param mouseY 鼠标Y坐标
     * @param partialTicks 部分tick
     */
    protected void renderContent(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        if (selectedTabIndex >= 0 && selectedTabIndex < tabs.size()) {
            Tab tab = tabs.get(selectedTabIndex);
            WidgetContainer content = tab.getContent();

            // 设置内容位置
            content.setPosition(getAbsoluteX(), getAbsoluteY() + tabHeight);

            // 渲染内容
            content.render(guiGraphics, mouseX, mouseY, partialTicks);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!isEnabled() || !isVisible() || !isInside(mouseX, mouseY)) {
            return false;
        }

        // 检查是否点击了标签页
        if (mouseY < getAbsoluteY() + tabHeight) {
            int tabX = getAbsoluteX();

            for (int i = 0; i < tabs.size(); i++) {
                Tab tab = tabs.get(i);
                int tabWidth = getTabWidth(tab);

                if (mouseX >= tabX && mouseX < tabX + tabWidth) {
                    // 切换到点击的标签页
                    selectTab(i);
                    return true;
                }

                tabX += tabWidth + tabSpacing;
            }
        }
        // 如果点击了内容区域，将事件传递给当前标签页内容
        else if (selectedTabIndex >= 0 && selectedTabIndex < tabs.size()) {
            Tab tab = tabs.get(selectedTabIndex);
            return tab.getContent().mouseClicked(mouseX, mouseY, button);
        }

        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (!isEnabled() || !isVisible()) {
            return false;
        }

        // 将事件传递给当前标签页内容
        if (selectedTabIndex >= 0 && selectedTabIndex < tabs.size()) {
            Tab tab = tabs.get(selectedTabIndex);
            return tab.getContent().mouseReleased(mouseX, mouseY, button);
        }

        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (!isEnabled() || !isVisible()) {
            return false;
        }

        // 将事件传递给当前标签页内容
        if (selectedTabIndex >= 0 && selectedTabIndex < tabs.size()) {
            Tab tab = tabs.get(selectedTabIndex);
            return tab.getContent().mouseDragged(mouseX, mouseY, button, dragX, dragY);
        }

        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (!isEnabled() || !isVisible() || !isInside(mouseX, mouseY)) {
            return false;
        }

        // 将事件传递给当前标签页内容
        if (selectedTabIndex >= 0 && selectedTabIndex < tabs.size() && mouseY >= getAbsoluteY() + tabHeight) {
            Tab tab = tabs.get(selectedTabIndex);
            return tab.getContent().mouseScrolled(mouseX, mouseY, delta);
        }

        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (!isEnabled() || !isVisible()) {
            return false;
        }

        // 将事件传递给当前标签页内容
        if (selectedTabIndex >= 0 && selectedTabIndex < tabs.size()) {
            Tab tab = tabs.get(selectedTabIndex);
            return tab.getContent().keyPressed(keyCode, scanCode, modifiers);
        }

        return false;
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        if (!isEnabled() || !isVisible()) {
            return false;
        }

        // 将事件传递给当前标签页内容
        if (selectedTabIndex >= 0 && selectedTabIndex < tabs.size()) {
            Tab tab = tabs.get(selectedTabIndex);
            return tab.getContent().keyReleased(keyCode, scanCode, modifiers);
        }

        return false;
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (!isEnabled() || !isVisible()) {
            return false;
        }

        // 将事件传递给当前标签页内容
        if (selectedTabIndex >= 0 && selectedTabIndex < tabs.size()) {
            Tab tab = tabs.get(selectedTabIndex);
            return tab.getContent().charTyped(codePoint, modifiers);
        }

        return false;
    }

    /**
     * 添加标签页
     *
     * @param title 标签页标题
     * @param content 标签页内容
     * @return this，用于链式调用
     */
    public TabWidget addTab(Component title, WidgetContainer content) {
        return addTab(title, null, content);
    }

    /**
     * 添加标签页
     *
     * @param title 标签页标题
     * @param icon 标签页图标
     * @param content 标签页内容
     * @return this，用于链式调用
     */
    public TabWidget addTab(Component title, ResourceLocation icon, WidgetContainer content) {
        // 创建新标签页
        Tab tab = new Tab(title, icon, content);

        // 设置内容大小
        content.setSize(width, height - tabHeight);

        // 添加标签页
        tabs.add(tab);

        // 如果这是第一个标签页，选中它
        if (tabs.size() == 1) {
            selectTab(0);
        }

        return this;
    }

    /**
     * 移除标签页
     *
     * @param index 标签页索引
     * @return this，用于链式调用
     */
    public TabWidget removeTab(int index) {
        if (index >= 0 && index < tabs.size()) {
            tabs.remove(index);

            // 如果移除的是当前选中的标签页
            if (index == selectedTabIndex) {
                // 如果还有标签页，选中第一个
                if (!tabs.isEmpty()) {
                    selectTab(0);
                } else {
                    selectedTabIndex = -1;
                }
            }
            // 如果移除的标签页在当前选中的标签页之前，需要调整选中的索引
            else if (index < selectedTabIndex) {
                selectedTabIndex--;
            }
        }

        return this;
    }

    /**
     * 选择标签页
     *
     * @param index 标签页索引
     * @return this，用于链式调用
     */
    public TabWidget selectTab(int index) {
        if (index >= 0 && index < tabs.size() && index != selectedTabIndex) {
            selectedTabIndex = index;
            
            // 触发标签页切换事件
            if (onTabChanged != null) {
                onTabChanged.accept(selectedTabIndex);
            }
        }
        
        return this;
    }
    
    /**
     * 获取当前选中的标签页索引
     *
     * @return 当前选中的标签页索引
     */
    public int getSelectedTabIndex() {
        return selectedTabIndex;
    }
    
    /**
     * 获取当前选中的标签页
     *
     * @return 当前选中的标签页，如果没有选中的标签页，返回null
     */
    public Tab getSelectedTab() {
        return selectedTabIndex >= 0 && selectedTabIndex < tabs.size() ? tabs.get(selectedTabIndex) : null;
    }
    
    /**
     * 获取标签页数量
     *
     * @return 标签页数量
     */
    public int getTabCount() {
        return tabs.size();
    }
    
    /**
     * 获取标签页
     *
     * @param index 标签页索引
     * @return 标签页
     */
    public Tab getTab(int index) {
        return index >= 0 && index < tabs.size() ? tabs.get(index) : null;
    }
    
    /**
     * 计算标签页宽度
     *
     * @param tab 标签页
     * @return 标签页宽度
     */
    protected int getTabWidth(Tab tab) {
        int width = 8; // 基础内边距
        
        // 如果有图标，添加图标宽度和间距
        if (tab.getIcon() != null) {
            width += 16 + 2; // 图标宽度 + 间距
        }
        
        // 添加文本宽度
        width += Minecraft.getInstance().font.width(tab.getTitle());
        
        return width;
    }
    
    /**
     * 设置标签页切换事件处理器
     *
     * @param onTabChanged 标签页切换事件处理器
     * @return this，用于链式调用
     */
    public TabWidget setOnTabChanged(Consumer<Integer> onTabChanged) {
        this.onTabChanged = onTabChanged;
        return this;
    }
    
    /**
     * 设置标签页高度
     *
     * @param tabHeight 标签页高度
     * @return this，用于链式调用
     */
    public TabWidget setTabHeight(int tabHeight) {
        this.tabHeight = tabHeight;
        return this;
    }
    
    /**
     * 设置标签页间距
     *
     * @param tabSpacing 标签页间距
     * @return this，用于链式调用
     */
    public TabWidget setTabSpacing(int tabSpacing) {
        this.tabSpacing = tabSpacing;
        return this;
    }
    
    /**
     * 设置标签页背景颜色
     *
     * @param color 颜色
     * @return this，用于链式调用
     */
    public TabWidget setTabBackgroundColor(int color) {
        this.tabBackgroundColor = color;
        return this;
    }
    
    /**
     * 设置选中标签页背景颜色
     *
     * @param color 颜色
     * @return this，用于链式调用
     */
    public TabWidget setSelectedTabBackgroundColor(int color) {
        this.selectedTabBackgroundColor = color;
        return this;
    }
    
    /**
     * 设置悬停标签页背景颜色
     *
     * @param color 颜色
     * @return this，用于链式调用
     */
    public TabWidget setHoverTabBackgroundColor(int color) {
        this.hoverTabBackgroundColor = color;
        return this;
    }
    
    /**
     * 设置标签页文本颜色
     *
     * @param color 颜色
     * @return this，用于链式调用
     */
    public TabWidget setTabTextColor(int color) {
        this.tabTextColor = color;
        return this;
    }
    
    /**
     * 设置选中标签页文本颜色
     *
     * @param color 颜色
     * @return this，用于链式调用
     */
    public TabWidget setSelectedTabTextColor(int color) {
        this.selectedTabTextColor = color;
        return this;
    }
    
    /**
     * 设置内容区域背景颜色
     *
     * @param color 颜色
     * @return this，用于链式调用
     */
    public TabWidget setContentBackgroundColor(int color) {
        this.contentBackgroundColor = color;
        return this;
    }
}