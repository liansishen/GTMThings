package com.hepdd.gtmthings.utils

import net.minecraft.client.Minecraft
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent

import com.gregtechceu.gtceu.GTCEu
import com.gregtechceu.gtceu.api.GTValues
import com.gregtechceu.gtceu.utils.FormattingUtil
import com.gregtechceu.gtceu.utils.GTUtil

import java.math.BigDecimal
import java.math.BigInteger
import java.math.RoundingMode

class FormatUtil {

    companion object {
        @JvmStatic
        fun formatNumber(number: Long): String = if (number < 1000) {
            number.toString()
        } else if (number < 1000000) {
            String.format("%.1fK", number / 1000.0)
        } else if (number < 1000000000) {
            String.format("%.2fM", number / 1000000.0)
        } else {
            String.format("%.2fG", number / 1000000000.0)
        }

        @JvmStatic
        fun formatBigDecimalNumberOrSic(number: BigDecimal): String = if (number > BigDecimal.valueOf(Long.Companion.MAX_VALUE)) {
            FormattingUtil.DECIMAL_FORMAT_SIC_2F.format(
                number,
            )
        } else {
            FormattingUtil.formatNumberReadable(number.toLong())
        }

        @JvmStatic
        fun formatBigIntegerNumberOrSic(number: BigInteger): String = if (number > BigInteger.valueOf(Long.Companion.MAX_VALUE)) {
            FormattingUtil.DECIMAL_FORMAT_SIC_2F.format(
                number,
            )
        } else {
            FormattingUtil.formatNumberReadable(number.toLong())
        }

        @JvmStatic
        fun formatWithConstantWidth(labelKey: String, width: Int, body: Component, vararg appends: Component?): MutableComponent {
            val a = arrayOfNulls<Component>(appends.size + 1)
            a[0] = body
            var i = 0
            for (c in appends) {
                a[++i] = c
            }
            val tmp = Component.translatable(labelKey, *a as Array<*>)
            val baseLength = getComponentLength(tmp)
            val spaceLength = width - baseLength
            if (spaceLength <= 0) return tmp
            // var spacerCount = (spaceLength / 2) - 4;
            val spacerCount = 0 // 暂时移除对齐功能
            // val spacer = if (spacerCount > 0) (".".repeat((spaceLength / 2) - 4) + " ") else ""
            val spacer = ""
            val spacerComponent = Component.literal(spacer)
            // return tmp;
            a[0] = spacerComponent.append(body)
            return Component.translatable(labelKey, *a as Array<*>)
        }

        @JvmStatic
        fun voltageName(avgEnergy: BigDecimal): Component = Component.literal(GTValues.VNF[GTUtil.getFloorTierByVoltage(avgEnergy.abs().toLong()).toInt()])

        @JvmStatic
        fun voltageAmperage(avgEnergy: BigDecimal): BigDecimal = avgEnergy.abs().divide(
            BigDecimal.valueOf(
                GTValues.VEX[GTUtil.getFloorTierByVoltage(avgEnergy.abs().toLong()).toInt()],
            ),
            1,
            RoundingMode.FLOOR,
        )

        @JvmStatic
        private fun getComponentLength(component: Component): Int = if (GTCEu.isClientSide()) {
            Minecraft.getInstance().font.width(component.string)
        } else {
            component.string.length / 2
        }
    }
}
