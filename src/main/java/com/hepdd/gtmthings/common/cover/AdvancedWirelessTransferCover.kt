package com.hepdd.gtmthings.common.cover;

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
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

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

    public AdvancedWirelessTransferCover(CoverDefinition definition, ICoverable coverHolder, Direction attachedSide, int transferType) {
        super(definition, coverHolder, attachedSide);
        this.transferType = transferType;

        filterHandlerFluid = FilterHandlers.fluid(this)
                .onFilterLoaded(f -> configureFilter())
                .onFilterUpdated(f -> configureFilter())
                .onFilterRemoved(f -> configureFilter());

        filterHandlerItem = FilterHandlers.item(this)
                .onFilterLoaded(f -> configureFilter())
                .onFilterUpdated(f -> configureFilter())
                .onFilterRemoved(f -> configureFilter());
    }

    @Override
    public boolean canAttach() {
        var targetMachine = MetaMachine.getMachine(coverHolder.getLevel(), coverHolder.getPos());
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
            GetLevel();
        }
        var targetMachine = MetaMachine.getMachine(coverHolder.getLevel(), coverHolder.getPos());
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
        GetLevel();
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
        long timer = coverHolder.getOffsetTimer();
        if (timer % 5 == 0) {
            if (transferType == TRANSFER_ITEM) {
                var adjacentItemTransfer = getAdjacentItemTransfer();
                var myItemHandler = getOwnItemTransfer();

                if (adjacentItemTransfer != null && myItemHandler != null) {
                    moveInventoryItems(myItemHandler, adjacentItemTransfer);
                }
            } else if (transferType == TRANSFER_FLUID) {
                var adjacentFluidTransfer = getAdjacentFluidTransfer();
                var ownFluidTransfer = getOwnFluidTransfer();
                if (ownFluidTransfer != null && adjacentFluidTransfer != null) {
                    transferAny(ownFluidTransfer, adjacentFluidTransfer);
                }
            }
        }
    }

    protected void moveInventoryItems(IItemHandler sourceInventory, IItemHandler targetInventory) {
        ItemFilter filter = filterHandlerItem.getFilter();
        int itemsLeftToTransfer = Integer.MAX_VALUE;

        for (int srcIndex = 0; srcIndex < sourceInventory.getSlots(); srcIndex++) {
            ItemStack sourceStack = sourceInventory.extractItem(srcIndex, itemsLeftToTransfer, true);
            if (sourceStack.isEmpty()) {
                continue;
            }

            if (!filter.test(sourceStack)) {
                continue;
            }

            ItemStack remainder = ItemHandlerHelper.insertItemStacked(targetInventory, sourceStack, true);
            int amountToInsert = sourceStack.getCount() - remainder.getCount();

            if (amountToInsert > 0) {
                sourceStack = sourceInventory.extractItem(srcIndex, amountToInsert, false);
                if (!sourceStack.isEmpty()) {
                    ItemHandlerHelper.insertItemStacked(targetInventory, sourceStack, false);
                    itemsLeftToTransfer -= sourceStack.getCount();

                    if (itemsLeftToTransfer == 0) {
                        break;
                    }
                }
            }
        }
    }

    protected void transferAny(IFluidHandler source, IFluidHandler destination) {
        GTTransferUtils.transferFluidsFiltered(source, destination,
                filterHandlerFluid.getFilter(), Integer.MAX_VALUE);
    }

    protected void GetLevel() {
        if (this.dimensionId == null) return;
        ResourceLocation resLoc = tryParse(this.dimensionId);
        ResourceKey<Level> resKey = ResourceKey.create(Registries.DIMENSION, resLoc);
        this.targetLever = Objects.requireNonNull(coverHolder.getLevel().getServer()).getLevel(resKey);
    }

    protected void configureFilter() {
        // Do nothing in the base implementation. This is intended to be overridden by subclasses.
    }

    protected @Nullable IItemHandler getOwnItemTransfer() {
        return coverHolder.getItemHandlerCap(attachedSide, false);
    }

    protected @Nullable IItemHandler getAdjacentItemTransfer() {
        if (targetLever == null || targetPos == null) return null;
        return GTTransferUtils.getItemHandler(targetLever, targetPos, facing.getOpposite()).resolve().orElse(null);
    }

    protected @Nullable IFluidHandler getOwnFluidTransfer() {
        return coverHolder.getFluidHandlerCap(attachedSide, false);
    }

    protected @Nullable IFluidHandler getAdjacentFluidTransfer() {
        if (targetLever == null || targetPos == null) return null;
        return FluidUtil.getFluidHandler(targetLever, targetPos, facing.getOpposite()).resolve().orElse(null);
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
