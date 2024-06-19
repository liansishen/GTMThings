package com.hepdd.gtmthings.data;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.registry.registrate.MachineBuilder;
import com.gregtechceu.gtceu.common.machine.electric.HullMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.part.FluidHatchPartMachine;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import com.hepdd.gtmthings.common.block.machine.electric.WirelessEnergyMonitor;
import com.hepdd.gtmthings.common.block.machine.multiblock.part.*;
import net.minecraft.network.chat.Component;

import java.util.Locale;
import java.util.function.BiFunction;

import static com.gregtechceu.gtceu.api.GTValues.*;
import static com.gregtechceu.gtceu.common.registry.GTRegistration.REGISTRATE;

public class CustomMachines {

    public static final int[] ALL_TIERS = GTValues.tiersBetween(LV, MAX);
    public static final int[] HIGH_TIERS = GTValues.tiersBetween(IV, MAX);

    //energy input hatch
    public static final MachineDefinition[] ENERGY_INPUT_HATCH = registerCreativeEnergyHatch(2,PartAbility.INPUT_ENERGY);
    public static final MachineDefinition[] ENERGY_INPUT_HATCH_X4 = registerCreativeEnergyHatch(4,PartAbility.INPUT_ENERGY);
    public static final MachineDefinition[] ENERGY_INPUT_HATCH_X16 = registerCreativeEnergyHatch(16,PartAbility.INPUT_ENERGY);
    //laser input hatch
    public static final MachineDefinition[] LASER_INPUT_HATCH_256 = registerCreativeLaserHatch(IO.IN,256,PartAbility.INPUT_LASER);
    public static final MachineDefinition[] LASER_INPUT_HATCH_1024 = registerCreativeLaserHatch(IO.IN,1024,PartAbility.INPUT_LASER);
    public static final MachineDefinition[] LASER_INPUT_HATCH_4096 = registerCreativeLaserHatch(IO.IN,4096,PartAbility.INPUT_LASER);
//    public static final MachineDefinition[] LASER_INPUT_HATCH_16384 = registerLaserHatch(IO.IN,16384,PartAbility.INPUT_LASER);
//    public static final MachineDefinition[] LASER_INPUT_HATCH_65536 = registerLaserHatch(IO.IN,65536,PartAbility.INPUT_LASER);
//    public static final MachineDefinition[] LASER_INPUT_HATCH_262144 = registerLaserHatch(IO.IN,262144,PartAbility.INPUT_LASER);
//    public static final MachineDefinition[] LASER_INPUT_HATCH_1048576 = registerLaserHatch(IO.IN,1048576,PartAbility.INPUT_LASER);
//    public static final MachineDefinition[] LASER_INPUT_HATCH_4194304 = registerLaserHatch(IO.IN,4194304,PartAbility.INPUT_LASER);
//    public static final MachineDefinition[] LASER_INPUT_HATCH_16777216 = registerLaserHatch(IO.IN,16777216,PartAbility.INPUT_LASER);
    public static final MachineDefinition[] LASER_INPUT_HATCH_67108864 = registerCreativeLaserHatch(IO.IN,67108864,PartAbility.INPUT_LASER);

    public static final MachineDefinition CREATIVE_FLUID_INPUT_HATCH = REGISTRATE.machine(
            "creative_fluid_input_hatch", CreativeInputHatchPartMachine::new)
            .rotationState(RotationState.ALL)
            .overlayTieredHullRenderer("fluid_hatch.import_9x")
            .tooltips(Component.translatable("gtceu.creative_tooltip.1"),
                    Component.translatable("gtceu.creative_tooltip.2"),
                    Component.translatable("gtceu.creative_tooltip.3"))
            .abilities(PartAbility.IMPORT_FLUIDS,PartAbility.IMPORT_FLUIDS_9X)
            .compassNode("fluid_hatch")
            .tier(MAX)
            .register();

