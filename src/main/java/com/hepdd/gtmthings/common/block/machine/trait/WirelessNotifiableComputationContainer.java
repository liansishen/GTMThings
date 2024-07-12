package com.hepdd.gtmthings.common.block.machine.trait;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.capability.IOpticalComputationProvider;
import com.gregtechceu.gtceu.api.capability.forge.GTCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.machine.trait.MachineTrait;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableComputationContainer;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.common.blockentity.OpticalPipeBlockEntity;
import com.gregtechceu.gtceu.utils.GTUtil;
import com.hepdd.gtmthings.common.block.machine.multiblock.part.computation.WirelessOpticalComputationHatchMachine;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.List;


public class WirelessNotifiableComputationContainer extends NotifiableComputationContainer {


    private int currentOutputCwu = 0, lastOutputCwu = 0;

    public WirelessNotifiableComputationContainer(MetaMachine machine, IO handlerIO, boolean transmitter) {
        super(machine, handlerIO, transmitter);
    }

    @Override
    public int requestCWUt(int cwut, boolean simulate, @NotNull Collection<IOpticalComputationProvider> seen) {
        var latestTimeStamp = getMachine().getOffsetTimer();
        if (lastTimeStamp < latestTimeStamp) {
            lastOutputCwu = currentOutputCwu;
            currentOutputCwu = 0;
            lastTimeStamp = latestTimeStamp;
        }

        seen.add(this);
        if (handlerIO == IO.IN) {
            if (isTransmitter()) {
                // Ask the Multiblock controller, which *should* be an IOpticalComputationProvider
                if (machine instanceof IOpticalComputationProvider provider) {
                    return provider.requestCWUt(cwut, simulate, seen);
                } else if (machine instanceof IMultiPart part) {
                    if (part.getControllers().isEmpty()) {
                        return 0;
                    }
                    for (IMultiController controller : part.getControllers()) {
                        if (controller instanceof IOpticalComputationProvider provider) {
                            return provider.requestCWUt(cwut, simulate, seen);
                        }
                        for (MachineTrait trait : controller.self().getTraits()) {
                            if (trait instanceof IOpticalComputationProvider provider) {
                                return provider.requestCWUt(cwut, simulate, seen);
                            }
                        }
                    }
                    GTCEu.LOGGER
                            .error("NotifiableComputationContainer could request CWU/t from its machine's controller!");
                    return 0;
                } else {
                    GTCEu.LOGGER.error("NotifiableComputationContainer could request CWU/t from its machine!");
                    return 0;
                }
            } else {
                // Ask the attached Transmitter hatch, if it exists
                IOpticalComputationProvider provider = getOpticalNetProvider();
                if (provider == null) return 0;
                return provider.requestCWUt(cwut, simulate, seen);
            }
        } else {
            lastOutputCwu = lastOutputCwu - cwut;
            return Math.min(lastOutputCwu, cwut);
        }
    }

    @Override
    public int getMaxCWUt(@NotNull Collection<IOpticalComputationProvider> seen) {
        seen.add(this);
        if (handlerIO == IO.IN) {
            if (isTransmitter()) {
                // Ask the Multiblock controller, which *should* be an IOpticalComputationProvider
                if (machine instanceof IOpticalComputationProvider provider) {
                    return provider.getMaxCWUt(seen);
                } else if (machine instanceof IMultiPart part) {
                    if (part.getControllers().isEmpty()) {
                        return 0;
                    }
                    for (IMultiController controller : part.getControllers()) {
                        if (!controller.isFormed()) {
                            continue;
                        }
                        if (controller instanceof IOpticalComputationProvider provider) {
                            return provider.getMaxCWUt(seen);
                        }
                        for (MachineTrait trait : controller.self().getTraits()) {
                            if (trait instanceof IOpticalComputationProvider provider) {
                                return provider.getMaxCWUt(seen);
                            }
                        }
                    }
                    GTCEu.LOGGER.error(
                            "NotifiableComputationContainer could not get maximum CWU/t from its machine's controller!");
                    return 0;
                } else {
                    GTCEu.LOGGER.error("NotifiableComputationContainer could not get maximum CWU/t from its machine!");
                    return 0;
                }
            } else {
                // Ask the attached Transmitter hatch, if it exists
                IOpticalComputationProvider provider = getOpticalNetProvider();
                if (provider == null) return 0;
                return provider.getMaxCWUt(seen);
            }
        } else {
            return lastOutputCwu;
        }
    }

