package com.hepdd.gtmthings.client.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * 物品槽Widget，用于显示和交互物品
 */
public class SlotWidget extends AbstractWidget {

    // 默认物品槽背景纹理
    private static final ResourceLocation DEFAULT_SLOT_TEXTURE = ResourceLocation.tryBuild("minecraft", "textures/gui/container/inventory.png");

    // 物品槽背景纹理的UV坐标
    private int backgroundU = 7;
    private int backgroundV = 7;

    // 物品槽的尺寸
    private static final int DEFAULT_SIZE = 18;

    // 物品槽中的物品
    private ItemStack itemStack;

    // 物品槽的物品提供者
    private Supplier<ItemStack> itemStackSupplier;

    // 物品槽的物品变更监听器
    private Consumer<ItemStack> itemChangeListener;

    // 物品槽是否可交互
    private boolean isInteractive = true;

    // 物品槽是否显示背景
    private boolean showBackground = true;

    // 物品槽的背景纹理
    private ResourceLocation backgroundTexture = DEFAULT_SLOT_TEXTURE;

    // 物品槽的悬停文本
    private List<Component> hoverText = new ArrayList<>();

    private Slot slot;

    /**
     * 创建一个新的物品槽Widget
     *
     * @param x X坐标
     * @param y Y坐标
     */
    public SlotWidget(int x, int y) {
        this(x, y, ItemStack.EMPTY);
    }

    public SlotWidget(int x, int y, IItemHandlerModifiable itemHandler, int slotIndex) {
        super(x, y, DEFAULT_SIZE, DEFAULT_SIZE);
        createSlot(itemHandler,slotIndex,x,y);
        this.itemStack = slot.getItem();
    }

    /**
     * 创建一个新的物品槽Widget
     *
     * @param x X坐标
     * @param y Y坐标
     * @param itemStack 物品栈
     */
    public SlotWidget(int x, int y, ItemStack itemStack) {
        super(x, y, DEFAULT_SIZE, DEFAULT_SIZE);
        this.itemStack = itemStack;
    }

    /**
     * 创建一个新的物品槽Widget，使用物品提供者
     *
     * @param x X坐标
     * @param y Y坐标
     * @param itemStackSupplier 物品栈提供者
     */
    public SlotWidget(int x, int y, Supplier<ItemStack> itemStackSupplier) {
        super(x, y, DEFAULT_SIZE, DEFAULT_SIZE);
        this.itemStackSupplier = itemStackSupplier;
        this.itemStack = ItemStack.EMPTY;
    }

    private void createSlot(IItemHandlerModifiable itemHandler, int slotIndex,int x,int y) {
        this.slot = new WidgetSlotItemHandler(itemHandler,slotIndex,x,y);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        if (!isVisible()) {
            return;
        }

        // 更新物品栈（如果使用提供者）
        if (itemStackSupplier != null) {
            ItemStack newStack = itemStackSupplier.get();
            if (!ItemStack.matches(itemStack, newStack)) {
                itemStack = newStack;
            }
        }

        // 渲染背景
        if (showBackground) {
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.setShaderTexture(0, backgroundTexture);
            guiGraphics.blit(backgroundTexture, getX(), getY(), backgroundU, backgroundV, DEFAULT_SIZE, DEFAULT_SIZE);
        }

        // 渲染物品
        if (!itemStack.isEmpty()) {
            ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
            guiGraphics.renderItem(itemStack, getX() + 1, getY() + 1);
            guiGraphics.renderItemDecorations(Minecraft.getInstance().font, itemStack, getX() + 1, getY() + 1);
        }

        // 检查鼠标悬停
//        if (isMouseOver(mouseX, mouseY)) {
//            // 渲染悬停效果
//            guiGraphics.fill(getX(), getY(), getX() + getWidth(), getY() + getHeight(), 0x80FFFFFF);
//        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!isEnabled() || !isVisible() || !isInside(mouseX, mouseY)) {
            return false;
        }

        // 处理点击事件
        handleClick(button);
        return true;
    }

    /**
     * 处理点击事件
     *
     * @param button 鼠标按钮
     */
    private void handleClick(int button) {
        // 这里可以实现与物品栏交互的逻辑
        // 例如，与玩家物品栏交换物品等
        // 在实际应用中，这部分逻辑可能需要与容器（Container）交互
        Player player = Minecraft.getInstance().player;
        if (player != null) {
            AbstractContainerMenu abstractcontainermenu = player.containerMenu;
            abstractcontainermenu.clicked(0, 0, ClickType.PICKUP, player);
//            ItemStack itemstack10 = slot.getItem().copy();
//            player.updateTutorialInventoryAction(itemstack10, slot.getItem(), ClickAction.PRIMARY);
//            slot.onTake(player,slot.getItem());
//            slot.setChanged();
        }

    }

