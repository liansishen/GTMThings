package com.hepdd.gtmthings.data;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.item.ComponentItem;
import com.gregtechceu.gtceu.api.registry.registrate.CompassNode;
import com.gregtechceu.gtceu.api.registry.registrate.CompassSection;
import com.gregtechceu.gtceu.common.item.CoverPlaceBehavior;
import com.gregtechceu.gtceu.common.item.TooltipBehavior;
import com.hepdd.gtmthings.common.item.Behaviour.WirelessTransferCoverPlaceBehavior;
import com.hepdd.gtmthings.common.item.Behaviour.WirelessTransferCoverTooltipBehavior;
import com.tterrag.registrate.util.entry.ItemEntry;
import com.tterrag.registrate.util.nullness.NonNullConsumer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.ItemLike;

import java.util.Locale;

import static com.gregtechceu.gtceu.api.GTValues.*;
import static com.gregtechceu.gtceu.api.registry.registrate.CompassNode.getOrCreate;
import static com.gregtechceu.gtceu.common.data.GTItems.attach;
import static com.hepdd.gtmthings.common.registry.GTMTRegistration.GTMTHINGS_REGISTRATE;

public class CustomItems {

    public static ItemEntry<ComponentItem> WIRELESS_ITEM_TRANSFER_COVER = GTMTHINGS_REGISTRATE
            .item("wireless_item_transfer_cover",ComponentItem::create)
            .onRegister(attach(new WirelessTransferCoverPlaceBehavior(WirelessCovers.WIRELESS_ITEM_TRANSFER),
                    new CoverPlaceBehavior(WirelessCovers.WIRELESS_ITEM_TRANSFER),
                    new WirelessTransferCoverTooltipBehavior(lines->{
                        lines.add(Component.translatable("item.gtmthings.wireless_transfer.item.tooltip.1"));
                        lines.add(Component.translatable("item.gtmthings.wireless_transfer.tooltip.2"));
                    })))
            .register();

    public static ItemEntry<ComponentItem> WIRELESS_FLUID_TRANSFER_COVER = GTMTHINGS_REGISTRATE
            .item("wireless_fluid_transfer_cover",ComponentItem::create)
            .onRegister(attach(new WirelessTransferCoverPlaceBehavior(WirelessCovers.WIRELESS_FLUID_TRANSFER),
                    new CoverPlaceBehavior(WirelessCovers.WIRELESS_FLUID_TRANSFER),
                    new WirelessTransferCoverTooltipBehavior(lines->{
                        lines.add(Component.translatable("item.gtmthings.wireless_transfer.fluid.tooltip.1"));
                        lines.add(Component.translatable("item.gtmthings.wireless_transfer.tooltip.2"));
                    })))
            .register();

     public static ItemEntry<ComponentItem> WIRELESS_ENERGY_RECEIVE_COVER_LV =
             registerTieredCover("wireless_energy_receive_cover","Wireless Energy Receive Cover",LV);
    public static ItemEntry<ComponentItem> WIRELESS_ENERGY_RECEIVE_COVER_MV =
            registerTieredCover("wireless_energy_receive_cover","Wireless Energy Receive Cover",MV);
    public static ItemEntry<ComponentItem> WIRELESS_ENERGY_RECEIVE_COVER_HV =
            registerTieredCover("wireless_energy_receive_cover","Wireless Energy Receive Cover",HV);
    public static ItemEntry<ComponentItem> WIRELESS_ENERGY_RECEIVE_COVER_EV =
            registerTieredCover("wireless_energy_receive_cover","Wireless Energy Receive Cover",EV);
    public static ItemEntry<ComponentItem> WIRELESS_ENERGY_RECEIVE_COVER_IV =
            registerTieredCover("wireless_energy_receive_cover","Wireless Energy Receive Cover",IV);
    public static ItemEntry<ComponentItem> WIRELESS_ENERGY_RECEIVE_COVER_LUV =
            registerTieredCover("wireless_energy_receive_cover","Wireless Energy Receive Cover",LuV);
    public static ItemEntry<ComponentItem> WIRELESS_ENERGY_RECEIVE_COVER_ZPM =
            registerTieredCover("wireless_energy_receive_cover","Wireless Energy Receive Cover",ZPM);
    public static ItemEntry<ComponentItem> WIRELESS_ENERGY_RECEIVE_COVER_UV =
            registerTieredCover("wireless_energy_receive_cover","Wireless Energy Receive Cover",UV);
    public static ItemEntry<ComponentItem> WIRELESS_ENERGY_RECEIVE_COVER_UHV =
            registerTieredCover("wireless_energy_receive_cover","Wireless Energy Receive Cover",UHV);
    public static ItemEntry<ComponentItem> WIRELESS_ENERGY_RECEIVE_COVER_UEV =
            registerTieredCover("wireless_energy_receive_cover","Wireless Energy Receive Cover",UEV);
    public static ItemEntry<ComponentItem> WIRELESS_ENERGY_RECEIVE_COVER_UIV =
            registerTieredCover("wireless_energy_receive_cover","Wireless Energy Receive Cover",UIV);
    public static ItemEntry<ComponentItem> WIRELESS_ENERGY_RECEIVE_COVER_UXV =
            registerTieredCover("wireless_energy_receive_cover","Wireless Energy Receive Cover",UXV);
    public static ItemEntry<ComponentItem> WIRELESS_ENERGY_RECEIVE_COVER_OPV =
            registerTieredCover("wireless_energy_receive_cover","Wireless Energy Receive Cover",OpV);


    private static ItemEntry<ComponentItem> registerTieredCover(String name, String lang,int tier) {
        return GTMTHINGS_REGISTRATE
                .item(GTValues.VN[tier].toLowerCase(Locale.ROOT) + "_" + name, ComponentItem::create)
                .lang(VNF[tier] + " " + lang)
                .onRegister(attach(new TooltipBehavior(lines -> {
                    lines.add(Component.translatable("item.gtmthings.wireless_energy_receive_cover.tooltip.1"));
                    lines.add(Component.translatable("item.gtmthings.wireless_energy_receive_cover.tooltip.2"));
                    lines.add(Component.translatable("item.gtmthings.wireless_energy_receive_cover.tooltip.3",GTValues.V[tier]));
                }), new CoverPlaceBehavior(WirelessCovers.WIRELESS_ENERGY_RECEIVE[tier-1]))).register();
    }

    public static <T extends ItemLike> NonNullConsumer<T> compassNode(CompassSection section, CompassNode... preNodes) {
        return item -> getOrCreate(section, item::asItem).addPreNode(preNodes);
    }
    public static void init() { }
}
