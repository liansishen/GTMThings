package com.hepdd.gtmthings.api.misc;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;

import com.lowdragmc.lowdraglib.gui.widget.ComponentPanelWidget;

import java.math.BigDecimal;
import java.util.UUID;

import static com.hepdd.gtmthings.utils.FormatUtil.*;
import static com.hepdd.gtmthings.utils.TeamUtil.GetName;

public interface ITransferData {

    UUID UUID();

    long Throughput();

    MetaMachine machine();

    default Component getInfo() {
        MetaMachine machine = machine();
        long eut = Throughput();
        String pos = machine.getPos().toShortString();
        return Component.translatable(machine.getBlockState().getBlock().getDescriptionId())
                .withStyle(Style.EMPTY.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.translatable("recipe.condition.dimension.tooltip", machine.getLevel().dimension().location()).append(" [").append(pos).append("] ").append(Component.translatable("gtmthings.machine.wireless_energy_monitor.tooltip.0", GetName(machine.getLevel(), UUID()))))))
                .append((eut > 0 ? " +" : " ") + formatBigDecimalNumberOrSic(BigDecimal.valueOf(eut))).append(" EU/t (").append(GTValues.VNF[GTUtil.getFloorTierByVoltage(Math.abs(eut))]).append(")")
                .append(ComponentPanelWidget.withButton(Component.literal(" [ ] "), pos));
    }
}
