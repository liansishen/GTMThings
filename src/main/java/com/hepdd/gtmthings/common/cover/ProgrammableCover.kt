package com.hepdd.gtmthings.common.cover

import net.minecraft.core.Direction

import com.gregtechceu.gtceu.api.capability.ICoverable
import com.gregtechceu.gtceu.api.cover.CoverBehavior
import com.gregtechceu.gtceu.api.cover.CoverDefinition
import com.gregtechceu.gtceu.api.machine.MetaMachine
import com.gregtechceu.gtceu.api.machine.SimpleTieredMachine

class ProgrammableCover(definition: CoverDefinition, coverHolder: ICoverable, attachedSide: Direction) : CoverBehavior(definition, coverHolder, attachedSide) {
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
