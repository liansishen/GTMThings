package com.hepdd.gtmthings.client.widget;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

/**
 * 基础Widget接口，定义了所有widget的共同行为
 */
public interface Widget {
    
    /**
     * 获取父widget
     *
     * @return 父widget，如果没有则返回null
     */
    Widget getParent();
    
    /**
     * 设置父widget
     *
     * @param parent 父widget
     * @return this，用于链式调用
     */
    Widget setParent(Widget parent);
    
    /**
     * 获取相对X坐标（相对于父widget）
     *
     * @return 相对X坐标
     */
    int getRelativeX();
    
    /**
     * 获取相对Y坐标（相对于父widget）
     *
     * @return 相对Y坐标
     */
    int getRelativeY();
    
    /**
     * 设置相对位置（相对于父widget）
     *
     * @param x 相对X坐标
     * @param y 相对Y坐标
     * @return this，用于链式调用
     */
    Widget setRelativePosition(int x, int y);
    
    /**
     * 获取绝对X坐标（屏幕坐标）
     *
     * @return 绝对X坐标
     */
    int getAbsoluteX();
    
    /**
     * 获取绝对Y坐标（屏幕坐标）
     *
     * @return 绝对Y坐标
     */
    int getAbsoluteY();

    /**
     * 渲染widget
     *
     * @param guiGraphics 图形上下文
     * @param mouseX 鼠标X坐标
     * @param mouseY 鼠标Y坐标
     * @param partialTicks 部分tick
     */
    void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks);

    /**
     * 处理鼠标点击
     *
     * @param mouseX 鼠标X坐标
     * @param mouseY 鼠标Y坐标
     * @param button 鼠标按钮
     * @return 是否处理了点击事件
     */
    boolean mouseClicked(double mouseX, double mouseY, int button);

    /**
     * 处理鼠标释放
     *
     * @param mouseX 鼠标X坐标
     * @param mouseY 鼠标Y坐标
     * @param button 鼠标按钮
     * @return 是否处理了释放事件
     */
    boolean mouseReleased(double mouseX, double mouseY, int button);

    /**
     * 处理鼠标拖动
     *
     * @param mouseX 鼠标X坐标
     * @param mouseY 鼠标Y坐标
     * @param button 鼠标按钮
     * @param dragX X方向拖动距离
     * @param dragY Y方向拖动距离
     * @return 是否处理了拖动事件
     */
    boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY);

    /**
     * 处理鼠标滚轮
     *
     * @param mouseX 鼠标X坐标
     * @param mouseY 鼠标Y坐标
     * @param delta 滚轮增量
     * @return 是否处理了滚轮事件
     */
    boolean mouseScrolled(double mouseX, double mouseY, double delta);

    /**
     * 处理键盘按键
     *
     * @param keyCode 键码
     * @param scanCode 扫描码
     * @param modifiers 修饰键
     * @return 是否处理了按键事件
     */
    boolean keyPressed(int keyCode, int scanCode, int modifiers);

    /**
     * 处理键盘按键释放
     *
     * @param keyCode 键码
     * @param scanCode 扫描码
     * @param modifiers 修饰键
     * @return 是否处理了按键释放事件
     */
    boolean keyReleased(int keyCode, int scanCode, int modifiers);

    /**
     * 处理字符输入
     *
     * @param codePoint 字符码点
     * @param modifiers 修饰键
     * @return 是否处理了字符输入事件
     */
    boolean charTyped(char codePoint, int modifiers);

    /**
     * 获取widget的X坐标
     *
     * @return X坐标
     */
    int getX();

    /**
     * 获取widget的Y坐标
     *
     * @return Y坐标
     */
    int getY();

    /**
     * 获取widget的宽度
     *
     * @return 宽度
     */
    int getWidth();

    /**
     * 获取widget的高度
     *
     * @return 高度
     */
    int getHeight();

    /**
     * 设置widget的位置
     *
     * @param x X坐标
     * @param y Y坐标
     * @return this，用于链式调用
     */
    Widget setPosition(int x, int y);

    /**
     * 设置widget的大小
     *
     * @param width 宽度
     * @param height 高度
     * @return this，用于链式调用
     */
    Widget setSize(int width, int height);

    /**
     * 检查点(x, y)是否在widget内
     *
     * @param x X坐标
     * @param y Y坐标
     * @return 如果点在widget内，返回true
     */
    boolean isInside(double x, double y);

    /**
     * 获取widget是否可见
     *
     * @return 如果widget可见，返回true
     */
    boolean isVisible();

    /**
     * 设置widget的可见性
     *
     * @param visible 可见性
     * @return this，用于链式调用
     */
    Widget setVisible(boolean visible);

    /**
     * 获取widget是否启用
     *
     * @return 如果widget启用，返回true
     */
    boolean isEnabled();

    /**
     * 设置widget的启用状态
     *
     * @param enabled 启用状态
     * @return this，用于链式调用
     */
    Widget setEnabled(boolean enabled);

    /**
     * 获取widget的提示文本
     *
     * @return 提示文本，如果没有则返回null
     */
    Component getTooltip();

    /**
     * 设置widget的提示文本
     *
     * @param tooltip 提示文本
     * @return this，用于链式调用
     */
    Widget setTooltip(Component tooltip);

    /**
     * 更新widget状态
     */
    void update();

    /**
     * 初始化widget
     */
    void init();
}
