package com.hepdd.gtmthings.common.item;

import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.block.MetaMachineBlock;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.item.component.IItemUIFactory;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableMultiblockMachine;
import com.gregtechceu.gtceu.common.block.CoilBlock;

import com.lowdragmc.lowdraglib.gui.editor.ColorPattern;
import com.lowdragmc.lowdraglib.gui.factory.HeldItemUIFactory;
import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.widget.DraggableScrollableWidgetGroup;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.utils.BlockInfo;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

import com.hepdd.gtmthings.api.gui.widget.LabelWidgetEx;
import com.hepdd.gtmthings.api.gui.widget.TerminalInputWidget;
import com.hepdd.gtmthings.api.misc.Hatch;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

import static com.hepdd.gtmthings.api.pattern.AdvancedBlockPattern.getAdvancedBlockPattern;

public class AdvancedTerminalBehavior implements IItemUIFactory {

    public AdvancedTerminalBehavior() {}

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (context.getPlayer() != null && context.getPlayer().isShiftKeyDown()) {
            Level level = context.getLevel();
            BlockPos blockPos = context.getClickedPos();
            if (context.getPlayer() != null && !level.isClientSide() &&
                    MetaMachine.getMachine(level, blockPos) instanceof IMultiController controller) {
                AutoBuildSetting autoBuildSetting = getAutoBuildSetting(context.getPlayer().getMainHandItem());

                if (!controller.isFormed()) {
                    getAdvancedBlockPattern(controller.getPattern()).autoBuild(context.getPlayer(), controller.getMultiblockState(), autoBuildSetting);
                } else if (MetaMachine.getMachine(level, blockPos) instanceof WorkableMultiblockMachine workableMultiblockMachine && autoBuildSetting.isReplaceCoilMode()) {
                    getAdvancedBlockPattern(controller.getPattern()).autoBuild(context.getPlayer(), controller.getMultiblockState(), autoBuildSetting);
                    workableMultiblockMachine.onPartUnload();
                }

            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return InteractionResult.PASS;
    }

    private AutoBuildSetting getAutoBuildSetting(ItemStack itemStack) {
        AutoBuildSetting autoBuildSetting = new AutoBuildSetting();
        var tag = itemStack.getTag();
        if (tag != null && !tag.isEmpty()) {
            autoBuildSetting.setCoilTier(tag.getInt("CoilTier"));
            autoBuildSetting.setRepeatCount(tag.getInt("RepeatCount"));
            autoBuildSetting.setNoHatchMode(tag.getInt("NoHatchMode"));
            autoBuildSetting.setReplaceCoilMode(tag.getInt("ReplaceCoilMode"));
        } else {
            autoBuildSetting.setCoilTier(0);
            autoBuildSetting.setRepeatCount(0);
            autoBuildSetting.setNoHatchMode(1);
            autoBuildSetting.setReplaceCoilMode(0);
        }
        return autoBuildSetting;
    }

    @Override
    public ModularUI createUI(HeldItemUIFactory.HeldItemHolder holder, Player entityPlayer) {
        return new ModularUI(176, 166, holder, entityPlayer).widget(createWidget(entityPlayer));
    }

    private Widget createWidget(Player entityPlayer) {
        ItemStack handItem = entityPlayer.getMainHandItem();
        var group = new WidgetGroup(0, 0, 182 + 8, 117 + 8);
        int rowIndex = 1;
        List<Component> lines = new ArrayList<>(List.of());
        lines.add(Component.translatable("item.gtmthings.advanced_terminal.setting.1.tooltip"));
        GTCEuAPI.HEATING_COILS.entrySet().stream()
                .sorted(Comparator.comparingInt(value -> value.getKey().getTier()))
                .forEach(coil -> lines.add(Component.literal(String.valueOf(coil.getKey().getTier() + 1)).append(":").append(coil.getValue().get().getName())));

        group.addWidget(
                new DraggableScrollableWidgetGroup(4, 4, 182, 117)
                        .setBackground(GuiTextures.DISPLAY)
                        .setYScrollBarWidth(2)
                        .setYBarStyle(null, ColorPattern.T_WHITE.rectTexture().setRadius(1))
                        .addWidget(new LabelWidgetEx(40, 5, Component.translatable("item.gtmthings.advanced_terminal.setting.title")))
                        .addWidget(new LabelWidgetEx(4, 5 + 16 * rowIndex, Component.translatable("item.gtmthings.advanced_terminal.setting.1"))
                                .setHoverTooltips(lines))
                        .addWidget(new TerminalInputWidget(140, 5 + 16 * rowIndex++, 20, 16, () -> getCoilTier(handItem),
                                (v) -> setCoilTier(v, handItem))
                                .setMin(0).setMax(GTCEuAPI.HEATING_COILS.size()))
                        .addWidget(new LabelWidgetEx(4, 5 + 16 * rowIndex, Component.translatable("item.gtmthings.advanced_terminal.setting.2"))
                                .setHoverTooltips(Component.translatable("item.gtmthings.advanced_terminal.setting.2.tooltip")))
                        .addWidget(new TerminalInputWidget(140, 5 + 16 * rowIndex++, 20, 16, () -> getRepeatCount(handItem),
                                (v) -> setRepeatCount(v, handItem))
                                .setMin(0).setMax(99))
                        .addWidget(new LabelWidgetEx(4, 5 + 16 * rowIndex, Component.translatable("item.gtmthings.advanced_terminal.setting.3"))
                                .setHoverTooltips("item.gtmthings.advanced_terminal.setting.3.tooltip"))
                        .addWidget(new TerminalInputWidget(140, 5 + 16 * rowIndex++, 20, 16, () -> getIsBuildHatches(handItem),
                                (v) -> setIsBuildHatches(v, handItem)).setMin(0).setMax(1))
                        .addWidget(new LabelWidgetEx(4, 5 + 16 * rowIndex, Component.translatable("item.gtmthings.advanced_terminal.setting.4"))
                                .setHoverTooltips("item.gtmthings.advanced_terminal.setting.4.tooltip"))
                        .addWidget(new TerminalInputWidget(140, 5 + 16 * rowIndex++, 20, 16, () -> getReplaceCoilMode(handItem),
                                (v) -> setReplaceCoilMode(v, handItem)).setMin(0).setMax(1)));

        group.setBackground(GuiTextures.BACKGROUND_INVERSE);
        return group;
    }

    private int getCoilTier(ItemStack itemStack) {
        var tag = itemStack.getTag();
        if (tag != null && !tag.isEmpty()) {
            return tag.getInt("CoilTier");
        } else {
            return 0;
        }
    }

    private void setCoilTier(int coilTier, ItemStack itemStack) {
        var tag = itemStack.getTag();
        if (tag == null) tag = new CompoundTag();
        tag.putInt("CoilTier", coilTier);
        itemStack.setTag(tag);
    }

    private int getRepeatCount(ItemStack itemStack) {
        var tag = itemStack.getTag();
        if (tag != null && !tag.isEmpty()) {
            return tag.getInt("RepeatCount");
        } else {
            return 0;
        }
    }

    private void setRepeatCount(int repeatCount, ItemStack itemStack) {
        var tag = itemStack.getTag();
        if (tag == null) tag = new CompoundTag();
        tag.putInt("RepeatCount", repeatCount);
        itemStack.setTag(tag);
    }

    private int getIsBuildHatches(ItemStack itemStack) {
        var tag = itemStack.getTag();
        if (tag != null && !tag.isEmpty()) {
            return tag.getInt("NoHatchMode");
        } else {
            return 1;
        }
    }

    private void setIsBuildHatches(int isBuildHatches, ItemStack itemStack) {
        var tag = itemStack.getTag();
        if (tag == null) tag = new CompoundTag();
        tag.putInt("NoHatchMode", isBuildHatches);
        itemStack.setTag(tag);
    }

    private int getReplaceCoilMode(ItemStack itemStack) {
        var tag = itemStack.getTag();
        if (tag != null && !tag.isEmpty()) {
            return tag.getInt("ReplaceCoilMode");
        } else {
            return 0;
        }
    }

    private void setReplaceCoilMode(int isReplaceCoil, ItemStack itemStack) {
        var tag = itemStack.getTag();
        if (tag == null) tag = new CompoundTag();
        tag.putInt("ReplaceCoilMode", isReplaceCoil);
        itemStack.setTag(tag);
    }

    @Setter
    @Getter
    public static class AutoBuildSetting {

        private int coilTier, repeatCount, noHatchMode, replaceCoilMode;

        public AutoBuildSetting() {
            this.coilTier = 0;
            this.repeatCount = 0;
            this.noHatchMode = 1;
            this.replaceCoilMode = 0;
        }

        public List<ItemStack> apply(BlockInfo[] blockInfos) {
            List<ItemStack> candidates = new ArrayList<>();
            if (blockInfos != null) {
                if (Arrays.stream(blockInfos).anyMatch(
                        info -> info.getBlockState().getBlock() instanceof CoilBlock)) {
                    var tier = Math.min(coilTier - 1, blockInfos.length - 1);
                    if (tier == -1) {
                        for (int i = 0; i < blockInfos.length - 1; i++) {
                            candidates.add(blockInfos[i].getItemStackForm());
                        }
                    } else {
                        candidates.add(blockInfos[tier].getItemStackForm());
                    }
                    return candidates;
                }
                for (BlockInfo info : blockInfos) {
                    if (info.getBlockState().getBlock() != Blocks.AIR) candidates.add(info.getItemStackForm());
                }
            }
            return candidates;
        }

        public boolean isPlaceHatch(BlockInfo[] blockInfos) {
            if (this.noHatchMode == 0) return true;
            if (blockInfos != null && blockInfos.length > 0) {
                var blockInfo = blockInfos[0];
                return !(blockInfo.getBlockState().getBlock() instanceof MetaMachineBlock machineBlock) || !Hatch.Set.contains(machineBlock);
            }
            return true;
        }

        public boolean isReplaceCoilMode() {
            return replaceCoilMode == 1;
        }
    }
}
