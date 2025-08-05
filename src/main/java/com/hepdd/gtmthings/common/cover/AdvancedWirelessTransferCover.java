package com.hepdd.gtmthings.common.cover;

import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.ICoverable;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.cover.CoverBehavior;
import com.gregtechceu.gtceu.api.cover.CoverDefinition;
import com.gregtechceu.gtceu.api.cover.IUICover;
import com.gregtechceu.gtceu.api.cover.filter.FilterHandler;
import com.gregtechceu.gtceu.api.cover.filter.FilterHandlers;
import com.gregtechceu.gtceu.api.cover.filter.FluidFilter;
import com.gregtechceu.gtceu.api.cover.filter.ItemFilter;
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
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandler;

import com.hepdd.gtmthings.api.misc.BlockEntityCache;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

import javax.annotation.ParametersAreNonnullByDefault;

import static net.minecraft.resources.ResourceLocation.tryParse;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class AdvancedWirelessTransferCover extends CoverBehavior implements IUICover {

    public static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            AdvancedWirelessTransferCover.class, CoverBehavior.MANAGED_FIELD_HOLDER);

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

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

    @Persisted
    @DescSynced
    @Getter
    protected final FilterHandler<FluidStack, FluidFilter> filterHandlerFluid;
    @Persisted
    @DescSynced
    @Getter
    protected final FilterHandler<ItemStack, ItemFilter> filterHandlerItem;

    private final BlockEntityCache target = new BlockEntityCache(() -> targetLever.getBlockEntity(targetPos));

    public AdvancedWirelessTransferCover(CoverDefinition definition, ICoverable coverHolder, Direction attachedSide, int transferType) {
        super(definition, coverHolder, attachedSide);
        this.transferType = transferType;
        filterHandlerFluid = FilterHandlers.fluid(this);
        filterHandlerItem = FilterHandlers.item(this);
    }

    @Override
    public boolean canAttach() {
        var targetMachine = MetaMachine.getMachine(coverHolder.holder());
        if (targetMachine instanceof WorkableTieredMachine workableTieredMachine) {
            return (workableTieredMachine.exportItems.getSlots() > 0 && this.transferType == TRANSFER_ITEM) || (workableTieredMachine.exportFluids.getTanks() > 0 && this.transferType == TRANSFER_FLUID);
        } else if (targetMachine instanceof PumpMachine || targetMachine instanceof QuantumTankMachine || targetMachine instanceof DrumMachine) {
            return this.transferType == TRANSFER_FLUID;
        } else if (targetMachine instanceof QuantumChestMachine || targetMachine instanceof CrateMachine) {
            return this.transferType == TRANSFER_ITEM;
        } else return (targetMachine instanceof ItemBusPartMachine itemBusPartMachine && itemBusPartMachine.getInventory().handlerIO != IO.IN && this.transferType == TRANSFER_ITEM) || (targetMachine instanceof FluidHatchPartMachine fluidHatchPartMachine && fluidHatchPartMachine.tank.handlerIO != IO.IN && this.transferType == TRANSFER_FLUID);
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

    @Override
    public List<ItemStack> getAdditionalDrops() {
        var list = super.getAdditionalDrops();
        if (!filterHandlerFluid.getFilterItem().isEmpty()) {
            list.add(filterHandlerFluid.getFilterItem());
        }
        if (!filterHandlerItem.getFilterItem().isEmpty()) {
            list.add(filterHandlerItem.getFilterItem());
        }
        return list;
    }

    @Override
    public void onLoad() {
        super.onLoad();
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

    private void update() {
        if (coverHolder.getOffsetTimer() % 20 == 0) {
            if (transferType == TRANSFER_ITEM) {
                var targetItemTransfer = getTargetItemTransfer();
                var ownItemTransfer = getOwnItemTransfer();
                if (ownItemTransfer != null && targetItemTransfer != null) {
                    GTTransferUtils.transferItemsFiltered(ownItemTransfer, targetItemTransfer, filterHandlerItem.getFilter(), Integer.MAX_VALUE);
                }
            } else if (transferType == TRANSFER_FLUID) {
                var targetFluidTransfer = getTargetFluidTransfer();
                var ownFluidTransfer = getOwnFluidTransfer();
                if (ownFluidTransfer != null && targetFluidTransfer != null) {
                    GTTransferUtils.transferFluidsFiltered(ownFluidTransfer, targetFluidTransfer, filterHandlerFluid.getFilter(), Integer.MAX_VALUE);
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

    @Override
    public Widget createUIWidget() {
        if (transferType == TRANSFER_ITEM) {
            return createItemUIWidget();
        } else {
            return createFluidUIWidget();
        }
    }

    public Widget createItemUIWidget() {
        final var group = new WidgetGroup(0, 0, 176, 107);
        var titleLabel = new LabelWidget(10, 5, Component.translatable("item.gtmthings.advanced_wireless_item_transfer_cover"));
        titleLabel.setText(Component.translatable("item.gtmthings.advanced_wireless_item_transfer_cover").getString());
        group.addWidget(titleLabel);
        group.addWidget(filterHandlerItem.createFilterSlotUI(10, 20));
        group.addWidget(filterHandlerItem.createFilterConfigUI(10, 42, 156, 60));

        return group;
    }

    public Widget createFluidUIWidget() {
        final var group = new WidgetGroup(0, 0, 176, 107);
        var titleLabel = new LabelWidget(10, 5, Component.translatable("item.gtmthings.advanced_wireless_fluid_transfer_cover"));
        titleLabel.setText(Component.translatable("item.gtmthings.advanced_wireless_fluid_transfer_cover").getString());
        group.addWidget(titleLabel);
        group.addWidget(filterHandlerFluid.createFilterSlotUI(10, 20));
        group.addWidget(filterHandlerFluid.createFilterConfigUI(10, 42, 156, 60));

        return group;
    }
}
