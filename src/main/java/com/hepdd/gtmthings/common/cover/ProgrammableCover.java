package com.hepdd.gtmthings.common.cover;

import com.gregtechceu.gtceu.api.capability.ICoverable;
import com.gregtechceu.gtceu.api.cover.CoverBehavior;
import com.gregtechceu.gtceu.api.cover.CoverDefinition;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.SimpleTieredMachine;

import net.minecraft.core.Direction;

public final class ProgrammableCover extends CoverBehavior {

    public ProgrammableCover(CoverDefinition definition, ICoverable coverHolder, Direction attachedSide) {
        super(definition, coverHolder, attachedSide);
    }

    @Override
    public boolean canAttach() {
        MetaMachine machine = MetaMachine.getMachine(coverHolder.getLevel(), coverHolder.getPos());
        if (machine instanceof SimpleTieredMachine) {
            for (CoverBehavior cover : machine.getCoverContainer().getCovers()) {
                if (cover instanceof ProgrammableCover) return false;
            }
            return true;
        }
        return false;
    }
}
