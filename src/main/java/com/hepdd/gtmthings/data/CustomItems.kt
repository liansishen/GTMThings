package com.hepdd.gtmthings.data

import net.minecraft.network.chat.Component
import net.minecraft.world.item.Item

import com.gregtechceu.gtceu.GTCEu
import com.gregtechceu.gtceu.api.GTCEuAPI
import com.gregtechceu.gtceu.api.GTValues
import com.gregtechceu.gtceu.api.item.ComponentItem
import com.gregtechceu.gtceu.api.item.component.ICustomRenderer
import com.gregtechceu.gtceu.common.data.GTItems
import com.gregtechceu.gtceu.common.item.CoverPlaceBehavior
import com.gregtechceu.gtceu.common.item.TooltipBehavior
import com.hepdd.gtmthings.client.VirtualItemProviderRenderer
import com.hepdd.gtmthings.common.item.*
import com.hepdd.gtmthings.common.item.behaviour.WirelessTransferCoverPlaceBehavior
import com.hepdd.gtmthings.common.item.behaviour.WirelessTransferCoverTooltipBehavior
import com.hepdd.gtmthings.common.registry.GTMTRegistration
import com.tterrag.registrate.util.entry.ItemEntry
import com.tterrag.registrate.util.nullness.NonNullConsumer
import lombok.Getter

@Getter
open class CustomItems {

