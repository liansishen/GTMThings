package com.hepdd.gtmthings.data;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.registry.registrate.MachineBuilder;
import com.gregtechceu.gtceu.client.renderer.machine.MinerRenderer;
import com.hepdd.gtmthings.common.block.machine.electric.DigitalMiner;
import com.hepdd.gtmthings.common.block.machine.multiblock.part.HugeBusPartMachine;
import net.minecraft.network.chat.Component;

import java.util.Locale;
import java.util.function.BiFunction;

import static com.gregtechceu.gtceu.api.GTValues.*;
import static com.hepdd.gtmthings.common.block.machine.multiblock.part.HugeBusPartMachine.INV_MULTIPLE;
import static com.hepdd.gtmthings.common.registry.GTMTRegistration.GTMTHINGS_REGISTRATE;
import static com.hepdd.gtmthings.data.GTMTRecipeTypes.DIGITAL_MINER_RECIPE;

public class CustomMachines {

    public static final int[] ALL_TIERS = GTValues.tiersBetween(ULV, GTCEuAPI.isHighTier() ? MAX : UHV);

    static {
        GTMTHINGS_REGISTRATE.creativeModeTab(() -> CreativeModeTabs.MORE_MACHINES);
    }

    public static final MachineDefinition DIGITAL_MINER = GTMTHINGS_REGISTRATE
            .machine("digital_miner", DigitalMiner::new)
            .rotationState(RotationState.NON_Y_AXIS)
            .renderer(() -> new MinerRenderer(MV, GTCEu.id("block/machines/miner")))
            .recipeTypes(DIGITAL_MINER_RECIPE)
            .compassNode("miner")
            .register();

    public static final MachineDefinition[] HUGE_ITEM_IMPORT_BUS = registerTieredMachines("huge_item_import_bus",
            (holder,tier) -> new HugeBusPartMachine(holder, tier, IO.IN),
            (tier, builder) -> builder
                    .langValue(VNF[tier] + " Input Bus")
                    .rotationState(RotationState.ALL)
                    .abilities(
                            tier == 0 ? new PartAbility[] { PartAbility.IMPORT_ITEMS, PartAbility.STEAM_IMPORT_ITEMS } :
                                    new PartAbility[] { PartAbility.IMPORT_ITEMS })
                    .overlayTieredHullRenderer("item_bus.import")
                    .tooltips(Component.translatable("gtmthings.machine.huge_item_bus.import.tooltip"),
                            Component.translatable("gtceu.universal.tooltip.item_storage_capacity",
                                    (1 + tier) * INV_MULTIPLE))
                    .compassNode("item_bus")
                    .register(),
            ALL_TIERS);

    public static final MachineDefinition[] HUGE_ITEM_EXPORT_BUS = registerTieredMachines("huge_item_export_bus",
            (holder,tier) -> new HugeBusPartMachine(holder, tier, IO.OUT),
            (tier, builder) -> builder
                    .langValue(VNF[tier] + " Output Bus")
                    .rotationState(RotationState.ALL)
                    .abilities(
                            tier == 0 ? new PartAbility[] { PartAbility.EXPORT_ITEMS, PartAbility.STEAM_EXPORT_ITEMS } :
                                    new PartAbility[] { PartAbility.EXPORT_ITEMS })
                    .overlayTieredHullRenderer("item_bus.export")
                    .tooltips(Component.translatable("gtmthings.machine.huge_item_bus.export.tooltip"),
                            Component.translatable("gtceu.universal.tooltip.item_storage_capacity",
                                    (1 + tier)* INV_MULTIPLE))
                    .compassNode("item_bus")
                    .register(),
            ALL_TIERS);

    public static MachineDefinition[] registerTieredMachines(String name,
                                                             BiFunction<IMachineBlockEntity, Integer, MetaMachine> factory,
                                                             BiFunction<Integer, MachineBuilder<MachineDefinition>, MachineDefinition> builder,
                                                             int... tiers) {
        MachineDefinition[] definitions = new MachineDefinition[GTValues.TIER_COUNT];
        for (int tier : tiers) {
            var register = GTMTHINGS_REGISTRATE
                    .machine(GTValues.VN[tier].toLowerCase(Locale.ROOT) + "_" + name,
                            holder -> factory.apply(holder, tier))
                    .tier(tier);
            definitions[tier] = builder.apply(tier, register);
        }
        return definitions;
    }

    public static void init() {}
}
