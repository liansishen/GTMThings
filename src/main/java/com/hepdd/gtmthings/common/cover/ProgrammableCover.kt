package com.hepdd.gtmthings.common.cover

import com.gregtechceu.gtceu.api.capability.ICoverable
import com.gregtechceu.gtceu.api.cover.CoverBehavior
import com.gregtechceu.gtceu.api.cover.CoverDefinition
import com.gregtechceu.gtceu.api.machine.MetaMachine
import com.gregtechceu.gtceu.api.machine.SimpleTieredMachine
import net.minecraft.core.Direction

class ProgrammableCover(definition: CoverDefinition, coverHolder: ICoverable, attachedSide: Direction) :
    CoverBehavior(definition, coverHolder, attachedSide) {
    override fun canAttach(): Boolean {
        val machine = MetaMachine.getMachine(coverHolder.level, coverHolder.pos)
        if (machine is SimpleTieredMachine) {
            for (cover in machine.getCoverContainer().covers) {
                if (cover is ProgrammableCover) return false
            }
            return true
        }
        return false
    }
}
