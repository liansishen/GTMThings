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
import com.gregtechceu.gtceu.client.renderer.machine.MinerRenderer;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import com.hepdd.gtmthings.common.block.machine.electric.DigitalMiner;
import com.hepdd.gtmthings.common.block.machine.multiblock.part.HugeBusPartMachine;
import com.hepdd.gtmthings.common.block.machine.multiblock.part.appeng.MEOutputPartMachine;
import com.hepdd.gtmthings.common.block.machine.normal.PersonalSpacePortal;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import java.util.Locale;
import java.util.function.BiFunction;

import static com.gregtechceu.gtceu.api.GTValues.*;
import static com.hepdd.gtmthings.common.block.machine.multiblock.part.HugeBusPartMachine.INV_MULTIPLE;
import static com.hepdd.gtmthings.common.registry.GTMTRegistration.GTMTHINGS_REGISTRATE;
import static com.hepdd.gtmthings.data.GTMTRecipeTypes.DIGITAL_MINER_RECIPE;

public class CustomMachines {

    public static final int[] ALL_TIERS = GTValues.tiersBetween(ULV, GTCEuAPI.isHighTier() ? MAX : UHV);

    static {
        GTMTHINGS_REGISTRATE.creativeModeTab(() -> CreativeModeTabs.MORE_MACHINES);
    }

    public static final MachineDefinition PERSONAL_SPACE_PORTAL = GTMTHINGS_REGISTRATE.machine("personal_space_portal"
            , PersonalSpacePortal::new)
            .rotationState(RotationState.NON_Y_AXIS)
            .blockProp(p -> p.strength(50.0F,6000000.0F))
            .tier(0)
            .register();

    public static final MachineDefinition[] DIGITAL_MINER = registerTieredMachines("digital_miner",
            DigitalMiner::new,
            (tier, builder) -> builder
            .langValue("%s Digital Miner %s".formatted(VLVH[tier], VLVT[tier]))
            .rotationState(RotationState.NON_Y_AXIS).tier(tier)
            .renderer(() -> new MinerRenderer(tier, GTCEu.id("block/machines/miner")))
            .tooltipBuilder((stack, tooltip) -> {
                int maxArea = (int) (8 * Math.pow(2, tier));
                long energyPerTick = GTValues.V[tier - 1];
                tooltip.add(Component.translatable("gtceu.universal.tooltip.uses_per_tick", energyPerTick)
                        .append(Component.literal(", ").withStyle(ChatFormatting.GRAY))
                        .append(Component.literal("§7每个方块需要§f" + (int) (40 / Math.pow(2, tier)) + "§7刻。")));
                tooltip.add(Component.translatable("gtceu.universal.tooltip.voltage_in",
                        FormattingUtil.formatNumbers(GTValues.V[tier]),
                        GTValues.VNF[tier]));
                tooltip.add(
                        Component.translatable("gtceu.universal.tooltip.working_area_max", maxArea, maxArea));
            })
            .recipeTypes(DIGITAL_MINER_RECIPE)
            .compassNode("miner")
            .register(),
            LV, MV, HV);

    public static final MachineDefinition ME_EXPORT_BUFFER = GTMTHINGS_REGISTRATE.machine("me_export_buffer"
            , MEOutputPartMachine::new)
            .rotationState(RotationState.ALL)
            .abilities(PartAbility.EXPORT_ITEMS,PartAbility.EXPORT_FLUIDS)
            .overlayTieredHullRenderer("me.export")
            .tier(LuV)
            .register();

    public static final MachineDefinition[] HUGE_ITEM_IMPORT_BUS = registerTieredMachines("huge_item_import_bus",
            (holder,tier) -> new HugeBusPartMachine(holder, tier, IO.IN),
            (tier, builder) -> builder
                    .langValue(VNF[tier] + " Input Bus")
                    .rotationState(RotationState.ALL)
                    .abilities(
                            tier == 0 ? new PartAbility[] { PartAbility.IMPORT_ITEMS, PartAbility.STEAM_IMPORT_ITEMS } :
                                    new PartAbility[] { PartAbility.IMPORT_ITEMS })
                    .overlayTieredHullRenderer("item_bus.import")
                    .tooltips(Component.translatable("gtmthings.machine.huge_item_bus.import.tooltip"),
                            Component.translatable("gtceu.universal.tooltip.item_storage_capacity",
                                    (1 + tier) * INV_MULTIPLE))
                    .compassNode("item_bus")
                    .register(),
            ALL_TIERS);

    public static final MachineDefinition[] HUGE_ITEM_EXPORT_BUS = registerTieredMachines("huge_item_export_bus",
            (holder,tier) -> new HugeBusPartMachine(holder, tier, IO.OUT),
            (tier, builder) -> builder
                    .langValue(VNF[tier] + " Output Bus")
                    .rotationState(RotationState.ALL)
                    .abilities(
                            tier == 0 ? new PartAbility[] { PartAbility.EXPORT_ITEMS, PartAbility.STEAM_EXPORT_ITEMS } :
                                    new PartAbility[] { PartAbility.EXPORT_ITEMS })
                    .overlayTieredHullRenderer("item_bus.export")
                    .tooltips(Component.translatable("gtmthings.machine.huge_item_bus.export.tooltip"),
                            Component.translatable("gtceu.universal.tooltip.item_storage_capacity",
                                    (1 + tier)* INV_MULTIPLE))
                    .compassNode("item_bus")
                    .register(),
            ALL_TIERS);

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
