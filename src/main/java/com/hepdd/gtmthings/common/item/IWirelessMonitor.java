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
import com.hepdd.gtmthings.utils.BigIntegerUtils;
import com.hepdd.gtmthings.utils.TeamUtil;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Duration;
import java.util.*;

import static com.hepdd.gtmthings.utils.FormatUtil.*;
import static com.hepdd.gtmthings.utils.TeamUtil.GetName;

public interface IWirelessMonitor extends IWirelessEnergyContainerHolder {

    default List<Component> getDisplayText(boolean all, int displayTextWidth) {
        List<Component> textListCache = new ArrayList<>();
        displayTextWidth -= 16;
        WirelessEnergyContainer container = getWirelessEnergyContainer();
        if (container == null) return List.of();
        BigInteger energyTotal = container.getStorage();
        textListCache.add(Component.translatable("gtmthings.machine.wireless_energy_monitor.tooltip.0", GetName(getLevel(), getUUID())).withStyle(ChatFormatting.AQUA));
        textListCache.add(formatWithConstantWidth("gtmthings.machine.wireless_energy_monitor.tooltip.1", displayTextWidth, Component.literal(formatBigIntegerNumberOrSic(energyTotal))).withStyle(ChatFormatting.GOLD));
        if (ConfigHolder.INSTANCE.isWirelessRateEnable) {
            long rate = container.getRate();
            // textListCache.add(Component.translatable("gtmthings.machine.wireless_energy_monitor.tooltip.2",
            // FormattingUtil.formatNumbers(rate), rate / GTValues.VEX[GTUtil.getFloorTierByVoltage(rate)],
            // Component.literal(GTValues.VNF[GTUtil.getFloorTierByVoltage(rate)])).withStyle(ChatFormatting.GRAY));
            textListCache.add(formatWithConstantWidth("gtmthings.machine.wireless_energy_monitor.tooltip.2", displayTextWidth, Component.literal(formatBigIntegerNumberOrSic(BigInteger.valueOf(rate))), Component.literal(String.valueOf(rate / GTValues.VEX[GTUtil.getFloorTierByVoltage(rate)])), Component.literal(GTValues.VNF[GTUtil.getFloorTierByVoltage(rate)])).withStyle(ChatFormatting.GRAY));
        }

        var stat = container.getEnergyStat();
        textListCache.add(Component.translatable("gtmthings.machine.wireless_energy_monitor.tooltip.net_power"));

        BigDecimal avgMinute = stat.getMinuteAvg();
        textListCache.add(formatWithConstantWidth("gtmthings.machine.wireless_energy_monitor.tooltip.last_minute", displayTextWidth, Component.literal(formatBigDecimalNumberOrSic(avgMinute)).withStyle(ChatFormatting.DARK_AQUA), Component.literal(voltageAmperage(avgMinute).toEngineeringString()), voltageName(avgMinute)));
        BigDecimal avgHour = stat.getHourAvg();
        textListCache.add(formatWithConstantWidth("gtmthings.machine.wireless_energy_monitor.tooltip.last_hour", displayTextWidth, Component.literal(formatBigDecimalNumberOrSic(avgHour)).withStyle(ChatFormatting.YELLOW), Component.literal(voltageAmperage(avgHour).toEngineeringString()), voltageName(avgHour)));
        BigDecimal avgDay = stat.getDayAvg();
        textListCache.add(formatWithConstantWidth("gtmthings.machine.wireless_energy_monitor.tooltip.last_day", displayTextWidth, Component.literal(formatBigDecimalNumberOrSic(avgDay)).withStyle(ChatFormatting.DARK_GREEN), Component.literal(voltageAmperage(avgDay).toEngineeringString()), voltageName(avgDay)));
        // average useage
        BigDecimal avgEnergy = stat.getAvgEnergy();
        textListCache.add(formatWithConstantWidth("gtmthings.machine.wireless_energy_monitor.tooltip.now", displayTextWidth, Component.literal(formatBigDecimalNumberOrSic(avgEnergy)).withStyle(ChatFormatting.DARK_PURPLE), Component.literal(voltageAmperage(avgEnergy).toEngineeringString()), voltageName(avgEnergy)));

        int compare = avgEnergy.compareTo(BigDecimal.valueOf(0));
        BigInteger multiply = avgEnergy.abs().toBigInteger().multiply(BigInteger.valueOf(20));
        if (compare > 0) {
            textListCache.add(Component.translatable("gtceu.multiblock.power_substation.time_to_fill",
                    container.getCapacity() == null ? Component.translatable("gtmthings.machine.wireless_energy_monitor.tooltip.time_to_fill") : getTimeToFillDrainText((container.getCapacity().subtract(energyTotal)).divide(multiply))).withStyle(ChatFormatting.GRAY));
        } else if (compare < 0) {
            textListCache.add(Component.translatable("gtceu.multiblock.power_substation.time_to_drain",
                    getTimeToFillDrainText(energyTotal.divide(multiply))).withStyle(ChatFormatting.GRAY));
        }

        if (ConfigHolder.INSTANCE.isWirelessRateEnable && container.getBindPos() != null) {
            String pos = container.getBindPos().pos().toShortString();
            textListCache.add(Component.translatable("gtmthings.machine.wireless_energy_hatch.tooltip.2", Component.translatable("recipe.condition.dimension.tooltip", container.getBindPos().dimension().location().toString()).append(" [").append(pos).append("] ")).withStyle(ChatFormatting.GRAY));
        }
        textListCache.add(Component.translatable("gtmthings.machine.wireless_energy_monitor.tooltip.statistics").append(ComponentPanelWidget.withButton(all ? Component.translatable("gtmthings.machine.wireless_energy_monitor.tooltip.all") : Component.translatable("gtmthings.machine.wireless_energy_monitor.tooltip.team"), "all")));
        WirelessEnergyContainer.observed = true;
        WirelessEnergyContainer.TRANSFER_DATA.clear();
        for (Map.Entry<MetaMachine, ITransferData> m : WirelessEnergyContainer.TRANSFER_DATA.entrySet().stream().sorted(Comparator.comparingLong(entry -> entry.getValue().Throughput())).toList()) {
            UUID uuid = m.getValue().UUID();
            if (all || uuid.equals(TeamUtil.getTeamUUID(this.getUUID()))) {
                textListCache.add(m.getValue().getInfo());
            }
        }

        return textListCache;
    }

