package com.hepdd.gtmthings.client.widget;

import java.util.function.Consumer;
import java.util.function.Function;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * 一个滑块widget，允许用户通过拖动滑块来选择一个范围内的值
 */
public class SliderWidget extends AbstractWidget {

    // 滑块纹理
    private static final ResourceLocation SLIDER_TEXTURE = ResourceLocation.parse("textures/gui/slider.png");

    // 最小值
    protected double minValue;

    // 最大值
    protected double maxValue;

    // 当前值
    protected double value;

    // 是否正在拖动
    protected boolean isDragging;

    // 是否悬停
    protected boolean isHovered;

    // 值变更事件处理器
    protected Consumer<Double> onValueChanged;

    // 值格式化器
    protected Function<Double, Component> valueFormatter;

    // 步长（如果为0，则连续）
    protected double stepSize;

    /**
     * 创建一个新的SliderWidget
     *
     * @param x X坐标
     * @param y Y坐标
     * @param width 宽度
     * @param height 高度
     * @param minValue 最小值
     * @param maxValue 最大值
     * @param initialValue 初始值
     */
    public SliderWidget(int x, int y, int width, int height, double minValue, double maxValue, double initialValue) {
        this(x, y, width, height, minValue, maxValue, initialValue, null, null);
    }

    /**
     * 创建一个新的SliderWidget
     *
     * @param x X坐标
     * @param y Y坐标
     * @param width 宽度
     * @param height 高度
     * @param minValue 最小值
     * @param maxValue 最大值
     * @param initialValue 初始值
     * @param valueFormatter 值格式化器
     * @param onValueChanged 值变更事件处理器
     */
    public SliderWidget(int x, int y, int width, int height, double minValue, double maxValue, double initialValue,
                        Function<Double, Component> valueFormatter, Consumer<Double> onValueChanged) {
        super(x, y, width, height);
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.value = Mth.clamp(initialValue, minValue, maxValue);
        this.valueFormatter = valueFormatter;
        this.onValueChanged = onValueChanged;
        this.stepSize = 0;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        if (!isVisible()) {
            return;
        }

        // 更新悬停状态
        this.isHovered = isEnabled() && isInside(mouseX, mouseY);

        // 渲染滑块背景
        renderBackground(guiGraphics);

        // 渲染滑块
        renderSlider(guiGraphics);

        // 渲染值
        renderValue(guiGraphics);
    }

    /**
     * 渲染滑块背景
     *
     * @param guiGraphics 图形上下文
     */
    protected void renderBackground(GuiGraphics guiGraphics) {
        // 背景颜色
        int backgroundColor = isEnabled() ? 0xFF000000 : 0xFF555555;

        // 渲染背景
        guiGraphics.fill(getAbsoluteX(), getAbsoluteY() + (height / 2) - 1, getAbsoluteX() + width, getAbsoluteY() + (height / 2) + 1, backgroundColor);
    }

    /**
     * 渲染滑块
     *
     * @param guiGraphics 图形上下文
     */
    protected void renderSlider(GuiGraphics guiGraphics) {
        // 计算滑块位置
        int sliderPos = (int)(getAbsoluteX() + (getNormalizedValue() * (width - 8)));

        // 滑块颜色
        int sliderColor;
        if (!isEnabled()) {
            sliderColor = 0xFF555555; // 禁用状态
        } else if (isDragging) {
            sliderColor = 0xFF0000FF; // 拖动状态
        } else if (isHovered) {
            sliderColor = 0xFF00FFFF; // 悬停状态
        } else {
            sliderColor = 0xFFFFFFFF; // 正常状态
        }

        // 渲染滑块
        guiGraphics.fill(sliderPos, getAbsoluteY(), sliderPos + 8, getAbsoluteY() + height, sliderColor);
    }

