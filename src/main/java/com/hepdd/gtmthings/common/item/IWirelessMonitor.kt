package com.hepdd.gtmthings.common.item

import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component
import net.minecraft.world.level.Level

import com.gregtechceu.gtceu.api.GTValues
import com.gregtechceu.gtceu.api.machine.MetaMachine
import com.gregtechceu.gtceu.utils.FormattingUtil
import com.gregtechceu.gtceu.utils.GTUtil
import com.hepdd.gtmthings.api.machine.IWirelessEnergyContainerHolder
import com.hepdd.gtmthings.api.misc.ITransferData
import com.hepdd.gtmthings.api.misc.WirelessEnergyContainer
import com.hepdd.gtmthings.config.ConfigHolder
import com.hepdd.gtmthings.utils.BigIntegerUtils
import com.hepdd.gtmthings.utils.FormatUtil
import com.hepdd.gtmthings.utils.TeamUtil
import com.lowdragmc.lowdraglib.gui.widget.ComponentPanelWidget

import java.math.BigDecimal
import java.math.BigInteger
import java.time.Duration
import java.util.*

interface IWirelessMonitor : IWirelessEnergyContainerHolder {

    fun getDisplayText(all: Boolean, displayTextWidth: Int): List<Component> {
        var displayTextWidth = displayTextWidth
        val textListCache: MutableList<Component> = ArrayList()
        displayTextWidth -= 16
        val container = getWirelessEnergyContainer() ?: return listOf()
        val energyTotal = container.storage

        textListCache.add(
            Component.translatable(
                "gtmthings.machine.wireless_energy_monitor.tooltip.0",
                TeamUtil.getName(
                    getMonitorLevel()!!,
                    getUUID()!!,
                ),
            ).withStyle(ChatFormatting.AQUA),
        )
        textListCache.add(
            FormatUtil.formatWithConstantWidth(
                "gtmthings.machine.wireless_energy_monitor.tooltip.1",
                displayTextWidth,
                Component.literal(
                    FormatUtil.formatBigIntegerNumberOrSic(energyTotal!!),
                ),
            ).withStyle(ChatFormatting.GOLD),
        )
        ConfigHolder.INSTANCE?.let {
            if (it.isWirelessRateEnable) {
                val rate = container.rate
                textListCache.add(
                    FormatUtil.formatWithConstantWidth(
                        "gtmthings.machine.wireless_energy_monitor.tooltip.2",
                        displayTextWidth,
                        Component.literal(
                            FormatUtil.formatBigIntegerNumberOrSic(BigInteger.valueOf(rate)),
                        ),
                        Component.literal((rate / GTValues.VEX[GTUtil.getFloorTierByVoltage(rate).toInt()]).toString()),
                        Component.literal(
                            GTValues.VNF[GTUtil.getFloorTierByVoltage(rate).toInt()],
                        ),
                    ).withStyle(ChatFormatting.GRAY),
                )
            }
        }

        val stat = container.energyStat
        textListCache.add(Component.translatable("gtmthings.machine.wireless_energy_monitor.tooltip.net_power"))

        val avgMinute = stat.getMinuteAvg()
        textListCache.add(
            FormatUtil.formatWithConstantWidth(
                "gtmthings.machine.wireless_energy_monitor.tooltip.last_minute",
                displayTextWidth,
                Component.literal(
                    FormatUtil.formatBigDecimalNumberOrSic(avgMinute),
                ).withStyle(ChatFormatting.DARK_AQUA),
                Component.literal(
                    FormatUtil.voltageAmperage(avgMinute).toEngineeringString(),
                ),
                FormatUtil.voltageName(avgMinute),
            ),
        )
        val avgHour = stat.getHourAvg()
        textListCache.add(
            FormatUtil.formatWithConstantWidth(
                "gtmthings.machine.wireless_energy_monitor.tooltip.last_hour",
                displayTextWidth,
                Component.literal(
                    FormatUtil.formatBigDecimalNumberOrSic(avgHour),
                ).withStyle(ChatFormatting.YELLOW),
                Component.literal(
                    FormatUtil.voltageAmperage(avgHour).toEngineeringString(),
                ),
                FormatUtil.voltageName(avgHour),
            ),
        )
        val avgDay = stat.getDayAvg()
        textListCache.add(
            FormatUtil.formatWithConstantWidth(
                "gtmthings.machine.wireless_energy_monitor.tooltip.last_day",
                displayTextWidth,
                Component.literal(
                    FormatUtil.formatBigDecimalNumberOrSic(avgDay),
                ).withStyle(ChatFormatting.DARK_GREEN),
                Component.literal(
                    FormatUtil.voltageAmperage(avgDay).toEngineeringString(),
                ),
                FormatUtil.voltageName(avgDay),
            ),
        )
        // average useage
        val avgEnergy = stat.getAvgEnergy()
        textListCache.add(
            FormatUtil.formatWithConstantWidth(
                "gtmthings.machine.wireless_energy_monitor.tooltip.now",
                displayTextWidth,
                Component.literal(
                    FormatUtil.formatBigDecimalNumberOrSic(avgEnergy),
                ).withStyle(ChatFormatting.DARK_PURPLE),
                Component.literal(
                    FormatUtil.voltageAmperage(avgEnergy).toEngineeringString(),
                ),
                FormatUtil.voltageName(avgEnergy),
            ),
        )

        val compare = avgEnergy.compareTo(BigDecimal.valueOf(0))
        val multiply = avgEnergy.abs().toBigInteger().multiply(BigInteger.valueOf(20))
        if (compare > 0) {
            textListCache.add(
                Component.translatable(
                    "gtceu.multiblock.power_substation.time_to_fill",
                    if (container.getCapacity() == null) {
                        Component.translatable("gtmthings.machine.wireless_energy_monitor.tooltip.time_to_fill")
                    } else {
                        getTimeToFillDrainText(
                            (container.getCapacity()!!.subtract(energyTotal)).divide(multiply),
                        )
                    },
                ).withStyle(ChatFormatting.GRAY),
            )
        } else if (compare < 0) {
            textListCache.add(
                Component.translatable(
                    "gtceu.multiblock.power_substation.time_to_drain",
                    getTimeToFillDrainText(energyTotal!!.divide(multiply)),
                ).withStyle(ChatFormatting.GRAY),
            )
        }

        if (ConfigHolder.INSTANCE?.isWirelessRateEnable == true && container.bindPos != null) {
            val pos = container.bindPos!!.pos().toShortString()
            textListCache.add(
                Component.translatable(
                    "gtmthings.machine.wireless_energy_hatch.tooltip.2",
                    Component.translatable(
                        "recipe.condition.dimension.tooltip",
                        container.bindPos!!.dimension().location().toString(),
                    ).append(" [").append(pos).append("] "),
                ).withStyle(ChatFormatting.GRAY),
            )
        }
        textListCache.add(
            Component.translatable("gtmthings.machine.wireless_energy_monitor.tooltip.statistics").append(
                ComponentPanelWidget.withButton(
                    if (all) {
                        Component.translatable("gtmthings.machine.wireless_energy_monitor.tooltip.all")
                    } else {
                        Component.translatable(
                            "gtmthings.machine.wireless_energy_monitor.tooltip.team",
                        )
                    },
                    "all",
                ),
            ),
        )

        for ((_, value) in WirelessEnergyContainer.TRANSFER_DATA.entries.stream()
            .sorted(Comparator.comparingLong<Map.Entry<MetaMachine?, ITransferData>> { entry: Map.Entry<MetaMachine?, ITransferData> -> entry.value.throughput() })
            .toList()) {
            val uuid = value.UUID()
            if (all || uuid == TeamUtil.getTeamUUID(this.getUUID())) {
                textListCache.add(value.getInfo())
            }
        }

        WirelessEnergyContainer.observed = true
        WirelessEnergyContainer.TRANSFER_DATA.clear()

        return textListCache
    }

