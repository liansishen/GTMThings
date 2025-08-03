package com.hepdd.gtmthings.client.screen;

import com.gregtechceu.gtceu.GTCEu;
import com.hepdd.gtmthings.api.misc.WirelessEnergyContainer;
import com.hepdd.gtmthings.client.menu.EnergyDisplayMenu;
import com.hepdd.gtmthings.client.widget.ButtonWidget;
import com.hepdd.gtmthings.client.widget.CheckboxWidget;
import com.hepdd.gtmthings.client.widget.LabelWidget;
import com.hepdd.gtmthings.client.widget.ScrollPanelWidget;
import com.hepdd.gtmthings.client.widget.TabWidget;
import com.hepdd.gtmthings.client.widget.TextFieldWidget;
import com.hepdd.gtmthings.client.widget.WidgetContainer;
import com.hepdd.gtmthings.common.item.IWirelessMonitor;
import com.hepdd.gtmthings.common.item.WirelessEnergyTerminalBehavior;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

import static com.hepdd.gtmthings.common.block.machine.electric.WirelessEnergyMonitor.DISPLAY_TEXT_WIDTH;

@OnlyIn(Dist.CLIENT)
public class EnergyDisplayScreen extends GTMTBaseScreen<EnergyDisplayMenu>{

    protected final WidgetContainer rootContainer;
    private int containerWidth;
    private int containerHeight;
    private double scale = 1;
    private WirelessMonitor monitor;

    public EnergyDisplayScreen(EnergyDisplayMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        scale = Minecraft.getInstance().getWindow().getGuiScale();
        containerWidth = 276;
        containerHeight = 340;
        rootContainer = new WidgetContainer(getPosX(),getPosY(),containerWidth,containerHeight);
        monitor = new WirelessMonitor(playerInventory.player.getUUID(),playerInventory.player.level());
    }

    @Override
    protected void renderBg(@NotNull GuiGraphics guiGraphics, float v, int i, int i1) {
        guiGraphics.blit(ResourceLocation.parse("textures/gui/container/example.png"), this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
    }

    @Override
    protected void init() {
        super.init();

        rootContainer.setPosition(getPosX(),getPosY());

        initWidgets();
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        rootContainer.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    private int getPosX() {
        return (Minecraft.getInstance().getWindow().getGuiScaledWidth() - containerWidth)/2;
    }

    private int getPosY() {
        return (Minecraft.getInstance().getWindow().getGuiScaledHeight() - 21 - containerHeight)/2;
    }

    protected void initWidgets() {
        rootContainer.clearWidgets();

        // 0 ~ 20: title

        // 20 ~ 170:Summary information
        WidgetContainer summaryContainer = new WidgetContainer(0,20, rootContainer.getWidth(), 190,0xFF4A4A4A);
        rootContainer.addWidget(summaryContainer);
        LabelWidget labelWidget = new LabelWidget(0,0,(text)-> addDisplayText(text,monitor));
        summaryContainer.addWidget(labelWidget);


        // 170 ~ end:Tab
        TabWidget tabWidget = new TabWidget(0, 170, rootContainer.getWidth(), rootContainer.getHeight() -  170);
        rootContainer.addWidget(tabWidget);

        // Tab1: Energy In Panel
        WidgetContainer tab1Content = new WidgetContainer(0, 0, 0, 0);
        ScrollPanelWidget energyInPanel = new ScrollPanelWidget(0,0,tab1Content.getWidth(),tab1Content.getHeight(),tab1Content.getHeight() + 200);
        tab1Content.addWidget(energyInPanel);

        // Tab2: Energy Out Panel
        WidgetContainer tab2Content = new WidgetContainer(0, 0, 0, 0);
        ScrollPanelWidget energyOutPanel = new ScrollPanelWidget(0,0,tab1Content.getWidth(),tab2Content.getHeight(),tab2Content.getHeight() + 200);
        tab2Content.addWidget(energyOutPanel);

        tabWidget.addTab(Component.literal("能量生产"), tab1Content);
        tabWidget.addTab(Component.literal("能量消耗"), tab2Content);
    }

    private static void addDisplayText(Component text, WirelessMonitor monitor) {
        if (monitor.isRemote()) return;
        if (monitor.displayTextCache == null || monitor.level.getServer().getTickCount() % 10 == 0) {
            monitor.displayTextCache = monitor.getDisplayText(false, DISPLAY_TEXT_WIDTH);
        }
        text = monitor.displayTextCache.get(0);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // 将事件传递给根容器
        if (rootContainer.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        // 将事件传递给根容器
        if (rootContainer.mouseReleased(mouseX, mouseY, button)) {
            return true;
        }

        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        // 将事件传递给根容器
        if (rootContainer.mouseDragged(mouseX, mouseY, button, dragX, dragY)) {
            return true;
        }

        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        // 将事件传递给根容器
        if (rootContainer.mouseScrolled(mouseX, mouseY, delta)) {
            return true;
        }

        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // 将事件传递给根容器
        if (rootContainer.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        // 将事件传递给根容器
        if (rootContainer.keyReleased(keyCode, scanCode, modifiers)) {
            return true;
        }

        return super.keyReleased(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        // 将事件传递给根容器
        if (rootContainer.charTyped(codePoint, modifiers)) {
            return true;
        }

        return super.charTyped(codePoint, modifiers);
    }

    private static class WirelessMonitor implements IWirelessMonitor {

        private WirelessMonitor(UUID uuid, Level level) {
            this.uuid = uuid;
            this.level = level;
        }

        private boolean isRemote() {
            return level == null ? GTCEu.isClientThread() : level.isClientSide;
        }

        private final UUID uuid;
        private final Level level;

        private List<Component> displayTextCache;

        @Getter
        @Setter
        private WirelessEnergyContainer WirelessEnergyContainerCache;

        /**
         * @return cached uuid of player/team
         */
        @Override
        public @Nullable UUID getUUID() {
            return uuid;
        }

        /**
         * @return false
         */
        @Override
        public boolean display() {
            return false;
        }

        /**
         * @return level
         */
        @Override
        public Level getLevel() {
            return level;
        }
    }
}
