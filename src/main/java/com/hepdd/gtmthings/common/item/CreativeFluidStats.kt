package com.hepdd.gtmthings.common.item

import com.gregtechceu.gtceu.api.gui.GuiTextures
import com.gregtechceu.gtceu.api.gui.UITemplate
import com.gregtechceu.gtceu.api.gui.widget.PhantomFluidWidget
import com.gregtechceu.gtceu.api.item.component.IAddInformation
import com.gregtechceu.gtceu.api.item.component.IItemComponent
import com.gregtechceu.gtceu.api.item.component.IItemUIFactory
import com.gregtechceu.gtceu.api.item.component.forge.IComponentCapability
import com.gregtechceu.gtceu.api.transfer.fluid.CustomFluidTank
import com.hepdd.gtmthings.api.gui.widget.TerminalInputWidget
import com.hepdd.gtmthings.api.misc.CreativeFluidHandlerItemStack
import com.lowdragmc.lowdraglib.gui.factory.HeldItemUIFactory.HeldItemHolder
import com.lowdragmc.lowdraglib.gui.modular.ModularUI
import com.lowdragmc.lowdraglib.gui.texture.GuiTextureGroup
import com.lowdragmc.lowdraglib.gui.texture.ResourceBorderTexture
import com.lowdragmc.lowdraglib.gui.texture.ResourceTexture
import com.lowdragmc.lowdraglib.gui.texture.TextTexture
import com.lowdragmc.lowdraglib.gui.util.ClickData
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget
import com.lowdragmc.lowdraglib.gui.widget.SwitchWidget
import com.lowdragmc.lowdraglib.gui.widget.Widget
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.chat.Component
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResultHolder
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.TooltipFlag
import net.minecraft.world.level.Level
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.common.capabilities.ForgeCapabilities
import net.minecraftforge.common.util.LazyOptional
import net.minecraftforge.fluids.FluidStack
import net.minecraftforge.fluids.FluidUtil
import java.util.function.Consumer

class CreativeFluidStats : IItemComponent, IComponentCapability, IAddInformation, IItemUIFactory {
    private var itemStack: ItemStack? = null
    private val creativeTank: CustomFluidTank = CustomFluidTank(1000)

    override fun appendHoverText(
        stack: ItemStack,
        level: Level?,
        tooltipComponents: MutableList<Component?>,
        isAdvanced: TooltipFlag?
    ) {
        tooltipComponents.add(Component.translatable("gtmthings.creative_tooltip"))
        if (stack.hasTag() && stack.tag!!.contains("Fluid")) {
            FluidUtil.getFluidContained(stack).ifPresent(Consumer { tank: FluidStack? ->
                tooltipComponents
                    .add(Component.translatable("item.gtmthings.creative_fluid_cell.tooltip1", tank!!.displayName))
            })
            if (getAccurate(stack)) {
                tooltipComponents
                    .add(Component.translatable("item.gtmthings.creative_fluid_cell.tooltip3", getCapacity(stack)))
            }
        } else {
            tooltipComponents.add(Component.translatable("item.gtmthings.creative_fluid_cell.tooltip2"))
        }
    }

    override fun <T> getCapability(itemStack: ItemStack, cap: Capability<T?>): LazyOptional<T?> {
        if (cap === ForgeCapabilities.FLUID_HANDLER_ITEM) {
            val fluidStack = getStored(itemStack)
            val capacity = if (getAccurate(itemStack)) getCapacity(itemStack) else Int.Companion.MAX_VALUE
            if (!fluidStack.isEmpty) {
                return ForgeCapabilities.FLUID_HANDLER_ITEM.orEmpty<T?>(
                    cap, LazyOptional.of {
                        CreativeFluidHandlerItemStack(
                            itemStack,
                            capacity,
                            fluidStack
                        )
                    }
                )
            }
        }
        return LazyOptional.empty<T?>()
    }

    override fun createUI(holder: HeldItemHolder?, entityPlayer: Player): ModularUI {
        return ModularUI(176, 166, holder, entityPlayer)
            .widget(createWidget())
            .widget(
                UITemplate.bindPlayerInventory(
                    entityPlayer.inventory, GuiTextures.SLOT, 7, 50,
                    true
                )
            )
    }

