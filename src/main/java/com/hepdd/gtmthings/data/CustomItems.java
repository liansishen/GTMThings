package com.hepdd.gtmthings.data;

import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.item.ComponentItem;
import com.gregtechceu.gtceu.api.registry.registrate.CompassNode;
import com.gregtechceu.gtceu.api.registry.registrate.CompassSection;
import com.gregtechceu.gtceu.common.item.CoverPlaceBehavior;
import com.gregtechceu.gtceu.common.item.TooltipBehavior;

import net.minecraft.network.chat.Component;
import net.minecraft.world.level.ItemLike;

import com.hepdd.gtmthings.common.item.AdvancedTerminalBehavior;
import com.hepdd.gtmthings.common.item.Behaviour.WirelessTransferCoverPlaceBehavior;
import com.hepdd.gtmthings.common.item.Behaviour.WirelessTransferCoverTooltipBehavior;
import com.hepdd.gtmthings.common.item.WirelessEnergyBindingToolBehavior;
import com.tterrag.registrate.util.entry.ItemEntry;
import com.tterrag.registrate.util.nullness.NonNullConsumer;

import java.util.Locale;

import static com.gregtechceu.gtceu.api.GTValues.*;
import static com.gregtechceu.gtceu.api.registry.registrate.CompassNode.getOrCreate;
import static com.gregtechceu.gtceu.common.data.GTItems.attach;
import static com.hepdd.gtmthings.common.registry.GTMTRegistration.GTMTHINGS_REGISTRATE;

public class CustomItems {

    public static ItemEntry<ComponentItem> WIRELESS_ITEM_TRANSFER_COVER = GTMTHINGS_REGISTRATE
            .item("wireless_item_transfer_cover", ComponentItem::create)
            .onRegister(attach(new WirelessTransferCoverPlaceBehavior(WirelessCovers.WIRELESS_ITEM_TRANSFER),
                    new CoverPlaceBehavior(WirelessCovers.WIRELESS_ITEM_TRANSFER),
                    new WirelessTransferCoverTooltipBehavior(lines -> {
                        lines.add(Component.translatable("item.gtmthings.wireless_transfer.item.tooltip.1"));
                        lines.add(Component.translatable("item.gtmthings.wireless_transfer.tooltip.2"));
                    })))
            .register();

    public static ItemEntry<ComponentItem> WIRELESS_FLUID_TRANSFER_COVER = GTMTHINGS_REGISTRATE
            .item("wireless_fluid_transfer_cover", ComponentItem::create)
            .onRegister(attach(new WirelessTransferCoverPlaceBehavior(WirelessCovers.WIRELESS_FLUID_TRANSFER),
                    new CoverPlaceBehavior(WirelessCovers.WIRELESS_FLUID_TRANSFER),
                    new WirelessTransferCoverTooltipBehavior(lines -> {
                        lines.add(Component.translatable("item.gtmthings.wireless_transfer.fluid.tooltip.1"));
                        lines.add(Component.translatable("item.gtmthings.wireless_transfer.tooltip.2"));
                    })))
            .register();

    public static ItemEntry<ComponentItem> ADVANCED_WIRELESS_ITEM_TRANSFER_COVER = GTMTHINGS_REGISTRATE
            .item("advanced_wireless_item_transfer_cover", ComponentItem::create)
            .onRegister(attach(new WirelessTransferCoverPlaceBehavior(WirelessCovers.ADVANCED_WIRELESS_ITEM_TRANSFER),
                    new CoverPlaceBehavior(WirelessCovers.ADVANCED_WIRELESS_ITEM_TRANSFER),
                    new WirelessTransferCoverTooltipBehavior(lines -> {
                        lines.add(Component.translatable("item.gtmthings.wireless_transfer.item.tooltip.1"));
                        lines.add(Component.translatable("item.gtmthings.advanced_wireless_transfer.tooltip.1"));
                        lines.add(Component.translatable("item.gtmthings.wireless_transfer.tooltip.2"));
                    })))
            .register();

