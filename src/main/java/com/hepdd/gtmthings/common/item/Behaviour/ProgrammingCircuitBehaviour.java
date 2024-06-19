package com.hepdd.gtmthings.common.item.Behaviour;

import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.item.component.IAddInformation;
import com.gregtechceu.gtceu.api.item.component.IItemUIFactory;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.lowdragmc.lowdraglib.gui.factory.HeldItemUIFactory;
import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.SlotWidget;
import com.lowdragmc.lowdraglib.misc.ItemStackTransfer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ProgrammingCircuitBehaviour implements IItemUIFactory, IAddInformation {
    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag isAdvanced) {

    }

    public static int getCircuitConfiguration(ItemStack itemStack) {

        return 0;
    }



    public static ItemStack stack(int configuration) {
        var stack = GTItems.INTEGRATED_CIRCUIT.asStack();
        setCircuitConfiguration(stack, configuration);
        return stack;
    }

//    public static void setCircuitConfiguration(HeldItemUIFactory.HeldItemHolder holder, int configuration) {
//        setCircuitConfiguration(holder.getHeld(), configuration);
//        holder.markAsDirty();
//    }

    public static void setCircuitConfiguration(ItemStack itemStack, int configuration) {
//        if (configuration < 0 || configuration > CIRCUIT_MAX)
//            throw new IllegalArgumentException("Given configuration number is out of range!");
//        var tagCompound = itemStack.m_41784_();
//        tagCompound.m_128405_("Configuration", configuration);
    }

    @Override
    public ModularUI createUI(HeldItemUIFactory.HeldItemHolder holder, Player entityPlayer) {
        LabelWidget label = new LabelWidget(9, 8, "Programming Circuit Configuration");
        label.setDropShadow(false);
        label.setTextColor(0x404040);
        var modular = new ModularUI(184, 132, holder, entityPlayer)
                .widget(label);
        SlotWidget slotwidget = new SlotWidget(new ItemStackTransfer(ItemStack.EMPTY), 0, 82, 20, true, true);
        slotwidget.setBackground(GuiTextures.SLOT);
        modular.widget(slotwidget);

        modular.mainGroup.setBackground(GuiTextures.BACKGROUND);
        return modular;
    }
}
