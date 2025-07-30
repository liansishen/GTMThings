package com.hepdd.gtmthings.client.screen;

import com.gregtechceu.gtceu.api.transfer.item.CustomItemStackHandler;
import com.hepdd.gtmthings.client.menu.ExampleMenu;
import com.hepdd.gtmthings.client.widget.*;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;

import javax.swing.plaf.basic.BasicComboBoxUI;

/**
 * 一个演示屏幕，展示如何使用widget系统
 */
public class DemoWidgetScreen extends WidgetScreen {

    // 演示用的纹理
    private static final ResourceLocation DEMO_TEXTURE = ResourceLocation.parse("textures/gui/demo_background.png");

    /**
     * 创建一个新的DemoWidgetScreen
     */
    public DemoWidgetScreen(ExampleMenu menu, Inventory playerInventory, int width, int height) {
        super(menu,playerInventory,Component.translatable("screen.gtmthings.demo_widget"));
        // 设置背景颜色
        setBackgroundColor(0xFF2C2C2C);
    }

    @Override
    protected void initWidgets() {
        // 清除现有widgets
        clearWidgets();

        // 添加标题标签
        addWidget(new LabelWidget(width / 2, 10, Component.translatable("screen.gtmthings.demo_widget.title"), 0xFFFFFF)
                .setAlignment(LabelWidget.TextAlignment.CENTER)
                .setShadow(true));

        // 创建左侧面板
        WidgetContainer leftPanel = new WidgetContainer(10, 30, width / 2 - 15, height - 40);
        addWidget(leftPanel);

        // 创建右侧面板
        WidgetContainer rightPanel = new WidgetContainer(width / 2 + 5, 30, width / 2 - 15, height - 40);
        addWidget(rightPanel);

        // 添加左侧面板内容
        initLeftPanel(leftPanel);

        // 添加右侧面板内容
        initRightPanel(rightPanel);
    }

    /**
     * 初始化左侧面板内容
     *
     * @param panel 左侧面板
     */
    private void initLeftPanel(WidgetContainer panel) {
        int y = 0;

        // 添加标签
        panel.addWidget(new LabelWidget(0, y, Component.translatable("screen.gtmthings.demo_widget.basic_widgets"), 0xFFAA00)
                .setShadow(true));
        y += 20;

        // 添加按钮
        panel.addWidget(new ButtonWidget(0, y, 120, 20, Component.translatable("screen.gtmthings.demo_widget.button"), button -> {
            Minecraft.getInstance().player.sendSystemMessage(Component.translatable("screen.gtmthings.demo_widget.button_clicked"));
        }));
        y += 30;

        // 添加文本输入框
        TextFieldWidget textField = panel.addWidget(new TextFieldWidget(0, y, 120, 20, Component.translatable("screen.gtmthings.demo_widget.text_field"), ""));
        textField.setTextChangeListener(text -> {
            // 文本变更时的处理
        });
        y += 30;

        // 添加复选框
        panel.addWidget(new CheckboxWidget(0, y, Component.translatable("screen.gtmthings.demo_widget.checkbox"), false)
                .setOnSelectionChanged(selected -> {
                    // 选中状态变更时的处理
                }));
        y += 30;

        // 添加滑块
        panel.addWidget(new SliderWidget(0, y, 120, 20, 0, 100, 50,
                value -> Component.literal(String.format("%.0f%%", value)),
                value -> {
                    // 值变更时的处理
                }));
        y += 40;

        // 添加图像
        panel.addWidget(new ImageWidget(0, y, 64, 64, ResourceLocation.parse("minecraft:textures/block/crafting_table_top.png")));
        y += 74;

        // 添加分隔线
        panel.addWidget(new LabelWidget(0, y, Component.literal("-------------------------"), 0x888888));
        y += 20;

        // 创建物品槽
        IItemHandlerModifiable itemhandler = new CustomItemStackHandler(3);
        itemhandler.setStackInSlot(0, new ItemStack(Items.DIAMOND));
        itemhandler.setStackInSlot(2, new ItemStack(Items.GOLD_INGOT));

        SlotWidget diamondSlot = new SlotWidget(0, y,itemhandler,0);
        diamondSlot.setHoverText(Component.literal("钻石物品槽"));
        panel.addWidget(diamondSlot);

        // 创建空物品槽
        SlotWidget emptySlot = new SlotWidget(30, y,itemhandler,1);
        emptySlot.setHoverText(Component.literal("空物品槽"));
        panel.addWidget(emptySlot);

        // 创建使用物品提供者的物品槽
        SlotWidget dynamicSlot = new SlotWidget(60, y, itemhandler,2);
        dynamicSlot.setHoverText(Component.literal("动态物品槽"));
        panel.addWidget(dynamicSlot);
        y += 20;

        // 添加关闭按钮
        panel.addWidget(new ButtonWidget(0, y, 120, 20, Component.translatable("gui.close"), button -> {
            this.onClose();
        }));
    }

