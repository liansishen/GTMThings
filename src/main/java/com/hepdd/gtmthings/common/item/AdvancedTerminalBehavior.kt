package com.hepdd.gtmthings.common.item

import com.gregtechceu.gtceu.api.GTCEuAPI
import com.gregtechceu.gtceu.api.block.ICoilType
import com.gregtechceu.gtceu.api.block.MetaMachineBlock
import com.gregtechceu.gtceu.api.gui.GuiTextures
import com.gregtechceu.gtceu.api.item.component.IItemUIFactory
import com.gregtechceu.gtceu.api.machine.MetaMachine
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableMultiblockMachine
import com.gregtechceu.gtceu.common.block.CoilBlock
import com.hepdd.gtmthings.api.gui.widget.TerminalInputWidget
import com.hepdd.gtmthings.api.misc.Hatch
import com.hepdd.gtmthings.api.pattern.AdvancedBlockPattern.Companion.getAdvancedBlockPattern
import com.lowdragmc.lowdraglib.gui.editor.ColorPattern
import com.lowdragmc.lowdraglib.gui.factory.HeldItemUIFactory.HeldItemHolder
import com.lowdragmc.lowdraglib.gui.modular.ModularUI
import com.lowdragmc.lowdraglib.gui.widget.DraggableScrollableWidgetGroup
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget
import com.lowdragmc.lowdraglib.gui.widget.Widget
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup
import com.lowdragmc.lowdraglib.utils.BlockInfo
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.chat.Component
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.context.UseOnContext
import net.minecraft.world.level.block.Blocks
import java.util.*
import java.util.function.Supplier
import java.util.function.ToIntFunction
import kotlin.math.min

class AdvancedTerminalBehavior:IItemUIFactory {

    override fun useOn(context: UseOnContext): InteractionResult {
        if (context.player != null && context.player!!.isShiftKeyDown) {
            val level = context.level
            val blockPos = context.clickedPos
            if (context.player != null && !level.isClientSide() &&
                MetaMachine.getMachine(level, blockPos) is IMultiController
            ) {
                val controller = MetaMachine.getMachine(level, blockPos) as IMultiController
                val autoBuildSetting = getAutoBuildSetting(context.player!!.mainHandItem)

                if (!controller.isFormed) {
                    getAdvancedBlockPattern(controller.pattern)!!.autoBuild(
                        context.player!!,
                        controller.multiblockState,
                        autoBuildSetting
                    )
                } else if (MetaMachine.getMachine(
                        level,
                        blockPos
                    ) is WorkableMultiblockMachine && autoBuildSetting.isReplaceCoilMode()
                ) {
                    val workableMultiblockMachine = MetaMachine.getMachine(level,blockPos) as WorkableMultiblockMachine
                    getAdvancedBlockPattern(controller.pattern)!!.autoBuild(
                        context.player!!,
                        controller.multiblockState,
                        autoBuildSetting
                    )
                    workableMultiblockMachine.onPartUnload()
                }
            }
            return InteractionResult.sidedSuccess(level.isClientSide)
        }
        return InteractionResult.PASS
    }

    private fun getAutoBuildSetting(itemStack: ItemStack): AutoBuildSetting {
        val autoBuildSetting = AutoBuildSetting()
        val tag = itemStack.tag
        if (tag != null && !tag.isEmpty) {
            autoBuildSetting.coilTier = tag.getInt("CoilTier")
            autoBuildSetting.repeatCount = tag.getInt("RepeatCount")
            autoBuildSetting.noHatchMode = tag.getInt("NoHatchMode")
            autoBuildSetting.replaceCoilMode = tag.getInt("ReplaceCoilMode")
            autoBuildSetting.isUseAE = tag.getInt("IsUseAE")
        } else {
            autoBuildSetting.coilTier = 0
            autoBuildSetting.repeatCount = 0
            autoBuildSetting.noHatchMode = 1
            autoBuildSetting.replaceCoilMode = 0
            autoBuildSetting.isUseAE = 0
        }
        return autoBuildSetting
    }

