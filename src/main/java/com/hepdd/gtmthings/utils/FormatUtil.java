package com.hepdd.gtmthings.utils;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.client.ComponentCollector;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;

import com.google.common.collect.Lists;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static com.gregtechceu.gtceu.utils.FormattingUtil.DECIMAL_FORMAT_SIC_2F;
import static com.gregtechceu.gtceu.utils.FormattingUtil.formatNumberReadable;

public class FormatUtil {

    public static String formatNumber(long number) {
        if (number < 1000) {
            return String.valueOf(number);
        } else if (number < 1_000_000) {
            return String.format("%.1fK", number / 1000.0);
        } else if (number < 1_000_000_000) {
            return String.format("%.2fM", number / 1_000_000.0);
        } else {
            return String.format("%.2fG", number / 1_000_000_000.0);
        }
    }

    public static String formatBigDecimalNumberOrSic(BigDecimal number) {
        return number.compareTo(BigDecimal.valueOf(Long.MAX_VALUE)) > 0 ? DECIMAL_FORMAT_SIC_2F.format(number) : formatNumberReadable(number.longValue());
    }

    public static String formatBigIntegerNumberOrSic(BigInteger number) {
        return number.compareTo(BigInteger.valueOf(Long.MAX_VALUE)) > 0 ? DECIMAL_FORMAT_SIC_2F.format(number) : formatNumberReadable(number.longValue());
    }

    public static MutableComponent formatWithConstantWidth(String labelKey, Component body, Component... appends) {
        var a = new Component[appends.length + 1];
        a[0] = body;
        int i = 0;
        for (var c : appends) {
            a[++i] = c;
        }
        var spacer = ".";
        var spacerComponent = Component.literal(spacer);
        a[0] = spacerComponent.append(body);
        return Component.translatable(labelKey, (Object[]) a);
    }

    public static Component voltageName(BigDecimal avgEnergy) {
        return Component.literal(GTValues.VNF[GTUtil.getFloorTierByVoltage(avgEnergy.abs().longValue())]);
    }

    public static BigDecimal voltageAmperage(BigDecimal avgEnergy) {
        return avgEnergy.abs().divide(BigDecimal.valueOf(GTValues.VEX[GTUtil.getFloorTierByVoltage(avgEnergy.abs().longValue())]), 1, RoundingMode.FLOOR);
    }

    public static String getSpacer(Font font, String splitChar, int spaceLength) {
        int spacerCount = spaceLength / font.width(splitChar);
        while (font.width(splitChar.repeat(spacerCount) + " ") <= spaceLength) {
            spacerCount++;
        }
        return splitChar.repeat(spacerCount - 2) + " ";
    }

    public static List<FormattedCharSequence> formatJustifyComponent(FormattedText component, int maxWidth, Font font, String splitChar) {
        ComponentCollector componentcollector = new ComponentCollector();
        AtomicInteger before = new AtomicInteger();
        AtomicInteger after = new AtomicInteger();
        AtomicBoolean hasSplit = new AtomicBoolean(false);
        component.visit((style, text) -> {
            if (text.equals(splitChar)) {
                hasSplit.set(true);
            } else {
                int width = font.width(text);
                (hasSplit.get() ? after : before).getAndAdd(width);
            }
            return Optional.empty();
        }, Style.EMPTY);
        component.visit((style, text) -> {
            String content = text.equals(splitChar) ? getSpacer(font, splitChar, maxWidth - before.get() - after.get()) : text;
            componentcollector.append(FormattedText.of(stripColor(content), style));
            return Optional.empty();
        }, Style.EMPTY);
        List<FormattedCharSequence> list = Lists.newArrayList();
        font.getSplitter().splitLines(componentcollector.getResultOrEmpty(), maxWidth, Style.EMPTY, (text, p_94004_) -> {
            FormattedCharSequence formattedcharsequence = Language.getInstance().getVisualOrder(text);
            list.add(p_94004_ ? FormattedCharSequence.composite(FormattedCharSequence.codepoint(32, Style.EMPTY), formattedcharsequence) : formattedcharsequence);
        });
        return (list.isEmpty() ? Lists.newArrayList(FormattedCharSequence.EMPTY) : list);
    }

    private static String stripColor(String text) {
        return Minecraft.getInstance().options.chatColors().get() ? text : ChatFormatting.stripFormatting(text);
    }
}
