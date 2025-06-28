package com.hepdd.gtmthings.data

import net.minecraft.network.chat.Component
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraftforge.fluids.FluidStack
import net.minecraftforge.fluids.FluidUtil

import com.gregtechceu.gtceu.api.GTValues
import com.gregtechceu.gtceu.api.data.RotationState
import com.gregtechceu.gtceu.api.item.ComponentItem
import com.gregtechceu.gtceu.api.item.component.ICustomDescriptionId
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity
import com.gregtechceu.gtceu.api.machine.MachineDefinition
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility
import com.gregtechceu.gtceu.common.data.GTItems
import com.gregtechceu.gtceu.common.item.CoverPlaceBehavior
import com.gregtechceu.gtceu.common.item.ItemFluidContainer
import com.gregtechceu.gtceu.common.item.TooltipBehavior
import com.hepdd.gtmthings.common.block.machine.multiblock.part.CreativeEnergyHatchPartMachine
import com.hepdd.gtmthings.common.block.machine.multiblock.part.CreativeInputBusPartMachine
import com.hepdd.gtmthings.common.block.machine.multiblock.part.CreativeInputHatchPartMachine
import com.hepdd.gtmthings.common.block.machine.multiblock.part.CreativeLaserHatchPartMachine
import com.hepdd.gtmthings.common.item.CreativeFluidStats
import com.hepdd.gtmthings.common.registry.GTMTRegistration
import com.tterrag.registrate.providers.DataGenContext
import com.tterrag.registrate.providers.ProviderType
import com.tterrag.registrate.providers.RegistrateItemModelProvider
import com.tterrag.registrate.util.entry.ItemEntry
import com.tterrag.registrate.util.nullness.NonNullBiConsumer

import java.util.function.Function
import java.util.function.Supplier

class CreativeMachines {

    companion object {
        init {
            GTMTRegistration.Companion.GTMTHINGS_REGISTRATE.creativeModeTab { CreativeModeTabs.CREATIVE_TAB }
        }

        @JvmStatic
        val CREATIVE_FLUID_INPUT_HATCH: MachineDefinition = GTMTRegistration.Companion.GTMTHINGS_REGISTRATE.machine(
            "creative_fluid_input_hatch",
        ) { holder: IMachineBlockEntity? -> CreativeInputHatchPartMachine(holder!!) }
            .rotationState(RotationState.ALL)
            .overlayTieredHullRenderer("fluid_hatch.import_9x")
            .tooltips(Component.translatable("gtmthings.creative_tooltip"))
            .abilities(PartAbility.IMPORT_FLUIDS, PartAbility.IMPORT_FLUIDS_9X)
            .tier(GTValues.MAX)
            .register()

        @JvmStatic
        val CREATIVE_ITEM_INPUT_BUS: MachineDefinition = GTMTRegistration.Companion.GTMTHINGS_REGISTRATE.machine(
            "creative_item_input_bus",
        ) { holder: IMachineBlockEntity? -> CreativeInputBusPartMachine(holder!!) }
            .rotationState(RotationState.ALL)
            .overlayTieredHullRenderer("item_bus.import")
            .tooltips(Component.translatable("gtmthings.creative_tooltip"))
            .abilities(PartAbility.IMPORT_ITEMS)
            .tier(GTValues.MAX)
            .register()

        // energy input hatch
        @JvmStatic
        val CREATIVE_ENERGY_INPUT_HATCH: MachineDefinition = GTMTRegistration.Companion.GTMTHINGS_REGISTRATE.machine(
            "creative_energy_hatch",
        ) { holder: IMachineBlockEntity? -> CreativeEnergyHatchPartMachine(holder!!) }
            .rotationState(RotationState.ALL)
            .tooltips(Component.translatable("gtmthings.creative_tooltip"))
            .overlayTieredHullRenderer("energy_hatch.input")
            .abilities(PartAbility.INPUT_ENERGY)
            .tier(GTValues.MAX)
            .register()

        // laser input hatch
        @JvmStatic
        val CREATIVE_LASER_INPUT_HATCH: MachineDefinition = GTMTRegistration.Companion.GTMTHINGS_REGISTRATE.machine(
            "creative_laser_hatch",
        ) { holder: IMachineBlockEntity? -> CreativeLaserHatchPartMachine(holder!!) }
            .rotationState(RotationState.ALL)
            .tooltips(Component.translatable("gtmthings.creative_tooltip"))
            .overlayTieredHullRenderer("laser_hatch.target")
            .abilities(PartAbility.INPUT_LASER)
            .tier(GTValues.MAX)
            .register()

        @JvmStatic
        var CREATIVE_ENERGY_COVER: ItemEntry<ComponentItem?> = GTMTRegistration.Companion.GTMTHINGS_REGISTRATE
            .item<ComponentItem?>(
                "creative_energy_cover",
            ) { properties: Item.Properties? -> ComponentItem.create(properties) }
            .onRegister(
                GTItems.attach<ComponentItem?>(
                    CoverPlaceBehavior(GTMTCovers.CREATIVE_ENERGY),
                    TooltipBehavior { lines: MutableList<Component?>? -> lines!!.add(Component.translatable("gtmthings.creative_tooltip")) },
                ),
            )
            .register()

        @JvmStatic
        var CREATIVE_FLUID_CELL: ItemEntry<ComponentItem?> = GTMTRegistration.Companion.GTMTHINGS_REGISTRATE
            .item<ComponentItem?>(
                "creative_fluid_cell",
            ) { properties: Item.Properties? -> ComponentItem.create(properties) }
            .color { Supplier { GTItems.cellColor() } }
            .setData<RegistrateItemModelProvider?>(
                ProviderType.ITEM_MODEL,
                NonNullBiConsumer.noop<DataGenContext<Item?, ComponentItem?>?, RegistrateItemModelProvider?>(),
            )
            .onRegister(
                GTItems.attach<ComponentItem?>(
                    cellName(),
                    CreativeFluidStats(),
                    ItemFluidContainer(),
                ),
            )
            .register()

        fun cellName(): ICustomDescriptionId {
            return object : ICustomDescriptionId {
                override fun getItemName(stack: ItemStack): Component? {
                    val prefix = FluidUtil.getFluidContained(stack)
                        .map(Function { obj: FluidStack? -> obj!!.displayName })
                        .orElse(Component.translatable("gtceu.fluid.empty"))
                    return Component.translatable(stack.descriptionId, prefix)
                }
            }
        }

        fun init() {}
    }
}
