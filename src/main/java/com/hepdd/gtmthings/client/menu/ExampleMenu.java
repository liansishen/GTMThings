package com.hepdd.gtmthings.client.menu;

import com.hepdd.gtmthings.data.GTMTMenuTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class ExampleMenu extends AbstractContainerMenu {

    private final Container inventory;

    public ExampleMenu(int containerId, Inventory playerInventory) {
        super(GTMTMenuTypes.EXAMPLE_MENU.get(), containerId);
        this.inventory = playerInventory;

        // 添加槽位
        this.addSlot(new Slot(inventory, 0, 80, 35));

        // 添加玩家物品栏槽位
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }

        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 142));
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return this.inventory.stillValid(player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        // 实现Shift+点击物品转移逻辑
        // ...
        return ItemStack.EMPTY;
    }
}