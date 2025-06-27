package com.hepdd.gtmthings.api.capability;

import com.gregtechceu.gtceu.api.machine.feature.IMachineLife;
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;

import com.hepdd.gtmthings.common.block.machine.trait.miner.DigitalMinerLogic;

public interface IDigitalMiner extends IRecipeLogicMachine, IMachineLife {

    @Override
    DigitalMinerLogic getRecipeLogic();

    boolean drainInput(boolean simulate);

    static int getWorkingArea(int maximumRadius) {
        return maximumRadius * 2 + 1;
    }
}
