package com.hepdd.gtmthings.data;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.client.renderer.machine.OverlayTieredMachineRenderer;

import net.minecraft.network.chat.Component;

import com.hepdd.gtmthings.GTMThings;
import com.hepdd.gtmthings.common.block.machine.electric.WirelessEnergyInterface;
import com.hepdd.gtmthings.common.block.machine.electric.WirelessEnergyMonitor;
import com.hepdd.gtmthings.common.block.machine.multiblock.part.computation.WirelessOpticalComputationHatchMachine;

import static com.gregtechceu.gtceu.api.GTValues.*;
import static com.hepdd.gtmthings.common.registry.GTMTRegistration.GTMTHINGS_REGISTRATE;

public class WirelessMachines {

    static {
        GTMTHINGS_REGISTRATE.creativeModeTab(() -> CreativeModeTabs.WIRELESS_TAB);
    }

    public static final MachineDefinition WIRELESS_ENERGY_MONITOR = GTMTHINGS_REGISTRATE
            .machine("wireless_energy_monitor", WirelessEnergyMonitor::new)
            .rotationState(RotationState.NON_Y_AXIS)
            .workableTieredHullRenderer(GTMThings.id("block/machines/wireless_energy_monitor"))
            .tier(IV)
            .register();

    public static final MachineDefinition WIRELESS_ENERGY_INTERFACE = GTMTHINGS_REGISTRATE
            .machine("wireless_energy_interface", WirelessEnergyInterface::new)
            .rotationState(RotationState.ALL)
            .renderer(() -> new OverlayTieredMachineRenderer(LV, GTCEu.id("block/machine/part/energy_hatch.input")))
            .tier(LV)
            .register();

    public static final MachineDefinition WIRELESS_COMPUTATION_HATCH_TRANSMITTER = GTMTHINGS_REGISTRATE
            .machine("wireless_computation_transmitter_hatch", (holder) -> new WirelessOpticalComputationHatchMachine(holder, true))
            .rotationState(RotationState.ALL)
            .abilities(PartAbility.COMPUTATION_DATA_TRANSMISSION)
            .renderer(() -> new OverlayTieredMachineRenderer(UV, GTCEu.id("block/machine/part/computation_data_hatch")))
            .tooltips(Component.translatable("gtmthings.machine.wireless_computation_transmitter_hatch.tooltip.1"),
                    Component.translatable("gtmthings.machine.wireless_computation_transmitter_hatch.tooltip.2"))
            .tier(UV)
            .register();

    public static final MachineDefinition WIRELESS_COMPUTATION_HATCH_RECEIVER = GTMTHINGS_REGISTRATE
            .machine("wireless_computation_receiver_hatch", (holder) -> new WirelessOpticalComputationHatchMachine(holder, false))
            .rotationState(RotationState.ALL)
            .abilities(PartAbility.COMPUTATION_DATA_RECEPTION)
            .renderer(() -> new OverlayTieredMachineRenderer(UV, GTCEu.id("block/machine/part/computation_data_hatch")))
            .tooltips(Component.translatable("gtmthings.machine.wireless_computation_receiver_hatch.tooltip.1"),
                    Component.translatable("gtmthings.machine.wireless_computation_receiver_hatch.tooltip.2"))
            .tier(UV)
            .register();

    public static void init() {};
}
