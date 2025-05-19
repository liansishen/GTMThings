package com.hepdd.gtmthings.common.block.machine.multiblock.part;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.fancy.ConfiguratorPanel;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.fancyconfigurator.CircuitFancyConfigurator;
import com.gregtechceu.gtceu.api.machine.feature.IMachineLife;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IDistinctPart;
import com.gregtechceu.gtceu.api.machine.multiblock.part.TieredIOPartMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.common.item.IntCircuitBehaviour;

import com.lowdragmc.lowdraglib.gui.texture.GuiTextureGroup;
import com.lowdragmc.lowdraglib.gui.texture.TextTexture;
import com.lowdragmc.lowdraglib.gui.util.ClickData;
import com.lowdragmc.lowdraglib.gui.widget.ComponentPanelWidget;
import com.lowdragmc.lowdraglib.gui.widget.DraggableScrollableWidgetGroup;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.side.item.ItemTransferHelper;
import com.lowdragmc.lowdraglib.syncdata.ISubscription;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.ChatFormatting;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

import com.hepdd.gtmthings.api.machine.fancyconfigurator.ButtonConfigurator;
import com.hepdd.gtmthings.api.machine.fancyconfigurator.InventoryFancyConfigurator;
import com.hepdd.gtmthings.api.misc.UnlimitedItemStackTransfer;
import com.hepdd.gtmthings.api.transfer.UnlimitItemTransferHelper;
import com.hepdd.gtmthings.common.block.machine.trait.CatalystItemStackHandler;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import javax.annotation.ParametersAreNonnullByDefault;

