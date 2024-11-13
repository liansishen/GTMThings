package com.hepdd.gtmthings.data;

import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.cover.CoverDefinition;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.client.renderer.cover.ICoverRenderer;
import com.gregtechceu.gtceu.client.renderer.cover.SimpleCoverRenderer;

import com.hepdd.gtmthings.GTMThings;
import com.hepdd.gtmthings.common.cover.AdvancedWirelessTransferCover;
import com.hepdd.gtmthings.common.cover.WirelessEnergyReceiveCover;
import com.hepdd.gtmthings.common.cover.WirelessTransferCover;
import it.unimi.dsi.fastutil.ints.Int2ObjectFunction;

import java.util.Arrays;
import java.util.Locale;

import static com.hepdd.gtmthings.common.registry.GTMTRegistration.GTMTHINGS_REGISTRATE;

public class WirelessCovers {

    static {
        GTMTHINGS_REGISTRATE.creativeModeTab(() -> CreativeModeTabs.WIRELESS_TAB);
    }
    public static final int[] ALL_TIERS = GTValues.tiersBetween(GTValues.LV,
            GTCEuAPI.isHighTier() ? GTValues.OpV : GTValues.UV);

    public final static CoverDefinition[] WIRELESS_ENERGY_RECEIVE = registerTieredWirelessCover(
            "wireless_energy_receive", 1, ALL_TIERS);

    public final static CoverDefinition[] WIRELESS_ENERGY_RECEIVE_4A = registerTieredWirelessCover(
            "4a_wireless_energy_receive", 4, ALL_TIERS);

    public final static CoverDefinition WIRELESS_ITEM_TRANSFER = register("wireless_item_transfer",
            (holder, coverable, side) -> new WirelessTransferCover(holder, coverable, side, WirelessTransferCover.TRANSFER_ITEM), new SimpleCoverRenderer(GTMThings.id("block/cover/overlay_wireless_item_transfer")));

    public final static CoverDefinition WIRELESS_FLUID_TRANSFER = register("wireless_fluid_transfer",
            (holder, coverable, side) -> new WirelessTransferCover(holder, coverable, side, WirelessTransferCover.TRANSFER_FLUID), new SimpleCoverRenderer(GTMThings.id("block/cover/overlay_wireless_fluid_transfer")));

    public final static CoverDefinition ADVANCED_WIRELESS_ITEM_TRANSFER = register("advanced_wireless_item_transfer",
            (holder, coverable, side) -> new AdvancedWirelessTransferCover(holder, coverable, side, WirelessTransferCover.TRANSFER_ITEM), new SimpleCoverRenderer(GTMThings.id("block/cover/overlay_wireless_item_transfer")));

    public final static CoverDefinition ADVANCED_WIRELESS_FLUID_TRANSFER = register("advanced_wireless_fluid_transfer",
            (holder, coverable, side) -> new AdvancedWirelessTransferCover(holder, coverable, side, WirelessTransferCover.TRANSFER_FLUID), new SimpleCoverRenderer(GTMThings.id("block/cover/overlay_wireless_fluid_transfer")));

    public static CoverDefinition register(String id, CoverDefinition.CoverBehaviourProvider behaviorCreator,
                                           ICoverRenderer coverRenderer) {
        var definition = new CoverDefinition(GTMThings.id(id), behaviorCreator, coverRenderer);
        GTRegistries.COVERS.register(GTMThings.id(id), definition);
        return definition;
    }

    public static CoverDefinition[] registerTieredWirelessCover(String id, int amperage, int[] tiers) {
        return Arrays.stream(tiers).mapToObj(tier -> {
            var name = id + "." + GTValues.VN[tier].toLowerCase(Locale.ROOT);
            return register(name,
                    (holder, coverable, side) -> new WirelessEnergyReceiveCover(holder, coverable, side, tier, amperage),
                    new SimpleCoverRenderer(GTMThings.id("block/cover/overlay_" + (amperage == 1 ? "" : "4a_") + "wireless_energy_receive")));
        }).toArray(CoverDefinition[]::new);
    }

    public static CoverDefinition[] registerTiered(String id,
                                                   CoverDefinition.TieredCoverBehaviourProvider behaviorCreator,
                                                   Int2ObjectFunction<ICoverRenderer> coverRenderer, int amperage, int... tiers) {
        return Arrays.stream(tiers).mapToObj(tier -> {
            var name = id + "." + GTValues.VN[tier].toLowerCase(Locale.ROOT);
            return register(name, (def, coverable, side) -> behaviorCreator.create(def, coverable, side, tier),
                    coverRenderer.apply(tier));
        }).toArray(CoverDefinition[]::new);
    }

    public static void init() {}
}