    @Override
    public boolean canBridge(@NotNull Collection<IOpticalComputationProvider> seen) {
        seen.add(this);
        if (handlerIO == IO.IN) {
            if (isTransmitter()) {
                // Ask the Multiblock controller, which *should* be an IOpticalComputationProvider
                if (machine instanceof IOpticalComputationProvider provider) {
                    return provider.canBridge(seen);
                } else if (machine instanceof IMultiPart part) {
                    if (part.getControllers().isEmpty()) {
                        return false;
                    }
                    for (IMultiController controller : part.getControllers()) {
                        if (!controller.isFormed()) {
                            continue;
                        }
                        if (controller instanceof IOpticalComputationProvider provider) {
                            return provider.canBridge(seen);
                        }
                        for (MachineTrait trait : controller.self().getTraits()) {
                            if (trait instanceof IOpticalComputationProvider provider) {
                                return provider.canBridge(seen);
                            }
                        }
                    }
                    GTCEu.LOGGER.error(
                            "NotifiableComputationContainer could not test bridge status of its machine's controller!");
                    return false;
                } else {
                    GTCEu.LOGGER.error("NotifiableComputationContainer could not test bridge status of its machine!");
                    return false;
                }
            } else {
                // Ask the attached Transmitter hatch, if it exists
                IOpticalComputationProvider provider = getOpticalNetProvider();
                if (provider == null) return true; // nothing found, so don't report a problem, just pass quietly
                return provider.canBridge(seen);
            }
        } else {
            return false;
        }
    }

    @Override
    public List<Integer> handleRecipeInner(IO io, GTRecipe recipe, List<Integer> left, @Nullable String slotName,
                                           boolean simulate) {
        IOpticalComputationProvider provider = getOpticalNetProvider();
        if (provider == null) return left;

        int sum = left.stream().reduce(0, Integer::sum);
        if (io == IO.IN) {
            int availableCWUt = requestCWUt(Integer.MAX_VALUE, true);
            if (availableCWUt >= sum) {
                if (recipe.data.getBoolean("duration_is_total_cwu")) {
                    int drawn = provider.requestCWUt(availableCWUt, simulate);
                    if (!simulate) {
                        if (machine instanceof IRecipeLogicMachine rlm) {
                            // first, remove the progress the recipe logic adds.
                            rlm.getRecipeLogic().setProgress(rlm.getRecipeLogic().getProgress() - 1 + drawn);
//                            rlm.getRecipeLogic().progress -= 1;
//                            rlm.getRecipeLogic().progress += drawn;
                        } else if (machine instanceof IMultiPart multiPart) {
                            for (IMultiController controller : multiPart.getControllers()) {
                                if (controller instanceof IRecipeLogicMachine rlm) {
                                    rlm.getRecipeLogic().setProgress(rlm.getRecipeLogic().getProgress() - 1 + drawn);
//                                    rlm.getRecipeLogic().progress -= 1;
//                                    rlm.getRecipeLogic().progress += drawn;
                                }
                            }
                        }
                    }
                    sum -= drawn;
                } else {
                    sum -= provider.requestCWUt(sum, simulate);
                }
            }
        } else if (io == IO.OUT) {
            int canInput = this.getMaxCWUt() - this.lastOutputCwu;
            if (!simulate) {
                this.currentOutputCwu = Math.min(canInput, sum);
            }
            sum = sum - canInput;
        }
        return sum <= 0 ? null : Collections.singletonList(sum);
    }

    @Nullable
    private IOpticalComputationProvider getOpticalNetProvider() {
        if (machine instanceof WirelessOpticalComputationHatchMachine woc && woc.getTransmitterPos() != null) {
            var transmitterMachine = MetaMachine.getMachine(machine.getLevel(),woc.getTransmitterPos());
            if (transmitterMachine instanceof WirelessOpticalComputationHatchMachine transmitter) {
                return transmitter.getComputationContainer();
            }
        }
        return null;
    }
}
