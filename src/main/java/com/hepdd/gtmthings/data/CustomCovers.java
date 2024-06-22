package com.hepdd.gtmthings.data;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.addon.AddonFinder;
import com.gregtechceu.gtceu.api.cover.CoverDefinition;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.client.renderer.cover.ConveyorCoverRenderer;
import com.gregtechceu.gtceu.client.renderer.cover.ICoverRenderer;
import com.gregtechceu.gtceu.client.renderer.cover.SimpleCoverRenderer;
import com.hepdd.gtmthings.common.cover.TestCover;
import com.hepdd.gtmthings.common.cover.WirelessEnergyReceiveCover;
import it.unimi.dsi.fastutil.ints.Int2ObjectFunction;
import net.minecraftforge.fml.ModLoader;
import com.gregtechceu.gtceu.api.addon.IGTAddon;

import java.util.Arrays;
import java.util.Locale;

public class CustomCovers {

    public static final int[] ALL_TIERS = GTValues.tiersBetween(GTValues.LV,
            GTCEuAPI.isHighTier() ? GTValues.OpV : GTValues.UV);

    public final static CoverDefinition[] WIRELESS_ENERGY_RECEIVE = registerTiered(
            "wireless_energy_receive", WirelessEnergyReceiveCover::new,
            tier -> new SimpleCoverRenderer(GTCEu.id("block/cover/overlay_controller")), ALL_TIERS);


    public static CoverDefinition register(String id, CoverDefinition.CoverBehaviourProvider behaviorCreator,
                                           ICoverRenderer coverRenderer) {

        var definition = new CoverDefinition(GTCEu.id(id), behaviorCreator, coverRenderer);
        GTRegistries.COVERS.register(GTCEu.id(id), definition);
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
