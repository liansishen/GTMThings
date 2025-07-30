package com.hepdd.gtmthings.client.widget;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

/**
 * 列表组件，用于显示可滚动的项目列表
 *
 * @param <T> 列表项目类型
 */
public class ListWidget<T> extends AbstractWidget {

    // 列表项目
    protected final List<T> items = new ArrayList<>();

    // 当前选中的项目索引
    protected int selectedIndex = -1;

    // 滚动偏移量
    protected int scrollOffset = 0;

    // 项目高度
    protected int itemHeight = 20;

    // 项目渲染函数
    protected BiConsumer<GuiGraphics, ListItem<T>> itemRenderer;

    // 项目点击事件处理器
    protected Consumer<T> onItemClicked;

    // 项目选择事件处理器
    protected Consumer<T> onItemSelected;

    // 项目文本提供者
    protected Function<T, Component> textProvider;

    // 列表背景颜色
    protected int backgroundColor = 0xFF222222;

    // 项目背景颜色
    protected int itemBackgroundColor = 0xFF333333;

    // 选中项目背景颜色
    protected int selectedItemBackgroundColor = 0xFF555555;

    // 悬停项目背景颜色
    protected int hoverItemBackgroundColor = 0xFF444444;

    // 项目文本颜色
    protected int itemTextColor = 0xFFFFFFFF;

    // 选中项目文本颜色
    protected int selectedItemTextColor = 0xFFFFFFFF;

    // 是否显示滚动条
    protected boolean showScrollbar = true;

    // 滚动条宽度
    protected int scrollbarWidth = 6;

    // 滚动条颜色
    protected int scrollbarColor = 0xFF666666;

    // 滚动条悬停颜色
    protected int scrollbarHoverColor = 0xFF888888;

    // 是否正在拖动滚动条
    protected boolean isDraggingScrollbar = false;

    /**
     * 列表项目类，表示列表中的一个项目
     *
     * @param <T> 项目类型
     */
    public class ListItem<T> {
        // 项目数据
        private final T item;

        // 项目索引
        private final int index;

        // 项目X坐标
        private final int x;

        // 项目Y坐标
        private final int y;

        // 项目宽度
        private final int width;

        // 项目高度
        private final int height;

        // 是否选中
        private final boolean isSelected;

        // 是否悬停
        private final boolean isHovered;

        /**
         * 创建一个新的列表项目
         *
         * @param item 项目数据
         * @param index 项目索引
         * @param x 项目X坐标
         * @param y 项目Y坐标
         * @param width 项目宽度
         * @param height 项目高度
         * @param isSelected 是否选中
         * @param isHovered 是否悬停
         */
        public ListItem(T item, int index, int x, int y, int width, int height, boolean isSelected, boolean isHovered) {
            this.item = item;
            this.index = index;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.isSelected = isSelected;
            this.isHovered = isHovered;
        }

        /**
         * 获取项目数据
         *
         * @return 项目数据
         */
        public T getItem() {
            return item;
        }

        /**
         * 获取项目索引
         *
         * @return 项目索引
         */
        public int getIndex() {
            return index;
        }

        /**
         * 获取项目X坐标
         *
         * @return 项目X坐标
         */
        public int getX() {
            return x;
        }

        /**
         * 获取项目Y坐标
         *
         * @return 项目Y坐标
         */
        public int getY() {
            return y;
        }

        /**
         * 获取项目宽度
         * 
         * @return 项目宽度
         */
        public int getWidth() {
            return width;
        }
        
        /**
         * 获取项目高度
         * 
         * @return 项目高度
         */
        public int getHeight() {
            return height;
        }
        
        /**
         * 获取是否选中
         * 
         * @return 如果选中，返回true
         */
        public boolean isSelected() {
            return isSelected;
        }
        
        /**
         * 获取是否悬停
         * 
         * @return 如果悬停，返回true
         */
        public boolean isHovered() {
            return isHovered;
        }
    }
    