    /**
     * 渲染值
     *
     * @param guiGraphics 图形上下文
     */
    protected void renderValue(GuiGraphics guiGraphics) {
        // 如果有值格式化器，渲染格式化后的值
        if (valueFormatter != null) {
            Component formattedValue = valueFormatter.apply(value);

            int textColor = isEnabled() ? 0xFFFFFF : 0xA0A0A0;

            guiGraphics.drawCenteredString(
                Minecraft.getInstance().font,
                formattedValue,
                    getAbsoluteX() + width / 2,
                    getAbsoluteY() - 12,
                textColor
            );
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (isEnabled() && isVisible() && isInside(mouseX, mouseY) && button == 0) {
            // 开始拖动
            this.isDragging = true;

            // 更新值
            updateValueFromMouse(mouseX);

            return true;
        }
        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (isDragging && button == 0) {
            // 结束拖动
            this.isDragging = false;
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (isDragging && button == 0) {
            // 更新值
            updateValueFromMouse(mouseX);
            return true;
        }
        return false;
    }

    /**
     * 根据鼠标位置更新值
     *
     * @param mouseX 鼠标X坐标
     */
    protected void updateValueFromMouse(double mouseX) {
        // 计算归一化值 (0.0 - 1.0)
        double normalized = Mth.clamp((mouseX - getAbsoluteX()) / width, 0.0, 1.0);

        // 计算实际值
        double newValue = minValue + (maxValue - minValue) * normalized;

        // 如果有步长，将值吸附到最近的步长
        if (stepSize > 0) {
            newValue = Math.round(newValue / stepSize) * stepSize;
        }

        // 设置值
        setValue(newValue);
    }

    /**
     * 获取归一化值 (0.0 - 1.0)
     *
     * @return 归一化值
     */
    protected double getNormalizedValue() {
        return (value - minValue) / (maxValue - minValue);
    }

    /**
     * 获取当前值
     *
     * @return 当前值
     */
    public double getValue() {
        return value;
    }

    /**
     * 设置当前值
     *
     * @param value 新值
     * @return this，用于链式调用
     */
    public SliderWidget setValue(double value) {
        // 限制值在有效范围内
        double oldValue = this.value;
        this.value = Mth.clamp(value, minValue, maxValue);

        // 如果值变更，通知监听器
        if (oldValue != this.value && onValueChanged != null) {
            onValueChanged.accept(this.value);
        }

        return this;
    }

    /**
     * 获取最小值
     *
     * @return 最小值
     */
    public double getMinValue() {
        return minValue;
    }

    /**
     * 获取最大值
     *
     * @return 最大值
     */
    public double getMaxValue() {
        return maxValue;
    }

    /**
     * 设置值范围
     *
     * @param minValue 最小值
     * @param maxValue 最大值
     * @return this，用于链式调用
     */
    public SliderWidget setValueRange(double minValue, double maxValue) {
        this.minValue = minValue;
        this.maxValue = maxValue;

        // 确保当前值在新范围内
        setValue(this.value);

        return this;
    }

    /**
     * 设置值变更事件处理器
     *
     * @param onValueChanged 值变更事件处理器
     * @return this，用于链式调用
     */
    public SliderWidget setOnValueChanged(Consumer<Double> onValueChanged) {
        this.onValueChanged = onValueChanged;
        return this;
    }

    /**
     * 设置值格式化器
     *
     * @param valueFormatter 值格式化器
     * @return this，用于链式调用
     */
    public SliderWidget setValueFormatter(Function<Double, Component> valueFormatter) {
        this.valueFormatter = valueFormatter;
        return this;
    }

    /**
     * 设置步长
     *
     * @param stepSize 步长（如果为0，则连续）
     * @return this，用于链式调用
     */
    public SliderWidget setStepSize(double stepSize) {
        this.stepSize = stepSize;

        // 如果有步长，将当前值吸附到最近的步长
        if (stepSize > 0) {
            setValue(Math.round(value / stepSize) * stepSize);
        }

        return this;
    }

    /**
     * 获取步长
     *
     * @return 步长
     */
    public double getStepSize() {
        return stepSize;
    }
}