import static com.hepdd.gtmthings.utils.FormatUtil.formatNumber;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class HugeBusPartMachine extends TieredIOPartMachine implements IDistinctPart, IMachineLife {

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(HugeBusPartMachine.class,
            TieredIOPartMachine.MANAGED_FIELD_HOLDER);

    public static final int INV_MULTIPLE = 2;
    @Getter
    @Persisted
    protected final NotifiableItemStackHandler inventory;
    @Nullable
    protected TickableSubscription autoIOSubs;
    @Nullable
    protected ISubscription inventorySubs;
    @Getter
    @Persisted
    protected final NotifiableItemStackHandler circuitInventory;
    @Getter
    @Persisted
    protected final CatalystItemStackHandler shareInventory;

    public HugeBusPartMachine(IMachineBlockEntity holder, int tier, IO io, Object... args) {
        this(holder, tier, io, 4, args);
    }

    public HugeBusPartMachine(IMachineBlockEntity holder, int tier, IO io, int shareSize, Object... args) {
        super(holder, tier, io);
        this.inventory = createInventory(args);
        this.circuitInventory = createCircuitItemHandler(io);
        this.shareInventory = new CatalystItemStackHandler(this, shareSize, IO.IN, IO.NONE);
    }

    //////////////////////////////////////
    // ***** Initialization ******//
    //////////////////////////////////////
    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    protected int getInventorySize() {
        if (getTier() < GTValues.EV) return 1 + getTier();
        else return (1 + getTier()) * INV_MULTIPLE;
    }

    protected NotifiableItemStackHandler createInventory(Object... args) {
        return new NotifiableItemStackHandler(this, getInventorySize(), io, io, UnlimitedItemStackTransfer::new) {

            @Override
            public boolean canCapOutput() {
                return true;
            }
        };
    }

    protected NotifiableItemStackHandler createCircuitItemHandler(Object... args) {
        if (args.length > 0 && args[0] instanceof IO io && io == IO.IN) {
            return new NotifiableItemStackHandler(this, 1, IO.IN, IO.NONE)
                    .setFilter(IntCircuitBehaviour::isIntegratedCircuit);
        } else {
            return new NotifiableItemStackHandler(this, 0, IO.NONE);
        }
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (getLevel() instanceof ServerLevel serverLevel) {
            serverLevel.getServer().tell(new TickTask(0, this::updateInventorySubscription));
        }
        inventorySubs = getInventory().addChangedListener(this::updateInventorySubscription);
    }

    @Override
    public void onUnload() {
        super.onUnload();
        if (inventorySubs != null) {
            inventorySubs.unsubscribe();
            inventorySubs = null;
        }
    }

    @Override
    public void saveCustomPersistedData(CompoundTag tag, boolean forDrop) {
        super.saveCustomPersistedData(tag, forDrop);
    }

    @Override
    public void loadCustomPersistedData(CompoundTag tag) {
        super.loadCustomPersistedData(tag);
    }

    @Override
    public boolean isDistinct() {
        return getInventory().isDistinct() && circuitInventory.isDistinct() && shareInventory.isDistinct();
    }

    @Override
    public void setDistinct(boolean isDistinct) {
        getInventory().setDistinct(isDistinct);
        circuitInventory.setDistinct(isDistinct);
        shareInventory.setDistinct(isDistinct);
    }

    protected void refundAll(ClickData clickData) {
        if (ItemTransferHelper.getItemTransfer(getLevel(), getPos().relative(getFrontFacing()),
                getFrontFacing().getOpposite()) != null) {
            setWorkingEnabled(false);
            exportToNearby(getInventory(), getFrontFacing());
        }
    }
    //////////////////////////////////////
    // ******** Auto IO *********//
    //////////////////////////////////////

    @Override
    public void onNeighborChanged(Block block, BlockPos fromPos, boolean isMoving) {
        super.onNeighborChanged(block, fromPos, isMoving);
        updateInventorySubscription();
    }

    @Override
    public void onRotated(Direction oldFacing, Direction newFacing) {
        super.onRotated(oldFacing, newFacing);
        updateInventorySubscription();
    }

    @Override
    public void onMachineRemoved() {
        clearInventory(shareInventory);
    }

    protected void updateInventorySubscription() {
        if (isWorkingEnabled() && ((io == IO.OUT && !getInventory().isEmpty()) || io == IO.IN) &&
                ItemTransferHelper.getItemTransfer(getLevel(), getPos().relative(getFrontFacing()),
                        getFrontFacing().getOpposite()) != null) {
            autoIOSubs = subscribeServerTick(autoIOSubs, this::autoIO);
        } else if (autoIOSubs != null) {
            autoIOSubs.unsubscribe();
            autoIOSubs = null;
        }
    }

    protected void autoIO() {
        if (getOffsetTimer() % 5 == 0) {
            if (isWorkingEnabled()) {
                if (io == IO.OUT) {
                    exportToNearby(getInventory(), getFrontFacing());
                } else if (io == IO.IN) {
                    getInventory().importFromNearby(getFrontFacing());
                }
            }
            updateInventorySubscription();
        }
    }

    public void exportToNearby(NotifiableItemStackHandler handler, @NotNull Direction... facings) {
        if (handler.isEmpty()) return;
        var level = getLevel();
        var pos = getPos();
        for (Direction facing : facings) {
            UnlimitItemTransferHelper.exportToTarget(handler, Integer.MAX_VALUE, f -> true, level, pos.relative(facing),
                    facing.getOpposite());
        }
    }

    @Override
    public void setWorkingEnabled(boolean workingEnabled) {
        super.setWorkingEnabled(workingEnabled);
        updateInventorySubscription();
    }

    //////////////////////////////////////
    // ********** GUI ***********//
    //////////////////////////////////////

    public void attachConfigurators(ConfiguratorPanel configuratorPanel) {
        IDistinctPart.super.attachConfigurators(configuratorPanel);
        if (this.io == IO.IN) {
            configuratorPanel.attachConfigurators(new CircuitFancyConfigurator(circuitInventory.storage));
            configuratorPanel.attachConfigurators(new ButtonConfigurator(new GuiTextureGroup(GuiTextures.BUTTON, new TextTexture("🔙")), this::refundAll)
                    .setTooltips(List.of(Component.translatable("gtmthings.machine.huge_item_bus.tooltip.1"))));
            configuratorPanel.attachConfigurators(new InventoryFancyConfigurator(
                    shareInventory.storage, Component.translatable("gui.gtmthings.share_inventory.title"))
                    .setTooltips(List.of(
                            Component.translatable("gui.gtmthings.share_inventory.desc.0"),
                            Component.translatable("gui.gtmthings.share_inventory.desc.1"),
                            Component.translatable("gui.gtmthings.share_inventory.desc.2"))));
        }
    }

    @Override
    public Widget createUIWidget() {
        int height = 117;
        int width = 178;
        var group = new WidgetGroup(0, 0, width + 8, height + 4);

        var componentPanel = new ComponentPanelWidget(8, 5, this::addDisplayText).setMaxWidthLimit(width - 16);
        var screen = new DraggableScrollableWidgetGroup(4, 4, width, height)
                .setBackground(GuiTextures.DISPLAY)
                .addWidget(componentPanel);
        group.addWidget(screen);

        return group;
    }

    private void addDisplayText(@NotNull List<Component> textList) {
        int itemCount = 0;
        for (int i = 0; i < getInventorySize(); i++) {
            ItemStack is = getInventory().getStackInSlot(i);
            if (!is.isEmpty()) {
                textList.add(is.getDisplayName().copy()
                        .setStyle(Style.EMPTY.withColor(ChatFormatting.YELLOW))
                        .append(Component.literal(formatNumber(is.getCount()))
                                .setStyle(Style.EMPTY.withColor(ChatFormatting.AQUA))));
                itemCount++;
            }
        }
        if (textList.isEmpty()) {
            textList.add(Component.translatable("gtmthings.machine.huge_item_bus.tooltip.3"));
        }
        textList.add(0, Component.translatable("gtmthings.machine.huge_item_bus.tooltip.2", itemCount, getInventorySize())
                .setStyle(Style.EMPTY.withColor(ChatFormatting.GREEN)));
    }
}