    public static final MachineDefinition CREATIVE_ITEM_INPUT_BUS = REGISTRATE.machine(
            "creative_item_input_bus", CreativeInputBusPartMachine::new)
            .rotationState(RotationState.ALL)
            .overlayTieredHullRenderer("item_bus.import")
            .tooltips(Component.translatable("gtceu.creative_tooltip.1"),
                    Component.translatable("gtceu.creative_tooltip.2"),
                    Component.translatable("gtceu.creative_tooltip.3"))
            .abilities(PartAbility.IMPORT_ITEMS)
            .compassNode("item_bus")
            .tier(MAX)
            .register();


//    public final static MachineDefinition[] FLUID_IMPORT_HATCH = registerFluidHatches(
//            "distinct_input_hatch", "Input Hatch", "fluid_hatch.import", "fluid_hatch.import",
//            IO.IN, DistinctFluidHatchPartMachine.INITIAL_TANK_CAPACITY_1X, 1,
//            ALL_TIERS, PartAbility.IMPORT_FLUIDS,
//            PartAbility.IMPORT_FLUIDS_1X);
//
//    public final static MachineDefinition[] FLUID_IMPORT_HATCH_4X = registerFluidHatches(
//            "distinct_input_hatch_4x", "Quadruple Input Hatch", "fluid_hatch.import_4x", "fluid_hatch.import",
//            IO.IN, DistinctFluidHatchPartMachine.INITIAL_TANK_CAPACITY_4X, 4,
//            MULTI_HATCH_TIERS, PartAbility.IMPORT_FLUIDS,
//            PartAbility.IMPORT_FLUIDS_4X);
//
//    public final static MachineDefinition[] FLUID_IMPORT_HATCH_9X = registerFluidHatches(
//            "distinct_input_hatch_9x", "Nonuple Input Hatch", "fluid_hatch.import_9x", "fluid_hatch.import",
//            IO.IN, DistinctFluidHatchPartMachine.INITIAL_TANK_CAPACITY_9X, 9,
//            MULTI_HATCH_TIERS, PartAbility.IMPORT_FLUIDS,
//            PartAbility.IMPORT_FLUIDS_9X);


    public static final MachineDefinition[] WIRELESS_ENERGY_INPUT_HATCH = registerWirelessEnergyHatch(IO.IN,2,PartAbility.INPUT_ENERGY);
    public static final MachineDefinition[] WIRELESS_ENERGY_INPUT_HATCH_4A = registerWirelessEnergyHatch(IO.IN,4,PartAbility.INPUT_ENERGY);
    public static final MachineDefinition[] WIRELESS_ENERGY_INPUT_HATCH_16A = registerWirelessEnergyHatch(IO.IN,16,PartAbility.INPUT_ENERGY);
    public static final MachineDefinition[] WIRELESS_ENERGY_OUTPUT_HATCH = registerWirelessEnergyHatch(IO.OUT,2,PartAbility.OUTPUT_ENERGY);
    public static final MachineDefinition[] WIRELESS_ENERGY_OUTPUT_HATCH_4A = registerWirelessEnergyHatch(IO.OUT,4,PartAbility.OUTPUT_ENERGY);
    public static final MachineDefinition[] WIRELESS_ENERGY_OUTPUT_HATCH_16A = registerWirelessEnergyHatch(IO.OUT,16,PartAbility.OUTPUT_ENERGY);


    public static final MachineDefinition WIRELESS_ENERGY_MONITOR = REGISTRATE
            .machine("wireless_energy_monitor",WirelessEnergyMonitor::new)
            .rotationState(RotationState.NON_Y_AXIS)
            .compassNodeSelf()
            .workableTieredHullRenderer(GTCEu.id("block/machines/wireless_energy_monitor"))
            .tier(IV)
            .register();


    public static final MachineDefinition[] HUGE_ITEM_IMPORT_BUS = registerTieredMachines(
            "huge_input_bus",
            (holder, tier) -> new HugeItemBusPartMachine(holder,tier,IO.IN),
            (tier, builder) -> builder
                    .langValue(VNF[tier] + " Input Bus")
                    .rotationState(RotationState.ALL)
                    .abilities(
                            tier == 0 ? new PartAbility[] { PartAbility.IMPORT_ITEMS, PartAbility.STEAM_IMPORT_ITEMS } :
                                    new PartAbility[] { PartAbility.IMPORT_ITEMS })
                    .overlayTieredHullRenderer("item_bus.import")
                    .tooltips(Component.translatable("gtceu.machine.item_bus.import.tooltip"),
                            Component.translatable("gtceu.universal.tooltip.item_storage_capacity",
                                    (1 + Math.min(9, tier)) * (1 + Math.min(9, tier))))
                    .compassNode("item_bus")
                    .register(),
            ALL_TIERS);


