package com.hepdd.gtmthings.data;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.item.ComponentItem;
import com.gregtechceu.gtceu.api.item.component.ICustomRenderer;
import com.gregtechceu.gtceu.common.item.CoverPlaceBehavior;
import com.gregtechceu.gtceu.common.item.TooltipBehavior;

import net.minecraft.network.chat.Component;

import com.hepdd.gtmthings.client.VirtualItemProviderRenderer;
import com.hepdd.gtmthings.common.item.AdvancedTerminalBehavior;
import com.hepdd.gtmthings.common.item.Behaviour.WirelessTransferCoverPlaceBehavior;
import com.hepdd.gtmthings.common.item.Behaviour.WirelessTransferCoverTooltipBehavior;
import com.hepdd.gtmthings.common.item.VirtualItemProviderBehavior;
import com.hepdd.gtmthings.common.item.VirtualItemProviderCellItem;
import com.hepdd.gtmthings.common.item.WirelessEnergyBindingToolBehavior;
import com.tterrag.registrate.util.entry.ItemEntry;
import com.tterrag.registrate.util.nullness.NonNullConsumer;

import java.util.Locale;

import static com.gregtechceu.gtceu.api.GTValues.*;
import static com.gregtechceu.gtceu.common.data.GTItems.attach;
import static com.hepdd.gtmthings.common.registry.GTMTRegistration.GTMTHINGS_REGISTRATE;

public class CustomItems {

    public static <T extends ComponentItem> NonNullConsumer<T> attachRenderer(ICustomRenderer customRenderer) {
        return !GTCEu.isClientSide() ? NonNullConsumer.noop() : (item) -> item.attachComponents(customRenderer);
    }

    public static final ItemEntry<ComponentItem> VIRTUAL_ITEM_PROVIDER = GTMTHINGS_REGISTRATE.item("virtual_item_provider", ComponentItem::create)
            .properties(p -> p.stacksTo(1))
            .onRegister(attach(VirtualItemProviderBehavior.INSTANCE))
            .onRegister(attachRenderer(() -> VirtualItemProviderRenderer.INSTANCE))
            .register();

    public static final ItemEntry<VirtualItemProviderCellItem> VIRTUAL_ITEM_PROVIDER_CELL = GTMTHINGS_REGISTRATE.item("virtual_item_provider_cell", VirtualItemProviderCellItem::new).register();

    public static ItemEntry<ComponentItem> WIRELESS_ITEM_TRANSFER_COVER = GTMTHINGS_REGISTRATE
            .item("wireless_item_transfer_cover", ComponentItem::create)
            .onRegister(attach(new WirelessTransferCoverPlaceBehavior(GTMTCovers.WIRELESS_ITEM_TRANSFER),
                    new CoverPlaceBehavior(GTMTCovers.WIRELESS_ITEM_TRANSFER),
                    new WirelessTransferCoverTooltipBehavior(lines -> {
                        lines.add(Component.translatable("item.gtmthings.wireless_transfer.item.tooltip.1"));
                        lines.add(Component.translatable("item.gtmthings.wireless_transfer.tooltip.2"));
                    })))
            .register();

    public static ItemEntry<ComponentItem> WIRELESS_FLUID_TRANSFER_COVER = GTMTHINGS_REGISTRATE
            .item("wireless_fluid_transfer_cover", ComponentItem::create)
            .onRegister(attach(new WirelessTransferCoverPlaceBehavior(GTMTCovers.WIRELESS_FLUID_TRANSFER),
                    new CoverPlaceBehavior(GTMTCovers.WIRELESS_FLUID_TRANSFER),
                    new WirelessTransferCoverTooltipBehavior(lines -> {
                        lines.add(Component.translatable("item.gtmthings.wireless_transfer.fluid.tooltip.1"));
                        lines.add(Component.translatable("item.gtmthings.wireless_transfer.tooltip.2"));
                    })))
            .register();

