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
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.utils.BlockInfo;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

import com.hepdd.gtmthings.api.gui.widget.TerminalInputWidget;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

import static com.hepdd.gtmthings.api.pattern.AdvancedBlockPattern.getAdvancedBlockPattern;

public class AdvancedTerminalBehavior implements IItemUIFactory {

    private AutoBuildSetting autoBuildSetting = null;
    private ItemStack itemStack;

    public AdvancedTerminalBehavior() {
        autoBuildSetting = new AutoBuildSetting();
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (context.getPlayer() != null && context.getPlayer().isShiftKeyDown()) {
            Level level = context.getLevel();
            BlockPos blockPos = context.getClickedPos();
            if (context.getPlayer() != null &&
                    MetaMachine.getMachine(level, blockPos) instanceof IMultiController controller) {
                if (!controller.isFormed()) {
                    if (!level.isClientSide) {
                        getAdvancedBlockPattern(controller.getPattern()).autoBuild(context.getPlayer(), controller.getMultiblockState(), autoBuildSetting);
                    }
                    return InteractionResult.sidedSuccess(level.isClientSide);
                } else if (MetaMachine.getMachine(level, blockPos) instanceof WorkableMultiblockMachine workableMultiblockMachine && autoBuildSetting.isReplaceCoilMode()) {
                    if (!level.isClientSide) {
                        getAdvancedBlockPattern(controller.getPattern()).autoBuild(context.getPlayer(), controller.getMultiblockState(), autoBuildSetting);
                        workableMultiblockMachine.onPartUnload();
                    }
                    return InteractionResult.sidedSuccess(level.isClientSide);
                }
            }
        }
        return InteractionResult.PASS;
    }

    @Override
    public ModularUI createUI(HeldItemUIFactory.HeldItemHolder holder, Player entityPlayer) {
        return new ModularUI(176, 166, holder, entityPlayer).widget(createWidget());
    }

    private Widget createWidget() {
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
                        .addWidget(new LabelWidget(40, 5, Component.translatable("item.gtmthings.advanced_terminal.setting.title").getString()))
                        .addWidget(new LabelWidget(4, 5 + 16 * rowIndex, Component.translatable("item.gtmthings.advanced_terminal.setting.1").getString())
                                .setHoverTooltips(lines))
                        .addWidget(new TerminalInputWidget(140, 5 + 16 * rowIndex++, 20, 16, autoBuildSetting::getCoilTier,
                                this::setCoilTier)
                                .setMin(0).setMax(GTCEuAPI.HEATING_COILS.size()))
                        .addWidget(new LabelWidget(4, 5 + 16 * rowIndex, Component.translatable("item.gtmthings.advanced_terminal.setting.2").getString())
                                .setHoverTooltips(Component.translatable("item.gtmthings.advanced_terminal.setting.2.tooltip")))
                        .addWidget(new TerminalInputWidget(140, 5 + 16 * rowIndex++, 20, 16, autoBuildSetting::getRepeatCount,
                                this::setRepeatCount)
                                .setMin(0).setMax(99))
                        .addWidget(new LabelWidget(4, 5 + 16 * rowIndex, Component.translatable("item.gtmthings.advanced_terminal.setting.3").getString())
                                .setHoverTooltips("item.gtmthings.advanced_terminal.setting.3.tooltip"))
                        .addWidget(new TerminalInputWidget(140, 5 + 16 * rowIndex++, 20, 16, autoBuildSetting::getNoHatchMode,
                                this::setIsBuildHatches).setMin(0).setMax(1))
                        .addWidget(new LabelWidget(4, 5 + 16 * rowIndex, Component.translatable("item.gtmthings.advanced_terminal.setting.4").getString())
                                .setHoverTooltips("item.gtmthings.advanced_terminal.setting.4.tooltip"))
                        .addWidget(new TerminalInputWidget(140, 5 + 16 * rowIndex++, 20, 16, autoBuildSetting::getReplaceCoilMode,
                                this::setReplaceCoilMode).setMin(0).setMax(1)));

        group.setBackground(GuiTextures.BACKGROUND_INVERSE);
        return group;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Item item, Level level, Player player, InteractionHand usedHand) {
        this.itemStack = player.getItemInHand(usedHand);
        var tag = this.itemStack.getTag();
        if (tag != null && !tag.isEmpty()) {
            this.autoBuildSetting.setCoilTier(tag.getInt("CoilTier"));
            this.autoBuildSetting.setRepeatCount(tag.getInt("RepeatCount"));
            this.autoBuildSetting.setNoHatchMode(tag.getInt("NoHatchMode"));
        } else {
            tag = new CompoundTag();
            tag.putInt("CoilTier", 0);
            tag.putInt("RepeatCount", 0);
            tag.putInt("NoHatchMode", 1);
            this.itemStack.setTag(tag);
            this.autoBuildSetting.setCoilTier(0);
            this.autoBuildSetting.setRepeatCount(0);
            this.autoBuildSetting.setNoHatchMode(1);
        }
        return IItemUIFactory.super.use(item, level, player, usedHand);
    }

    private void setCoilTier(int coilTier) {
        autoBuildSetting.setCoilTier(coilTier);
        var tag = this.itemStack.getTag();
        if (tag == null) tag = new CompoundTag();
        tag.putInt("CoilTier", coilTier);
        this.itemStack.setTag(tag);
    }

    private void setRepeatCount(int repeatCount) {
        autoBuildSetting.setRepeatCount(repeatCount);
        var tag = this.itemStack.getTag();
        if (tag == null) tag = new CompoundTag();
        tag.putInt("RepeatCount", repeatCount);
        this.itemStack.setTag(tag);
    }

    private void setIsBuildHatches(int isBuildHatches) {
        autoBuildSetting.setNoHatchMode(isBuildHatches);
        var tag = this.itemStack.getTag();
        if (tag == null) tag = new CompoundTag();
        tag.putInt("NoHatchMode", isBuildHatches);
        this.itemStack.setTag(tag);
    }

    private void setReplaceCoilMode(int isReplaceCoil) {
        autoBuildSetting.setReplaceCoilMode(isReplaceCoil);
        var tag = this.itemStack.getTag();
        if (tag == null) tag = new CompoundTag();
        tag.putInt("ReplaceCoilMode", isReplaceCoil);
        this.itemStack.setTag(tag);
    }

    @Setter
    @Getter
    public static class AutoBuildSetting {

        public static final Set<String> HATCH_NAMES = new HashSet<>(Set.of("input_hatch", "output_hatch", "input_bus", "output_bus", "laser_target", "laser_source",
                "transmitter_hatch", "receiver_hatch", "maintenance_hatch", "parallel_hatch", "import_bus", "export_bus"));

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
                if (blockInfo.getBlockState().getBlock() instanceof MetaMachineBlock machineBlock) {
                    var id = machineBlock.getDefinition().getName();
                    for (String hatchName : HATCH_NAMES) {
                        if (id.contains(hatchName)) return false;
                    }
                }
                return true;
            }
            return true;
        }

        public boolean isReplaceCoilMode() {
            return replaceCoilMode == 1;
        }
    }
}
