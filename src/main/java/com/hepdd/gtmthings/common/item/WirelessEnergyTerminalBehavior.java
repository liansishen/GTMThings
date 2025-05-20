package com.hepdd.gtmthings.common.item;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.item.component.IItemUIFactory;

import com.lowdragmc.lowdraglib.gui.editor.ColorPattern;
import com.lowdragmc.lowdraglib.gui.factory.HeldItemUIFactory;
import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.widget.*;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import com.hepdd.gtmthings.api.misc.WirelessEnergyContainer;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static com.hepdd.gtmthings.common.block.machine.electric.WirelessEnergyMonitor.DISPLAY_TEXT_WIDTH;

public class WirelessEnergyTerminalBehavior implements IItemUIFactory {

    @Override
    public ModularUI createUI(HeldItemUIFactory.HeldItemHolder holder, Player entityPlayer) {
        return new ModularUI(DISPLAY_TEXT_WIDTH + 8 + 8, 117 + 8 + 8 + 8 + 17, holder, entityPlayer).widget(createWidget(holder.getHeld().getDescriptionId(), new WirelessMonitor(entityPlayer.getUUID(), entityPlayer.level())));
    }

    private static Widget createWidget(String descriptionId, WirelessMonitor monitor) {
        var group = new WidgetGroup(0, 0, DISPLAY_TEXT_WIDTH + 8 + 8, 117 + 8 + 8 + 8 + 17);
        Widget label = new LabelWidget(4, 5, descriptionId);
        label.setSelfPositionX(group.getSizeWidth() / 2 - label.getSizeWidth() / 2);
        group.addWidget(
                new DraggableScrollableWidgetGroup(4, 4, DISPLAY_TEXT_WIDTH + 8, 117 + 8 + 8 + 17)
                        .setBackground(GuiTextures.DISPLAY)
                        .setYScrollBarWidth(2)
                        .setYBarStyle(null, ColorPattern.T_WHITE.rectTexture().setRadius(1))
                        .addWidget(label)
                        .addWidget(new ComponentPanelWidget(8, 17, text -> addDisplayText(text, monitor)).setMaxWidthLimit(DISPLAY_TEXT_WIDTH)));

        group.setBackground(GuiTextures.BACKGROUND_INVERSE);
        return group;
    }

    private static void addDisplayText(List<Component> textList, WirelessMonitor monitor) {
        if (monitor.isRemote()) return;
        if (monitor.displayTextCache == null || monitor.level.getServer().getTickCount() % 10 == 0) {
            monitor.displayTextCache = monitor.getDisplayText(false, DISPLAY_TEXT_WIDTH);
        }
        textList.addAll(monitor.displayTextCache);
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