    Level getLevel();

    private static Component getTimeToFillDrainText(BigInteger timeToFillSeconds) {
        if (timeToFillSeconds.compareTo(BigIntegerUtils.BIG_INTEGER_MAX_LONG) > 0) {
            timeToFillSeconds = BigIntegerUtils.BIG_INTEGER_MAX_LONG;
        }

        Duration duration = Duration.ofSeconds(timeToFillSeconds.longValue());
        String key;
        long fillTime;
        if (duration.getSeconds() <= 180) {
            fillTime = duration.getSeconds();
            key = "gtceu.multiblock.power_substation.time_seconds";
        } else if (duration.toMinutes() <= 180) {
            fillTime = duration.toMinutes();
            key = "gtceu.multiblock.power_substation.time_minutes";
        } else if (duration.toHours() <= 72) {
            fillTime = duration.toHours();
            key = "gtceu.multiblock.power_substation.time_hours";
        } else if (duration.toDays() <= 730) { // 2 years
            fillTime = duration.toDays();
            key = "gtceu.multiblock.power_substation.time_days";
        } else if (duration.toDays() / 365 < 1_000_000) {
            fillTime = duration.toDays() / 365;
            key = "gtceu.multiblock.power_substation.time_years";
        } else {
            return Component.translatable("gtceu.multiblock.power_substation.time_forever");
        }

        return Component.translatable(key, FormattingUtil.formatNumbers(fillTime));
    }
}
