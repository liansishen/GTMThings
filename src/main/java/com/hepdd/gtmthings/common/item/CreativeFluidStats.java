package com.hepdd.gtmthings.common.item;

import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.UITemplate;
import com.gregtechceu.gtceu.api.gui.widget.PhantomFluidWidget;
import com.gregtechceu.gtceu.api.item.component.IAddInformation;
import com.gregtechceu.gtceu.api.item.component.IItemComponent;
import com.gregtechceu.gtceu.api.item.component.IItemUIFactory;
import com.gregtechceu.gtceu.api.item.component.forge.IComponentCapability;
import com.gregtechceu.gtceu.api.transfer.fluid.CustomFluidTank;

import com.lowdragmc.lowdraglib.gui.factory.HeldItemUIFactory;
import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.texture.GuiTextureGroup;
import com.lowdragmc.lowdraglib.gui.texture.ResourceBorderTexture;
import com.lowdragmc.lowdraglib.gui.texture.ResourceTexture;
import com.lowdragmc.lowdraglib.gui.texture.TextTexture;
import com.lowdragmc.lowdraglib.gui.widget.*;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;

import com.hepdd.gtmthings.api.gui.widget.TerminalInputWidget;
import com.hepdd.gtmthings.api.misc.CreativeFluidHandlerItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class CreativeFluidStats implements IItemComponent, IComponentCapability, IAddInformation, IItemUIFactory {

    private ItemStack itemStack;
    protected final CustomFluidTank creativeTank;

    public CreativeFluidStats() {
        this.creativeTank = new CustomFluidTank(1000);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag isAdvanced) {
        tooltipComponents.add(Component.translatable("gtmthings.creative_tooltip"));
        if (stack.hasTag() && stack.getTag().contains("Fluid")) {
            FluidUtil.getFluidContained(stack).ifPresent(tank -> {
                tooltipComponents
                        .add(Component.translatable("item.gtmthings.creative_fluid_cell.tooltip1", tank.getDisplayName()));
            });
            if (getAccurate(stack)) {
                tooltipComponents
                        .add(Component.translatable("item.gtmthings.creative_fluid_cell.tooltip3", getCapacity(stack)));
            }
        } else {
            tooltipComponents.add(Component.translatable("item.gtmthings.creative_fluid_cell.tooltip2"));
        }
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(ItemStack itemStack, @NotNull Capability<T> cap) {
        if (cap == ForgeCapabilities.FLUID_HANDLER_ITEM) {
            FluidStack fluidStack = getStored(itemStack);
            int capacity = getAccurate(itemStack) ? getCapacity(itemStack) : Integer.MAX_VALUE;
            if (!fluidStack.isEmpty()) {
                return ForgeCapabilities.FLUID_HANDLER_ITEM.orEmpty(cap, LazyOptional.of(() -> new CreativeFluidHandlerItemStack(itemStack, capacity, fluidStack)));
            }
        }
        return LazyOptional.empty();
    }

    @Override
    public ModularUI createUI(HeldItemUIFactory.HeldItemHolder holder, Player entityPlayer) {
        return new ModularUI(176, 166, holder, entityPlayer)
                .widget(createWidget())
                .widget(UITemplate.bindPlayerInventory(entityPlayer.getInventory(), GuiTextures.SLOT, 7, 50,
                        true));
    }

    private Widget createWidget() {
        var group = new WidgetGroup(0, 0, 176, 131);
        group.setBackground(new ResourceTexture("gtceu:textures/gui/base/bordered_background.png"));
        group.addWidget(new PhantomFluidWidget(creativeTank, 0, 36, 6, 18, 18, this::getStored, this::setStored)
                .setShowAmount(false)
                .setBackground(GuiTextures.FLUID_SLOT));
        group.addWidget(new LabelWidget(7, 9, "gtceu.creative.tank.fluid"));
        group.addWidget(new SwitchWidget(7, 30, 60, 15, (clickData, aBoolean) -> setAccurate(aBoolean))
                .setTexture(
                        new GuiTextureGroup(ResourceBorderTexture.BUTTON_COMMON, new TextTexture("item.gtmthings.creative_fluid_cell.gui.button1")),
                        new GuiTextureGroup(ResourceBorderTexture.BUTTON_COMMON, new TextTexture("item.gtmthings.creative_fluid_cell.gui.button2")))
                .setPressed(getAccurate()));
        group.addWidget(new TerminalInputWidget(72, 31, 90, 10, this::getCapacity, this::setCapacity)
                .setMin(1).setMax(Integer.MAX_VALUE));

        return group;
    }

    private boolean getAccurate() {
        return getAccurate(this.itemStack);
    }

    private boolean getAccurate(ItemStack fluidCell) {
        CompoundTag tagCompound = fluidCell.getTag();
        return tagCompound != null && tagCompound.contains("Accurate") && tagCompound.getBoolean("Accurate");
    }

    private void setAccurate(boolean isEnable) {
        if (!this.itemStack.hasTag()) {
            this.itemStack.setTag(new CompoundTag());
        }
        this.itemStack.getTag().putBoolean("Accurate", isEnable);
    }

    private FluidStack getStored() {
        return getStored(this.itemStack);
    }

    private FluidStack getStored(ItemStack fluidCell) {
        CompoundTag tagCompound = fluidCell.getTag();
        return tagCompound != null && tagCompound.contains("Fluid") ? FluidStack.loadFluidStackFromNBT(tagCompound.getCompound("Fluid")) : FluidStack.EMPTY;
    }

    private void setStored(FluidStack fluid) {
        if (fluid.isEmpty()) {
            this.creativeTank.setFluid(FluidStack.EMPTY);
            this.itemStack.getTag().remove("Fluid");
            setAccurate(false);
        } else {
            FluidStack stored = fluid.copy();
            stored.setAmount(1000);
            this.creativeTank.setFluid(stored);
            if (!this.itemStack.hasTag()) {
                this.itemStack.setTag(new CompoundTag());
            }
            CompoundTag fluidTag = new CompoundTag();
            stored.writeToNBT(fluidTag);
            this.itemStack.getTag().put("Fluid", fluidTag);
        }
    }

    private int getCapacity() {
        return getCapacity(this.itemStack);
    }

    private int getCapacity(ItemStack fluidCell) {
        CompoundTag tagCompound = fluidCell.getTag();
        return tagCompound != null && tagCompound.contains("Capacity") ? tagCompound.getInt("Capacity") : 1000;
    }

    private void setCapacity(int capacity) {
        if (!this.itemStack.hasTag()) {
            this.itemStack.setTag(new CompoundTag());
        }
        this.itemStack.getTag().putInt("Capacity", capacity);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Item item, Level level, Player player, InteractionHand usedHand) {
        this.itemStack = player.getItemInHand(usedHand);
        this.creativeTank.setFluid(getStored());
        return IItemUIFactory.super.use(item, level, player, usedHand);
    }
}
