package com.hepdd.gtmthings.common.block.machine.multiblock.part;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.fancy.ConfiguratorPanel;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.fancyconfigurator.CircuitFancyConfigurator;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IDistinctPart;
import com.gregtechceu.gtceu.api.machine.multiblock.part.TieredIOPartMachine;
import com.gregtechceu.gtceu.api.machine.trait.ItemHandlerProxyRecipeTrait;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.common.item.IntCircuitBehaviour;
import com.hepdd.gtmthings.api.misc.UnlimitedItemStackTransfer;
import com.lowdragmc.lowdraglib.gui.widget.DraggableScrollableWidgetGroup;
import com.lowdragmc.lowdraglib.gui.widget.SlotWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.jei.IngredientIO;
import com.lowdragmc.lowdraglib.side.item.ItemTransferHelper;
import com.lowdragmc.lowdraglib.syncdata.ISubscription;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import lombok.Getter;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Set;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class HugeItemBusPartMachine extends TieredIOPartMachine implements IDistinctPart {

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(HugeItemBusPartMachine.class,
            TieredIOPartMachine.MANAGED_FIELD_HOLDER);

    @Getter
    @Persisted
    private final NotifiableItemStackHandler inventory;
    @Nullable
    protected TickableSubscription autoIOSubs;
    @Nullable
    protected ISubscription inventorySubs;
    @Getter
    @Persisted
    protected final NotifiableItemStackHandler circuitInventory;
    @Getter
    protected final ItemHandlerProxyRecipeTrait combinedInventory;

    public HugeItemBusPartMachine(IMachineBlockEntity holder, int tier, IO io, Object... args) {
        super(holder, tier, io);
        this.inventory = createInventory(args);
        this.circuitInventory = createCircuitItemHandler(io);
        this.combinedInventory = createCombinedItemHandler(io);
    }

    protected int getInventorySize() {
        int sizeRoot = 1 + Math.min(9, getTier());
        return sizeRoot * sizeRoot * 2;
    }

    protected NotifiableItemStackHandler createInventory(Object... args) {
//        return new NotifiableItemStackHandler(this, getInventorySize(), io);
        return new NotifiableItemStackHandler(this, getInventorySize(), io, io, UnlimitedItemStackTransfer::new) {

            @Override
            public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate, boolean notifyChanges) {
                var extracted = super.extractItem(slot, amount, simulate, notifyChanges).copy();
                if (!extracted.isEmpty()) {
                    extracted.setCount(Integer.MAX_VALUE);
                }
                return extracted;
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

    protected ItemHandlerProxyRecipeTrait createCombinedItemHandler(Object... args) {
        if (args.length > 0 && args[0] instanceof IO io && io == IO.IN) {
            return new ItemHandlerProxyRecipeTrait(this, Set.of(getInventory(), circuitInventory), IO.IN, IO.NONE);

        } else {
            return new ItemHandlerProxyRecipeTrait(this, Set.of(getInventory(), circuitInventory), IO.NONE, IO.NONE);
        }
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (getLevel() instanceof ServerLevel serverLevel) {
            serverLevel.getServer().tell(new TickTask(0, this::updateInventorySubscription));
        }
        inventorySubs = getInventory().addChangedListener(this::updateInventorySubscription);

        combinedInventory.recomputeEnabledState();
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
    public void onNeighborChanged(Block block, BlockPos fromPos, boolean isMoving) {
        super.onNeighborChanged(block, fromPos, isMoving);
        updateInventorySubscription();
    }

    @Override
    public void onRotated(Direction oldFacing, Direction newFacing) {
        super.onRotated(oldFacing, newFacing);
        updateInventorySubscription();
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
                    getInventory().exportToNearby(getFrontFacing());
                } else if (io == IO.IN) {
                    getInventory().importFromNearby(getFrontFacing());
                }
            }
            updateInventorySubscription();
        }
    }

    @Override
    public void setWorkingEnabled(boolean workingEnabled) {
        super.setWorkingEnabled(workingEnabled);
        updateInventorySubscription();
    }
    @Override
    public boolean isDistinct() {
        return this.inventory.isDistinct() && this.circuitInventory.isDistinct();
    }

    @Override
    public void setDistinct(boolean isDistinct) {
        this.inventory.setDistinct(isDistinct);
        this.circuitInventory.setDistinct(isDistinct);
    }

    public void attachConfigurators(ConfiguratorPanel configuratorPanel) {
        IDistinctPart.super.attachConfigurators(configuratorPanel);
        if (this.io == IO.IN) {
            configuratorPanel.attachConfigurators(new CircuitFancyConfigurator(circuitInventory.storage));
        }
    }

    @Override
    public Widget createUIWidget() {
        int rowSize = (int) Math.sqrt(getInventorySize());
        int colSize = rowSize;
        if (getInventorySize() == 8) {
            rowSize = 4;
            colSize = 2;
        }
        //var group = new WidgetGroup(0, 0, 18 * rowSize + 16 + 8, (18 * colSize + 16)/2);
        var container = new DraggableScrollableWidgetGroup(0, 0, 18 * rowSize + 12, 18 * colSize / 2 + 8);
        container.setYScrollBarWidth(8);
        container.setYBarStyle(GuiTextures.SLIDER_BACKGROUND_VERTICAL, GuiTextures.BUTTON);
//        container.addWidget(scrollableGroup);

        //var container = new WidgetGroup(4, 4, 18 * rowSize + 8, 18 * colSize + 8);
        int index = 0;
        for (int y = 0; y < colSize; y++) {
            for (int x = 0; x < rowSize; x++) {
                container.addWidget(
                        new SlotWidget(getInventory().storage, index++, 4 + x * 18, 4 + y * 18, true, io.support(IO.IN)) {

                        }
                        .setBackgroundTexture(GuiTextures.SLOT)
                        .setIngredientIO(this.io == IO.IN ? IngredientIO.INPUT : IngredientIO.OUTPUT));
            }
        }

        //container.setBackground(GuiTextures.BACKGROUND_INVERSE);
        //group.addWidget(container);

        return container;
    }
}
