package com.hepdd.gtmthings.client.menu;

import com.hepdd.gtmthings.data.GTMTMenuTypes;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class EnergyDisplayMenu extends AbstractContainerMenu {

    public static final MenuType<EnergyDisplayMenu> TYPE = new MenuType<>(EnergyDisplayMenu::new, FeatureFlagSet.of());

    public EnergyDisplayMenu(int containerId, Inventory playerInventory) {
        super(GTMTMenuTypes.ENERGY_DISPLAY_MENU.get(),containerId);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int i) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }
}
