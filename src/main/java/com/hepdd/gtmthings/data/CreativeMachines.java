package com.hepdd.gtmthings.data;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.hepdd.gtmthings.common.block.machine.multiblock.part.CreativeEnergyHatchPartMachine;
import com.hepdd.gtmthings.common.block.machine.multiblock.part.CreativeInputBusPartMachine;
import com.hepdd.gtmthings.common.block.machine.multiblock.part.CreativeInputHatchPartMachine;
import com.hepdd.gtmthings.common.block.machine.multiblock.part.CreativeLaserHatchPartMachine;
import net.minecraft.network.chat.Component;

import static com.gregtechceu.gtceu.api.GTValues.*;
import static com.hepdd.gtmthings.common.registry.GTMTRegistration.GTMTHINGS_REGISTRATE;

public class CreativeMachines {

    public static final int[] ALL_TIERS = GTValues.tiersBetween(LV, MAX);
    public static final int[] HIGH_TIERS = GTValues.tiersBetween(IV, MAX);

    static {
        GTMTHINGS_REGISTRATE.creativeModeTab(() -> CreativeModeTabs.CREATIVE_TAB);
    }

    public static final MachineDefinition CREATIVE_FLUID_INPUT_HATCH = GTMTHINGS_REGISTRATE.machine(
                    "creative_fluid_input_hatch", CreativeInputHatchPartMachine::new)
            .rotationState(RotationState.ALL)
            .overlayTieredHullRenderer("fluid_hatch.import_9x")
            .tooltips(Component.translatable("gtmthings.creative_tooltip"))
            .abilities(PartAbility.IMPORT_FLUIDS,PartAbility.IMPORT_FLUIDS_9X)
            .compassNode("fluid_hatch")
            .tier(MAX)
            .register();

    public static final MachineDefinition CREATIVE_ITEM_INPUT_BUS = GTMTHINGS_REGISTRATE.machine(
                    "creative_item_input_bus", CreativeInputBusPartMachine::new)
            .rotationState(RotationState.ALL)
            .overlayTieredHullRenderer("item_bus.import")
            .tooltips(Component.translatable("gtmthings.creative_tooltip"))
            .abilities(PartAbility.IMPORT_ITEMS)
            .compassNode("item_bus")
            .tier(MAX)
            .register();

    //energy input hatch
    public static final MachineDefinition CREATIVE_ENERGY_INPUT_HATCH = GTMTHINGS_REGISTRATE.machine(
            "creative_energy_hatch",CreativeEnergyHatchPartMachine::new)
            .rotationState(RotationState.ALL)
            .tooltips(Component.translatable("gtmthings.creative_tooltip"))
            .overlayTieredHullRenderer("energy_hatch.input")
            .abilities(PartAbility.INPUT_ENERGY)
            .compassNode("energy_hatch")
            .tier(MAX)
            .register();

    //laser input hatch
    public static final MachineDefinition CREATIVE_LASER_INPUT_HATCH = GTMTHINGS_REGISTRATE.machine(
                    "creative_laser_hatch",CreativeLaserHatchPartMachine::new)
            .rotationState(RotationState.ALL)
            .tooltips(Component.translatable("gtmthings.creative_tooltip"))
            .overlayTieredHullRenderer("laser_hatch.target")
            .abilities(PartAbility.INPUT_LASER)
            .tier(MAX)
            .register();

    public static void init() { }
}