    private fun createWidget(): Widget {
        val group = WidgetGroup(0, 0, 176, 131)
        group.setBackground(ResourceTexture("gtceu:textures/gui/base/bordered_background.png"))
        group.addWidget(
            PhantomFluidWidget(
                creativeTank,
                0,
                36,
                6,
                18,
                18,
                { this.stored },
                { fluid: FluidStack ->
                    this.stored = fluid
                })
                .setShowAmount(false)
                .setBackground(GuiTextures.FLUID_SLOT)
        )
        group.addWidget(LabelWidget(7, 9, "gtceu.creative.tank.fluid"))
        group.addWidget(
            SwitchWidget(7, 30, 60, 15) { clickData: ClickData?, aBoolean: Boolean? ->
                this.accurate = aBoolean!!
            }
                .setTexture(
                    GuiTextureGroup(
                        ResourceBorderTexture.BUTTON_COMMON,
                        TextTexture("item.gtmthings.creative_fluid_cell.gui.button1")
                    ),
                    GuiTextureGroup(
                        ResourceBorderTexture.BUTTON_COMMON,
                        TextTexture("item.gtmthings.creative_fluid_cell.gui.button2")
                    )
                )
                .setPressed(this.accurate)
        )
        group.addWidget(
            TerminalInputWidget(72, 31, 90, 10, { this.capacity }, { capacity: Int? ->
                this.capacity = capacity!!
            })
                .setMin(1).setMax(Int.Companion.MAX_VALUE)
        )

        return group
    }

    private var accurate: Boolean
        get() = getAccurate(this.itemStack!!)
        set(isEnable) {
            if (!this.itemStack!!.hasTag()) {
                this.itemStack!!.tag = CompoundTag()
            }
            this.itemStack!!.tag!!.putBoolean("Accurate", isEnable)
        }

    private fun getAccurate(fluidCell: ItemStack): Boolean {
        val tagCompound = fluidCell.tag
        return tagCompound != null && tagCompound.contains("Accurate") && tagCompound.getBoolean("Accurate")
    }

    private var stored: FluidStack
        get() = getStored(this.itemStack!!)
        set(fluid) {
            if (fluid.isEmpty) {
                this.creativeTank.setFluid(FluidStack.EMPTY)
                this.itemStack!!.tag!!.remove("Fluid")
                this.accurate = false
            } else {
                val stored: FluidStack = fluid.copy()
                stored.amount = 1000
                this.creativeTank.setFluid(stored)
                if (!this.itemStack!!.hasTag()) {
                    this.itemStack!!.tag = CompoundTag()
                }
                val fluidTag = CompoundTag()
                stored.writeToNBT(fluidTag)
                this.itemStack!!.tag!!.put("Fluid", fluidTag)
            }
        }

    private fun getStored(fluidCell: ItemStack): FluidStack {
        val tagCompound = fluidCell.tag
        return if (tagCompound != null && tagCompound.contains("Fluid")) FluidStack.loadFluidStackFromNBT(
            tagCompound.getCompound(
                "Fluid"
            )
        ) else FluidStack.EMPTY
    }

    private var capacity: Int
        get() = getCapacity(this.itemStack!!)
        set(capacity) {
            if (!this.itemStack!!.hasTag()) {
                this.itemStack!!.tag = CompoundTag()
            }
            this.itemStack!!.tag!!.putInt("Capacity", capacity)
        }

    private fun getCapacity(fluidCell: ItemStack): Int {
        val tagCompound = fluidCell.tag
        return if (tagCompound != null && tagCompound.contains("Capacity")) tagCompound.getInt("Capacity") else 1000
    }

    override fun use(
        item: Item?,
        level: Level?,
        player: Player,
        usedHand: InteractionHand
    ): InteractionResultHolder<ItemStack?>? {
        this.itemStack = player.getItemInHand(usedHand)
        this.creativeTank.setFluid(this.stored)
        return super<IItemUIFactory>.use(item, level, player, usedHand)
    }
}
