package com.hepdd.gtmthings.api.misc

import net.minecraft.core.BlockPos
import net.minecraft.world.level.block.Block

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition
import com.gregtechceu.gtceu.api.machine.multiblock.part.MultiblockPartMachine
import com.gregtechceu.gtceu.api.registry.GTRegistries

object Hatch {
    @JvmField
    var Set: Set<Block> = HashSet<Block>().apply {
        GTRegistries.MACHINES.forEach { d ->
            if (d.recipeTypes != null || d is MultiblockMachineDefinition) return@forEach

            val block = d.block
            if (d.createMetaMachine(d.blockEntityType.create(BlockPos.ZERO, block.defaultBlockState()) as IMachineBlockEntity)
                    is MultiblockPartMachine
            ) {
                add(block)
            }
        }
    }
}
