package com.hepdd.gtmthings.data;

import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.cover.CoverDefinition;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.client.renderer.cover.ICoverRenderer;
import com.gregtechceu.gtceu.client.renderer.cover.SimpleCoverRenderer;
import com.hepdd.gtmthings.GTMThings;
import com.hepdd.gtmthings.common.cover.WirelessEnergyReceiveCover;
import it.unimi.dsi.fastutil.ints.Int2ObjectFunction;

import java.util.Arrays;
import java.util.Locale;

import static com.hepdd.gtmthings.common.registry.GTMTRegistration.GTMTHINGS_REGISTRATE;

public class CustomCovers {

    static {
        GTMTHINGS_REGISTRATE.creativeModeTab(() -> CustomTabs.GTMTHINGS_TAB);
    }
    public static final int[] ALL_TIERS = GTValues.tiersBetween(GTValues.LV,
            GTCEuAPI.isHighTier() ? GTValues.OpV : GTValues.UV);

    public final static CoverDefinition[] WIRELESS_ENERGY_RECEIVE = registerTiered(
            "wireless_energy_receive", WirelessEnergyReceiveCover::new,
            tier -> new SimpleCoverRenderer(GTMThings.id("block/cover/overlay_wireless_energy_receive")), ALL_TIERS);


    public static CoverDefinition register(String id, CoverDefinition.CoverBehaviourProvider behaviorCreator,
                                           ICoverRenderer coverRenderer) {

        var definition = new CoverDefinition(GTMThings.id(id), behaviorCreator, coverRenderer);
        GTRegistries.COVERS.register(GTMThings.id(id), definition);
        return definition;
    }

    public static CoverDefinition[] registerTiered(String id,
                                                   CoverDefinition.TieredCoverBehaviourProvider behaviorCreator,
                                                   Int2ObjectFunction<ICoverRenderer> coverRenderer, int... tiers) {
        return Arrays.stream(tiers).mapToObj(tier -> {
            var name = id + "." + GTValues.VN[tier].toLowerCase(Locale.ROOT);
            return register(name, (def, coverable, side) -> behaviorCreator.create(def, coverable, side, tier),
                    coverRenderer.apply(tier));
        }).toArray(CoverDefinition[]::new);
    }

    public static void init() { }
}
