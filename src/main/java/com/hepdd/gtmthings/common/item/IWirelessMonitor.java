package com.hepdd.gtmthings.common.item;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;

import com.hepdd.gtmthings.api.machine.IWirelessEnergyContainerHolder;
import com.hepdd.gtmthings.api.misc.ITransferData;
import com.hepdd.gtmthings.api.misc.WirelessEnergyContainer;
import com.hepdd.gtmthings.utils.TeamUtil;
import com.lowdragmc.lowdraglib.gui.widget.ComponentPanelWidget;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

import static com.gregtechceu.gtceu.common.machine.multiblock.electric.PowerSubstationMachine.getTimeToFillDrainText;
import static com.hepdd.gtmthings.utils.FormatUtil.*;
import static com.hepdd.gtmthings.utils.TeamUtil.GetName;

public interface IWirelessMonitor extends IWirelessEnergyContainerHolder {

    default List<Component> getDisplayText(boolean all, int powerDisplayMode, int displayTextWidth) {
        List<Component> textListCache = new ArrayList<>();
        displayTextWidth -= 16;
        WirelessEnergyContainer container = getWirelessEnergyContainer();
        if (container == null) return List.of();
        BigInteger energyTotal = container.getStorage();
        textListCache.add(Component.translatable("gtmthings.machine.wireless_energy_monitor.tooltip.0", GetName(getLevel(), getUUID())).withStyle(ChatFormatting.AQUA));
        textListCache.add(formatWithConstantWidth("gtmthings.machine.wireless_energy_monitor.tooltip.1", Component.literal(formatBigIntegerNumberOrSic(energyTotal))).withStyle(ChatFormatting.GOLD));
        long rate = container.getRate();
        // textListCache.add(Component.translatable("gtmthings.machine.wireless_energy_monitor.tooltip.2",
        // FormattingUtil.formatNumbers(rate), rate / GTValues.VEX[GTUtil.getFloorTierByVoltage(rate)],
        // Component.literal(GTValues.VNF[GTUtil.getFloorTierByVoltage(rate)])).withStyle(ChatFormatting.GRAY));
        textListCache.add(formatWithConstantWidth("gtmthings.machine.wireless_energy_monitor.tooltip.2", Component.literal(formatBigIntegerNumberOrSic(BigInteger.valueOf(rate))), Component.literal(String.valueOf(rate / GTValues.VEX[GTUtil.getFloorTierByVoltage(rate)])), Component.literal(GTValues.VNF[GTUtil.getFloorTierByVoltage(rate)])).withStyle(ChatFormatting.GRAY));

        var stat = container.getEnergyStat();
        Component powerTitleKeys;
        switch (powerDisplayMode) {
            case 1 -> powerTitleKeys = Component.translatable("gtmthings.machine.wireless_energy_monitor.tooltip.input_power");
            case 2 -> powerTitleKeys = Component.translatable("gtmthings.machine.wireless_energy_monitor.tooltip.output_power");
            default -> powerTitleKeys = Component.translatable("gtmthings.machine.wireless_energy_monitor.tooltip.net_power");
        }
        textListCache.add(powerTitleKeys);

        BigDecimal avgMinute, avgHour, avgDay, avgEnergy;
        switch (powerDisplayMode) {
            case 1 -> { // Input
                avgMinute = stat.minute.getAvgInputByTick();
                avgHour = stat.hour.getAvgInputByTick();
                avgDay = stat.day.getAvgInputByTick();
                avgEnergy = stat.getAvgInput();
            }
            case 2 -> { // Output
                avgMinute = stat.minute.getAvgOutputByTick();
                avgHour = stat.hour.getAvgOutputByTick();
                avgDay = stat.day.getAvgOutputByTick();
                avgEnergy = stat.getAvgOutput();
            }
            default -> { // Net (case 0)
                avgMinute = stat.minute.getAvgByTick();
                avgHour = stat.hour.getAvgByTick();
                avgDay = stat.day.getAvgByTick();
                avgEnergy = stat.getAvgEnergy();
            }
        }

        textListCache.add(formatWithConstantWidth("gtmthings.machine.wireless_energy_monitor.tooltip.last_minute", Component.literal(formatBigDecimalNumberOrSic(avgMinute)).withStyle(ChatFormatting.DARK_AQUA), Component.literal(voltageAmperage(avgMinute).toEngineeringString()), voltageName(avgMinute)));

        textListCache.add(formatWithConstantWidth("gtmthings.machine.wireless_energy_monitor.tooltip.last_hour", Component.literal(formatBigDecimalNumberOrSic(avgHour)).withStyle(ChatFormatting.YELLOW), Component.literal(voltageAmperage(avgHour).toEngineeringString()), voltageName(avgHour)));

        textListCache.add(formatWithConstantWidth("gtmthings.machine.wireless_energy_monitor.tooltip.last_day", Component.literal(formatBigDecimalNumberOrSic(avgDay)).withStyle(ChatFormatting.DARK_GREEN), Component.literal(voltageAmperage(avgDay).toEngineeringString()), voltageName(avgDay)));
        // average useage
        textListCache.add(formatWithConstantWidth("gtmthings.machine.wireless_energy_monitor.tooltip.now", Component.literal(formatBigDecimalNumberOrSic(avgEnergy)).withStyle(ChatFormatting.DARK_PURPLE), Component.literal(voltageAmperage(avgEnergy).toEngineeringString()), voltageName(avgEnergy)));

        int compare = avgEnergy.compareTo(BigDecimal.valueOf(0));
        BigInteger multiply = avgEnergy.abs().toBigInteger().multiply(BigInteger.valueOf(20));
        if (compare > 0) {
            textListCache.add(Component.translatable("gtceu.multiblock.power_substation.time_to_fill",
                    container.getCapacity() == null ? Component.translatable("gtmthings.machine.wireless_energy_monitor.tooltip.time_to_fill") : getTimeToFillDrainText((container.getCapacity().subtract(energyTotal)).divide(multiply))).withStyle(ChatFormatting.GRAY));
        } else if (compare < 0) {
            textListCache.add(Component.translatable("gtceu.multiblock.power_substation.time_to_drain",
                    getTimeToFillDrainText(energyTotal.divide(multiply))).withStyle(ChatFormatting.GRAY));
        }

        if (container.getBindPos() != null) {
            String pos = container.getBindPos().pos().toShortString();
            textListCache.add(Component.translatable("gtmthings.machine.wireless_energy_hatch.tooltip.2", Component.translatable("recipe.condition.dimension.tooltip", container.getBindPos().dimension().location().toString()).append(" [").append(pos).append("] ")).withStyle(ChatFormatting.GRAY));
        }
        Component modeButtonText;
        switch (powerDisplayMode) {
            case 1 -> modeButtonText = Component.translatable("gtmthings.machine.wireless_energy_monitor.button.input");
            case 2 -> modeButtonText = Component.translatable("gtmthings.machine.wireless_energy_monitor.button.output");
            default -> modeButtonText = Component.translatable("gtmthings.machine.wireless_energy_monitor.button.net");
        }

        textListCache.add(Component.translatable("gtmthings.machine.wireless_energy_monitor.tooltip.statistics")
                .append(ComponentPanelWidget.withButton(all ? Component.translatable("gtmthings.machine.wireless_energy_monitor.tooltip.all") : Component.translatable("gtmthings.machine.wireless_energy_monitor.tooltip.team"), "all"))
                .append(Component.literal(" ")) // 添加一个空格分隔
                .append(ComponentPanelWidget.withButton(modeButtonText, "power_mode")));

        for (Map.Entry<MetaMachine, ITransferData> m : WirelessEnergyContainer.TRANSFER_DATA.entrySet()
                .stream()
                .filter(entry -> {
                    // 根据 powerDisplayMode 过滤条目
                    long throughput = entry.getValue().Throughput();
                    return switch (powerDisplayMode) {
                        case 1 -> // Input (输入模式): 只显示发电的机器 (吞吐量 > 0)
                                throughput > 0;
                        case 2 -> // Output (输出模式): 只显示用电的机器 (吞吐量 < 0)
                                throughput < 0;
                        default -> // Net (净值模式, case 0): 显示所有机器
                                true;
                    };
                })
                .sorted(Comparator.comparingLong(entry -> entry.getValue().Throughput()))
                .toList()) {
            UUID uuid = m.getValue().UUID();
            if (all || uuid.equals(TeamUtil.getTeamUUID(this.getUUID()))) {
                textListCache.add(m.getValue().getInfo());
            }
        }

        WirelessEnergyContainer.observed = true;
        WirelessEnergyContainer.TRANSFER_DATA.clear();

        return textListCache;
    }

    Level getLevel();
}
