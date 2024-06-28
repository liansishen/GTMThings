package com.hepdd.gtmthings.data;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.registry.registrate.MachineBuilder;
import com.hepdd.gtmthings.GTMThings;
import com.hepdd.gtmthings.common.block.machine.electric.WirelessEnergyMonitor;
import com.hepdd.gtmthings.common.block.machine.multiblock.part.WirelessEnergyHatchPartMachine;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.function.BiFunction;

import static com.gregtechceu.gtceu.api.GTValues.*;
import static com.hepdd.gtmthings.common.registry.GTMTRegistration.GTMTHINGS_REGISTRATE;

public class WirelessMachines {

    public static final int[] ALL_TIERS = GTValues.tiersBetween(LV, MAX);
    public static final int[] WIRELL_ENERGY_HIGH_TIERS = GTValues.tiersBetween(EV,MAX);

    static {
        GTMTHINGS_REGISTRATE.creativeModeTab(() -> CreativeModeTabs.WIRELESS_TAB);
    }

    public static final MachineDefinition WIRELESS_ENERGY_MONITOR = GTMTHINGS_REGISTRATE
            .machine("wireless_energy_monitor", WirelessEnergyMonitor::new)
            .rotationState(RotationState.NON_Y_AXIS)
            .compassNodeSelf()
            .workableTieredHullRenderer(GTMThings.id("block/machines/wireless_energy_monitor"))
            .tier(IV)
            .register();

    public static final MachineDefinition[] WIRELESS_ENERGY_INPUT_HATCH = registerWirelessEnergyHatch(IO.IN,2, PartAbility.INPUT_ENERGY, ALL_TIERS);
    public static final MachineDefinition[] WIRELESS_ENERGY_INPUT_HATCH_4A = registerWirelessEnergyHatch(IO.IN,4,PartAbility.INPUT_ENERGY, ALL_TIERS);
    public static final MachineDefinition[] WIRELESS_ENERGY_INPUT_HATCH_16A = registerWirelessEnergyHatch(IO.IN,16,PartAbility.INPUT_ENERGY, ALL_TIERS);
    public static final MachineDefinition[] WIRELESS_ENERGY_INPUT_HATCH_64A = registerWirelessEnergyHatch(IO.IN,64,PartAbility.INPUT_ENERGY,WIRELL_ENERGY_HIGH_TIERS);
    public static final MachineDefinition[] WIRELESS_ENERGY_INPUT_HATCH_256A = registerWirelessEnergyHatch(IO.IN,256,PartAbility.INPUT_ENERGY,WIRELL_ENERGY_HIGH_TIERS);
    public static final MachineDefinition[] WIRELESS_ENERGY_INPUT_HATCH_1024A = registerWirelessEnergyHatch(IO.IN,1024,PartAbility.INPUT_ENERGY,WIRELL_ENERGY_HIGH_TIERS);
    public static final MachineDefinition[] WIRELESS_ENERGY_INPUT_HATCH_4096A = registerWirelessEnergyHatch(IO.IN,4096,PartAbility.INPUT_ENERGY,WIRELL_ENERGY_HIGH_TIERS);
    public static final MachineDefinition[] WIRELESS_ENERGY_INPUT_HATCH_16384A = registerWirelessEnergyHatch(IO.IN,16384,PartAbility.INPUT_ENERGY,WIRELL_ENERGY_HIGH_TIERS);
    public static final MachineDefinition[] WIRELESS_ENERGY_INPUT_HATCH_65536A = registerWirelessEnergyHatch(IO.IN,65536,PartAbility.INPUT_ENERGY,WIRELL_ENERGY_HIGH_TIERS);
    public static final MachineDefinition[] WIRELESS_ENERGY_OUTPUT_HATCH = registerWirelessEnergyHatch(IO.OUT,2,PartAbility.OUTPUT_ENERGY, ALL_TIERS);
    public static final MachineDefinition[] WIRELESS_ENERGY_OUTPUT_HATCH_4A = registerWirelessEnergyHatch(IO.OUT,4,PartAbility.OUTPUT_ENERGY, ALL_TIERS);
    public static final MachineDefinition[] WIRELESS_ENERGY_OUTPUT_HATCH_16A = registerWirelessEnergyHatch(IO.OUT,16,PartAbility.OUTPUT_ENERGY, ALL_TIERS);

    public static MachineDefinition[] registerTieredMachines(String name,
                                                             BiFunction<IMachineBlockEntity, Integer, MetaMachine> factory,
                                                             BiFunction<Integer, MachineBuilder<MachineDefinition>, MachineDefinition> builder,
                                                             int... tiers) {
        MachineDefinition[] definitions = new MachineDefinition[GTValues.TIER_COUNT];
        for (int tier : tiers) {
            var register = GTMTHINGS_REGISTRATE
                    .machine(GTValues.VN[tier].toLowerCase(Locale.ROOT) + "_" + name,
                            holder -> factory.apply(holder, tier))
                    .tier(tier);
            definitions[tier] = builder.apply(tier, register);
        }
        return definitions;
    }

    public static MachineDefinition[] registerWirelessEnergyHatch(IO io, int amperage, PartAbility ability,int[] tiers) {
        var name = io == IO.IN ? "input" : "output";
        String finalRender = getRender(amperage, name);
        return registerTieredMachines(amperage + "a_wireless_energy_" + name + "_hatch",
                (holder, tier) -> new WirelessEnergyHatchPartMachine(holder, tier, io, amperage),
                (tier, builder) -> builder
                        .langValue(VNF[tier] + (io == IO.IN ? " Energy Hatch" : " Dynamo Hatch"))
                        .rotationState(RotationState.ALL)
                        .abilities(ability)
                        .tooltips(Component.translatable("gtmthings.machine.energy_hatch." + name + ".tooltip")
                                ,(Component.translatable("gtmthings.machine.wireless_energy_hatch." + name + ".tooltip")))
                        .overlayTieredHullRenderer(finalRender)
                        .compassNode("energy_hatch")
                        .register(),
                tiers);
    }

    private static @NotNull String getRender(int amperage, String name) {
        String render = "wireless_energy_hatch." + name;
        render = switch (amperage) {
            case 2 -> render;
            case 4 -> render + "_4a" ;
            case 16 -> render + "_16a";
            case 64 -> render + "_64a";
            default -> "wireless_laser_hatch.target";
        };
        return render;
    }

    public static void init() {};
}
