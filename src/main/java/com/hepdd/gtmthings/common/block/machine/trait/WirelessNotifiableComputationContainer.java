package com.hepdd.gtmthings.common.block.machine.trait;

import com.gregtechceu.gtceu.api.capability.IOpticalComputationProvider;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableComputationContainer;

import com.hepdd.gtmthings.api.misc.CleanableReferenceSupplier;
import com.hepdd.gtmthings.common.block.machine.multiblock.part.computation.WirelessOpticalComputationHatchMachine;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public class WirelessNotifiableComputationContainer extends NotifiableComputationContainer {

    private final Supplier<WirelessOpticalComputationHatchMachine> target = new CleanableReferenceSupplier<>(() -> {
        if (machine instanceof WirelessOpticalComputationHatchMachine woc && woc.getTransmitterPos() != null) {
            if (MetaMachine.getMachine(machine.getLevel(), woc.getTransmitterPos()) instanceof WirelessOpticalComputationHatchMachine hatchMachine) {
                return hatchMachine;
            }
        }
        return null;
    }, MetaMachine::isInValid);

    public WirelessNotifiableComputationContainer(MetaMachine machine, boolean transmitter) {
        super(machine, transmitter);
    }

    @Nullable
    protected IOpticalComputationProvider getOpticalNetProvider() {
        var t = target.get();
        if (t != null && t.isTransmitter()) {
            return t.getComputationContainer();
        }
        return null;
    }
}