    /**
     * 创建一个新的列表组件
     * 
     * @param x X坐标
     * @param y Y坐标
     * @param width 宽度
     * @param height 高度
     */
    public ListWidget(int x, int y, int width, int height) {
        super(x, y, width, height);
        
        // 默认项目渲染函数
        this.itemRenderer = (guiGraphics, item) -> {
            // 渲染项目背景
            int bgColor;
            if (item.isSelected()) {
                bgColor = selectedItemBackgroundColor;
            } else if (item.isHovered()) {
                bgColor = hoverItemBackgroundColor;
            } else {
                bgColor = itemBackgroundColor;
            }
            
            guiGraphics.fill(item.getX(), item.getY(), item.getX() + item.getWidth(), item.getY() + item.getHeight(), bgColor);
            
            // 渲染项目文本
            if (textProvider != null) {
                Component text = textProvider.apply(item.getItem());
                int textColor = item.isSelected() ? selectedItemTextColor : itemTextColor;
                
                guiGraphics.drawString(
                    Minecraft.getInstance().font,
                    text,
                    item.getX() + 4,
                    item.getY() + (item.getHeight() - 8) / 2,
                    textColor
                );
            }
        };
        
        // 默认文本提供者
        this.textProvider = text -> Component.literal((String) text);
    }
    
    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        if (!isVisible()) {
            return;
        }
        
        // 渲染列表背景
        guiGraphics.fill(getAbsoluteX(), getAbsoluteY(), getAbsoluteX() + width, getAbsoluteY() + height, backgroundColor);
        
        // 计算可见项目数量
        int visibleItems = height / itemHeight;
        
        // 计算最大滚动偏移量
        int maxScrollOffset = Math.max(0, items.size() - visibleItems);
        scrollOffset = Math.max(0, Math.min(scrollOffset, maxScrollOffset));
        
        // 计算列表内容宽度（考虑滚动条）
        int contentWidth = width - (showScrollbar && items.size() > visibleItems ? scrollbarWidth : 0);
        
        // 渲染可见项目
        for (int i = 0; i < visibleItems && i + scrollOffset < items.size(); i++) {
            int itemIndex = i + scrollOffset;
            T item = items.get(itemIndex);
            
            int itemX = getAbsoluteX();
            int itemY = getAbsoluteY() + i * itemHeight;
            
            // 检查鼠标是否悬停在项目上
            boolean isHovered = isEnabled() && mouseX >= itemX && mouseX < itemX + contentWidth && mouseY >= itemY && mouseY < itemY + itemHeight;
            
            // 创建列表项目对象
            ListItem<T> listItem = new ListItem<>(item, itemIndex, itemX, itemY, contentWidth, itemHeight, itemIndex == selectedIndex, isHovered);
            
            // 渲染项目
            itemRenderer.accept(guiGraphics, listItem);
        }
        
