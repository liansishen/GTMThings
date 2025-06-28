package com.hepdd.gtmthings.data

import com.gregtechceu.gtceu.GTCEu
import com.gregtechceu.gtceu.api.GTCEuAPI
import com.gregtechceu.gtceu.api.GTValues
import com.gregtechceu.gtceu.api.capability.ICoverable
import com.gregtechceu.gtceu.api.cover.CoverDefinition
import com.gregtechceu.gtceu.api.cover.CoverDefinition.CoverBehaviourProvider
import com.gregtechceu.gtceu.api.registry.GTRegistries
import com.gregtechceu.gtceu.client.renderer.cover.ICoverRenderer
import com.gregtechceu.gtceu.client.renderer.cover.SimpleCoverRenderer
import com.hepdd.gtmthings.GTMThings.Companion.id
import com.hepdd.gtmthings.common.cover.*
import com.hepdd.gtmthings.common.registry.GTMTRegistration
import it.unimi.dsi.fastutil.ints.Int2ObjectFunction
import net.minecraft.core.Direction
import java.util.*

class GTMTCovers {
    
    companion object {
        init {
            GTMTRegistration.Companion.GTMTHINGS_REGISTRATE.creativeModeTab { CreativeModeTabs.WIRELESS_TAB }
        }

        @JvmStatic
        val ALL_TIERS: IntArray = GTValues.tiersBetween(
            GTValues.LV,
            if (GTCEuAPI.isHighTier()) GTValues.OpV else GTValues.UV
        )

        @JvmStatic
        val CREATIVE_ENERGY: CoverDefinition = register(
            "creative_energy",
            { definition: CoverDefinition, coverHolder: ICoverable, attachedSide: Direction ->
                CreativeEnergyCover(
                    definition,
                    coverHolder,
                    attachedSide
                )
            }, SimpleCoverRenderer(id("block/cover/overlay_creative_energy"))
        )

        @JvmStatic
        val PROGRAMMABLE_COVER: CoverDefinition = register(
            "programmable_cover",
            { definition: CoverDefinition, coverHolder: ICoverable, attachedSide: Direction ->
                ProgrammableCover(
                    definition,
                    coverHolder,
                    attachedSide
                )
            }, SimpleCoverRenderer(GTCEu.id("item/programmed_circuit/1"))
        )

        @JvmStatic
        val WIRELESS_ENERGY_RECEIVE: Array<CoverDefinition> = registerTieredWirelessCover(
            "wireless_energy_receive", 1, ALL_TIERS
        )

        @JvmStatic
        val WIRELESS_ENERGY_RECEIVE_4A: Array<CoverDefinition> = registerTieredWirelessCover(
            "4a_wireless_energy_receive", 4, ALL_TIERS
        )

        @JvmStatic
        val WIRELESS_ITEM_TRANSFER: CoverDefinition = register(
            "wireless_item_transfer",
            { holder: CoverDefinition, coverable: ICoverable, side: Direction ->
                WirelessTransferCover(
                    holder,
                    coverable,
                    side,
                    WirelessTransferCover.TRANSFER_ITEM
                )
            }, SimpleCoverRenderer(id("block/cover/overlay_wireless_item_transfer"))
        )

        @JvmStatic
        val WIRELESS_FLUID_TRANSFER: CoverDefinition = register(
            "wireless_fluid_transfer",
            { holder: CoverDefinition, coverable: ICoverable, side: Direction ->
                WirelessTransferCover(
                    holder,
                    coverable,
                    side,
                    WirelessTransferCover.TRANSFER_FLUID
                )
            }, SimpleCoverRenderer(id("block/cover/overlay_wireless_fluid_transfer"))
        )

        @JvmStatic
        val ADVANCED_WIRELESS_ITEM_TRANSFER: CoverDefinition = register(
            "advanced_wireless_item_transfer",
            { holder: CoverDefinition, coverable: ICoverable, side: Direction ->
                AdvancedWirelessTransferCover(
                    holder,
                    coverable,
                    side,
                    WirelessTransferCover.TRANSFER_ITEM
                )
            }, SimpleCoverRenderer(id("block/cover/overlay_wireless_item_transfer"))
        )

        @JvmStatic
        val ADVANCED_WIRELESS_FLUID_TRANSFER: CoverDefinition = register(
            "advanced_wireless_fluid_transfer",
            { holder: CoverDefinition, coverable: ICoverable, side: Direction ->
                AdvancedWirelessTransferCover(
                    holder,
                    coverable,
                    side,
                    WirelessTransferCover.TRANSFER_FLUID
                )
            }, SimpleCoverRenderer(id("block/cover/overlay_wireless_fluid_transfer"))
        )

        fun register(
            id: String, behaviorCreator: CoverBehaviourProvider?,
            coverRenderer: ICoverRenderer?
        ): CoverDefinition {
            val definition = CoverDefinition(id(id), behaviorCreator, coverRenderer)
            GTRegistries.COVERS.register(id(id), definition)
            return definition
        }

        fun registerTieredWirelessCover(id: String?, amperage: Int, tiers: IntArray): Array<CoverDefinition> {
            return tiers.map { tier ->
                val name = "$id.${GTValues.VN[tier].lowercase(Locale.ROOT)}"
                register(
                    name,
                    { holder, coverable, side ->
                        WirelessEnergyReceiveCover(holder, coverable, side, tier, amperage)
                    },
                    SimpleCoverRenderer(
                        id(
                            "block/cover/overlay_${if (amperage == 1) "" else "4a_"}wireless_energy_receive"
                        )
                    )
                )
            }.toTypedArray()
        }

        fun registerTiered(
            id: String,
            behaviorCreator: CoverDefinition.TieredCoverBehaviourProvider,
            coverRenderer: Int2ObjectFunction<ICoverRenderer>,
            amperage: Int,
            vararg tiers: Int
        ): Array<CoverDefinition> {
            return tiers.map { tier ->
                val name = "$id.${GTValues.VN[tier].lowercase(Locale.ROOT)}"
                register(
                    name,
                    { def, coverable, side -> behaviorCreator.create(def, coverable, side, tier) },
                    coverRenderer.apply(tier)
                )
            }.toTypedArray()
        }

        fun init() {}
    }
}