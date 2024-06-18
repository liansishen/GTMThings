package com.example.examplemod.data;

import com.example.examplemod.ExampleMod;
import com.example.examplemod.item.Behaviour.ProgrammingCircuitBehaviour;
import com.example.examplemod.item.ItemProgrammingCircuit;
import com.gregtechceu.gtceu.api.item.ComponentItem;
import com.gregtechceu.gtceu.api.registry.registrate.CompassNode;
import com.gregtechceu.gtceu.api.registry.registrate.CompassSection;
import com.gregtechceu.gtceu.common.data.GTCompassNodes;
import com.gregtechceu.gtceu.common.data.GTCompassSections;
import com.gregtechceu.gtceu.common.item.CoverPlaceBehavior;
import com.gregtechceu.gtceu.common.item.TooltipBehavior;
import com.lowdragmc.lowdraglib.LDLib;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.RegistrateItemModelProvider;
import com.tterrag.registrate.util.entry.ItemEntry;
import com.tterrag.registrate.util.nullness.NonNullBiConsumer;
import com.tterrag.registrate.util.nullness.NonNullConsumer;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.client.model.generators.ModelFile;

import java.util.function.Function;

import static com.example.examplemod.ExampleMod.EXAMPLE_REGISTRATE;
import static com.example.examplemod.data.CustomTabs.GTADDON;
import static com.gregtechceu.gtceu.api.registry.registrate.CompassNode.getOrCreate;
import static com.gregtechceu.gtceu.common.data.GTItems.attach;

public class CustomItems {

    static {
        EXAMPLE_REGISTRATE.creativeModeTab(() -> GTADDON);
    }

    public static ItemEntry<ComponentItem> TEST_COVER = EXAMPLE_REGISTRATE
            .item("test_cover", ComponentItem::create)
            .onRegister(compassNode(GTCompassSections.COVERS, GTCompassNodes.COVER))
            .onRegister(attach(new TooltipBehavior(lines -> {
                lines.add(Component.translatable("examplemod:test_cover.tooltip"));
            }), new CoverPlaceBehavior(CustomCovers.TEST_COVER))).register();

    public static ItemEntry<ComponentItem> PROGRAMER_CIRCUIT = EXAMPLE_REGISTRATE
            .item("programming_circuit", ItemProgrammingCircuit::create)
            .lang("programming_circuit Circuit")
            .model(overrideModel(new ResourceLocation(ExampleMod.MOD_ID,"circuit"), 1))
            .onRegister(modelPredicate(new ResourceLocation(ExampleMod.MOD_ID,"circuit"),
                    (itemStack) -> ProgrammingCircuitBehaviour.getCircuitConfiguration(itemStack) / 100f))
            .onRegister(attach(new ProgrammingCircuitBehaviour()))
//            .onRegister(compassNode(GTCompassSections.MISC))
            .register();

    public static <T extends Item> NonNullConsumer<T> modelPredicate(ResourceLocation predicate,
                                                                     Function<ItemStack, Float> property) {
        return item -> {
            if (LDLib.isClient()) {
                ItemProperties.register(item, predicate, (itemStack, c, l, i) -> property.apply(itemStack));
            }
        };
    }

    public static <T extends Item> NonNullBiConsumer<DataGenContext<Item, T>, RegistrateItemModelProvider> overrideModel(ResourceLocation predicate,
                                                                                                                  int modelNumber) {
        if (modelNumber <= 0) return NonNullBiConsumer.noop();
        return (ctx, prov) -> {
            var rootModel = prov.generated(ctx::getEntry, prov.modLoc("item/%s/1".formatted(prov.name(ctx))));
            for (int i = 0; i < modelNumber; i++) {
                var subModelBuilder = prov.getBuilder("item/" + prov.name(ctx::getEntry) + "/" + i)
                        .parent(new ModelFile.UncheckedModelFile("item/generated"));
                subModelBuilder.texture("layer0", prov.modLoc("item/%s/%d".formatted(prov.name(ctx), i + 1)));

                rootModel = rootModel.override().predicate(predicate, i / 100f)
                        .model(new ModelFile.UncheckedModelFile(prov.modLoc("item/%s/%d".formatted(prov.name(ctx), i))))
                        .end();
            }
        };
    }

    public static <T extends ItemLike> NonNullConsumer<T> compassNode(CompassSection section, CompassNode... preNodes) {
        return item -> getOrCreate(section, item::asItem).addPreNode(preNodes);
    }
    public static void init() {

    }
}