//    @Override
//    public List<Component> getTooltipLines() {
//        if (!itemStack.isEmpty()) {
//            // 返回物品的悬停文本
//            List<Component> tooltip = new ArrayList<>();
//            tooltip.addAll(itemStack.getTooltipLines(Minecraft.getInstance().player, Minecraft.getInstance().options.advancedItemTooltips ? net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner.ADVANCED : net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner.NORMAL));
//            tooltip.addAll(hoverText);
//            return tooltip;
//        }
//        return hoverText.isEmpty() ? null : hoverText;
//    }

    /**
     * 设置物品栈
     *
     * @param itemStack 物品栈
     * @return this
     */
    public SlotWidget setItemStack(ItemStack itemStack) {
        if (!ItemStack.matches(this.itemStack, itemStack)) {
            this.itemStack = itemStack;
            slot.set(itemStack);
            if (itemChangeListener != null) {
                itemChangeListener.accept(itemStack);
            }
        }
        return this;
    }

    /**
     * 获取物品栈
     *
     * @return 物品栈
     */
    public ItemStack getItemStack() {
        if (itemStackSupplier != null) {
            return itemStackSupplier.get();
        }
        if (slot != null) {
            return slot.getItem();
        }
        return itemStack;
    }

    /**
     * 设置物品变更监听器
     *
     * @param listener 监听器
     * @return this
     */
    public SlotWidget setItemChangeListener(Consumer<ItemStack> listener) {
        this.itemChangeListener = listener;
        return this;
    }

    /**
     * 设置是否可交互
     *
     * @param interactive 是否可交互
     * @return this
     */
    public SlotWidget setInteractive(boolean interactive) {
        this.isInteractive = interactive;
        return this;
    }

    /**
     * 设置是否显示背景
     *
     * @param showBackground 是否显示背景
     * @return this
     */
    public SlotWidget setShowBackground(boolean showBackground) {
        this.showBackground = showBackground;
        return this;
    }

    /**
     * 设置背景纹理
     *
     * @param texture 纹理
     * @param u U坐标
     * @param v V坐标
     * @return this
     */
    public SlotWidget setBackground(ResourceLocation texture, int u, int v) {
        this.backgroundTexture = texture;
        this.backgroundU = u;
        this.backgroundV = v;
        return this;
    }

    /**
     * 添加悬停文本
     *
     * @param text 文本
     * @return this
     */
    public SlotWidget addHoverText(Component text) {
        this.hoverText.add(text);
        return this;
    }

    /**
     * 设置悬停文本
     *
     * @param text 文本
     * @return this
     */
    public SlotWidget setHoverText(Component text) {
        this.hoverText.clear();
        this.hoverText.add(text);
        return this;
    }

    /**
     * 设置悬停文本
     *
     * @param texts 文本列表
     * @return this
     */
    public SlotWidget setHoverText(List<Component> texts) {
        this.hoverText.clear();
        this.hoverText.addAll(texts);
        return this;
    }

    /**
     * 清除悬停文本
     *
     * @return this
     */
    public SlotWidget clearHoverText() {
        this.hoverText.clear();
        return this;
    }

    public class WidgetSlotItemHandler extends Slot {

        private static final Container emptyInventory = new SimpleContainer(0);
        @Getter
        private final IItemHandlerModifiable itemHandler;
        private final int index;

        public WidgetSlotItemHandler(IItemHandlerModifiable itemHandler, int index, int xPosition, int yPosition) {
            super(emptyInventory, index, xPosition, yPosition);
            this.itemHandler = itemHandler;
            this.index = index;
        }

        @Override
        public boolean mayPlace(@NotNull ItemStack stack) {
            return (!stack.isEmpty() && this.itemHandler.isItemValid(this.index, stack));
        }

        @Override
        public boolean mayPickup(@Nullable Player playerIn) {
            return !this.itemHandler.extractItem(index, 1, true).isEmpty();
        }

        @Override
        @NotNull
        public ItemStack getItem() {
            return this.itemHandler.getStackInSlot(index);
        }

        @Override
        public void setByPlayer(@NotNull ItemStack stack) {
            this.itemHandler.setStackInSlot(index, stack);
        }

        @Override
        public void set(@NotNull ItemStack stack) {
            this.itemHandler.setStackInSlot(index, stack);
            this.setChanged();
        }

        @Override
        public void onQuickCraft(@NotNull ItemStack oldStackIn, @NotNull ItemStack newStackIn) {}

        @Override
        public int getMaxStackSize() {
            return this.itemHandler.getSlotLimit(this.index);
        }

        @Override
        public int getMaxStackSize(@NotNull ItemStack stack) {
            ItemStack maxAdd = stack.copy();
            int maxInput = stack.getMaxStackSize();
            maxAdd.setCount(maxInput);
            ItemStack currentStack = this.itemHandler.getStackInSlot(index);
            this.itemHandler.setStackInSlot(index, ItemStack.EMPTY);
            ItemStack remainder = this.itemHandler.insertItem(index, maxAdd, true);
            this.itemHandler.setStackInSlot(index, currentStack);
            return maxInput - remainder.getCount();
        }

        @NotNull
        @Override
        public ItemStack remove(int amount) {
            var result = this.itemHandler.extractItem(index, amount, false);
//            if (changeListener != null && !getItem().isEmpty()) {
//                changeListener.run();
//            }
            return result;
        }

        @Override
        public void setChanged() {
//            if (changeListener != null) {
//                changeListener.run();
//            }
//            this.onSlotChanged();
        }

        @Override
        public boolean isActive() {
            return true;
        }
    }
}