        // 渲染滚动条
        if (showScrollbar && items.size() > visibleItems) {
            renderScrollbar(guiGraphics, mouseX, mouseY, visibleItems, maxScrollOffset);
        }
    }
    
    /**
     * 渲染滚动条
     * 
     * @param guiGraphics 图形上下文
     * @param mouseX 鼠标X坐标
     * @param mouseY 鼠标Y坐标
     * @param visibleItems 可见项目数量
     * @param maxScrollOffset 最大滚动偏移量
     */
    protected void renderScrollbar(GuiGraphics guiGraphics, int mouseX, int mouseY, int visibleItems, int maxScrollOffset) {
        int scrollbarX = getAbsoluteX() + width - scrollbarWidth;
        int scrollbarY = getAbsoluteY();
        int scrollbarHeight = height;
        
        // 渲染滚动条背景
        guiGraphics.fill(scrollbarX, scrollbarY, scrollbarX + scrollbarWidth, scrollbarY + scrollbarHeight, 0xFF111111);
        
        // 计算滑块高度和位置
        int thumbHeight = Math.max(20, scrollbarHeight * visibleItems / items.size());
        int thumbY = scrollbarY + (scrollbarHeight - thumbHeight) * scrollOffset / maxScrollOffset;
        
        // 检查鼠标是否悬停在滑块上
        boolean isThumbHovered = mouseX >= scrollbarX && mouseX < scrollbarX + scrollbarWidth && mouseY >= thumbY && mouseY < thumbY + thumbHeight;
        
        // 渲染滑块
        int thumbColor = isThumbHovered || isDraggingScrollbar ? scrollbarHoverColor : scrollbarColor;
        guiGraphics.fill(scrollbarX, thumbY, scrollbarX + scrollbarWidth, thumbY + thumbHeight, thumbColor);
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!isEnabled() || !isVisible() || !isInside(mouseX, mouseY)) {
            return false;
        }
        
        // 检查是否点击了滚动条
        if (showScrollbar && items.size() > height / itemHeight) {
            int scrollbarX = getAbsoluteX() + width - scrollbarWidth;
            
            if (mouseX >= scrollbarX && mouseX < scrollbarX + scrollbarWidth) {
                isDraggingScrollbar = true;
                
                // 直接跳转到点击位置
                updateScrollPosition(mouseY);
                
                return true;
            }
        }
        
        // 检查是否点击了项目
        int contentWidth = width - (showScrollbar && items.size() > height / itemHeight ? scrollbarWidth : 0);
        
        if (mouseX >= getAbsoluteX() && mouseX < getAbsoluteX() + contentWidth) {
            int clickedIndex = scrollOffset + (int) ((mouseY - getAbsoluteY()) / itemHeight);
            
            if (clickedIndex >= 0 && clickedIndex < items.size()) {
                // 选中点击的项目
                selectItem(clickedIndex);
                
                // 触发项目点击事件
                if (onItemClicked != null) {
                    onItemClicked.accept(items.get(clickedIndex));
                }
                
                return true;
            }
        }
        
        return false;
    }
    
    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        isDraggingScrollbar = false;
        return false;
    }
    
    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (!isEnabled() || !isVisible()) {
            return false;
        }
        
        // 处理滚动条拖动
        if (isDraggingScrollbar) {
            updateScrollPosition(mouseY);
            return true;
        }
        
        return false;
    }
    
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (!isEnabled() || !isVisible() || !isInside(mouseX, mouseY)) {
            return false;
        }
        
        // 滚动列表
        scrollOffset -= (int) delta;
        
        // 限制滚动范围
        int visibleItems = height / itemHeight;
        int maxScrollOffset = Math.max(0, items.size() - visibleItems);
        scrollOffset = Math.max(0, Math.min(scrollOffset, maxScrollOffset));
        
        return true;
    }
    
    /**
     * 更新滚动位置
     * 
     * @param mouseY 鼠标Y坐标
     */
    protected void updateScrollPosition(double mouseY) {
        int visibleItems = height / itemHeight;
        int maxScrollOffset = Math.max(0, items.size() - visibleItems);
        
        if (maxScrollOffset > 0) {
            // 计算鼠标在滚动条上的相对位置（0.0-1.0）
            double scrollPercentage = Math.max(0.0, Math.min(1.0, (mouseY - getAbsoluteY()) / height));
            
            // 更新滚动偏移量
            scrollOffset = (int) (maxScrollOffset * scrollPercentage);
        }
    }
    
    /**
     * 选中项目
     * 
     * @param index 项目索引
     */
    public void selectItem(int index) {
        if (index >= 0 && index < items.size() && index != selectedIndex) {
            selectedIndex = index;
            
            // 触发项目选择事件
            if (onItemSelected != null) {
                onItemSelected.accept(items.get(selectedIndex));
            }
        }
    }
    
    /**
     * 添加项目
     * 
     * @param item 项目
     * @return this，用于链式调用
     */
    public ListWidget<T> addItem(T item) {
        items.add(item);
        return this;
    }
    
    /**
     * 添加多个项目
     * 
     * @param items 项目列表
     * @return this，用于链式调用
     */
    public ListWidget<T> addItems(List<T> items) {
        this.items.addAll(items);
        return this;
    }
    
    /**
     * 移除项目
     * 
     * @param item 项目
     * @return this，用于链式调用
     */
    public ListWidget<T> removeItem(T item) {
        items.remove(item);
        
        // 如果移除的是当前选中的项目，取消选中
        if (selectedIndex >= items.size()) {
            selectedIndex = items.isEmpty() ? -1 : items.size() - 1;
        }
        
        return this;
    }
    
    /**
     * 清除所有项目
     * 
     * @return this，用于链式调用
     */
    public ListWidget<T> clearItems() {
        items.clear();
        selectedIndex = -1;
        scrollOffset = 0;
        return this;
    }
    
    /**
     * 设置项目
     * 
     * @param items 项目列表
     * @return this，用于链式调用
     */
    public ListWidget<T> setItems(List<T> items) {
        this.items.clear();
        this.items.addAll(items);
        selectedIndex = -1;
        scrollOffset = 0;
        return this;
    }
    
    /**
     * 获取项目列表
     * 
     * @return 项目列表
     */
    public List<T> getItems() {
        return new ArrayList<>(items);
    }
    
    /**
     * 获取选中的项目
     * 
     * @return 选中的项目，如果没有选中项目，返回null
     */
    public T getSelectedItem() {
        return selectedIndex >= 0 && selectedIndex < items.size() ? items.get(selectedIndex) : null;
    }
    
    /**
     * 获取选中的项目索引
     * 
     * @return 选中的项目索引，如果没有选中项目，返回-1
     */
    public int getSelectedIndex() {
        return selectedIndex;
    }
    
    /**
     * 设置项目高度
     * 
     * @param itemHeight 项目高度
     * @return this，用于链式调用
     */
    public ListWidget<T> setItemHeight(int itemHeight) {
        this.itemHeight = itemHeight;
        return this;
    }
    
    /**
     * 设置项目渲染函数
     * 
     * @param itemRenderer 项目渲染函数
     * @return this，用于链式调用
     */
    public ListWidget<T> setItemRenderer(BiConsumer<GuiGraphics, ListItem<T>> itemRenderer) {
        this.itemRenderer = itemRenderer;
        return this;
    }
    
    /**
     * 设置项目点击事件处理器
     * 
     * @param onItemClicked 项目点击事件处理器
     * @return this，用于链式调用
     */
    public ListWidget<T> setOnItemClicked(Consumer<T> onItemClicked) {
        this.onItemClicked = onItemClicked;
        return this;
    }
    
    /**
     * 设置项目选择事件处理器
     * 
     * @param onItemSelected 项目选择事件处理器
     * @return this，用于链式调用
     */
    public ListWidget<T> setOnItemSelected(Consumer<T> onItemSelected) {
        this.onItemSelected = onItemSelected;
        return this;
    }
    
    /**
     * 设置项目文本提供者
     * 
     * @param textProvider 项目文本提供者
     * @return this，用于链式调用
     */
    public ListWidget<T> setTextProvider(Function<T, Component> textProvider) {
        this.textProvider = textProvider;
        return this;
    }
    
    /**
     * 设置列表背景颜色
     * 
     * @param color 颜色
     * @return this，用于链式调用
     */
    public ListWidget<T> setBackgroundColor(int color) {
        this.backgroundColor = color;
        return this;
    }
    
    /**
     * 设置项目背景颜色
     * 
     * @param color 颜色
     * @return this，用于链式调用
     */
    public ListWidget<T> setItemBackgroundColor(int color) {
        this.itemBackgroundColor = color;
        return this;
    }
    
    /**
     * 设置选中项目背景颜色
     * 
     * @param color 颜色
     * @return this，用于链式调用
     */
    public ListWidget<T> setSelectedItemBackgroundColor(int color) {
        this.selectedItemBackgroundColor = color;
        return this;
    }
    
    /**
     * 设置悬停项目背景颜色
     * 
     * @param color 颜色
     * @return this，用于链式调用
     */
    public ListWidget<T> setHoverItemBackgroundColor(int color) {
        this.hoverItemBackgroundColor = color;
        return this;
    }
    
    /**
     * 设置项目文本颜色
     * 
     * @param color 颜色
     * @return this，用于链式调用
     */
    public ListWidget<T> setItemTextColor(int color) {
        this.itemTextColor = color;
        return this;
    }
    
    /**
     * 设置选中项目文本颜色
     * 
     * @param color 颜色
     * @return this，用于链式调用
     */
    public ListWidget<T> setSelectedItemTextColor(int color) {
        this.selectedItemTextColor = color;
        return this;
    }
    
    /**
     * 设置是否显示滚动条
     * 
     * @param showScrollbar 是否显示滚动条
     * @return this，用于链式调用
     */
    public ListWidget<T> setShowScrollbar(boolean showScrollbar) {
        this.showScrollbar = showScrollbar;
        return this;
    }
    
    /**
     * 设置滚动条宽度
     * 
     * @param scrollbarWidth 滚动条宽度
     * @return this，用于链式调用
     */
    public ListWidget<T> setScrollbarWidth(int scrollbarWidth) {
        this.scrollbarWidth = scrollbarWidth;
        return this;
    }
    
    /**
     * 设置滚动条颜色
     * 
     * @param color 颜色
     * @return this，用于链式调用
     */
    public ListWidget<T> setScrollbarColor(int color) {
        this.scrollbarColor = color;
        return this;
    }
    
    /**
     * 设置滚动条悬停颜色
     * 
     * @param color 颜色
     * @return this，用于链式调用
     */
    public ListWidget<T> setScrollbarHoverColor(int color) {
        this.scrollbarHoverColor = color;
        return this;
    }
}