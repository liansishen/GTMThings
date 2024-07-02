package com.hepdd.gtmthings.data;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.client.renderer.machine.MinerRenderer;
import com.hepdd.gtmthings.common.block.machine.electric.DigitalMiner;

import static com.gregtechceu.gtceu.api.GTValues.MV;
import static com.hepdd.gtmthings.common.registry.GTMTRegistration.GTMTHINGS_REGISTRATE;
import static com.hepdd.gtmthings.data.GTMTRecipeTypes.DIGITAL_MINER_RECIPE;

public class CustomMachines {

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

    public static void init() {}
}
