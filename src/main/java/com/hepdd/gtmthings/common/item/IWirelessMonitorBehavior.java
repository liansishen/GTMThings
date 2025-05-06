package com.hepdd.gtmthings.common.item;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import com.gregtechceu.gtceu.utils.GTUtil;

import com.lowdragmc.lowdraglib.gui.widget.ComponentPanelWidget;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;

import com.hepdd.gtmthings.api.machine.IWirelessEnergyContainerHolder;
import com.hepdd.gtmthings.api.misc.ITransferData;
import com.hepdd.gtmthings.api.misc.WirelessEnergyContainer;
import com.hepdd.gtmthings.config.ConfigHolder;
import com.hepdd.gtmthings.utils.TeamUtil;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

import static com.hepdd.gtmthings.common.block.machine.electric.WirelessEnergyMonitor.getTimeToFillDrainText;
import static com.hepdd.gtmthings.utils.FormatUtil.*;
import static com.hepdd.gtmthings.utils.TeamUtil.GetName;

public interface IWirelessMonitorBehavior extends IWirelessEnergyContainerHolder {

    default List<Component> getDisplayText(int DISPLAY_TEXT_WIDTH) {
        List<Component> textListCache = new ArrayList<>();

        WirelessEnergyContainer container = getWirelessEnergyContainer();
        if (container == null) return List.of();
        BigInteger energyTotal = container.getStorage();
        textListCache.add(Component.translatable("gtmthings.machine.wireless_energy_monitor.tooltip.0", GetName(getLevel(), this.getUUID())).withStyle(ChatFormatting.AQUA));
        textListCache.add(formatWithConstantWidth("gtmthings.machine.wireless_energy_monitor.tooltip.1", DISPLAY_TEXT_WIDTH, Component.literal(formatBigIntegerNumberOrSic(energyTotal))).withStyle(ChatFormatting.GOLD));
        if (ConfigHolder.INSTANCE.isWirelessRateEnable) {
            long rate = container.getRate();
            textListCache.add(Component.translatable("gtmthings.machine.wireless_energy_monitor.tooltip.2", FormattingUtil.formatNumbers(rate), rate / GTValues.VEX[GTUtil.getFloorTierByVoltage(rate)], Component.literal(GTValues.VNF[GTUtil.getFloorTierByVoltage(rate)])).withStyle(ChatFormatting.GRAY));
        }

        var stat = container.energyStat;
        textListCache.add(Component.translatable("gtmthings.machine.wireless_energy_monitor.tooltip.net_power"));

        BigDecimal avgMinute = stat.getMinuteAvg();
        textListCache.add(formatWithConstantWidth("gtmthings.machine.wireless_energy_monitor.tooltip.last_minute", DISPLAY_TEXT_WIDTH, Component.literal((avgMinute.signum() < 0 ? "-" : "") + formatBigDecimalNumberOrSic(avgMinute)).withStyle(ChatFormatting.DARK_AQUA), Component.literal(voltageAmperage(avgMinute).toEngineeringString()), voltageName(avgMinute)));
        BigDecimal avgHour = stat.getHourAvg();
        textListCache.add(formatWithConstantWidth("gtmthings.machine.wireless_energy_monitor.tooltip.last_hour", DISPLAY_TEXT_WIDTH, Component.literal((avgHour.signum() < 0 ? "-" : "") + formatBigDecimalNumberOrSic(avgHour)).withStyle(ChatFormatting.YELLOW), Component.literal(voltageAmperage(avgHour).toEngineeringString()), voltageName(avgHour)));
        BigDecimal avgDay = stat.getDayAvg();
        textListCache.add(formatWithConstantWidth("gtmthings.machine.wireless_energy_monitor.tooltip.last_day", DISPLAY_TEXT_WIDTH, Component.literal((avgDay.signum() < 0 ? "-" : "") + formatBigDecimalNumberOrSic(avgDay)).withStyle(ChatFormatting.DARK_GREEN), Component.literal(voltageAmperage(avgDay).toEngineeringString()), voltageName(avgDay)));
        // average useage
        BigDecimal avgEnergy = stat.getAvgEnergy();
        textListCache.add(formatWithConstantWidth("gtmthings.machine.wireless_energy_monitor.tooltip.now", DISPLAY_TEXT_WIDTH, Component.literal((energyTotal.signum() < 0 ? "-" : "") + formatBigDecimalNumberOrSic(avgEnergy)).withStyle(ChatFormatting.DARK_PURPLE), Component.literal(voltageAmperage(avgEnergy).toEngineeringString()), voltageName(avgEnergy)));

        int compare = avgEnergy.compareTo(BigDecimal.valueOf(0));
        if (compare > 0) {
            textListCache.add(Component.translatable("gtceu.multiblock.power_substation.time_to_fill",
                    container.getCapacity() == null ? Component.translatable("gtmthings.machine.wireless_energy_monitor.tooltip.time_to_fill") : getTimeToFillDrainText((container.getCapacity().subtract(energyTotal)).divide(avgEnergy.abs().toBigInteger().multiply(BigInteger.valueOf(20))))).withStyle(ChatFormatting.GRAY));
        } else if (compare < 0) {
            textListCache.add(Component.translatable("gtceu.multiblock.power_substation.time_to_drain",
                    getTimeToFillDrainText(energyTotal.divide(avgEnergy.abs().toBigInteger().multiply(BigInteger.valueOf(20))))).withStyle(ChatFormatting.GRAY));
        }

        if (ConfigHolder.INSTANCE.isWirelessRateEnable && container.getBindPos() != null) {
            String pos = container.getBindPos().pos().toShortString();
            textListCache.add(Component.translatable("gtmthings.machine.wireless_energy_hatch.tooltip.2", Component.translatable("recipe.condition.dimension.tooltip", container.getBindPos().dimension().location().toString()).append(" [").append(pos).append("] ")).withStyle(ChatFormatting.GRAY));
        }
        textListCache.add(Component.translatable("gtmthings.machine.wireless_energy_monitor.tooltip.statistics").append(ComponentPanelWidget.withButton(true ? Component.translatable("gtmthings.machine.wireless_energy_monitor.tooltip.all") : Component.translatable("gtmthings.machine.wireless_energy_monitor.tooltip.team"), "all")));
        WirelessEnergyContainer.observed = true;
        List<Map.Entry<MetaMachine, ITransferData>> sortedEntries = WirelessEnergyContainer.TRANSFER_DATA.entrySet()
                .stream()
                .sorted(Comparator.comparingLong(entry -> entry.getValue().Throughput()))
                .toList();
        WirelessEnergyContainer.TRANSFER_DATA.clear();
        for (Map.Entry<MetaMachine, ITransferData> m : sortedEntries) {
            UUID uuid = m.getValue().UUID();
            if (true || uuid.equals(TeamUtil.getTeamUUID(this.getUUID()))) {
                textListCache.add(m.getValue().getInfo());
            }
        }

        return textListCache;
    }

    Level getLevel();
}
