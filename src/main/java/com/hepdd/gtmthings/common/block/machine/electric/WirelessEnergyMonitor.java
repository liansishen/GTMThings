package com.hepdd.gtmthings.common.block.machine.electric;

import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.IFancyUIMachine;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;

import com.hepdd.gtmthings.api.misc.WirelessEnergyContainer;
import com.hepdd.gtmthings.common.item.IWirelessMonitor;
import com.lowdragmc.lowdraglib.gui.util.ClickData;
import com.lowdragmc.lowdraglib.gui.widget.*;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class WirelessEnergyMonitor extends MetaMachine implements IFancyUIMachine, IWirelessMonitor {

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(WirelessEnergyMonitor.class,
            MetaMachine.MANAGED_FIELD_HOLDER);

    public static int p;
    public static BlockPos pPos;

    public WirelessEnergyMonitor(IMachineBlockEntity holder) {
        super(holder);
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    @Getter
    @Setter
    private WirelessEnergyContainer WirelessEnergyContainerCache;

    private List<Component> textListCache;

    @Persisted
    private boolean all;

    //////////////////////////////////////
    // *********** GUI ***********//
    //////////////////////////////////////
    private void handleDisplayClick(String componentData, ClickData clickData) {
        if (componentData.equals("all")) {
            if (!clickData.isRemote) {
                all = !all;
            }
        } else if (clickData.isRemote) {
            p = 100;
            String[] parts = componentData.split(", ");
            pPos = new BlockPos(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]), Integer.parseInt(parts[2]));
        }
    }

    public static int DISPLAY_TEXT_WIDTH = 220;

    @Override
    public Widget createUIWidget() {
        var group = new WidgetGroup(0, 0, DISPLAY_TEXT_WIDTH + 8 + 8, 117 + 8);

        group.addWidget(new DraggableScrollableWidgetGroup(4, 4, DISPLAY_TEXT_WIDTH + 8, 117).setBackground(GuiTextures.DISPLAY)
                .addWidget(new LabelWidget(4, 5, self().getBlockState().getBlock().getDescriptionId()))
                .addWidget(new ComponentPanelWidget(4, 17, this::addDisplayText)
                        .setMaxWidthLimit(DISPLAY_TEXT_WIDTH)
                        .clickHandler(this::handleDisplayClick)));
        group.setBackground(GuiTextures.BACKGROUND_INVERSE);
        return group;
    }

    public void addDisplayText(@NotNull List<Component> textList) {
        if (isRemote()) return;
        if (textListCache == null || getOffsetTimer() % 10 == 0) {
            textListCache = getDisplayText(all, DISPLAY_TEXT_WIDTH);
        }
        textList.addAll(textListCache);
    }

    @Override
    public @Nullable UUID getUUID() {
        return this.getOwnerUUID();
    }

    @Override
    public boolean display() {
        return false;
    }
}
