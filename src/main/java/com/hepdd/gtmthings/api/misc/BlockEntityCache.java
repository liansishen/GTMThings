package com.hepdd.gtmthings.api.misc;

import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.function.Supplier;

public final class BlockEntityCache extends CleanableReferenceSupplier<BlockEntity> {

    public BlockEntityCache(Supplier<BlockEntity> blockEntitySupplier) {
        super(blockEntitySupplier, BlockEntity::isRemoved);
    }
}