    /**
     * 初始化右侧面板内容
     *
     * @param panel 右侧面板
     */
    private void initRightPanel(WidgetContainer panel) {
        int y = 0;

        // 添加标签
        panel.addWidget(new LabelWidget(0, y, Component.translatable("screen.gtmthings.demo_widget.advanced_widgets"), 0xFFAA00)
                .setShadow(true));
        y += 20;
        
        // 添加进度条组件
        panel.addWidget(new LabelWidget(0, y, Component.translatable("screen.gtmthings.demo_widget.progress_bar"), 0xFFFFFF));
        y += 15;
        
        // 水平进度条
        panel.addWidget(new ProgressBarWidget(0, y, 150, 15, () -> 0.75)
            .setShowText(true)
            .setHoverText(Component.translatable("screen.gtmthings.demo_widget.progress_bar.hover")));
        y += 25;
        
        // 垂直进度条
        panel.addWidget(new ProgressBarWidget(0, y, 20, 60, () -> 0.5)
            .setDirection(ProgressBarWidget.Direction.BOTTOM_TO_TOP)
            .setFillColor(0xFF0000AA)
            .setHoverText(Component.translatable("screen.gtmthings.demo_widget.vertical_progress_bar.hover")));
        
        // 自定义颜色进度条
        panel.addWidget(new ProgressBarWidget(30, y, 120, 20, () -> 0.25)
            .setBackgroundColor(0xFF222222)
            .setFillColor(0xFFAA0000)
            .setBorderColor(0xFF555555)
            .setShowText(true)
            .setTextFormatter(progress -> Component.literal(String.format("%.1f/100.0", progress * 100.0))));
        y += 70;
        
        // 添加列表组件
        panel.addWidget(new LabelWidget(0, y, Component.translatable("screen.gtmthings.demo_widget.list"), 0xFFFFFF));
        y += 15;
        
        // 创建列表
        ListWidget<String> listWidget = new ListWidget<>(0, y, 150, 100);
        panel.addWidget(listWidget);
        
        // 添加列表项目
        listWidget.addItem("Item 1");
        listWidget.addItem("Item 2");
        listWidget.addItem("Item 3");
        listWidget.addItem("Item 4");
        listWidget.addItem("Item 5");
        listWidget.addItem("Item 6");
        listWidget.addItem("Item 7");
        listWidget.addItem("Item 8");
        
        // 设置列表事件处理器
        listWidget.setOnItemSelected(item -> {
            Minecraft.getInstance().player.sendSystemMessage(Component.literal("Selected: " + item));
        });
        
        y += 110;

        // 创建滚动面板
        ScrollPanelWidget scrollPanel = new ScrollPanelWidget(0, y, panel.getWidth(), 100, 300);
        panel.addWidget(scrollPanel);

        // 添加内容到滚动面板
        for (int i = 0; i < 10; i++) {
            int itemY = i * 30;
            scrollPanel.addWidget(new LabelWidget(10, itemY, Component.literal("Scroll Item " + (i + 1)), 0xFFFFFF));
            scrollPanel.addWidget(new ButtonWidget(120, itemY, 80, 20, Component.literal("Button " + (i + 1)), click -> {
            }));
        }

        y += 110;

        // 创建标签页
        TabWidget tabWidget = new TabWidget(0, y, panel.getWidth(), panel.getHeight() - y);
        panel.addWidget(tabWidget);

        // 添加标签页
        WidgetContainer tab1Content = new WidgetContainer(0, 0, 0, 0);
        tab1Content.addWidget(new LabelWidget(10, 10, Component.translatable("screen.gtmthings.demo_widget.tab1_content"), 0xFFFFFF));
        tab1Content.addWidget(new ButtonWidget(10, 30, 100, 20, Component.literal("Tab 1 Button"), click -> {
        }));

        WidgetContainer tab2Content = new WidgetContainer(0, 0, 0, 0);
        tab2Content.addWidget(new LabelWidget(10, 10, Component.translatable("screen.gtmthings.demo_widget.tab2_content"), 0xFFFFFF));
        tab2Content.addWidget(new CheckboxWidget(10, 30, Component.literal("Tab 2 Checkbox"), false));

        WidgetContainer tab3Content = new WidgetContainer(0, 0, 0, 0);
        tab3Content.addWidget(new LabelWidget(10, 10, Component.translatable("screen.gtmthings.demo_widget.tab3_content"), 0xFFFFFF));
        tab3Content.addWidget(new TextFieldWidget(10, 30, 150, 20, Component.literal("Tab 3 TextField")));

        tabWidget.addTab(Component.translatable("screen.gtmthings.demo_widget.tab1"), tab1Content);
        tabWidget.addTab(Component.translatable("screen.gtmthings.demo_widget.tab2"), tab2Content);
        tabWidget.addTab(Component.translatable("screen.gtmthings.demo_widget.tab3"), tab3Content);
    }

}