    override fun createUI(holder: HeldItemHolder?, entityPlayer: Player): ModularUI {
        return ModularUI(176, 166, holder, entityPlayer).widget(createWidget(entityPlayer))
    }

    private fun createWidget(entityPlayer: Player): Widget {
        val handItem = entityPlayer.mainHandItem
        val group = WidgetGroup(0, 0, 182 + 8, 117 + 8)
        var rowIndex = 1
        val lines: MutableList<Component?> = ArrayList<Component?>(mutableListOf())
        lines.add(Component.translatable("item.gtmthings.advanced_terminal.setting.1.tooltip"))
        GTCEuAPI.HEATING_COILS.entries.stream()
            .sorted(Comparator.comparingInt(ToIntFunction { value: MutableMap.MutableEntry<ICoilType?, Supplier<CoilBlock?>?>? -> value!!.key!!.tier }))
            .forEach { coil: MutableMap.MutableEntry<ICoilType?, Supplier<CoilBlock?>?>? ->
                lines.add(
                    Component.literal((coil!!.key!!.tier + 1).toString()).append(":").append(
                        coil.value!!.get()!!.name
                    )
                )
            }

        group.addWidget(
            DraggableScrollableWidgetGroup(4, 4, 182, 117)
                .setBackground(GuiTextures.DISPLAY)
                .setYScrollBarWidth(2)
                .setYBarStyle(null, ColorPattern.T_WHITE.rectTexture().setRadius(1f))
                .addWidget(LabelWidget(40, 5, "item.gtmthings.advanced_terminal.setting.title"))
                .addWidget(
                    LabelWidget(4, 5 + 16 * rowIndex, "item.gtmthings.advanced_terminal.setting.1")
                        .setHoverTooltips(lines)
                )
                .addWidget(
                    TerminalInputWidget(
                        140, 5 + 16 * rowIndex++, 20, 16, { getCoilTier(handItem) },
                        { v: Int? -> setCoilTier(v!!, handItem) })
                        .setMin(0).setMax(GTCEuAPI.HEATING_COILS.size)
                )
                .addWidget(
                    LabelWidget(4, 5 + 16 * rowIndex, "item.gtmthings.advanced_terminal.setting.2")
                        .setHoverTooltips(Component.translatable("item.gtmthings.advanced_terminal.setting.2.tooltip"))
                )
                .addWidget(
                    TerminalInputWidget(
                        140, 5 + 16 * rowIndex++, 20, 16, { getRepeatCount(handItem) },
                        { v: Int? -> setRepeatCount(v!!, handItem) })
                        .setMin(0).setMax(99)
                )
                .addWidget(
                    LabelWidget(4, 5 + 16 * rowIndex, "item.gtmthings.advanced_terminal.setting.3")
                        .setHoverTooltips("item.gtmthings.advanced_terminal.setting.3.tooltip")
                )
                .addWidget(
                    TerminalInputWidget(
                        140, 5 + 16 * rowIndex++, 20, 16, { getIsBuildHatches(handItem) },
                        { v: Int? -> setIsBuildHatches(v!!, handItem) }).setMin(0).setMax(1)
                )
                .addWidget(
                    LabelWidget(4, 5 + 16 * rowIndex, "item.gtmthings.advanced_terminal.setting.4")
                        .setHoverTooltips("item.gtmthings.advanced_terminal.setting.4.tooltip")
                )
                .addWidget(
                    TerminalInputWidget(
                        140, 5 + 16 * rowIndex++, 20, 16, { getReplaceCoilMode(handItem) },
                        { v: Int? -> setReplaceCoilMode(v!!, handItem) }).setMin(0).setMax(1)
                )
                .addWidget(
                    LabelWidget(4, 5 + 16 * rowIndex, "item.gtmthings.advanced_terminal.setting.5")
                        .setHoverTooltips("item.gtmthings.advanced_terminal.setting.5.tooltip")
                )
                .addWidget(
                    TerminalInputWidget(
                        140, 5 + 16 * rowIndex++, 20, 16, { getIsUseAE(handItem) },
                        { v: Int? -> setIsUseAE(v!!, handItem) }).setMin(0).setMax(1)
                )
        )

        group.setBackground(GuiTextures.BACKGROUND_INVERSE)
        return group
    }

