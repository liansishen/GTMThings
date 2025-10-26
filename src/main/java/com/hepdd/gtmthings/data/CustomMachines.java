package com.hepdd.gtmthings.data;

import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;

import com.hepdd.gtmthings.common.registry.GTMTRegistration;

import static com.gregtechceu.gtceu.api.GTValues.*;
import static com.hepdd.gtmthings.common.registry.GTMTRegistration.GTMTHINGS_REGISTRATE;

public class CustomMachines {

    static {
        GTMTHINGS_REGISTRATE.creativeModeTab(() -> CreativeModeTabs.MORE_MACHINES);
    }

    public static final MachineDefinition ME_EXPORT_BUFFER = GTMTHINGS_REGISTRATE.machine("me_export_buffer", GTMTRegistration.ME_OUTPUT)
            .rotationState(RotationState.ALL)
            .abilities(PartAbility.EXPORT_ITEMS, PartAbility.EXPORT_FLUIDS)
            .overlayTieredHullRenderer("me.export")
            .tier(LuV)
            .register();

    public static void init() {}
}