    public static ItemEntry<ComponentItem> ADVANCED_WIRELESS_ITEM_TRANSFER_COVER = GTMTHINGS_REGISTRATE
            .item("advanced_wireless_item_transfer_cover", ComponentItem::create)
            .onRegister(attach(new WirelessTransferCoverPlaceBehavior(GTMTCovers.ADVANCED_WIRELESS_ITEM_TRANSFER),
                    new CoverPlaceBehavior(GTMTCovers.ADVANCED_WIRELESS_ITEM_TRANSFER),
                    new WirelessTransferCoverTooltipBehavior(lines -> {
                        lines.add(Component.translatable("item.gtmthings.wireless_transfer.item.tooltip.1"));
                        lines.add(Component.translatable("item.gtmthings.advanced_wireless_transfer.tooltip.1"));
                        lines.add(Component.translatable("item.gtmthings.wireless_transfer.tooltip.2"));
                    })))
            .register();

    public static ItemEntry<ComponentItem> ADVANCED_WIRELESS_FLUID_TRANSFER_COVER = GTMTHINGS_REGISTRATE
            .item("advanced_wireless_fluid_transfer_cover", ComponentItem::create)
            .onRegister(attach(new WirelessTransferCoverPlaceBehavior(GTMTCovers.ADVANCED_WIRELESS_FLUID_TRANSFER),
                    new CoverPlaceBehavior(GTMTCovers.ADVANCED_WIRELESS_FLUID_TRANSFER),
                    new WirelessTransferCoverTooltipBehavior(lines -> {
                        lines.add(Component.translatable("item.gtmthings.wireless_transfer.fluid.tooltip.1"));
                        lines.add(Component.translatable("item.gtmthings.advanced_wireless_transfer.tooltip.1"));
                        lines.add(Component.translatable("item.gtmthings.wireless_transfer.tooltip.2"));
                    })))
            .register();

    public static ItemEntry<ComponentItem> WIRELESS_ENERGY_RECEIVE_COVER_LV = registerTieredCover(LV, 1);
    public static ItemEntry<ComponentItem> WIRELESS_ENERGY_RECEIVE_COVER_MV = registerTieredCover(MV, 1);
    public static ItemEntry<ComponentItem> WIRELESS_ENERGY_RECEIVE_COVER_HV = registerTieredCover(HV, 1);
    public static ItemEntry<ComponentItem> WIRELESS_ENERGY_RECEIVE_COVER_EV = registerTieredCover(EV, 1);
    public static ItemEntry<ComponentItem> WIRELESS_ENERGY_RECEIVE_COVER_IV = registerTieredCover(IV, 1);
    public static ItemEntry<ComponentItem> WIRELESS_ENERGY_RECEIVE_COVER_LUV = registerTieredCover(LuV, 1);
    public static ItemEntry<ComponentItem> WIRELESS_ENERGY_RECEIVE_COVER_ZPM = registerTieredCover(ZPM, 1);
    public static ItemEntry<ComponentItem> WIRELESS_ENERGY_RECEIVE_COVER_UV = registerTieredCover(UV, 1);
    public static ItemEntry<ComponentItem> WIRELESS_ENERGY_RECEIVE_COVER_UHV = GTCEuAPI.isHighTier() ?
            registerTieredCover(UHV, 1) : null;
    public static ItemEntry<ComponentItem> WIRELESS_ENERGY_RECEIVE_COVER_UEV = GTCEuAPI.isHighTier() ?
            registerTieredCover(UEV, 1) : null;
    public static ItemEntry<ComponentItem> WIRELESS_ENERGY_RECEIVE_COVER_UIV = GTCEuAPI.isHighTier() ?
            registerTieredCover(UIV, 1) : null;
    public static ItemEntry<ComponentItem> WIRELESS_ENERGY_RECEIVE_COVER_UXV = GTCEuAPI.isHighTier() ?
            registerTieredCover(UXV, 1) : null;
    public static ItemEntry<ComponentItem> WIRELESS_ENERGY_RECEIVE_COVER_OPV = GTCEuAPI.isHighTier() ?
            registerTieredCover(OpV, 1) : null;

