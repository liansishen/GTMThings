package com.hepdd.gtmthings.api.machine.trait;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableFluidTank;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.ingredient.FluidIngredient;
import com.gregtechceu.gtceu.api.transfer.fluid.CustomFluidTank;
import com.gregtechceu.gtceu.integration.ae2.utils.KeyStorage;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;

import appeng.api.stacks.AEFluidKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class InaccessibleInfiniteTank extends NotifiableFluidTank {

    private final FluidStorageDelegate storage;

    public InaccessibleInfiniteTank(MetaMachine holder, KeyStorage internalBuffer) {
        super(holder, List.of(new FluidStorageDelegate(internalBuffer)), IO.OUT, IO.NONE);
        internalBuffer.setOnContentsChanged(this::onContentsChanged);
        storage = (FluidStorageDelegate) getStorages()[0];
        allowSameFluids = true;
    }

    public static Fluid getFirst(FluidIngredient fluidIngredient) {
        for (FluidIngredient.Value value : fluidIngredient.values) {
            for (Fluid fluid : value.getFluids()) {
                return fluid;
            }
        }
        return null;
    }

    @Override
    public List<FluidIngredient> handleRecipe(IO io, GTRecipe recipe, List<?> left, boolean simulate) {
        if (!simulate && io == IO.OUT) {
            for (Object ingredient : left) {
                if (((FluidIngredient) ingredient).isEmpty()) continue;
                Fluid fluid = getFirst((FluidIngredient) ingredient);
                if (fluid != null) {
                    storage.fill(fluid, ((FluidIngredient) ingredient).getAmount(), ((FluidIngredient) ingredient).getNbt());
                }
            }
            storage.internalBuffer.onChanged();
            return null;
        }
        return null;
    }

    @Override
    public int getTanks() {
        return 128;
    }

    @Override
    public List<Object> getContents() {
        return Collections.emptyList();
    }

    @Override
    public double getTotalContentAmount() {
        return 0;
    }

    @Override
    public boolean isEmpty() {
        return true;
    }

    @Override
    public @NotNull FluidStack getFluidInTank(int tank) {
        return FluidStack.EMPTY;
    }

    @Override
    public void setFluidInTank(int tank, @NotNull FluidStack fluidStack) {}

    @Override
    public int getTankCapacity(int tank) {
        return Integer.MAX_VALUE;
    }

    @Override
    public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
        return true;
    }

    @Override
    @Nullable
    public List<FluidIngredient> handleRecipeInner(IO io, GTRecipe recipe, List<FluidIngredient> left, boolean simulate) {
        if (io != IO.OUT) return left;
        FluidAction action = simulate ? FluidAction.SIMULATE : FluidAction.EXECUTE;
        for (var it = left.iterator(); it.hasNext();) {
            var ingredient = it.next();
            if (ingredient.isEmpty()) {
                it.remove();
                continue;
            }

            var fluids = ingredient.getStacks();
            if (fluids.length == 0 || fluids[0].isEmpty()) {
                it.remove();
                continue;
            }

            FluidStack output = fluids[0];
            ingredient.shrink(storage.fill(output, action));
            if (ingredient.getAmount() <= 0) it.remove();
        }
        return left.isEmpty() ? null : left;
    }

    private static class FluidStorageDelegate extends CustomFluidTank {

        private final KeyStorage internalBuffer;

        private FluidStorageDelegate(KeyStorage internalBuffer) {
            super(0);
            this.internalBuffer = internalBuffer;
        }

        private void fill(Fluid fluid, int amount, CompoundTag tag) {
            var key = AEFluidKey.of(fluid, tag);
            long oldValue = internalBuffer.storage.getOrDefault(key, 0);
            long changeValue = Math.min(Long.MAX_VALUE - oldValue, amount);
            if (changeValue > 0) {
                internalBuffer.storage.put(key, oldValue + changeValue);
            }
        }

        @Override
        public int getCapacity() {
            return Integer.MAX_VALUE;
        }

        @Override
        public void setFluid(FluidStack fluid) {}

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            var key = AEFluidKey.of(resource.getFluid(), resource.getTag());
            int amount = resource.getAmount();
            long oldValue = internalBuffer.storage.getOrDefault(key, 0);
            long changeValue = Math.min(Long.MAX_VALUE - oldValue, amount);
            if (changeValue > 0 && action.execute()) {
                internalBuffer.storage.put(key, oldValue + changeValue);
            }
            return (int) changeValue;
        }

        @Override
        public boolean supportsFill(int tank) {
            return false;
        }

        @Override
        public boolean supportsDrain(int tank) {
            return false;
        }
    }
}
