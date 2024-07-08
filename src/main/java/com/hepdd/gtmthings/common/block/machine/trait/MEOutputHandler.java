package com.hepdd.gtmthings.common.block.machine.trait;

import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;
import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.trait.IRecipeHandlerTrait;
import com.gregtechceu.gtceu.api.machine.trait.MachineTrait;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.ingredient.FluidIngredient;
import com.hepdd.gtmthings.common.block.machine.multiblock.part.appeng.MEOutputPartMachine;
import com.lowdragmc.lowdraglib.syncdata.ISubscription;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import lombok.Getter;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MEOutputHandler extends MachineTrait {

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER =
            new ManagedFieldHolder(MEOutputHandler.class);

    protected List<Runnable> listeners = new ArrayList<>();

    @Getter
    protected final IRecipeHandlerTrait<Ingredient> itemOutputHandler;

    @Getter
    protected final IRecipeHandlerTrait<FluidIngredient> fluidOutputHandler;

    public MEOutputHandler(MetaMachine machine) {
        super(machine);
        this.itemOutputHandler = new ItemOutputHandler();
        this.fluidOutputHandler = new FluidOutputHandler();
    }

    @Override
    public MEOutputPartMachine getMachine() {
        return (MEOutputPartMachine) super.getMachine();
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    @SuppressWarnings("rawtypes")
    public List<IRecipeHandlerTrait> getRecipeHandlers() {
        return List.of(fluidOutputHandler, itemOutputHandler);
    }

    public class ItemOutputHandler implements IRecipeHandlerTrait<Ingredient> {
        @Override
        public IO getHandlerIO() {
            return IO.OUT;
        }

        @Override
        public ISubscription addChangedListener(Runnable listener) {
            listeners.add(listener);
            return () -> listeners.remove(listener);
        }

        @Nullable
        @Override
        public List<Ingredient> handleRecipeInner(
                IO io,
                GTRecipe recipe,
                List<Ingredient> left,
                @Nullable String slotName,
                boolean simulate) {
            if (!getMachine().isWorkingEnabled() || io != IO.OUT) return left;
            if (!simulate) {
                for (Ingredient ingredient : left) {
                    var stack = ingredient.getItems()[0];
                    var key = AEItemKey.of(stack);
                    if (key == null) continue;
                    getMachine().getReturnBuffer().mergeLong(key, stack.getCount(), Long::sum);
                }
            }
            return null;
        }

        @Override
        public List<Object> getContents() {
            return Collections.emptyList();
        }

        @Override
        public double getTotalContentAmount() {
            return 1D;
        }

        @Override
        public int getPriority() {
            return Integer.MAX_VALUE;
        }

        @Override
        public boolean isDistinct() {
            return true;
        }

        @Override
        public RecipeCapability<Ingredient> getCapability() {
            return ItemRecipeCapability.CAP;
        }
    }

    public class FluidOutputHandler implements IRecipeHandlerTrait<FluidIngredient> {
        @Override
        public IO getHandlerIO() {
            return IO.OUT;
        }

        @Override
        public ISubscription addChangedListener(Runnable listener) {
            listeners.add(listener);
            return () -> listeners.remove(listener);
        }

        @Nullable @Override
        public List<FluidIngredient> handleRecipeInner(
                IO io,
                GTRecipe recipe,
                List<FluidIngredient> left,
                @Nullable String slotName,
                boolean simulate) {
            if (!getMachine().isWorkingEnabled() || io != IO.OUT) return left;
            if (!simulate) {
                for (FluidIngredient ingredient : left) {
                    if (ingredient.getAmount() <= 0) continue;
                    var stack = ingredient.getStacks()[0];
                    var key = AEFluidKey.of(stack.getFluid(), stack.getTag());
                    getMachine().getReturnBuffer().mergeLong(key, ingredient.getAmount(), Long::sum);
                }
            }
            return null;
        }

        @Override
        public List<Object> getContents() {
            return Collections.emptyList();
        }

        @Override
        public double getTotalContentAmount() {
            return 1D;
        }

        @Override
        public int getPriority() {
            return Integer.MAX_VALUE;
        }

        @Override
        public boolean isDistinct() {
            return true;
        }

        @Override
        public RecipeCapability<FluidIngredient> getCapability() {
            return FluidRecipeCapability.CAP;
        }
    }
}
