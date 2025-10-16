package com.hepdd.gtmthings.api.gui.widget;

import net.minecraft.util.Mth;

import com.lowdragmc.lowdraglib.gui.widget.DraggableScrollableWidgetGroup;
import com.lowdragmc.lowdraglib.gui.widget.Widget;

public class FixedDraggableScrollableWidgetGroup extends DraggableScrollableWidgetGroup {

    private static final int BOTTOM_PADDING = 32;

    public FixedDraggableScrollableWidgetGroup(int x, int y, int width, int height) {
        super(x, y, width, height);
    }

    /**
     * 重写 computeMax 方法，使用一个更安全、更简单的逻辑。
     * 这个版本只更新最大滚动范围，并确保当前滚动位置在有效范围内，
     * 而不会主动重置或修改用户当前的滚动位置。
     */
    @Override
    public void computeMax() {
        if (isComputingMax) {
            return;
        }
        isComputingMax = true;

        int newMaxWidth = 0;
        int newMaxHeight = 0;

        // 1. 遍历所有子控件，计算出实际需要的内容总宽度和总高度
        for (Widget widget : widgets) {
            // 注意：子控件的 selfPosition 是相对于滚动后的视图的
            // 所以要加上 scrollOffset 才能得到它在整个内容区域的绝对位置
            int widgetRight = widget.getSelfPosition().x + widget.getSize().width + scrollXOffset;
            int widgetBottom = widget.getSelfPosition().y + widget.getSize().height + scrollYOffset;
            if (widgetRight > newMaxWidth) {
                newMaxWidth = widgetRight;
            }
            if (widgetBottom > newMaxHeight) {
                newMaxHeight = widgetBottom;
            }
        }

        if (newMaxHeight > 0) { // 只有在有内容时才增加padding
            newMaxHeight += BOTTOM_PADDING;
        }

        // 2. 更新最大宽高。如果内容没有填满视图，则最大宽高就是视图本身的大小
        this.maxWidth = Math.max(newMaxWidth, getSize().width - yBarWidth);
        this.maxHeight = Math.max(newMaxHeight, getSize().height - xBarHeight);

        // 3. 夹紧当前的滚动偏移量，防止其超出新的范围
        // 这是最关键的一步：我们不重置滚动条，只是确保它不会滚出界外
        int maxScrollX = Math.max(0, this.maxWidth - (getSize().width - yBarWidth));
        int maxScrollY = Math.max(0, this.maxHeight - (getSize().height - xBarHeight));

        int clampedScrollX = Mth.clamp(this.scrollXOffset, 0, maxScrollX);
        int clampedScrollY = Mth.clamp(this.scrollYOffset, 0, maxScrollY);

        // 4. 如果夹紧后的值与当前值不同，才使用setter更新（setter会移动子控件）
        if (clampedScrollX != this.scrollXOffset) {
            setScrollXOffset(clampedScrollX);
        }
        if (clampedScrollY != this.scrollYOffset) {
            setScrollYOffset(clampedScrollY);
        }

        isComputingMax = false;
    }
}
