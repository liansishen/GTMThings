package com.hepdd.gtmthings.api.misc;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.part.MultiblockPartMachine;
import com.gregtechceu.gtceu.api.registry.GTRegistries;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;

import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;

import java.util.Set;

public final class Hatch {

    public static final Set<Block> Set = new ReferenceOpenHashSet<>();

    static {
        GTRegistries.MACHINES.forEach(d -> {
            if (d.getRecipeTypes() != null || d instanceof MultiblockMachineDefinition) return;
            var block = d.getBlock();
            try {
                if (d.createMetaMachine((MetaMachineBlockEntity) d.getBlockEntityType().create(BlockPos.ZERO, block.defaultBlockState())) instanceof MultiblockPartMachine) {
                    Set.add(block);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