    public static ItemEntry<ComponentItem> WIRELESS_ENERGY_RECEIVE_COVER_LV_4A = registerTieredCover(LV, 4);
    public static ItemEntry<ComponentItem> WIRELESS_ENERGY_RECEIVE_COVER_MV_4A = registerTieredCover(MV, 4);
    public static ItemEntry<ComponentItem> WIRELESS_ENERGY_RECEIVE_COVER_HV_4A = registerTieredCover(HV, 4);
    public static ItemEntry<ComponentItem> WIRELESS_ENERGY_RECEIVE_COVER_EV_4A = registerTieredCover(EV, 4);
    public static ItemEntry<ComponentItem> WIRELESS_ENERGY_RECEIVE_COVER_IV_4A = registerTieredCover(IV, 4);
    public static ItemEntry<ComponentItem> WIRELESS_ENERGY_RECEIVE_COVER_LUV_4A = registerTieredCover(LuV, 4);
    public static ItemEntry<ComponentItem> WIRELESS_ENERGY_RECEIVE_COVER_ZPM_4A = registerTieredCover(ZPM, 4);
    public static ItemEntry<ComponentItem> WIRELESS_ENERGY_RECEIVE_COVER_UV_4A = registerTieredCover(UV, 4);
    public static ItemEntry<ComponentItem> WIRELESS_ENERGY_RECEIVE_COVER_UHV_4A = GTCEuAPI.isHighTier() ?
            registerTieredCover(UHV, 4) : null;
    public static ItemEntry<ComponentItem> WIRELESS_ENERGY_RECEIVE_COVER_UEV_4A = GTCEuAPI.isHighTier() ?
            registerTieredCover(UEV, 4) : null;
    public static ItemEntry<ComponentItem> WIRELESS_ENERGY_RECEIVE_COVER_UIV_4A = GTCEuAPI.isHighTier() ?
            registerTieredCover(UIV, 4) : null;
    public static ItemEntry<ComponentItem> WIRELESS_ENERGY_RECEIVE_COVER_UXV_4A = GTCEuAPI.isHighTier() ?
            registerTieredCover(UXV, 4) : null;
    public static ItemEntry<ComponentItem> WIRELESS_ENERGY_RECEIVE_COVER_OPV_4A = GTCEuAPI.isHighTier() ?
            registerTieredCover(OpV, 4) : null;

    private static ItemEntry<ComponentItem> registerTieredCover(int tier, int amperage) {
        return GTMTHINGS_REGISTRATE
                .item(GTValues.VN[tier].toLowerCase(Locale.ROOT) + "_" + (amperage == 1 ? "" : amperage + "a_") + "wireless_energy_receive_cover", ComponentItem::create)
                .lang(VNF[tier] + " " + "Wireless Energy Receive Cover")
                .onRegister(attach(new TooltipBehavior(lines -> {
                    lines.add(Component.translatable("item.gtmthings.wireless_energy_receive_cover.tooltip.1"));
                    lines.add(Component.translatable("item.gtmthings.wireless_energy_receive_cover.tooltip.2"));
                    lines.add(Component.translatable("item.gtmthings.wireless_energy_receive_cover.tooltip.3", GTValues.VEX[tier] * amperage));
                }), new CoverPlaceBehavior(amperage == 1 ? GTMTCovers.WIRELESS_ENERGY_RECEIVE[tier - 1] : GTMTCovers.WIRELESS_ENERGY_RECEIVE_4A[tier - 1]))).register();
    }

    public static ItemEntry<ComponentItem> ADVANCED_TERMINAL = GTMTHINGS_REGISTRATE
            .item("advanced_terminal", ComponentItem::create)
            .properties(p -> p.stacksTo(1))
            .onRegister(attach(new AdvancedTerminalBehavior())).register();

    public static ItemEntry<ComponentItem> WIRELESS_ENERGY_BINDING_TOOL = GTMTHINGS_REGISTRATE
            .item("wireless_energy_binding_tool", ComponentItem::create)
            .properties(p -> p.stacksTo(1))
            .onRegister(attach(new WirelessEnergyBindingToolBehavior())).register();

    public static void init() {}
}
