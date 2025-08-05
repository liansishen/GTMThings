package com.hepdd.gtmthings.common.block.machine.multiblock.part;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.common.machine.multiblock.part.DualHatchPartMachine;

import com.hepdd.gtmthings.api.machine.trait.ProgrammableCircuitHandler;
import com.hepdd.gtmthings.data.CustomItems;
import org.jetbrains.annotations.NotNull;

public class ProgrammableHatchPartMachine extends DualHatchPartMachine {

    public ProgrammableHatchPartMachine(IMachineBlockEntity holder, int tier, IO io, Object... args) {
        super(holder, tier, io, args);
    }

    protected @NotNull NotifiableItemStackHandler createInventory(Object @NotNull... args) {
        return new NotifiableItemStackHandler(this, getInventorySize(), io).setFilter(itemStack -> !(itemStack.hasTag() && itemStack.is(CustomItems.VIRTUAL_ITEM_PROVIDER.get())));
    }

    @Override
    protected @NotNull NotifiableItemStackHandler createCircuitItemHandler(Object... args) {
        if (args.length > 0 && args[0] instanceof IO io && io == IO.IN) {
            return new ProgrammableCircuitHandler(this);
        } else {
            return new NotifiableItemStackHandler(this, 0, IO.NONE);
        }
    }
}
