package com.hepdd.gtmthings.common.block.machine.trait

import com.gregtechceu.gtceu.GTCEu
import com.gregtechceu.gtceu.api.capability.IOpticalComputationProvider
import com.gregtechceu.gtceu.api.capability.recipe.IO
import com.gregtechceu.gtceu.api.machine.MetaMachine
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart
import com.gregtechceu.gtceu.api.machine.trait.NotifiableComputationContainer
import com.gregtechceu.gtceu.api.recipe.GTRecipe
import com.hepdd.gtmthings.common.block.machine.multiblock.part.computation.WirelessOpticalComputationHatchMachine

import kotlin.math.min

class WirelessNotifiableComputationContainer(machine: MetaMachine, handlerIO: IO?, transmitter: Boolean) : NotifiableComputationContainer(machine, handlerIO, transmitter) {

    private var currentOutputCwu = 0
    private var lastOutputCwu: Int = 0

    override fun requestCWUt(cwut: Int, simulate: Boolean, seen: MutableCollection<IOpticalComputationProvider?>): Int {
        val latestTimeStamp = getMachine().offsetTimer
        if (lastTimeStamp < latestTimeStamp) {
            lastOutputCwu = currentOutputCwu
            currentOutputCwu = 0
            lastTimeStamp = latestTimeStamp
        }

        seen.add(this)
        if (handlerIO == IO.IN) {
            if (isTransmitter) {
                // Ask the Multiblock controller, which *should* be an IOpticalComputationProvider
                when (machine) {
                    is IOpticalComputationProvider -> {
                        return (machine as IOpticalComputationProvider).requestCWUt(cwut, simulate, seen)
                    }

                    is IMultiPart -> {
                        if ((machine as IMultiPart).controllers.isEmpty()) {
                            return 0
                        }
                        for (controller in (machine as IMultiPart).controllers) {
                            if (controller is IOpticalComputationProvider) {
                                return controller.requestCWUt(cwut, simulate, seen)
                            }
                            for (trait in controller.self().getTraits()) {
                                if (trait is IOpticalComputationProvider) {
                                    return trait.requestCWUt(cwut, simulate, seen)
                                }
                            }
                        }
                        GTCEu.LOGGER
                            .error("NotifiableComputationContainer could request CWU/t from its machine's controller!")
                        return 0
                    }

                    else -> {
                        GTCEu.LOGGER.error("NotifiableComputationContainer could request CWU/t from its machine!")
                        return 0
                    }
                }
            } else {
                // Ask the attached Transmitter hatch if it exists
                val provider: IOpticalComputationProvider? = getOpticalNetProvider()
                if (provider == null) return 0
                return provider.requestCWUt(cwut, simulate, seen)
            }
        } else {
            lastOutputCwu = lastOutputCwu - cwut
            return min(lastOutputCwu, cwut)
        }
    }

    override fun getMaxCWUt(seen: MutableCollection<IOpticalComputationProvider?>): Int {
        seen.add(this)
        if (handlerIO == IO.IN) {
            if (isTransmitter) {
                // Ask the Multiblock controller, which *should* be an IOpticalComputationProvider
                when (machine) {
                    is IOpticalComputationProvider -> {
                        return (machine as IOpticalComputationProvider).getMaxCWUt(seen)
                    }

                    is IMultiPart -> {
                        if ((machine as IMultiPart).controllers.isEmpty()) {
                            return 0
                        }
                        for (controller in (machine as IMultiPart).controllers) {
                            if (!controller.isFormed) {
                                continue
                            }
                            if (controller is IOpticalComputationProvider) {
                                return controller.getMaxCWUt(seen)
                            }
                            for (trait in controller.self().getTraits()) {
                                if (trait is IOpticalComputationProvider) {
                                    return trait.getMaxCWUt(seen)
                                }
                            }
                        }
                        GTCEu.LOGGER.error(
                            "NotifiableComputationContainer could not get maximum CWU/t from its machine's controller!",
                        )
                        return 0
                    }

                    else -> {
                        GTCEu.LOGGER.error("NotifiableComputationContainer could not get maximum CWU/t from its machine!")
                        return 0
                    }
                }
            } else {
                // Ask the attached Transmitter hatch if it exists
                val provider: IOpticalComputationProvider? = getOpticalNetProvider()
                if (provider == null) return 0
                return provider.getMaxCWUt(seen)
            }
        } else {
            return lastOutputCwu
        }
    }

