package com.hepdd.gtmthings.common.cover;

import com.gregtechceu.gtceu.api.capability.ICoverable;
import com.gregtechceu.gtceu.api.cover.CoverBehavior;
import com.gregtechceu.gtceu.api.cover.CoverDefinition;
import com.gregtechceu.gtceu.api.machine.MetaMachine;

import net.minecraft.core.Direction;

import com.hepdd.gtmthings.api.machine.IProgrammableMachine;

public final class ProgrammableCover extends CoverBehavior {

    public ProgrammableCover(CoverDefinition definition, ICoverable coverHolder, Direction attachedSide) {
        super(definition, coverHolder, attachedSide);
    }

    @Override
    public boolean canAttach() {
        MetaMachine machine = MetaMachine.getMachine(coverHolder.holder());
        if (machine instanceof IProgrammableMachine programmableMachine) {
            programmableMachine.setProgrammable(true);
            for (CoverBehavior cover : machine.getCoverContainer().getCovers()) {
                if (cover instanceof ProgrammableCover) return false;
            }
            return true;
        }
        return false;
    }

    @Override
    public void onRemoved() {
        MetaMachine machine = MetaMachine.getMachine(coverHolder.holder());
        if (machine instanceof IProgrammableMachine programmableMachine) {
            programmableMachine.setProgrammable(false);
        }
    }

    @Override
    public void onLoad() {
        super.onLoad();
        MetaMachine machine = MetaMachine.getMachine(coverHolder.holder());
        if (machine instanceof IProgrammableMachine programmableMachine) {
            programmableMachine.setProgrammable(true);
        }
    }
}
