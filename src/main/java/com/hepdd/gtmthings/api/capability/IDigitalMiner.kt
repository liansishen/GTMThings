package com.hepdd.gtmthings.api.capability

import com.gregtechceu.gtceu.api.machine.feature.IMachineLife
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine
import com.hepdd.gtmthings.common.block.machine.trait.miner.DigitalMinerLogic

interface IDigitalMiner: IRecipeLogicMachine, IMachineLife {

    companion object {
        @JvmStatic
        fun getWorkingArea(maximumRadius: Int): Int {
            return maximumRadius * 2 + 1
        }
    }

    override fun getRecipeLogic(): DigitalMinerLogic

    fun drainInput(simulate: Boolean): Boolean

}