    public static MachineDefinition[] registerTieredMachines(String name,
                                                             BiFunction<IMachineBlockEntity, Integer, MetaMachine> factory,
                                                             BiFunction<Integer, MachineBuilder<MachineDefinition>, MachineDefinition> builder,
                                                             int... tiers) {
        MachineDefinition[] definitions = new MachineDefinition[GTValues.TIER_COUNT];
        for (int tier : tiers) {
            var register = REGISTRATE
                    .machine(GTValues.VN[tier].toLowerCase(Locale.ROOT) + "_" + name,
                            holder -> factory.apply(holder, tier))
                    .tier(tier);
            definitions[tier] = builder.apply(tier, register);
        }
        return definitions;
    }


    public static MachineDefinition[] registerCreativeLaserHatch(IO io, int amperage, PartAbility ability) {
        String name = io == IO.IN ? "target" : "source";
        return registerTieredMachines(amperage + "a_creative_laser_hatch",
                (holder, tier) -> new CreativeLaserHatchPartMachine(holder, tier, amperage), (tier, builder) -> builder
                        .langValue(VNF[tier] + " " + FormattingUtil.formatNumbers(amperage) + "A Laser " +
                                FormattingUtil.toEnglishName(name) + " Hatch")
                        .rotationState(RotationState.ALL)
                        .tooltips(Component.translatable("gtceu.creative_tooltip.1"),
                                Component.translatable("gtceu.creative_tooltip.2"),
                                Component.translatable("gtceu.creative_tooltip.3"),
                                Component.translatable("gtceu.universal.disabled"))
                        .abilities(ability)
                        .overlayTieredHullRenderer("laser_hatch." + name)
                        .register(),
                HIGH_TIERS);
    }

    public static MachineDefinition[] registerCreativeEnergyHatch(int amperage, PartAbility ability) {
        return registerTieredMachines(amperage + "a_creative_energy_hatch",
                (holder, tier) -> new CreativeEnergyHatchPartMachine(holder, tier,  amperage),
                (tier, builder) -> builder
                        .langValue(VNF[tier] + " Energy Hatch")
                        .rotationState(RotationState.ALL)
                        .abilities(ability)
                        .tooltips(Component.translatable("gtceu.creative_tooltip.1"),
                                Component.translatable("gtceu.creative_tooltip.2"),
                                Component.translatable("gtceu.creative_tooltip.3"))
                        .overlayTieredHullRenderer("energy_hatch.input")
                        .compassNode("energy_hatch")
                        .register(),
                ALL_TIERS);
    }

    public static MachineDefinition[] registerWirelessEnergyHatch(IO io, int amperage, PartAbility ability) {
        var name = io == IO.IN ? "input" : "output";
        return registerTieredMachines(amperage + "a_wireless_energy_" + name + "_hatch",
                (holder, tier) -> new CreativeEnergyHatchPartMachine(holder, tier,  amperage),
                (tier, builder) -> builder
                        .langValue(VNF[tier] + (io == IO.IN ? " Energy Hatch" : " Dynamo Hatch"))
                        .rotationState(RotationState.ALL)
                        .abilities(ability)
                        .tooltips(Component.translatable("gtceu.machine.energy_hatch." + name + ".tooltip")
                                ,(Component.translatable("gtceu.machine.wireless_energy_hatch." + name + ".tooltip")))
                        .overlayTieredHullRenderer("energy_hatch." + name)
                        .compassNode("energy_hatch")
                        .register(),
                ALL_TIERS);
    }

    private static MachineDefinition[] registerFluidHatches(String name, String displayname, String model,
                                                            String tooltip, IO io, long initialCapacity, int slots,
                                                            int[] tiers, PartAbility... abilities) {
        return registerTieredMachines(name,
                (holder, tier) -> new DistinctFluidHatchPartMachine(holder, tier, io, initialCapacity, slots),
                (tier, builder) -> {
                    builder.langValue(VNF[tier] + ' ' + displayname)
                            .rotationState(RotationState.ALL)
                            .overlayTieredHullRenderer(model)
                            .abilities(abilities)
                            .compassNode("fluid_hatch")
                            .tooltips(Component.translatable("gtceu.machine." + tooltip + ".tooltip"));

                    if (slots == 1) {
                        builder.tooltips(Component.translatable("gtceu.universal.tooltip.fluid_storage_capacity",
                                FluidHatchPartMachine.getTankCapacity(initialCapacity, tier)));
                    } else {
                        builder.tooltips(Component.translatable("gtceu.universal.tooltip.fluid_storage_capacity_mult",
                                slots, FluidHatchPartMachine.getTankCapacity(initialCapacity, tier)));
                    }

                    return builder.register();
                },
                tiers);
    }

    public static void init() { }
}