    public static ItemEntry<ComponentItem> ADVANCED_WIRELESS_FLUID_TRANSFER_COVER = GTMTHINGS_REGISTRATE
            .item("advanced_wireless_fluid_transfer_cover", ComponentItem::create)
            .onRegister(attach(new WirelessTransferCoverPlaceBehavior(WirelessCovers.ADVANCED_WIRELESS_FLUID_TRANSFER),
                    new CoverPlaceBehavior(WirelessCovers.ADVANCED_WIRELESS_FLUID_TRANSFER),
                    new WirelessTransferCoverTooltipBehavior(lines -> {
                        lines.add(Component.translatable("item.gtmthings.wireless_transfer.fluid.tooltip.1"));
                        lines.add(Component.translatable("item.gtmthings.advanced_wireless_transfer.tooltip.1"));
                        lines.add(Component.translatable("item.gtmthings.wireless_transfer.tooltip.2"));
                    })))
            .register();

    public static ItemEntry<ComponentItem> WIRELESS_ENERGY_RECEIVE_COVER_LV = registerTieredCover("wireless_energy_receive_cover", "Wireless Energy Receive Cover", LV, 1);
    public static ItemEntry<ComponentItem> WIRELESS_ENERGY_RECEIVE_COVER_MV = registerTieredCover("wireless_energy_receive_cover", "Wireless Energy Receive Cover", MV, 1);
    public static ItemEntry<ComponentItem> WIRELESS_ENERGY_RECEIVE_COVER_HV = registerTieredCover("wireless_energy_receive_cover", "Wireless Energy Receive Cover", HV, 1);
    public static ItemEntry<ComponentItem> WIRELESS_ENERGY_RECEIVE_COVER_EV = registerTieredCover("wireless_energy_receive_cover", "Wireless Energy Receive Cover", EV, 1);
    public static ItemEntry<ComponentItem> WIRELESS_ENERGY_RECEIVE_COVER_IV = registerTieredCover("wireless_energy_receive_cover", "Wireless Energy Receive Cover", IV, 1);
    public static ItemEntry<ComponentItem> WIRELESS_ENERGY_RECEIVE_COVER_LUV = registerTieredCover("wireless_energy_receive_cover", "Wireless Energy Receive Cover", LuV, 1);
    public static ItemEntry<ComponentItem> WIRELESS_ENERGY_RECEIVE_COVER_ZPM = registerTieredCover("wireless_energy_receive_cover", "Wireless Energy Receive Cover", ZPM, 1);
    public static ItemEntry<ComponentItem> WIRELESS_ENERGY_RECEIVE_COVER_UV = registerTieredCover("wireless_energy_receive_cover", "Wireless Energy Receive Cover", UV, 1);
    public static ItemEntry<ComponentItem> WIRELESS_ENERGY_RECEIVE_COVER_UHV = registerTieredCover("wireless_energy_receive_cover", "Wireless Energy Receive Cover", UHV, 1);
    public static ItemEntry<ComponentItem> WIRELESS_ENERGY_RECEIVE_COVER_UEV = GTCEuAPI.isHighTier() ?
            registerTieredCover("wireless_energy_receive_cover", "Wireless Energy Receive Cover", UEV, 1) :
            null;
    public static ItemEntry<ComponentItem> WIRELESS_ENERGY_RECEIVE_COVER_UIV = GTCEuAPI.isHighTier() ?
            registerTieredCover("wireless_energy_receive_cover", "Wireless Energy Receive Cover", UIV, 1) :
            null;
    public static ItemEntry<ComponentItem> WIRELESS_ENERGY_RECEIVE_COVER_UXV = GTCEuAPI.isHighTier() ?
            registerTieredCover("wireless_energy_receive_cover", "Wireless Energy Receive Cover", UXV, 1) :
            null;
    public static ItemEntry<ComponentItem> WIRELESS_ENERGY_RECEIVE_COVER_OPV = GTCEuAPI.isHighTier() ?
            registerTieredCover("wireless_energy_receive_cover", "Wireless Energy Receive Cover", OpV, 1) :
            null;

