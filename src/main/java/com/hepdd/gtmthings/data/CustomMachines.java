package com.hepdd.gtmthings.data;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.registry.registrate.MachineBuilder;
import com.gregtechceu.gtceu.common.machine.multiblock.part.DualHatchPartMachine;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import com.hepdd.gtmthings.GTMThings;
import com.hepdd.gtmthings.common.block.machine.electric.DigitalMiner;
import com.hepdd.gtmthings.common.block.machine.multiblock.part.HugeBusPartMachine;
import com.hepdd.gtmthings.common.block.machine.multiblock.part.HugeDualHatchPartMachine;
import com.hepdd.gtmthings.common.block.machine.multiblock.part.ProgrammableHatchPartMachine;
import com.hepdd.gtmthings.common.block.machine.multiblock.part.appeng.MEOutputPartMachine;

import java.util.Locale;
import java.util.function.BiFunction;

import static com.gregtechceu.gtceu.api.GTValues.*;
import static com.gregtechceu.gtceu.api.capability.recipe.IO.IN;
import static com.gregtechceu.gtceu.common.data.machines.GTMachineUtils.*;
import static com.gregtechceu.gtceu.common.data.models.GTMachineModels.OVERLAY_ITEM_HATCH;
import static com.hepdd.gtmthings.common.block.machine.multiblock.part.HugeBusPartMachine.INV_MULTIPLE;
import static com.hepdd.gtmthings.common.registry.GTMTRegistration.GTMTHINGS_REGISTRATE;
import static com.hepdd.gtmthings.data.GTMTRecipeTypes.DIGITAL_MINER_RECIPE;

public class CustomMachines {

    public static final int[] ALL_TIERS = GTValues.tiersBetween(ULV, GTCEuAPI.isHighTier() ? MAX : UV);

    static {
        GTMTHINGS_REGISTRATE.creativeModeTab(() -> CreativeModeTabs.MORE_MACHINES);
    }

    public static final MachineDefinition[] DIGITAL_MINER = registerTieredMachines("digital_miner",
            DigitalMiner::new,
            (tier, builder) -> builder
                    .langValue("%s Digital Miner %s".formatted(VLVH[tier], VLVT[tier]))
                    .rotationState(RotationState.NON_Y_AXIS).tier(tier)
                    .workableTieredHullModel(GTMThings.id("block/machines/miner"))
                    .tooltipBuilder((stack, tooltip) -> {
                        int maxArea = (int) (8 * Math.pow(2, tier));
                        long energyPerTick = GTValues.VEX[tier - 1];
                        tooltip.add(Component.translatable("gtceu.universal.tooltip.uses_per_tick", energyPerTick)
                                .append(Component.literal(", ").withStyle(ChatFormatting.GRAY))
                                .append(Component.literal("§7每个方块需要§f" + (int) (40 / Math.pow(2, tier)) + "§7刻。")));
                        tooltip.add(Component.translatable("gtceu.universal.tooltip.voltage_in",
                                FormattingUtil.formatNumbers(GTValues.VEX[tier]),
                                GTValues.VNF[tier]));
                        tooltip.add(
                                Component.translatable("gtceu.universal.tooltip.working_area_max", maxArea, maxArea));
                    })
                    .recipeTypes(DIGITAL_MINER_RECIPE)
                    .register(),
            LV, MV, HV);

    public static final MachineDefinition ME_EXPORT_BUFFER = GTMTHINGS_REGISTRATE.machine("me_export_buffer", MEOutputPartMachine::new)
            .rotationState(RotationState.ALL)
            .abilities(PartAbility.EXPORT_ITEMS, PartAbility.EXPORT_FLUIDS)
            .colorOverlayTieredHullModel(GTMThings.id("block/overlay/appeng/me_output_bus"))
            .tier(LuV)
            .register();

