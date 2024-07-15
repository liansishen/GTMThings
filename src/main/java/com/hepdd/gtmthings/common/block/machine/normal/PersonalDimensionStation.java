package com.hepdd.gtmthings.common.block.machine.normal;

import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.IFancyUIMachine;
import com.gregtechceu.gtceu.api.machine.feature.IInteractedMachine;
import com.gregtechceu.gtceu.api.machine.feature.IMachineLife;
import com.hepdd.gtmthings.api.capability.IBindable;
import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.TextTexture;
import com.lowdragmc.lowdraglib.gui.util.ClickData;
import com.lowdragmc.lowdraglib.gui.widget.*;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class PersonalDimensionStation extends MetaMachine implements IInteractedMachine, IBindable, IMachineLife, IFancyUIMachine {

    private UUID owner;

    public PersonalDimensionStation(IMachineBlockEntity holder) {
        super(holder);
    }


    @Override
    public UUID getUUID() {
        return owner;
    }

    @Override
    public void setUUID(UUID uuid) {
        this.owner = uuid;
    }

    @Override
    public InteractionResult onUse(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        return IInteractedMachine.super.onUse(state, world, pos, player, hand, hit);
    }

    @Override
    public boolean shouldOpenUI(Player player, InteractionHand hand, BlockHitResult hit) {
        if (!player.getUUID().equals(owner)) return false;
        return IFancyUIMachine.super.shouldOpenUI(player, hand, hit);
    }

    @Override
    public void onMachinePlaced(@Nullable LivingEntity player, ItemStack stack) {
        if (player != null) {
            this.owner = player.getUUID();
        }
        IMachineLife.super.onMachinePlaced(player, stack);
    }

    @Override
    public Widget createUIWidget() {
        int height = 117;
        int width = 178;
        var group = new WidgetGroup(0, 0, width + 8, height + 4);
        //group.setBackground(GuiTextures.BLANK);
        group.addWidget(new ButtonWidget(4,4,20,12,new TextTexture("create"),this::createDim));

        return group;
    }

    private void createDim(ClickData clickData) {

    }

}
