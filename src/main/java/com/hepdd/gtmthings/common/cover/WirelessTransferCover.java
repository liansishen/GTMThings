package com.hepdd.gtmthings.common.cover;

import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.ICoverable;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.cover.CoverBehavior;
import com.gregtechceu.gtceu.api.cover.CoverDefinition;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.SimpleTieredMachine;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.WorkableTieredMachine;
import com.gregtechceu.gtceu.common.machine.electric.PumpMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.part.FluidHatchPartMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.part.ItemBusPartMachine;
import com.gregtechceu.gtceu.common.machine.storage.CrateMachine;
import com.gregtechceu.gtceu.common.machine.storage.DrumMachine;
import com.gregtechceu.gtceu.common.machine.storage.QuantumChestMachine;
import com.gregtechceu.gtceu.common.machine.storage.QuantumTankMachine;
import com.gregtechceu.gtceu.utils.GTTransferUtils;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandler;

import com.hepdd.gtmthings.api.misc.BlockEntityCache;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

import javax.annotation.ParametersAreNonnullByDefault;

import static net.minecraft.resources.ResourceLocation.tryParse;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class WirelessTransferCover extends CoverBehavior {

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(WirelessTransferCover.class,
            CoverBehavior.MANAGED_FIELD_HOLDER);

    public static final int TRANSFER_ITEM = 1;
    public static final int TRANSFER_FLUID = 2;

    @Persisted
    protected final int transferType;
    private TickableSubscription subscription;
    protected ServerLevel targetLever;
    @Persisted
    private String dimensionId;
    @Persisted
    protected BlockPos targetPos;
    @Persisted
    protected Direction facing;

    private final BlockEntityCache target = new BlockEntityCache(() -> targetLever.getBlockEntity(targetPos));

    public WirelessTransferCover(CoverDefinition definition, ICoverable coverHolder, Direction attachedSide, int transferType) {
        super(definition, coverHolder, attachedSide);
        this.transferType = transferType;
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    @Override
    public boolean canAttach() {
        var targetMachine = MetaMachine.getMachine(coverHolder.holder());
        if (targetMachine instanceof WorkableTieredMachine workableTieredMachine) {
            if ((workableTieredMachine.exportItems.getSlots() > 0 && this.transferType == TRANSFER_ITEM) || (workableTieredMachine.exportFluids.getTanks() > 0 && this.transferType == TRANSFER_FLUID)) {

                for (var cover : targetMachine.getCoverContainer().getCovers()) {
                    if (cover instanceof WirelessTransferCover wirelessTransferCover && wirelessTransferCover.transferType == this.transferType) return false;
                }
                return true;
            }
            return false;
        } else if (targetMachine instanceof PumpMachine || targetMachine instanceof QuantumTankMachine || targetMachine instanceof DrumMachine) {
            if (this.transferType == TRANSFER_FLUID) {
                for (var cover : targetMachine.getCoverContainer().getCovers()) {
                    if (cover instanceof WirelessTransferCover) return false;
                }
                return true;
            }
            return false;
        } else if (targetMachine instanceof QuantumChestMachine || targetMachine instanceof CrateMachine) {
            if (this.transferType == TRANSFER_ITEM) {
                for (var cover : targetMachine.getCoverContainer().getCovers()) {
                    if (cover instanceof WirelessTransferCover) return false;
                }
                return true;
            }
            return false;
        } else if ((targetMachine instanceof ItemBusPartMachine itemBusPartMachine && itemBusPartMachine.getInventory().handlerIO != IO.IN && this.transferType == TRANSFER_ITEM) || (targetMachine instanceof FluidHatchPartMachine fluidHatchPartMachine && fluidHatchPartMachine.tank.handlerIO != IO.IN && this.transferType == TRANSFER_FLUID)) {
            for (var cover : targetMachine.getCoverContainer().getCovers()) {
                if (cover instanceof WirelessTransferCover wirelessTransferCover && wirelessTransferCover.transferType == this.transferType) return false;
            }
            return true;
        }
        return false;
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (coverHolder.isRemote()) return;
        getTargetLevel();
        subscription = coverHolder.subscribeServerTick(subscription, this::update);
    }

    @Override
    public void onRemoved() {
        super.onRemoved();
        if (subscription != null) {
            subscription.unsubscribe();
        }
    }

    @Override
    public void onAttached(ItemStack itemStack, ServerPlayer player) {
        CompoundTag tag = itemStack.getTag();
        if (tag != null) {
            this.dimensionId = tag.getString("dimensionid");
            var intX = tag.getInt("x");
            var intY = tag.getInt("y");
            var intZ = tag.getInt("z");
            this.targetPos = new BlockPos(intX, intY, intZ);
            this.facing = Direction.byName(tag.getString("facing"));
            getTargetLevel();
        }
        var targetMachine = MetaMachine.getMachine(coverHolder.holder());
        if (targetMachine instanceof SimpleTieredMachine simpleTieredMachine) {
            if (this.transferType == TRANSFER_ITEM) simpleTieredMachine.setAutoOutputItems(false);
            if (this.transferType == TRANSFER_FLUID) simpleTieredMachine.setAutoOutputFluids(false);
        } else if (targetMachine instanceof ItemBusPartMachine itemBusPartMachine && this.transferType == TRANSFER_ITEM) {
            itemBusPartMachine.setWorkingEnabled(false);
        } else if (targetMachine instanceof FluidHatchPartMachine fluidHatchPartMachine && this.transferType == TRANSFER_FLUID) {
            fluidHatchPartMachine.setWorkingEnabled(false);
        }
        super.onAttached(itemStack, player);
    }

    private void update() {
        if (coverHolder.getOffsetTimer() % 20 == 0) {
            if (transferType == TRANSFER_ITEM) {
                var targetItemTransfer = getTargetItemTransfer();
                var ownItemTransfer = getOwnItemTransfer();
                if (ownItemTransfer != null && targetItemTransfer != null) {
                    GTTransferUtils.transferItemsFiltered(ownItemTransfer, targetItemTransfer, o -> true, Integer.MAX_VALUE);
                }
            } else if (transferType == TRANSFER_FLUID) {
                var targetFluidTransfer = getTargetFluidTransfer();
                var ownFluidTransfer = getOwnFluidTransfer();
                if (ownFluidTransfer != null && targetFluidTransfer != null) {
                    GTTransferUtils.transferFluidsFiltered(ownFluidTransfer, targetFluidTransfer, o -> true, Integer.MAX_VALUE);
                }
            }
        }
    }

    private void getTargetLevel() {
        if (this.dimensionId == null) return;
        ResourceLocation resLoc = tryParse(this.dimensionId);
        ResourceKey<Level> resKey = ResourceKey.create(Registries.DIMENSION, resLoc);
        this.targetLever = Objects.requireNonNull(coverHolder.getLevel().getServer()).getLevel(resKey);
    }

    protected @Nullable IItemHandler getOwnItemTransfer() {
        return coverHolder.getItemHandlerCap(attachedSide, false);
    }

    protected @Nullable IItemHandler getTargetItemTransfer() {
        if (targetLever == null || targetPos == null) return null;
        return GTCapabilityHelper.getItemHandler(target.get(), facing.getOpposite());
    }

    protected @Nullable IFluidHandler getOwnFluidTransfer() {
        return coverHolder.getFluidHandlerCap(attachedSide, false);
    }

    protected @Nullable IFluidHandler getTargetFluidTransfer() {
        if (targetLever == null || targetPos == null) return null;
        return GTCapabilityHelper.getFluidHandler(target.get(), facing.getOpposite());
    }
}
