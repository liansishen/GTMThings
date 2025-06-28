package com.hepdd.gtmthings.common.item;

import net.minecraft.world.item.ItemStack;

import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.GenericStack;
import appeng.items.storage.CreativeCellItem;
import appeng.util.ConfigInventory;
import com.hepdd.gtmthings.data.CustomItems;
import org.jetbrains.annotations.Nullable;

public final class VirtualItemProviderCellItem extends CreativeCellItem {

    public VirtualItemProviderCellItem(Properties props) {
        super(props);
    }

    @Override
    public ConfigInventory getConfigInventory(ItemStack is) {
        Holder holder = new Holder(is);
        holder.inv = new VirtualConfigInventory(63, holder::save);
        holder.load();
        return holder.inv;
    }

    private static class Holder {

        private final ItemStack stack;
        private ConfigInventory inv;

        Holder(ItemStack stack) {
            this.stack = stack;
        }

        void load() {
            if (stack.hasTag()) {
                inv.readFromChildTag(stack.getOrCreateTag(), "list");
            }
        }

        void save() {
            inv.writeToChildTag(stack.getOrCreateTag(), "list");
        }
    }

    private static class VirtualConfigInventory extends ConfigInventory {

        private VirtualConfigInventory(int size, @Nullable Runnable listener) {
            super(null, Mode.CONFIG_TYPES, size, listener, false);
        }

        public void setStack(int slot, @Nullable GenericStack stack) {
            if (stack == null) {
                super.setStack(slot, null);
            } else if (stack.what() instanceof AEItemKey itemKey && itemKey.getItem() == CustomItems.VIRTUAL_ITEM_PROVIDER.asItem() && itemKey.hasTag()) {
                boolean typesOnly = this.mode == Mode.CONFIG_TYPES;
                itemKey.getTag().putBoolean("marked", true);
                if (typesOnly && stack.amount() != 0L) {
                    stack = new GenericStack(itemKey, 0L);
                } else if (!typesOnly && stack.amount() <= 0L) {
                    stack = null;
                }

                super.setStack(slot, stack);
            }
        }
    }
}