    fun getMonitorLevel(): Level?

    fun getTimeToFillDrainText(timeToFillSeconds: BigInteger): Component {
        var t = timeToFillSeconds
        if (t.compareTo(BigIntegerUtils.BIG_INTEGER_MAX_LONG) > 0) {
            t = BigIntegerUtils.BIG_INTEGER_MAX_LONG
        }

        val duration = Duration.ofSeconds(t.toLong())
        val key: String
        val fillTime: Long
        when {
            duration.seconds <= 180 -> {
                fillTime = duration.seconds
                key = "gtceu.multiblock.power_substation.time_seconds"
            }
            duration.toMinutes() <= 180 -> {
                fillTime = duration.toMinutes()
                key = "gtceu.multiblock.power_substation.time_minutes"
            }
            duration.toHours() <= 72 -> {
                fillTime = duration.toHours()
                key = "gtceu.multiblock.power_substation.time_hours"
            }
            duration.toDays() <= 730 -> {
                fillTime = duration.toDays()
                key = "gtceu.multiblock.power_substation.time_days"
            }
            duration.toDays() / 365 < 1000000 -> {
                fillTime = duration.toDays() / 365
                key = "gtceu.multiblock.power_substation.time_years"
            }
            else -> {
                return Component.translatable("gtceu.multiblock.power_substation.time_forever")
            }
        }

        return Component.translatable(key, FormattingUtil.formatNumbers(fillTime))
    }
}
