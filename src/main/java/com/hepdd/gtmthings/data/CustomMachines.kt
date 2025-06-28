package com.hepdd.gtmthings.data

import com.gregtechceu.gtceu.GTCEu
import com.gregtechceu.gtceu.api.GTCEuAPI
import com.gregtechceu.gtceu.api.GTValues
import com.gregtechceu.gtceu.api.capability.recipe.IO
import com.gregtechceu.gtceu.api.data.RotationState
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity
import com.gregtechceu.gtceu.api.machine.MachineDefinition
import com.gregtechceu.gtceu.api.machine.MetaMachine
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility
import com.gregtechceu.gtceu.api.registry.registrate.MachineBuilder
import com.gregtechceu.gtceu.client.renderer.machine.MinerRenderer
import com.gregtechceu.gtceu.client.renderer.machine.OverlayTieredMachineRenderer
import com.gregtechceu.gtceu.common.data.machines.GTMachineUtils
import com.gregtechceu.gtceu.common.machine.multiblock.part.DualHatchPartMachine
import com.gregtechceu.gtceu.utils.FormattingUtil
import com.hepdd.gtmthings.common.block.machine.electric.DigitalMiner
import com.hepdd.gtmthings.common.block.machine.multiblock.part.HugeBusPartMachine
import com.hepdd.gtmthings.common.block.machine.multiblock.part.HugeDualHatchPartMachine
import com.hepdd.gtmthings.common.block.machine.multiblock.part.ProgrammableHatchPartMachine
import com.hepdd.gtmthings.common.block.machine.multiblock.part.appeng.MEOutputPartMachine
import com.hepdd.gtmthings.common.registry.GTMTRegistration
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component
import net.minecraft.world.item.ItemStack
import java.util.function.BiFunction
import kotlin.math.pow

class CustomMachines {

