package com.hepdd.gtmthings.client.widget;

import java.util.function.Consumer;
import java.util.function.Predicate;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

/**
 * 一个文本输入框widget，基于Minecraft的EditBox
 */
public class TextFieldWidget extends AbstractWidget {

    // 内部的EditBox实例
    protected final EditBox editBox;

    // 文本变更事件处理器
    protected Consumer<String> textChangeListener;

    /**
     * 创建一个新的TextFieldWidget
     *
     * @param x X坐标
     * @param y Y坐标
     * @param width 宽度
     * @param height 高度
     * @param text 初始文本
     */
    public TextFieldWidget(int x, int y, int width, int height, String text) {
        this(x, y, width, height, Component.empty(), text);
    }

    /**
     * 创建一个新的TextFieldWidget
     *
     * @param x X坐标
     * @param y Y坐标
     * @param width 宽度
     * @param height 高度
     * @param message 提示文本
     * @param text 初始文本
     */
    public TextFieldWidget(int x, int y, int width, int height, Component message, String text) {
        super(x, y, width, height);

        // 创建内部的EditBox实例
        this.editBox = new EditBox(Minecraft.getInstance().font, x, y, width, height, message);
        this.editBox.setValue(text);

        // 设置EditBox的响应器
        this.editBox.setResponder(this::onTextChanged);
    }

    public TextFieldWidget(int x, int y, int width, int height, MutableComponent literal) {
        this(x,y,width,height,literal.getString());
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        if (!isVisible()) {
            return;
        }

        // 更新EditBox的位置
        editBox.setX(getAbsoluteX());
        editBox.setY(getAbsoluteY());

        // 更新EditBox的启用状态
        editBox.setEditable(isEnabled());

        // 渲染EditBox
        editBox.render(guiGraphics, mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!isEnabled() || !isVisible() || !isInside(mouseX, mouseY)) {
            this.editBox.setFocused(false);
            return false;
        }
        this.editBox.setFocused(true);
        return editBox.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        return editBox.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        return editBox.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return editBox.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        return editBox.keyReleased(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        return editBox.charTyped(codePoint, modifiers);
    }

    @Override
    public Widget setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        editBox.setEditable(enabled);
        return this;
    }

    /**
     * 当文本变更时调用
     *
     * @param text 新文本
     */
    protected void onTextChanged(String text) {
        if (textChangeListener != null) {
            textChangeListener.accept(text);
        }
    }

    /**
     * 获取当前文本
     *
     * @return 当前文本
     */
    public String getText() {
        return editBox.getValue();
    }

    /**
     * 设置文本
     *
     * @param text 新文本
     * @return this，用于链式调用
     */
    public TextFieldWidget setText(String text) {
        editBox.setValue(text);
        return this;
    }

    /**
     * 设置文本变更事件处理器
     *
     * @param listener 文本变更事件处理器
     * @return this，用于链式调用
     */
    public TextFieldWidget setTextChangeListener(Consumer<String> listener) {
        this.textChangeListener = listener;
        return this;
    }

    /**
     * 设置文本验证器
     *
     * @param validator 文本验证器
     * @return this，用于链式调用
     */
    public TextFieldWidget setTextValidator(Predicate<String> validator) {
        editBox.setFilter(validator);
        return this;
    }

    /**
     * 设置最大文本长度
     *
     * @param length 最大文本长度
     * @return this，用于链式调用
     */
    public TextFieldWidget setMaxLength(int length) {
        editBox.setMaxLength(length);
        return this;
    }

    /**
     * 设置提示文本
     *
     * @param hint 提示文本
     * @return this，用于链式调用
     */
    public TextFieldWidget setHint(Component hint) {
        editBox.setHint(hint);
        return this;
    }

    /**
     * 设置文本颜色
     *
     * @param color 文本颜色
     * @return this，用于链式调用
     */
    public TextFieldWidget setTextColor(int color) {
        editBox.setTextColor(color);
        return this;
    }

    /**
     * 设置是否可编辑
     *
     * @param editable 是否可编辑
     * @return this，用于链式调用
     */
    public TextFieldWidget setEditable(boolean editable) {
        editBox.setEditable(editable);
        return this;
    }

    /**
     * 设置焦点
     *
     * @param focused 是否获取焦点
     * @return this，用于链式调用
     */
    public TextFieldWidget setFocused(boolean focused) {
        editBox.setFocused(focused);
        return this;
    }

    /**
     * 是否有焦点
     *
     * @return 如果有焦点，返回true
     */
    public boolean isFocused() {
        return editBox.isFocused();
    }

    /**
     * 获取内部的EditBox实例
     *
     * @return EditBox实例
     */
    public EditBox getEditBox() {
        return editBox;
    }
}
