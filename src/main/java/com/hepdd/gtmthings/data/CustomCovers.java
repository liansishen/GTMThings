package com.hepdd.gtmthings.data;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.cover.CoverDefinition;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.client.renderer.cover.ICoverRenderer;
import com.gregtechceu.gtceu.client.renderer.cover.SimpleCoverRenderer;
import com.hepdd.gtmthings.common.cover.TestCover;

public class CustomCovers {

    static {
        //GTRegistries.COVERS.unfreeze();
    }
    public final static CoverDefinition TEST_COVER = register(
            "test_cover", TestCover::new,
            new SimpleCoverRenderer(GTCEu.id("block/cover/overlay_infinite_water")));

    public static CoverDefinition register(String id, CoverDefinition.CoverBehaviourProvider behaviorCreator,
                                           ICoverRenderer coverRenderer) {

        var definition = new CoverDefinition(GTCEu.id(id), behaviorCreator, coverRenderer);
        GTRegistries.COVERS.register(GTCEu.id(id), definition);
        return definition;
    }

//    public static ItemEntry<ComponentItem> TEST_COVER_COVER = REGISTRATE
//            .item("test_cover_cover", ComponentItem::create)
//            .onRegister(compassNode(GTCompassSections.COVERS, GTCompassNodes.COVER))
//            .onRegister(attach(new TooltipBehavior(lines -> {
//                lines.add(Component.translatable("gtceu.universal.tooltip.produces_fluid", 16_000 / 20));
//            }), new CoverPlaceBehavior(TEST_COVER))).register();

    public static void init() {
//        AddonFinder.getAddons().forEach(IGTAddon::registerCovers);
//        ModLoader.get().postEvent(new GTCEuAPI.RegisterEvent<>(GTRegistries.COVERS, CoverDefinition.class));
//        GTRegistries.COVERS.freeze();
    }
}