    companion object {

        fun <T : ComponentItem> attachRenderer(customRenderer: ICustomRenderer): NonNullConsumer<T?> = if (!GTCEu.isClientSide()) {
            NonNullConsumer.noop<T?>()
        } else {
            NonNullConsumer { item: T? ->
                item!!.attachComponents(
                    customRenderer,
                )
            }
        }

        @JvmStatic
        val VIRTUAL_ITEM_PROVIDER: ItemEntry<ComponentItem> =
            GTMTRegistration.Companion.GTMTHINGS_REGISTRATE.item<ComponentItem>(
                "virtual_item_provider",
            ) { properties: Item.Properties -> ComponentItem.create(properties) }
                .properties { p: Item.Properties -> p.stacksTo(1) }
                .onRegister(GTItems.attach<ComponentItem>(VirtualItemProviderBehavior.INSTANCE))
                .onRegister(attachRenderer<ComponentItem>(VirtualItemProviderRenderer::INSTANCE))
                .register()

        @JvmStatic
        val VIRTUAL_ITEM_PROVIDER_CELL: ItemEntry<VirtualItemProviderCellItem> =
            GTMTRegistration.Companion.GTMTHINGS_REGISTRATE.item<VirtualItemProviderCellItem>(
                "virtual_item_provider_cell",
            ) { props: Item.Properties -> VirtualItemProviderCellItem(props) }.register()

        @JvmStatic
        val PROGRAMMABLE_COVER: ItemEntry<ComponentItem> =
            GTMTRegistration.Companion.GTMTHINGS_REGISTRATE.item<ComponentItem>(
                "programmable_cover",
            ) { properties: Item.Properties -> ComponentItem.create(properties) }
                .onRegister(GTItems.attach<ComponentItem>(CoverPlaceBehavior(GTMTCovers.PROGRAMMABLE_COVER)))
                .register()

        @JvmStatic
        var WIRELESS_ITEM_TRANSFER_COVER: ItemEntry<ComponentItem> = GTMTRegistration.Companion.GTMTHINGS_REGISTRATE
            .item<ComponentItem>(
                "wireless_item_transfer_cover",
            ) { properties: Item.Properties -> ComponentItem.create(properties) }
            .onRegister(
                GTItems.attach<ComponentItem>(
                    WirelessTransferCoverPlaceBehavior(GTMTCovers.WIRELESS_ITEM_TRANSFER),
                    CoverPlaceBehavior(GTMTCovers.WIRELESS_ITEM_TRANSFER),
                    WirelessTransferCoverTooltipBehavior { lines: MutableList<Component?>? ->
                        lines!!.add(Component.translatable("item.gtmthings.wireless_transfer.item.tooltip.1"))
                        lines.add(Component.translatable("item.gtmthings.wireless_transfer.tooltip.2"))
                    },
                ),
            )
            .register()

        @JvmStatic
        var WIRELESS_FLUID_TRANSFER_COVER: ItemEntry<ComponentItem> = GTMTRegistration.Companion.GTMTHINGS_REGISTRATE
            .item<ComponentItem>(
                "wireless_fluid_transfer_cover",
            ) { properties: Item.Properties -> ComponentItem.create(properties) }
            .onRegister(
                GTItems.attach<ComponentItem>(
                    WirelessTransferCoverPlaceBehavior(GTMTCovers.WIRELESS_FLUID_TRANSFER),
                    CoverPlaceBehavior(GTMTCovers.WIRELESS_FLUID_TRANSFER),
                    WirelessTransferCoverTooltipBehavior { lines: MutableList<Component?>? ->
                        lines!!.add(Component.translatable("item.gtmthings.wireless_transfer.fluid.tooltip.1"))
                        lines.add(Component.translatable("item.gtmthings.wireless_transfer.tooltip.2"))
                    },
                ),
            )
            .register()

        @JvmStatic
        var ADVANCED_WIRELESS_ITEM_TRANSFER_COVER: ItemEntry<ComponentItem> =
            GTMTRegistration.Companion.GTMTHINGS_REGISTRATE
                .item<ComponentItem>(
                    "advanced_wireless_item_transfer_cover",
                ) { properties: Item.Properties -> ComponentItem.create(properties) }
                .onRegister(
                    GTItems.attach<ComponentItem>(
                        WirelessTransferCoverPlaceBehavior(GTMTCovers.ADVANCED_WIRELESS_ITEM_TRANSFER),
                        CoverPlaceBehavior(GTMTCovers.ADVANCED_WIRELESS_ITEM_TRANSFER),
                        WirelessTransferCoverTooltipBehavior { lines: MutableList<Component?>? ->
                            lines!!.add(Component.translatable("item.gtmthings.wireless_transfer.item.tooltip.1"))
                            lines.add(Component.translatable("item.gtmthings.advanced_wireless_transfer.tooltip.1"))
                            lines.add(Component.translatable("item.gtmthings.wireless_transfer.tooltip.2"))
                        },
                    ),
                )
                .register()

        @JvmStatic
        var ADVANCED_WIRELESS_FLUID_TRANSFER_COVER: ItemEntry<ComponentItem> =
            GTMTRegistration.Companion.GTMTHINGS_REGISTRATE
                .item<ComponentItem>(
                    "advanced_wireless_fluid_transfer_cover",
                ) { properties: Item.Properties -> ComponentItem.create(properties) }
                .onRegister(
                    GTItems.attach<ComponentItem>(
                        WirelessTransferCoverPlaceBehavior(GTMTCovers.ADVANCED_WIRELESS_FLUID_TRANSFER),
                        CoverPlaceBehavior(GTMTCovers.ADVANCED_WIRELESS_FLUID_TRANSFER),
                        WirelessTransferCoverTooltipBehavior { lines: MutableList<Component?>? ->
                            lines!!.add(Component.translatable("item.gtmthings.wireless_transfer.fluid.tooltip.1"))
                            lines.add(Component.translatable("item.gtmthings.advanced_wireless_transfer.tooltip.1"))
                            lines.add(Component.translatable("item.gtmthings.wireless_transfer.tooltip.2"))
                        },
                    ),
                )
                .register()

        @JvmStatic
        var WIRELESS_ENERGY_RECEIVE_COVER_LV: ItemEntry<ComponentItem> = registerTieredCover(GTValues.LV, 1)

        @JvmStatic
        var WIRELESS_ENERGY_RECEIVE_COVER_MV: ItemEntry<ComponentItem> = registerTieredCover(GTValues.MV, 1)

        @JvmStatic
        var WIRELESS_ENERGY_RECEIVE_COVER_HV: ItemEntry<ComponentItem> = registerTieredCover(GTValues.HV, 1)

        @JvmStatic
        var WIRELESS_ENERGY_RECEIVE_COVER_EV: ItemEntry<ComponentItem> = registerTieredCover(GTValues.EV, 1)

        @JvmStatic
        var WIRELESS_ENERGY_RECEIVE_COVER_IV: ItemEntry<ComponentItem> = registerTieredCover(GTValues.IV, 1)

        @JvmStatic
        var WIRELESS_ENERGY_RECEIVE_COVER_LUV: ItemEntry<ComponentItem> = registerTieredCover(GTValues.LuV, 1)

        @JvmStatic
        var WIRELESS_ENERGY_RECEIVE_COVER_ZPM: ItemEntry<ComponentItem> = registerTieredCover(GTValues.ZPM, 1)

        @JvmStatic
        var WIRELESS_ENERGY_RECEIVE_COVER_UV: ItemEntry<ComponentItem> = registerTieredCover(GTValues.UV, 1)

        @JvmStatic
        var WIRELESS_ENERGY_RECEIVE_COVER_UHV: ItemEntry<ComponentItem>? =
            if (GTCEuAPI.isHighTier()) registerTieredCover(GTValues.UHV, 1) else null

        @JvmStatic
        var WIRELESS_ENERGY_RECEIVE_COVER_UEV: ItemEntry<ComponentItem>? =
            if (GTCEuAPI.isHighTier()) registerTieredCover(GTValues.UEV, 1) else null

        @JvmStatic
        var WIRELESS_ENERGY_RECEIVE_COVER_UIV: ItemEntry<ComponentItem>? =
            if (GTCEuAPI.isHighTier()) registerTieredCover(GTValues.UIV, 1) else null

        @JvmStatic
        var WIRELESS_ENERGY_RECEIVE_COVER_UXV: ItemEntry<ComponentItem>? =
            if (GTCEuAPI.isHighTier()) registerTieredCover(GTValues.UXV, 1) else null

        @JvmStatic
        var WIRELESS_ENERGY_RECEIVE_COVER_OPV: ItemEntry<ComponentItem>? =
            if (GTCEuAPI.isHighTier()) registerTieredCover(GTValues.OpV, 1) else null

        @JvmStatic
        var WIRELESS_ENERGY_RECEIVE_COVER_LV_4A: ItemEntry<ComponentItem> = registerTieredCover(GTValues.LV, 4)

        @JvmStatic
        var WIRELESS_ENERGY_RECEIVE_COVER_MV_4A: ItemEntry<ComponentItem> = registerTieredCover(GTValues.MV, 4)

        @JvmStatic
        var WIRELESS_ENERGY_RECEIVE_COVER_HV_4A: ItemEntry<ComponentItem> = registerTieredCover(GTValues.HV, 4)

        @JvmStatic
        var WIRELESS_ENERGY_RECEIVE_COVER_EV_4A: ItemEntry<ComponentItem> = registerTieredCover(GTValues.EV, 4)

        @JvmStatic
        var WIRELESS_ENERGY_RECEIVE_COVER_IV_4A: ItemEntry<ComponentItem> = registerTieredCover(GTValues.IV, 4)

        @JvmStatic
        var WIRELESS_ENERGY_RECEIVE_COVER_LUV_4A: ItemEntry<ComponentItem> = registerTieredCover(GTValues.LuV, 4)

        @JvmStatic
        var WIRELESS_ENERGY_RECEIVE_COVER_ZPM_4A: ItemEntry<ComponentItem> = registerTieredCover(GTValues.ZPM, 4)

        @JvmStatic
        var WIRELESS_ENERGY_RECEIVE_COVER_UV_4A: ItemEntry<ComponentItem> = registerTieredCover(GTValues.UV, 4)

        @JvmStatic
        var WIRELESS_ENERGY_RECEIVE_COVER_UHV_4A: ItemEntry<ComponentItem>? =
            if (GTCEuAPI.isHighTier()) registerTieredCover(GTValues.UHV, 4) else null

        @JvmStatic
        var WIRELESS_ENERGY_RECEIVE_COVER_UEV_4A: ItemEntry<ComponentItem>? =
            if (GTCEuAPI.isHighTier()) registerTieredCover(GTValues.UEV, 4) else null

        @JvmStatic
        var WIRELESS_ENERGY_RECEIVE_COVER_UIV_4A: ItemEntry<ComponentItem>? =
            if (GTCEuAPI.isHighTier()) registerTieredCover(GTValues.UIV, 4) else null

        @JvmStatic
        var WIRELESS_ENERGY_RECEIVE_COVER_UXV_4A: ItemEntry<ComponentItem>? =
            if (GTCEuAPI.isHighTier()) registerTieredCover(GTValues.UXV, 4) else null

        @JvmStatic
        var WIRELESS_ENERGY_RECEIVE_COVER_OPV_4A: ItemEntry<ComponentItem>? =
            if (GTCEuAPI.isHighTier()) registerTieredCover(GTValues.OpV, 4) else null

        private fun registerTieredCover(tier: Int, amperage: Int): ItemEntry<ComponentItem> = GTMTRegistration.Companion.GTMTHINGS_REGISTRATE
            .item(
                GTValues.VN[tier].lowercase() + "_" + (if (amperage == 1) "" else amperage.toString() + "a_") + "wireless_energy_receive_cover",
            ) { properties: Item.Properties -> ComponentItem.create(properties) }
            .lang(GTValues.VNF[tier] + " " + "Wireless Energy Receive Cover")
            .onRegister(
                GTItems.attach(
                    TooltipBehavior { lines: MutableList<Component?>? ->
                        lines!!.add(Component.translatable("item.gtmthings.wireless_energy_receive_cover.tooltip.1"))
                        lines.add(Component.translatable("item.gtmthings.wireless_energy_receive_cover.tooltip.2"))
                        lines.add(
                            Component.translatable(
                                "item.gtmthings.wireless_energy_receive_cover.tooltip.3",
                                GTValues.VEX[tier] * amperage,
                            ),
                        )
                    },
                    CoverPlaceBehavior(if (amperage == 1) GTMTCovers.WIRELESS_ENERGY_RECEIVE[tier - 1] else GTMTCovers.WIRELESS_ENERGY_RECEIVE_4A[tier - 1]),
                ),
            ).register()

        @JvmStatic
        var ADVANCED_TERMINAL: ItemEntry<ComponentItem> = GTMTRegistration.Companion.GTMTHINGS_REGISTRATE
            .item<ComponentItem>(
                "advanced_terminal",
            ) { properties: Item.Properties -> ComponentItem.create(properties) }
            .properties { p: Item.Properties -> p.stacksTo(1) }
            .onRegister(GTItems.attach<ComponentItem>(AdvancedTerminalBehavior())).register()

        @JvmStatic
        var WIRELESS_ENERGY_TERMINAL: ItemEntry<ComponentItem> = GTMTRegistration.Companion.GTMTHINGS_REGISTRATE
            .item<ComponentItem>(
                "wireless_energy_terminal",
            ) { properties: Item.Properties -> ComponentItem.create(properties) }
            .properties { p: Item.Properties -> p.stacksTo(1) }
            .onRegister(GTItems.attach<ComponentItem>(WirelessEnergyTerminalBehavior()))
            .onRegister(GTItems.attach<ComponentItem>(WirelessEnergyBindingToolBehavior())).register()

        @JvmStatic
        var WIRELESS_ENERGY_BINDING_TOOL: ItemEntry<ComponentItem> = GTMTRegistration.Companion.GTMTHINGS_REGISTRATE
            .item<ComponentItem>(
                "wireless_energy_binding_tool",
            ) { properties: Item.Properties -> ComponentItem.create(properties) }
            .properties { p: Item.Properties -> p.stacksTo(1) }
            .onRegister(GTItems.attach<ComponentItem>(WirelessEnergyBindingToolBehavior())).register()

        fun init() {}
    }
}
