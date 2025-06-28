package com.hepdd.gtmthings.api.misc

import net.minecraft.network.chat.Component
import net.minecraft.network.chat.HoverEvent
import net.minecraft.network.chat.Style

import com.gregtechceu.gtceu.api.GTValues
import com.gregtechceu.gtceu.api.machine.MetaMachine
import com.gregtechceu.gtceu.utils.GTUtil
import com.hepdd.gtmthings.utils.FormatUtil
import com.hepdd.gtmthings.utils.TeamUtil
import com.lowdragmc.lowdraglib.gui.widget.ComponentPanelWidget

import java.math.BigDecimal
import java.util.*
import kotlin.math.abs

interface ITransferData {
    fun UUID(): UUID?

    fun throughput(): Long

    fun machine(): MetaMachine?

    fun getInfo(): Component {
        val machine = machine()
        val eut = throughput()
        val pos = machine?.pos?.toShortString()
        return Component.translatable(machine?.blockState?.block?.descriptionId!!)
            .withStyle(
                Style.EMPTY.withHoverEvent(
                    HoverEvent(
                        HoverEvent.Action.SHOW_TEXT,
                        Component.translatable(
                            "recipe.condition.dimension.tooltip",
                            machine.level!!.dimension().location(),
                        ).append(" [").append(pos!!).append("] ").append(
                            Component.translatable(
                                "gtmthings.machine.wireless_energy_monitor.tooltip.0",
                                TeamUtil.getName(machine.level!!, UUID()!!),
                            ),
                        ),
                    ),
                ),
            )
            .append((if (eut > 0) " +" else " ") + FormatUtil.formatBigDecimalNumberOrSic(BigDecimal.valueOf(eut)))
            .append(" EU/t (").append(GTValues.VNF[GTUtil.getFloorTierByVoltage(abs(eut)).toInt()]).append(")")
            .append(ComponentPanelWidget.withButton(Component.literal(" [ ] "), pos))
    }
}
