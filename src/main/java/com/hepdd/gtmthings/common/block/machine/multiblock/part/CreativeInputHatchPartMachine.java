package com.hepdd.gtmthings.common.block.machine.multiblock.part;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.widget.PhantomFluidWidget;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IDistinctPart;
import com.gregtechceu.gtceu.api.machine.multiblock.part.TieredIOPartMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableFluidTank;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.ingredient.FluidIngredient;
import com.gregtechceu.gtceu.api.transfer.fluid.CustomFluidTank;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraftforge.fluids.FluidStack;

import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class CreativeInputHatchPartMachine extends TieredIOPartMachine implements IDistinctPart {

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(CreativeInputHatchPartMachine.class,
            TieredIOPartMachine.MANAGED_FIELD_HOLDER);

    private final int SLOT_COUNT = 9;

    @Persisted
    public final NotifiableFluidTank tank;
    private final int slots;
    @Nullable
    protected TickableSubscription autoIOSubs;
    private Map<Integer, FluidStack> fluidMap;
    @Persisted
    private CustomFluidTank[] creativeTanks;

    // The `Object... args` parameter is necessary in case a superclass needs to pass any args along to createTank().
    // We can't use fields here because those won't be available while createTank() is called.
    public CreativeInputHatchPartMachine(IMachineBlockEntity holder) {
        super(holder, GTValues.MAX, IO.IN);
        this.slots = SLOT_COUNT;
        this.tank = createTank();
        this.fluidMap = new HashMap<>();
        this.creativeTanks = new CustomFluidTank[SLOT_COUNT];
        for (int i = 0; i < this.creativeTanks.length; i++) {
            this.creativeTanks[i] = new CustomFluidTank(1);
        }
    }

    //////////////////////////////////////
    // ***** Initialization ******//
    //////////////////////////////////////
    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    protected NotifiableFluidTank createTank() {
        return new InfinityFluidTank(this, SLOT_COUNT, Integer.MAX_VALUE, IO.IN);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        for (int i = 0; i < SLOT_COUNT; i++) {
            if (this.creativeTanks[i] != null && !this.creativeTanks[i].getFluid().isEmpty()) {
                fluidMap.put(i, this.creativeTanks[i].getFluid());
            }
        }
        updateTankSubscription();
    }

    @Override
    public void onUnload() {
        super.onUnload();
    }

    protected void updateTankSubscription() {
        if (!fluidMap.isEmpty()) {
            autoIOSubs = subscribeServerTick(autoIOSubs, this::autoKeep);
        } else if (autoIOSubs != null) {
            clearAll();
            autoIOSubs.unsubscribe();
            autoIOSubs = null;
        }
    }

    protected void autoKeep() {
        if (getOffsetTimer() % 5 == 0) {
            for (int i = 0; i < SLOT_COUNT; i++) {
                if (fluidMap.containsKey(i)) {
                    var mFluid = this.creativeTanks[i].getFluid().copy();
                    mFluid.setAmount(Integer.MAX_VALUE);
                    this.tank.setFluidInTank(i, mFluid);
                } else {
                    if (!this.tank.getFluidInTank(i).isEmpty()) {
                        this.tank.setFluidInTank(i, FluidStack.EMPTY);
                    }
                }
            }
            updateTankSubscription();
        }
    }

    protected void clearAll() {
        for (int i = 0; i < SLOT_COUNT; i++) {
            if (!this.tank.getFluidInTank(i).isEmpty()) {
                this.tank.setFluidInTank(i, FluidStack.EMPTY);
            }
        }
    }

    @Override
    public void setWorkingEnabled(boolean workingEnabled) {
        super.setWorkingEnabled(workingEnabled);
    }

    //////////////////////////////////////
    // ********** GUI ***********//
    //////////////////////////////////////
    @Override
    public Widget createUIWidget() {
        int rowSize = (int) Math.sqrt(slots);
        int colSize = rowSize;
        if (slots == 8) {
            rowSize = 4;
            colSize = 2;
        }

        var group = new WidgetGroup(0, 0, 18 * rowSize + 16, 18 * colSize + 16);
        var container = new WidgetGroup(4, 4, 18 * rowSize + 8, 18 * colSize + 8);

        int index = 0;
        for (int y = 0; y < colSize; y++) {
            for (int x = 0; x < rowSize; x++) {
                int finalIndex = index++;
                container.addWidget(new PhantomFluidWidget(
                        this.creativeTanks[finalIndex], finalIndex,
                        4 + x * 18, 4 + y * 18, 18, 18,
                        () -> this.creativeTanks[finalIndex].getFluid(),
                        (fluid -> {
                            if (fluid.isEmpty()) {
                                this.creativeTanks[finalIndex].setFluid(fluid);
                                if (!fluidMap.isEmpty() && fluidMap.containsKey(finalIndex)) fluidMap.remove(finalIndex);
                                updateTankSubscription();
                                return;
                            }
                            for (Map.Entry entry : fluidMap.entrySet()) {
                                int i = (int) entry.getKey();
                                FluidStack f = (FluidStack) entry.getValue();
                                if (i != finalIndex && f.getFluid() == fluid.getFluid()) {
                                    return;
                                } else if (i == finalIndex && f.getFluid() != fluid.getFluid()) {
                                    setFluid(finalIndex, fluid);
                                    updateTankSubscription();
                                    return;
                                }
                            }
                            setFluid(finalIndex, fluid);
                            updateTankSubscription();
                        })).setShowAmount(false).setBackground(GuiTextures.FLUID_SLOT));
            }
        }

        container.setBackground(GuiTextures.BACKGROUND_INVERSE);
        group.addWidget(container);

        return group;
    }

    private void setFluid(int index, FluidStack fs) {
        var newFluid = fs.copy();
        newFluid.setAmount(1);
        this.creativeTanks[index].setFluid(newFluid);
        if (fluidMap.containsKey(index)) {
            fluidMap.replace(index, fs);
        } else {
            fluidMap.put(index, fs);
        }
    }

    @Override
    public boolean isDistinct() {
        return this.tank.isDistinct();
    }

    @Override
    public void setDistinct(boolean isDistinct) {
        this.tank.setDistinct(isDistinct);
    }

    private static class InfinityFluidTank extends NotifiableFluidTank {

        public InfinityFluidTank(MetaMachine machine, int slots, int capacity, IO io) {
            super(machine, slots, capacity, io);
        }

        @Override
        public List<FluidIngredient> handleRecipeInner(IO io, GTRecipe recipe, List<FluidIngredient> left, boolean simulate) {
            return super.handleRecipeInner(io, recipe, left, true);
        }
    }
}