    companion object {
        init {
            GTMTRegistration.Companion.GTMTHINGS_REGISTRATE.creativeModeTab { CreativeModeTabs.MORE_MACHINES }
        }

        @JvmStatic
        val ALL_TIERS: IntArray =
            GTValues.tiersBetween(GTValues.ULV, if (GTCEuAPI.isHighTier()) GTValues.MAX else GTValues.UV)

        @JvmStatic
        val DIGITAL_MINER: Array<MachineDefinition?> = registerTieredMachines(
            "digital_miner",
            BiFunction { holder: IMachineBlockEntity, tier: Int -> DigitalMiner(holder, tier) },
            BiFunction { tier: Int?, builder: MachineBuilder<MachineDefinition?>? ->
                builder!!
                    .langValue("%s Digital Miner %s".format(GTValues.VLVH[tier!!], GTValues.VLVT[tier]))
                    .rotationState(RotationState.NON_Y_AXIS).tier(tier)
                    .renderer { MinerRenderer(tier, GTCEu.id("block/machines/miner")) }
                    .tooltipBuilder { stack: ItemStack?, tooltip: MutableList<Component?>? ->
                        val maxArea = (8 * 2.0.pow(tier.toDouble())).toInt()
                        val energyPerTick = GTValues.VEX[tier - 1]
                        tooltip!!.add(
                            Component.translatable("gtceu.universal.tooltip.uses_per_tick", energyPerTick)
                                .append(Component.literal(", ").withStyle(ChatFormatting.GRAY))
                                .append(Component.literal("§7每个方块需要§f" + (40 / 2.0.pow(tier.toDouble())).toInt() + "§7刻。"))
                        )
                        tooltip.add(
                            Component.translatable(
                                "gtceu.universal.tooltip.voltage_in",
                                FormattingUtil.formatNumbers(GTValues.VEX[tier]),
                                GTValues.VNF[tier]
                            )
                        )
                        tooltip.add(
                            Component.translatable("gtceu.universal.tooltip.working_area_max", maxArea, maxArea)
                        )
                    }
                    .recipeTypes(GTMTRecipeTypes.DIGITAL_MINER_RECIPE)
                    .register()
            },
            GTValues.LV, GTValues.MV, GTValues.HV
        )

        @JvmStatic
        val ME_EXPORT_BUFFER: MachineDefinition = GTMTRegistration.Companion.GTMTHINGS_REGISTRATE.machine(
            "me_export_buffer"
        ) { holder: IMachineBlockEntity -> MEOutputPartMachine(holder) }
            .rotationState(RotationState.ALL)
            .abilities(PartAbility.EXPORT_ITEMS, PartAbility.EXPORT_FLUIDS)
            .overlayTieredHullRenderer("me.export")
            .tier(GTValues.LuV)
            .register()

        @JvmStatic
        val HUGE_ITEM_IMPORT_BUS: Array<MachineDefinition?> = registerTieredMachines(
            "huge_item_import_bus",
            { holder: IMachineBlockEntity?, tier: Int? -> HugeBusPartMachine(holder!!, tier!!, IO.IN) },
            { tier: Int?, builder: MachineBuilder<MachineDefinition?>? ->
                builder!!
                    .langValue(GTValues.VNF[tier!!] + " Input Bus")
                    .rotationState(RotationState.ALL)
                    .abilities(
                        *if (tier == 0) arrayOf<PartAbility>(
                            PartAbility.IMPORT_ITEMS,
                            PartAbility.STEAM_IMPORT_ITEMS
                        ) else arrayOf<PartAbility>(PartAbility.IMPORT_ITEMS)
                    )
                    .overlayTieredHullRenderer("item_bus.import")
                    .tooltips(
                        Component.translatable("gtmthings.machine.huge_item_bus.import.tooltip"),
                        Component.translatable(
                            "gtceu.universal.tooltip.item_storage_capacity",
                            (1 + tier) * HugeBusPartMachine.Companion.INV_MULTIPLE
                        )
                    )
                    .register()
            },
            *ALL_TIERS
        )

        @JvmStatic
        val HUGE_ITEM_EXPORT_BUS: Array<MachineDefinition?> = registerTieredMachines(
            "huge_item_export_bus",
            { holder: IMachineBlockEntity?, tier: Int? -> HugeBusPartMachine(holder!!, tier!!, IO.OUT) },
            { tier: Int?, builder: MachineBuilder<MachineDefinition?>? ->
                builder!!
                    .langValue(GTValues.VNF[tier!!] + " Output Bus")
                    .rotationState(RotationState.ALL)
                    .abilities(
                        *if (tier == 0) arrayOf<PartAbility>(
                            PartAbility.EXPORT_ITEMS,
                            PartAbility.STEAM_EXPORT_ITEMS
                        ) else arrayOf<PartAbility>(PartAbility.EXPORT_ITEMS)
                    )
                    .overlayTieredHullRenderer("item_bus.export")
                    .tooltips(
                        Component.translatable("gtmthings.machine.huge_item_bus.export.tooltip"),
                        Component.translatable(
                            "gtceu.universal.tooltip.item_storage_capacity",
                            (1 + tier) * HugeBusPartMachine.Companion.INV_MULTIPLE
                        )
                    )
                    .register()
            },
            *ALL_TIERS
        )

        @JvmStatic
        val HUGE_INPUT_DUAL_HATCH: Array<MachineDefinition?> = registerTieredMachines(
            "huge_dual_hatch",
            { holder: IMachineBlockEntity?, tier: Int? ->
                HugeDualHatchPartMachine(
                    holder!!,
                    tier!!,
                    IO.IN
                )
            },
            { tier: Int?, builder: MachineBuilder<MachineDefinition?>? ->
                builder!!.langValue(GTValues.VNF[tier!!] + " Huge Input Dual Hatch")
                    .rotationState(RotationState.ALL)
                    .overlayTieredHullRenderer("huge_dual_hatch.import")
                    .abilities(PartAbility.IMPORT_ITEMS)
                    .tooltips(Component.translatable("gtceu.machine.dual_hatch.import.tooltip"))
                builder.tooltips(
                    Component.translatable(
                        "gtceu.universal.tooltip.item_storage_capacity",
                        (1 + tier) * HugeBusPartMachine.Companion.INV_MULTIPLE
                    )
                )
                    .tooltips(
                        Component.translatable(
                            "gtceu.universal.tooltip.fluid_storage_capacity_mult",
                            tier, FormattingUtil.formatNumbers(Int.Companion.MAX_VALUE)
                        )
                    )
                builder.register()
            },
            *ALL_TIERS
        )

        @JvmStatic
        val PROGRAMMABLEC_HATCH: Array<MachineDefinition?> = registerTieredMachines(
            "programmablec_hatch",
            { holder: IMachineBlockEntity?, tier: Int? ->
                ProgrammableHatchPartMachine(
                    holder!!,
                    tier!!,
                    IO.IN
                )
            },
            { tier: Int?, builder: MachineBuilder<MachineDefinition?>? ->
                builder!!
                    .langValue("%s Programmablec Hatch".format(GTValues.VNF[tier!!]))
                    .rotationState(RotationState.ALL)
                    .abilities(PartAbility.IMPORT_ITEMS)
                    .renderer {
                        OverlayTieredMachineRenderer(
                            tier,
                            GTCEu.id("block/machine/part/dual_hatch.import")
                        )
                    }
                    .tooltips(
                        Component.translatable("gtceu.machine.dual_hatch.import.tooltip"),
                        Component.translatable(
                            "gtceu.universal.tooltip.item_storage_capacity",
                            (tier - 4).toDouble().pow(2.0).toInt()
                        ),
                        Component.translatable(
                            "gtceu.universal.tooltip.fluid_storage_capacity_mult",
                            (tier - 4),
                            DualHatchPartMachine.getTankCapacity(DualHatchPartMachine.INITIAL_TANK_CAPACITY, tier)
                        ),
                        Component.translatable("gtceu.universal.enabled")
                    )
                    .register()
            },
            *GTMachineUtils.DUAL_HATCH_TIERS
        )

        fun registerTieredMachines(
            name: String?,
            factory: BiFunction<IMachineBlockEntity?, Int?, MetaMachine?>,
            builder: BiFunction<Int?, MachineBuilder<MachineDefinition?>?, MachineDefinition?>,
            vararg tiers: Int
        ): Array<MachineDefinition?> {
            val definitions = arrayOfNulls<MachineDefinition>(GTValues.TIER_COUNT)
            for (tier in tiers) {
                val register = GTMTRegistration.Companion.GTMTHINGS_REGISTRATE
                    .machine(
                        GTValues.VN[tier].lowercase() + "_" + name
                    ) { holder: IMachineBlockEntity? -> factory.apply(holder, tier) }
                    .tier(tier)
                definitions[tier] = builder.apply(tier, register)
            }
            return definitions
        }

        fun init() {}
    }
}