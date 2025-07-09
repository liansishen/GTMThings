package com.hepdd.gtmthings.common.block.machine.multiblock.part;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.blockentity.IPaintable;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.fancy.ConfiguratorPanel;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.fancyconfigurator.CircuitFancyConfigurator;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IDistinctPart;
import com.gregtechceu.gtceu.api.machine.multiblock.part.TieredIOPartMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.transfer.item.CustomItemStackHandler;
import com.gregtechceu.gtceu.common.item.IntCircuitBehaviour;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import com.hepdd.gtmthings.api.misc.UnlimitedItemStackTransfer;
import com.lowdragmc.lowdraglib.gui.widget.PhantomSlotWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.misc.ItemStackTransfer;
import com.lowdragmc.lowdraglib.syncdata.ISubscription;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import com.lowdragmc.lowdraglib.utils.Position;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.IntFunction;

import javax.annotation.ParametersAreNonnullByDefault;

import static com.gregtechceu.gtceu.integration.ae2.gui.widget.slot.AEConfigSlotWidget.drawSelectionOverlay;
import static com.lowdragmc.lowdraglib.gui.util.DrawerHelper.drawItemStack;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class CreativeInputBusPartMachine extends TieredIOPartMachine implements IDistinctPart, IPaintable {

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(CreativeInputBusPartMachine.class,
            TieredIOPartMachine.MANAGED_FIELD_HOLDER);

    private final int ITEM_SIZE = 5;

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
    @Persisted
    private ItemStackTransfer creativeStorage;
    protected ArrayList<Item> lstItem;

    public CreativeInputBusPartMachine(IMachineBlockEntity holder, Function<Integer, ItemStackTransfer> transferFactory) {
        super(holder, GTValues.MAX, IO.IN);
        this.inventory = createInventory();
        this.circuitInventory = createCircuitItemHandler();
        this.creativeStorage = transferFactory.apply(this.getInventorySize());
        this.lstItem = new ArrayList<>();
    }

    public CreativeInputBusPartMachine(IMachineBlockEntity holder) {
        this(holder, ItemStackTransfer::new);
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    protected int getInventorySize() {
        return ITEM_SIZE * ITEM_SIZE;
    }

    protected NotifiableItemStackHandler createInventory() {
        return new InfinityItemStackHandler(this, getInventorySize(), io, io, UnlimitedItemStackTransfer::new);
    }

    protected NotifiableItemStackHandler createCircuitItemHandler() {
        return new NotifiableItemStackHandler(this, 1, IO.IN, IO.NONE)
                .setFilter(IntCircuitBehaviour::isIntegratedCircuit);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        for (int i = 0; i < this.getInventorySize(); i++) {
            ItemStack is = this.creativeStorage.getStackInSlot(i);
            if (!is.isEmpty()) {
                lstItem.add(is.getItem());
            }
        }
        getHandlerList().setColor(getPaintingColor());
        updateInventorySubscription();
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
    public void onPaintingColorChanged(int color) {
        getHandlerList().setColor(color, true);
    }

    @Override
    public int tintColor(int index) {
        if (index == 9) return getRealColor();
        return -1;
    }

    @Override
    public boolean isDistinct() {
        return getInventory().isDistinct() && circuitInventory.isDistinct();
    }

    @Override
    public void setDistinct(boolean isDistinct) {
        getInventory().setDistinct(isDistinct);
        circuitInventory.setDistinct(isDistinct);
    }

    protected void autoKeep() {
        if (getOffsetTimer() % 5 == 0) {
            for (int i = 0; i < this.getInventorySize(); i++) {
                ItemStack is = this.creativeStorage.getStackInSlot(i);
                if (!is.isEmpty()) {
                    var newItem = is.copy();
                    newItem.setCount(Integer.MAX_VALUE);
                    getInventory().storage.setStackInSlot(i, newItem);
                }
            }
            updateInventorySubscription();
        }
    }

    protected void updateInventorySubscription() {
        if (!lstItem.isEmpty()) {
            autoIOSubs = subscribeServerTick(autoIOSubs, this::autoKeep);
        } else if (autoIOSubs != null) {
            autoIOSubs.unsubscribe();
            autoIOSubs = null;
        }
    }

    @Override
    public void setWorkingEnabled(boolean workingEnabled) {
        super.setWorkingEnabled(workingEnabled);
    }

    public void attachConfigurators(ConfiguratorPanel configuratorPanel) {
        IDistinctPart.super.attachConfigurators(configuratorPanel);
        if (this.io == IO.IN) {
            configuratorPanel.attachConfigurators(new CircuitFancyConfigurator(circuitInventory.storage));
        }
    }

    @Override
    public Widget createUIWidget() {
        int rowSize = ITEM_SIZE;
        int colSize = ITEM_SIZE;
        if (getInventorySize() == 8) {
            rowSize = 4;
            colSize = 2;
        }
        var group = new WidgetGroup(0, 0, 18 * rowSize + 16, 18 * colSize + 16);
        var container = new WidgetGroup(4, 4, 18 * rowSize + 8, 18 * colSize + 8);
        int index = 0;
        for (int y = 0; y < colSize; y++) {
            for (int x = 0; x < rowSize; x++) {
                int finalIndex = index++;
                container.addWidget(
                        new PhantomSlotWidget(this.creativeStorage, finalIndex, 4 + x * 18, 4 + y * 18) {

                            @Override
                            public ItemStack slotClickPhantom(Slot slot, int mouseButton, ClickType clickTypeIn, ItemStack stackHeld) {
                                ItemStack stack = ItemStack.EMPTY;
                                ItemStack stackSlot = slot.getItem();
                                if (!stackSlot.isEmpty()) {
                                    stack = stackSlot.copy();
                                }

                                if (stackHeld.isEmpty() || mouseButton == 2 || mouseButton == 1) {   // held is
                                                                                                     // empty,right
                                                                                                     // click,middle
                                                                                                     // click -> clear
                                                                                                     // slot
                                    lstItem.remove(stackSlot.getItem());
                                    fillPhantomSlot(slot, ItemStack.EMPTY);
                                    getInventory().setStackInSlot(finalIndex, ItemStack.EMPTY);
                                    updateInventorySubscription();
                                } else if (stackSlot.isEmpty()) {   // slot is empty
                                    if (!stackHeld.isEmpty() && !lstItem.contains(stackHeld.getItem())) { // held is not
                                                                                                          // empty and
                                                                                                          // item not in
                                                                                                          // other slot
                                                                                                          // -> add to
                                                                                                          // slot
                                        lstItem.add(stackHeld.getItem());
                                        fillPhantomSlot(slot, stackHeld);
                                        var itemStack = stackHeld.copy();
                                        itemStack.setCount(Integer.MAX_VALUE);
                                        getInventory().setStackInSlot(finalIndex, itemStack);
                                        updateInventorySubscription();
                                    }
                                } else {
                                    if (!areItemsEqual(stackSlot, stackHeld)) {  // slot item not equal to held item
                                        if (!lstItem.contains(stackHeld.getItem())) { // item not in other slot ->
                                                                                      // change the slot
                                            lstItem.remove(stackSlot.getItem());
                                            lstItem.add(stackHeld.getItem());
                                            fillPhantomSlot(slot, stackHeld);
                                            var itemStack = stackHeld.copy();
                                            itemStack.setCount(Integer.MAX_VALUE);
                                            getInventory().setStackInSlot(finalIndex, itemStack);
                                            updateInventorySubscription();
                                        }
                                    }
                                }
                                return stack;
                            }

                            @Override
                            public void drawInBackground(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
                                super.drawInBackground(graphics, mouseX, mouseY, partialTicks);
                                Position position = getPosition();
                                GuiTextures.SLOT.draw(graphics, mouseX, mouseY, position.x, position.y, 18, 18);
                                GuiTextures.CONFIG_ARROW_DARK.draw(graphics, mouseX, mouseY, position.x, position.y, 18, 18);
                                int stackX = position.x + 1;
                                int stackY = position.y + 1;
                                ItemStack stack = null;
                                if (getHandler() != null) {
                                    stack = getHandler().getItem();
                                    drawItemStack(graphics, stack, stackX, stackY, 0xFFFFFFFF, null);
                                }
                                if (mouseOverStock(mouseX, mouseY)) {
                                    drawSelectionOverlay(graphics, stackX, stackY + 18, 16, 16);
                                }
                            }

                            private void fillPhantomSlot(Slot slot, ItemStack stackHeld) {
                                if (stackHeld.isEmpty()) {
                                    slot.set(ItemStack.EMPTY);
                                } else {
                                    ItemStack phantomStack = stackHeld.copy();
                                    phantomStack.setCount(1);
                                    slot.set(phantomStack);
                                }
                            }

                            public boolean areItemsEqual(ItemStack itemStack1, ItemStack itemStack2) {
                                return ItemStack.matches(itemStack1, itemStack2);
                            }

                            private boolean mouseOverStock(double mouseX, double mouseY) {
                                Position position = getPosition();
                                return isMouseOver(position.x, position.y + 18, 18, 18, mouseX, mouseY);
                            }
                        }
                                .setClearSlotOnRightClick(false)
                                .setChangeListener(this::markDirty));
            }
        }

        container.setBackground(GuiTextures.BACKGROUND_INVERSE);
        group.addWidget(container);

        return group;
    }

    private static class InfinityItemStackHandler extends NotifiableItemStackHandler {

        public InfinityItemStackHandler(MetaMachine machine, int slots, @NotNull IO handlerIO, @NotNull IO capabilityIO, IntFunction<CustomItemStackHandler> storageFactory) {
            super(machine, slots, handlerIO, capabilityIO, storageFactory);
        }

        @Override
        public List<Ingredient> handleRecipeInner(IO io, GTRecipe recipe, List<Ingredient> left, boolean simulate) {
            return super.handleRecipeInner(io, recipe, left, true);
        }
    }
}
