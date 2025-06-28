package com.hepdd.gtmthings.data

import com.gregtechceu.gtceu.api.GTCEuAPI
import com.gregtechceu.gtceu.api.GTValues
import com.gregtechceu.gtceu.api.capability.recipe.IO
import com.gregtechceu.gtceu.api.data.RotationState
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity
import com.gregtechceu.gtceu.api.machine.MachineDefinition
import com.gregtechceu.gtceu.api.machine.MetaMachine
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility
import com.gregtechceu.gtceu.api.registry.registrate.MachineBuilder
import com.gregtechceu.gtceu.utils.FormattingUtil
import com.hepdd.gtmthings.GTMThings.Companion.id
import com.hepdd.gtmthings.common.block.machine.electric.WirelessEnergyInterface
import com.hepdd.gtmthings.common.block.machine.electric.WirelessEnergyMonitor
import com.hepdd.gtmthings.common.block.machine.multiblock.part.WirelessEnergyHatchPartMachine
import com.hepdd.gtmthings.common.block.machine.multiblock.part.computation.WirelessOpticalComputationHatchMachine
import com.hepdd.gtmthings.common.registry.GTMTRegistration
import net.minecraft.network.chat.Component
import java.util.function.BiFunction

class WirelessMachines {

    companion object {
        @JvmStatic
        val ALL_TIERS: IntArray =
            GTValues.tiersBetween(GTValues.LV, if (GTCEuAPI.isHighTier()) GTValues.MAX else GTValues.UHV)
        @JvmStatic
        val WIRELL_ENERGY_HIGH_TIERS: IntArray =
            GTValues.tiersBetween(GTValues.EV, if (GTCEuAPI.isHighTier()) GTValues.MAX else GTValues.UHV)

        init {
            GTMTRegistration.Companion.GTMTHINGS_REGISTRATE.creativeModeTab { CreativeModeTabs.WIRELESS_TAB }
        }

        @JvmStatic
        val WIRELESS_ENERGY_MONITOR: MachineDefinition = GTMTRegistration.Companion.GTMTHINGS_REGISTRATE
            .machine(
                "wireless_energy_monitor"
            ) { holder: IMachineBlockEntity? -> WirelessEnergyMonitor(holder!!) }
            .rotationState(RotationState.NON_Y_AXIS)
            .workableTieredHullRenderer(id("block/machines/wireless_energy_monitor"))
            .tier(GTValues.IV)
            .register()

        @JvmStatic
        val WIRELESS_ENERGY_INTERFACE: MachineDefinition = GTMTRegistration.Companion.GTMTHINGS_REGISTRATE
            .machine(
                "wireless_energy_interface"
            ) { holder: IMachineBlockEntity? -> WirelessEnergyInterface(holder!!) }
            .rotationState(RotationState.ALL)
            .overlayTieredHullRenderer("energy_hatch.input")
            .tier(GTValues.IV)
            .register()

        @JvmStatic
        val WIRELESS_COMPUTATION_HATCH_TRANSMITTER: MachineDefinition = GTMTRegistration.Companion.GTMTHINGS_REGISTRATE
            .machine(
                "wireless_computation_transmitter_hatch"
            ) { holder: IMachineBlockEntity? -> WirelessOpticalComputationHatchMachine(holder!!, true) }
            .rotationState(RotationState.ALL)
            .abilities(PartAbility.COMPUTATION_DATA_TRANSMISSION)
            .overlayTieredHullRenderer("computation_data_hatch")
            .tooltips(
                Component.translatable("gtmthings.machine.wireless_computation_transmitter_hatch.tooltip.1"),
                Component.translatable("gtmthings.machine.wireless_computation_transmitter_hatch.tooltip.2")
            )
            .tier(GTValues.UV)
            .register()

        @JvmStatic
        val WIRELESS_COMPUTATION_HATCH_RECEIVER: MachineDefinition = GTMTRegistration.Companion.GTMTHINGS_REGISTRATE
            .machine(
                "wireless_computation_receiver_hatch"
            ) { holder: IMachineBlockEntity? -> WirelessOpticalComputationHatchMachine(holder!!, false) }
            .rotationState(RotationState.ALL)
            .abilities(PartAbility.COMPUTATION_DATA_RECEPTION)
            .overlayTieredHullRenderer("computation_data_hatch")
            .tooltips(
                Component.translatable("gtmthings.machine.wireless_computation_receiver_hatch.tooltip.1"),
                Component.translatable("gtmthings.machine.wireless_computation_receiver_hatch.tooltip.2")
            )
            .tier(GTValues.UV)
            .register()

        @JvmStatic
        val WIRELESS_ENERGY_INPUT_HATCH: Array<MachineDefinition?> =
            registerWirelessEnergyHatch(IO.IN, 2, PartAbility.INPUT_ENERGY, ALL_TIERS)
        @JvmStatic
        val WIRELESS_ENERGY_INPUT_HATCH_4A: Array<MachineDefinition?> =
            registerWirelessEnergyHatch(IO.IN, 4, PartAbility.INPUT_ENERGY, ALL_TIERS)
        @JvmStatic
        val WIRELESS_ENERGY_INPUT_HATCH_16A: Array<MachineDefinition?> =
            registerWirelessEnergyHatch(IO.IN, 16, PartAbility.INPUT_ENERGY, ALL_TIERS)
        // public static final MachineDefinition[] WIRELESS_ENERGY_INPUT_HATCH_64A =
        // registerWirelessEnergyHatch(IO.IN,64,PartAbility.INPUT_LASER, WIRELL_ENERGY_HIGH_TIERS);
        @JvmStatic
        val WIRELESS_ENERGY_INPUT_HATCH_256A: Array<MachineDefinition?> =
            registerWirelessLaserHatch(IO.IN, 256, PartAbility.INPUT_LASER, WIRELL_ENERGY_HIGH_TIERS)
        @JvmStatic
        val WIRELESS_ENERGY_INPUT_HATCH_1024A: Array<MachineDefinition?> =
            registerWirelessLaserHatch(IO.IN, 1024, PartAbility.INPUT_LASER, WIRELL_ENERGY_HIGH_TIERS)
        @JvmStatic
        val WIRELESS_ENERGY_INPUT_HATCH_4096A: Array<MachineDefinition?> =
            registerWirelessLaserHatch(IO.IN, 4096, PartAbility.INPUT_LASER, WIRELL_ENERGY_HIGH_TIERS)
        @JvmStatic
        val WIRELESS_ENERGY_INPUT_HATCH_16384A: Array<MachineDefinition?> =
            registerWirelessLaserHatch(IO.IN, 16384, PartAbility.INPUT_LASER, WIRELL_ENERGY_HIGH_TIERS)
        @JvmStatic
        val WIRELESS_ENERGY_INPUT_HATCH_65536A: Array<MachineDefinition?> =
            registerWirelessLaserHatch(IO.IN, 65536, PartAbility.INPUT_LASER, WIRELL_ENERGY_HIGH_TIERS)

        @JvmStatic
        val WIRELESS_ENERGY_OUTPUT_HATCH: Array<MachineDefinition?> =
            registerWirelessEnergyHatch(IO.OUT, 2, PartAbility.OUTPUT_ENERGY, ALL_TIERS)
        @JvmStatic
        val WIRELESS_ENERGY_OUTPUT_HATCH_4A: Array<MachineDefinition?> =
            registerWirelessEnergyHatch(IO.OUT, 4, PartAbility.OUTPUT_ENERGY, ALL_TIERS)
        @JvmStatic
        val WIRELESS_ENERGY_OUTPUT_HATCH_16A: Array<MachineDefinition?> =
            registerWirelessEnergyHatch(IO.OUT, 16, PartAbility.OUTPUT_ENERGY, ALL_TIERS)
        // public static final MachineDefinition[] WIRELESS_ENERGY_OUTPUT_HATCH_64A =
        // registerWirelessEnergyHatch(IO.OUT,64,PartAbility.OUTPUT_ENERGY, ALL_TIERS);
        @JvmStatic
        val WIRELESS_ENERGY_OUTPUT_HATCH_256A: Array<MachineDefinition?> =
            registerWirelessLaserHatch(IO.OUT, 256, PartAbility.OUTPUT_LASER, WIRELL_ENERGY_HIGH_TIERS)
        @JvmStatic
        val WIRELESS_ENERGY_OUTPUT_HATCH_1024A: Array<MachineDefinition?> =
            registerWirelessLaserHatch(IO.OUT, 1024, PartAbility.OUTPUT_LASER, WIRELL_ENERGY_HIGH_TIERS)
        @JvmStatic
        val WIRELESS_ENERGY_OUTPUT_HATCH_4096A: Array<MachineDefinition?> =
            registerWirelessLaserHatch(IO.OUT, 4096, PartAbility.OUTPUT_LASER, WIRELL_ENERGY_HIGH_TIERS)
        @JvmStatic
        val WIRELESS_ENERGY_OUTPUT_HATCH_16384A: Array<MachineDefinition?> =
            registerWirelessLaserHatch(IO.OUT, 16384, PartAbility.OUTPUT_LASER, WIRELL_ENERGY_HIGH_TIERS)
        @JvmStatic
        val WIRELESS_ENERGY_OUTPUT_HATCH_65536A: Array<MachineDefinition?> =
            registerWirelessLaserHatch(IO.OUT, 65536, PartAbility.OUTPUT_LASER, WIRELL_ENERGY_HIGH_TIERS)

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

        fun registerWirelessEnergyHatch(
            io: IO,
            amperage: Int,
            ability: PartAbility,
            tiers: IntArray
        ): Array<MachineDefinition?> {
            val name = if (io == IO.IN) "input" else "output"
            val finalRender = getRender(amperage)
            return registerTieredMachines(
                amperage.toString() + "a_wireless_energy_" + name + "_hatch",
                { holder: IMachineBlockEntity?, tier: Int? ->
                    WirelessEnergyHatchPartMachine(
                        holder!!,
                        tier!!,
                        io,
                        amperage
                    )
                },
                { tier: Int?, builder: MachineBuilder<MachineDefinition?>? ->
                    builder!!
                        .langValue(GTValues.VNF[tier!!] + (if (io == IO.IN) " Energy Hatch" else " Dynamo Hatch"))
                        .rotationState(RotationState.ALL)
                        .abilities(ability)
                        .tooltips(
                            Component.translatable("gtmthings.machine.energy_hatch.$name.tooltip"),
                            (Component.translatable("gtmthings.machine.wireless_energy_hatch.$name.tooltip"))
                        )
                        .overlayTieredHullRenderer(finalRender)
                        .register()
                },
                *tiers
            )
        }

        fun registerWirelessLaserHatch(
            io: IO,
            amperage: Int,
            ability: PartAbility,
            tiers: IntArray
        ): Array<MachineDefinition?> {
            val name = if (io == IO.IN) "target" else "source"
            val finalRender = getRender(amperage)
            return registerTieredMachines(
                amperage.toString() + "a_wireless_laser_" + name + "_hatch",
                { holder: IMachineBlockEntity?, tier: Int? ->
                    WirelessEnergyHatchPartMachine(
                        holder!!,
                        tier!!,
                        io,
                        amperage
                    )
                },
                { tier: Int?, builder: MachineBuilder<MachineDefinition?>? ->
                    builder!!
                        .langValue(
                            GTValues.VNF[tier!!] + " " + FormattingUtil.formatNumbers(amperage) + "A Laser " +
                                    FormattingUtil.toEnglishName(name) + " Hatch"
                        )
                        .rotationState(RotationState.ALL)
                        .abilities(ability)
                        .tooltips(
                            Component.translatable("gtmthings.machine.energy_hatch.$name.tooltip"),
                            (Component.translatable("gtmthings.machine.wireless_energy_hatch.$name.tooltip"))
                        )
                        .overlayTieredHullRenderer(finalRender)
                        .register()
                },
                *tiers
            )
        }

        private fun getRender(amperage: Int): String {
            var render = "wireless_energy_hatch"
            render = when (amperage) {
                2 -> render
                4 -> render + "_4a"
                16 -> render + "_16a"
                64 -> render + "_64a"
                else -> "wireless_laser_hatch.target"
            }
            return render
        }

        fun init() {}
    }
}