    private fun getCoilTier(itemStack: ItemStack): Int {
        val tag = itemStack.tag
        return if (tag != null && !tag.isEmpty) {
            tag.getInt("CoilTier")
        } else {
            0
        }
    }

    private fun setCoilTier(coilTier: Int, itemStack: ItemStack) {
        var tag = itemStack.tag
        if (tag == null) tag = CompoundTag()
        tag.putInt("CoilTier", coilTier)
        itemStack.tag = tag
    }

    private fun getRepeatCount(itemStack: ItemStack): Int {
        val tag = itemStack.tag
        return if (tag != null && !tag.isEmpty) {
            tag.getInt("RepeatCount")
        } else {
            0
        }
    }

    private fun setRepeatCount(repeatCount: Int, itemStack: ItemStack) {
        var tag = itemStack.tag
        if (tag == null) tag = CompoundTag()
        tag.putInt("RepeatCount", repeatCount)
        itemStack.tag = tag
    }

    private fun getIsBuildHatches(itemStack: ItemStack): Int {
        val tag = itemStack.tag
        return if (tag != null && !tag.isEmpty) {
            tag.getInt("NoHatchMode")
        } else {
            1
        }
    }

    private fun setIsBuildHatches(isBuildHatches: Int, itemStack: ItemStack) {
        var tag = itemStack.tag
        if (tag == null) tag = CompoundTag()
        tag.putInt("NoHatchMode", isBuildHatches)
        itemStack.tag = tag
    }

    private fun getReplaceCoilMode(itemStack: ItemStack): Int {
        val tag = itemStack.tag
        return if (tag != null && !tag.isEmpty) {
            tag.getInt("ReplaceCoilMode")
        } else {
            0
        }
    }

    private fun setReplaceCoilMode(isReplaceCoil: Int, itemStack: ItemStack) {
        var tag = itemStack.tag
        if (tag == null) tag = CompoundTag()
        tag.putInt("ReplaceCoilMode", isReplaceCoil)
        itemStack.tag = tag
    }

    private fun getIsUseAE(itemStack: ItemStack): Int {
        val tag = itemStack.tag
        return if (tag != null && !tag.isEmpty) {
            tag.getInt("IsUseAE")
        } else {
            0
        }
    }

    private fun setIsUseAE(isUseAE: Int, itemStack: ItemStack) {
        var tag = itemStack.tag
        if (tag == null) tag = CompoundTag()
        tag.putInt("IsUseAE", isUseAE)
        itemStack.tag = tag
    }

    open class AutoBuildSetting {
        var coilTier = 0
        var repeatCount = 0
        var noHatchMode = 1
        var replaceCoilMode = 0
        var isUseAE = 0

        fun apply(blockInfos: Array<BlockInfo?>?): MutableList<ItemStack?> {
            val candidates: MutableList<ItemStack?> = ArrayList<ItemStack?>()
            if (blockInfos != null) {
                if (Arrays.stream(blockInfos)
                        .anyMatch { info: BlockInfo? -> info?.blockState?.block is CoilBlock }
                ) {
                    val tier = min(coilTier - 1, blockInfos.size - 1)
                    if (tier == -1) {
                        for (i in 0..<blockInfos.size - 1) {
                            candidates.add(blockInfos[i]?.itemStackForm)
                        }
                    } else {
                        candidates.add(blockInfos[tier]?.itemStackForm)
                    }
                    return candidates
                }
                for (info in blockInfos) {
                    if (info?.blockState?.block !== Blocks.AIR) candidates.add(info?.itemStackForm)
                }
            }
            return candidates
        }

        fun isPlaceHatch(blockInfos: Array<BlockInfo>?): Boolean {
            if (this.noHatchMode == 0) return true
            if (blockInfos != null && blockInfos.isNotEmpty()) {
                val blockInfo = blockInfos[0]
                return blockInfo.blockState.block !is MetaMachineBlock || !Hatch.Set.contains(blockInfo.blockState.block)
            }
            return true
        }

        fun isReplaceCoilMode(): Boolean {
            return replaceCoilMode == 1
        }
    }
}