package com.hepdd.gtmthings.common.item.Behaviour;

import com.gregtechceu.gtceu.api.item.component.IAddInformation;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class WirelessTransferCoverTooltipBehavior implements IAddInformation {

    private final Consumer<List<Component>> tooltips;

    /**
     * @param tooltips a consumer adding translated tooltips to the tooltip list
     */
    public WirelessTransferCoverTooltipBehavior(@NotNull Consumer<List<Component>> tooltips) {
        this.tooltips = tooltips;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents,
                                TooltipFlag isAdvanced) {
        var tag = stack.getTag();
        if (tag != null) {
            String itemId = tag.getString("blockid");
            String pos = tag.getString("pos");
            if (!itemId.isEmpty() && !pos.isEmpty()) {
                List<Component> lst = new ArrayList<>();
                lst.add(Component.translatable("item.gtmthings.wireless_transfer.tooltip.1", Component.translatable(itemId), pos));
                tooltipComponents.addAll(lst);
            }
        }
        tooltips.accept(tooltipComponents);
    }
}