    public static final MachineDefinition[] HUGE_ITEM_IMPORT_BUS = registerTieredMachines("huge_item_import_bus",
            (holder, tier) -> new HugeBusPartMachine(holder, tier, IO.IN),
            (tier, builder) -> builder
                    .langValue(VNF[tier] + " Input Bus")
                    .rotationState(RotationState.ALL)
                    .abilities(
                            tier == 0 ? new PartAbility[] { PartAbility.IMPORT_ITEMS, PartAbility.STEAM_IMPORT_ITEMS } :
                                    new PartAbility[] { PartAbility.IMPORT_ITEMS })
                    .colorOverlayTieredHullModel("overlay_pipe_in_emissive", null, OVERLAY_ITEM_HATCH)
                    .tooltips(Component.translatable("gtmthings.machine.huge_item_bus.import.tooltip"),
                            Component.translatable("gtceu.universal.tooltip.item_storage_capacity",
                                    (1 + tier) * INV_MULTIPLE))
                    .register(),
            ALL_TIERS);

    public static final MachineDefinition[] HUGE_ITEM_EXPORT_BUS = registerTieredMachines("huge_item_export_bus",
            (holder, tier) -> new HugeBusPartMachine(holder, tier, IO.OUT),
            (tier, builder) -> builder
                    .langValue(VNF[tier] + " Output Bus")
                    .rotationState(RotationState.ALL)
                    .abilities(
                            tier == 0 ? new PartAbility[] { PartAbility.EXPORT_ITEMS, PartAbility.STEAM_EXPORT_ITEMS } :
                                    new PartAbility[] { PartAbility.EXPORT_ITEMS })
                    .colorOverlayTieredHullModel("overlay_pipe_out_emissive", null, OVERLAY_ITEM_HATCH)
                    .tooltips(Component.translatable("gtmthings.machine.huge_item_bus.export.tooltip"),
                            Component.translatable("gtceu.universal.tooltip.item_storage_capacity",
                                    (1 + tier) * INV_MULTIPLE))
                    .register(),
            ALL_TIERS);

    public static final MachineDefinition[] HUGE_INPUT_DUAL_HATCH = registerTieredMachines("huge_dual_hatch",
            (holder, tier) -> new HugeDualHatchPartMachine(holder, tier, IO.IN),
            (tier, builder) -> {
                builder.langValue(GTValues.VNF[tier] + " Huge Input Dual Hatch")
                        .rotationState(RotationState.ALL)
                        .colorOverlayTieredHullModel("overlay_pipe_in_emissive", null, "overlay_huge_dual_hatch")
                        .abilities(PartAbility.IMPORT_ITEMS)
                        .tooltips(Component.translatable("gtceu.machine.dual_hatch.import.tooltip"));
                builder.tooltips(Component.translatable("gtceu.universal.tooltip.item_storage_capacity",
                        (1 + tier) * INV_MULTIPLE))
                        .tooltips(Component.translatable("gtceu.universal.tooltip.fluid_storage_capacity_mult",
                                tier, FormattingUtil.formatNumbers(Integer.MAX_VALUE)));
                return builder.register();
            },
            ALL_TIERS);

    public static final MachineDefinition[] PROGRAMMABLEC_HATCH = registerTieredMachines(
            "programmablec_hatch", (holder, tier) -> new ProgrammableHatchPartMachine(holder, tier, IN),
            (tier, builder) -> builder
                    .langValue("%s Programmablec Hatch".formatted(VNF[tier]))
                    .rotationState(RotationState.ALL)
                    .abilities(PartAbility.IMPORT_ITEMS)
                    .workableTieredHullModel(GTCEu.id("block/machine/part/dual_hatch.import"))
                    .tooltips(Component.translatable("gtceu.machine.dual_hatch.import.tooltip"),
                            Component.translatable("gtceu.universal.tooltip.item_storage_capacity", (int) Math.pow((tier - 4), 2)),
                            Component.translatable("gtceu.universal.tooltip.fluid_storage_capacity_mult", (tier - 4), DualHatchPartMachine.getTankCapacity(DualHatchPartMachine.INITIAL_TANK_CAPACITY, tier)),
                            Component.translatable("gtceu.universal.enabled"))
                    .register(),
            DUAL_HATCH_TIERS);

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

    public static void init() {}
}