    override fun canBridge(seen: MutableCollection<IOpticalComputationProvider?>): Boolean {
        seen.add(this)
        if (handlerIO == IO.IN) {
            if (isTransmitter) {
                // Ask the Multiblock controller, which *should* be an IOpticalComputationProvider
                when (machine) {
                    is IOpticalComputationProvider -> {
                        return (machine as IOpticalComputationProvider).canBridge(seen)
                    }

                    is IMultiPart -> {
                        if ((machine as IMultiPart).controllers.isEmpty()) {
                            return false
                        }
                        for (controller in (machine as IMultiPart).controllers) {
                            if (!controller.isFormed) {
                                continue
                            }
                            if (controller is IOpticalComputationProvider) {
                                return controller.canBridge(seen)
                            }
                            for (trait in controller.self().getTraits()) {
                                if (trait is IOpticalComputationProvider) {
                                    return trait.canBridge(seen)
                                }
                            }
                        }
                        GTCEu.LOGGER.error(
                            "NotifiableComputationContainer could not test bridge status of its machine's controller!",
                        )
                        return false
                    }

                    else -> {
                        GTCEu.LOGGER.error("NotifiableComputationContainer could not test bridge status of its machine!")
                        return false
                    }
                }
            } else {
                // Ask the attached Transmitter hatch if it exists
                val provider: IOpticalComputationProvider? = getOpticalNetProvider()
                if (provider == null) return true // nothing found, so don't report a problem, just pass quietly

                return provider.canBridge(seen)
            }
        } else {
            return false
        }
    }

    override fun handleRecipeInner(io: IO?, recipe: GTRecipe, left: MutableList<Int?>, simulate: Boolean): MutableList<Int?>? {
        val provider: IOpticalComputationProvider? = getOpticalNetProvider()
        if (provider == null) return left

        var sum: Int = left.stream().reduce(0) { a: Int?, b: Int? -> Integer.sum(a!!, b!!) }!!
        if (io == IO.IN) {
            val availableCWUt = requestCWUt(Int.Companion.MAX_VALUE, true)
            if (availableCWUt >= sum) {
                if (recipe.data.getBoolean("duration_is_total_cwu")) {
                    val drawn = provider.requestCWUt(availableCWUt, simulate)
                    if (!simulate) {
                        when (machine) {
                            is IRecipeLogicMachine -> {
                                // first, remove the progress the recipe logic adds.
                                (machine as IRecipeLogicMachine).recipeLogic.setProgress((machine as IRecipeLogicMachine).recipeLogic.getProgress() - 1 + drawn)
                            }

                            else -> (machine as? IMultiPart)?.let {
                                for (controller in it.controllers) {
                                    if (controller is IRecipeLogicMachine) {
                                        controller.recipeLogic
                                            .setProgress(controller.recipeLogic.getProgress() - 1 + drawn)
                                    }
                                }
                            }
                        }
                    }
                    sum -= drawn
                } else {
                    sum -= provider.requestCWUt(sum, simulate)
                }
            }
        } else if (io == IO.OUT) {
            val canInput = this.getMaxCWUt() - this.lastOutputCwu
            if (!simulate) {
                this.currentOutputCwu = min(canInput, sum)
            }
            sum = sum - canInput
        }
        return if (sum <= 0) null else mutableListOf(sum)
    }

    private fun getOpticalNetProvider(): IOpticalComputationProvider? {
        if (machine is WirelessOpticalComputationHatchMachine && (machine as WirelessOpticalComputationHatchMachine).transmitterPos != null) {
            val transmitterMachine = MetaMachine.getMachine(machine.level!!, (machine as WirelessOpticalComputationHatchMachine).transmitterPos!!)
            if (transmitterMachine is WirelessOpticalComputationHatchMachine) {
                return transmitterMachine.computationContainer
            }
        }
        return null
    }
}