    public static ItemEntry<ComponentItem> WIRELESS_ENERGY_RECEIVE_COVER_LV_4A = registerTieredCover("wireless_energy_receive_cover", "Wireless Energy Receive Cover", LV, 4);
    public static ItemEntry<ComponentItem> WIRELESS_ENERGY_RECEIVE_COVER_MV_4A = registerTieredCover("wireless_energy_receive_cover", "Wireless Energy Receive Cover", MV, 4);
    public static ItemEntry<ComponentItem> WIRELESS_ENERGY_RECEIVE_COVER_HV_4A = registerTieredCover("wireless_energy_receive_cover", "Wireless Energy Receive Cover", HV, 4);
    public static ItemEntry<ComponentItem> WIRELESS_ENERGY_RECEIVE_COVER_EV_4A = registerTieredCover("wireless_energy_receive_cover", "Wireless Energy Receive Cover", EV, 4);
    public static ItemEntry<ComponentItem> WIRELESS_ENERGY_RECEIVE_COVER_IV_4A = registerTieredCover("wireless_energy_receive_cover", "Wireless Energy Receive Cover", IV, 4);
    public static ItemEntry<ComponentItem> WIRELESS_ENERGY_RECEIVE_COVER_LUV_4A = registerTieredCover("wireless_energy_receive_cover", "Wireless Energy Receive Cover", LuV, 4);
    public static ItemEntry<ComponentItem> WIRELESS_ENERGY_RECEIVE_COVER_ZPM_4A = registerTieredCover("wireless_energy_receive_cover", "Wireless Energy Receive Cover", ZPM, 4);
    public static ItemEntry<ComponentItem> WIRELESS_ENERGY_RECEIVE_COVER_UV_4A = registerTieredCover("wireless_energy_receive_cover", "Wireless Energy Receive Cover", UV, 4);
    public static ItemEntry<ComponentItem> WIRELESS_ENERGY_RECEIVE_COVER_UHV_4A = GTCEuAPI.isHighTier() ?
            registerTieredCover("wireless_energy_receive_cover", "Wireless Energy Receive Cover", UHV, 4) :
            null;
    public static ItemEntry<ComponentItem> WIRELESS_ENERGY_RECEIVE_COVER_UEV_4A = GTCEuAPI.isHighTier() ?
            registerTieredCover("wireless_energy_receive_cover", "Wireless Energy Receive Cover", UEV, 4) :
            null;
    public static ItemEntry<ComponentItem> WIRELESS_ENERGY_RECEIVE_COVER_UIV_4A = GTCEuAPI.isHighTier() ?
            registerTieredCover("wireless_energy_receive_cover", "Wireless Energy Receive Cover", UIV, 4) :
            null;
    public static ItemEntry<ComponentItem> WIRELESS_ENERGY_RECEIVE_COVER_UXV_4A = GTCEuAPI.isHighTier() ?
            registerTieredCover("wireless_energy_receive_cover", "Wireless Energy Receive Cover", UXV, 4) :
            null;
    public static ItemEntry<ComponentItem> WIRELESS_ENERGY_RECEIVE_COVER_OPV_4A = GTCEuAPI.isHighTier() ?
            registerTieredCover("wireless_energy_receive_cover", "Wireless Energy Receive Cover", OpV, 4) :
            null;

    private static ItemEntry<ComponentItem> registerTieredCover(String name, String lang, int tier, int amperage) {
        return GTMTHINGS_REGISTRATE
                .item(GTValues.VN[tier].toLowerCase(Locale.ROOT) + "_" + (amperage == 1 ? "" : amperage + "a_") + name, ComponentItem::create)
                .lang(VNF[tier] + " " + lang)
                .onRegister(attach(new TooltipBehavior(lines -> {
                    lines.add(Component.translatable("item.gtmthings.wireless_energy_receive_cover.tooltip.1"));
                    lines.add(Component.translatable("item.gtmthings.wireless_energy_receive_cover.tooltip.2"));
                    lines.add(Component.translatable("item.gtmthings.wireless_energy_receive_cover.tooltip.3", GTValues.V[tier] * amperage));
                }), new CoverPlaceBehavior(amperage == 1 ? WirelessCovers.WIRELESS_ENERGY_RECEIVE[tier - 1] : WirelessCovers.WIRELESS_ENERGY_RECEIVE_4A[tier - 1]))).register();
    }

    public static ItemEntry<ComponentItem> ADVANCED_TERMINAL = GTMTHINGS_REGISTRATE
            .item("advanced_terminal", ComponentItem::create)
            .properties(p -> p.stacksTo(1))
            .onRegister(attach(new AdvancedTerminalBehavior())).register();

    public static ItemEntry<ComponentItem> WIRELESS_ENERGY_BINDING_TOOL = GTMTHINGS_REGISTRATE
            .item("wireless_energy_binding_tool", ComponentItem::create)
            .properties(p -> p.stacksTo(1))
            .onRegister(attach(new WirelessEnergyBindingToolBehavior())).register();

    public static <T extends ItemLike> NonNullConsumer<T> compassNode(CompassSection section, CompassNode... preNodes) {
        return item -> getOrCreate(section, item::asItem).addPreNode(preNodes);
    }

    public static void init() {}
}
