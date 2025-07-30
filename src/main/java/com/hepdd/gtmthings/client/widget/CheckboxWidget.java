package com.hepdd.gtmthings.client.widget;

import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;

/**
 * 一个复选框widget，允许用户选择或取消选择一个选项
 */
public class CheckboxWidget extends AbstractWidget {

    // 复选框纹理
    private static final ResourceLocation CHECKBOX_TEXTURE = ResourceLocation.parse("textures/gui/checkbox.png");

    // 复选框大小
    private static final int CHECKBOX_SIZE = 20;

    // 复选框文本
    protected Component text;

    // 是否选中
    protected boolean selected;

    // 是否悬停
    protected boolean isHovered;

    // 选中状态变更事件处理器
    protected Consumer<Boolean> onSelectionChanged;

    /**
     * 创建一个新的CheckboxWidget
     *
     * @param x X坐标
     * @param y Y坐标
     * @param text 复选框文本
     * @param selected 初始选中状态
     */
    public CheckboxWidget(int x, int y, Component text, boolean selected) {
        this(x, y, text, selected, null);
    }

    /**
     * 创建一个新的CheckboxWidget
     *
     * @param x X坐标
     * @param y Y坐标
     * @param text 复选框文本
     * @param selected 初始选中状态
     * @param onSelectionChanged 选中状态变更事件处理器
     */
    public CheckboxWidget(int x, int y, Component text, boolean selected, Consumer<Boolean> onSelectionChanged) {
        super(x, y, CHECKBOX_SIZE + Minecraft.getInstance().font.width(text) + 2, CHECKBOX_SIZE);
        this.text = text;
        this.selected = selected;
        this.onSelectionChanged = onSelectionChanged;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        if (!isVisible()) {
            return;
        }

        // 更新悬停状态
        this.isHovered = isEnabled() && isInside(mouseX, mouseY);

        // 渲染复选框
        renderCheckbox(guiGraphics);

        // 渲染文本
        renderText(guiGraphics);
    }

    /**
     * 渲染复选框
     *
     * @param guiGraphics 图形上下文
     */
    protected void renderCheckbox(GuiGraphics guiGraphics) {
        // 计算UV坐标
        int u = 0;
        int v = selected ? 20 : 0;

        // 如果禁用，使用灰色纹理
        if (!isEnabled()) {
            v += 40;
        }
        // 如果悬停，使用高亮纹理
        else if (isHovered) {
            v += 20;
        }

        // 渲染复选框
        guiGraphics.blit(CHECKBOX_TEXTURE, getAbsoluteX(), getAbsoluteY(), u, v, CHECKBOX_SIZE, CHECKBOX_SIZE);
    }

    /**
     * 渲染文本
     *
     * @param guiGraphics 图形上下文
     */
    protected void renderText(GuiGraphics guiGraphics) {
        int textColor = isEnabled() ? 0xFFFFFF : 0xA0A0A0;

        guiGraphics.drawString(
            Minecraft.getInstance().font,
            text,
                getAbsoluteX() + CHECKBOX_SIZE + 2,
                getAbsoluteY() + (CHECKBOX_SIZE - 8) / 2,
            textColor
        );
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (isEnabled() && isVisible() && isInside(mouseX, mouseY) && button == 0) {
            // 切换选中状态
            setSelected(!selected);

            // 播放点击声音
            playClickSound();

            return true;
        }
        return false;
    }

    /**
     * 播放点击声音
     */
    protected void playClickSound() {
        Minecraft.getInstance().getSoundManager().play(
            net.minecraft.client.resources.sounds.SimpleSoundInstance.forUI(
                SoundEvents.UI_BUTTON_CLICK,
                1.0F
            )
        );
    }

    /**
     * 获取复选框文本
     *
     * @return 复选框文本
     */
    public Component getText() {
        return text;
    }

    /**
     * 设置复选框文本
     *
     * @param text 复选框文本
     * @return this，用于链式调用
     */
    public CheckboxWidget setText(Component text) {
        this.text = text;
        this.width = CHECKBOX_SIZE + Minecraft.getInstance().font.width(text) + 2;
        return this;
    }

    /**
     * 获取选中状态
     *
     * @return 如果选中，返回true
     */
    public boolean isSelected() {
        return selected;
    }

    /**
     * 设置选中状态
     *
     * @param selected 选中状态
     * @return this，用于链式调用
     */
    public CheckboxWidget setSelected(boolean selected) {
        if (this.selected != selected) {
            this.selected = selected;

            // 通知选中状态变更
            if (onSelectionChanged != null) {
                onSelectionChanged.accept(selected);
            }
        }
        return this;
    }

    /**
     * 设置选中状态变更事件处理器
     *
     * @param onSelectionChanged 选中状态变更事件处理器
     * @return this，用于链式调用
     */
    public CheckboxWidget setOnSelectionChanged(Consumer<Boolean> onSelectionChanged) {
        this.onSelectionChanged = onSelectionChanged;
        return this;
    }
}
