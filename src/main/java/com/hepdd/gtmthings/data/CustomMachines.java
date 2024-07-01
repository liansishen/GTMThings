package com.hepdd.gtmthings.data;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.IMiner;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.registry.registrate.MachineBuilder;
import com.gregtechceu.gtceu.client.renderer.machine.MinerRenderer;
import com.gregtechceu.gtceu.common.machine.electric.MinerMachine;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import com.hepdd.gtmthings.common.block.machine.electric.DigitalMiner;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import java.util.Locale;
import java.util.function.BiFunction;

import static com.gregtechceu.gtceu.api.GTValues.*;
import static com.gregtechceu.gtceu.common.data.GTRecipeTypes.DUMMY_RECIPES;
import static com.hepdd.gtmthings.common.registry.GTMTRegistration.GTMTHINGS_REGISTRATE;
import static com.hepdd.gtmthings.data.GTMTRecipeTypes.DIGITAL_MINER;

public class CustomMachines {

    public static final MachineDefinition[] MINER = registerTieredMachines("miner",
            (holder, tier) -> new DigitalMiner(holder, tier, 1, tier * 8, tier),
            (tier, builder) -> builder
                    .rotationState(RotationState.ALL)
                    .langValue("%s Miner %s".formatted(VLVH[tier], VLVT[tier]))
                    .renderer(() -> new MinerRenderer(tier, GTCEu.id("block/machines/miner")))
                    .recipeTypes(DIGITAL_MINER)
//                    .tooltipBuilder((stack, tooltip) -> {
//                        int maxArea = IMiner.getWorkingArea(tier * 8);
//                        long energyPerTick = GTValues.V[tier - 1];
//                        int tickSpeed = 320 / (tier * 2);
//                        tooltip.add(Component.translatable("gtceu.machine.miner.tooltip", maxArea, maxArea));
//                        tooltip.add(Component.translatable("gtceu.universal.tooltip.uses_per_tick", energyPerTick)
//                                .append(Component.literal(", ").withStyle(ChatFormatting.GRAY))
//                                .append(Component.translatable("gtceu.machine.miner.per_block", tickSpeed / 20)));
//                        tooltip.add(Component.translatable("gtceu.universal.tooltip.voltage_in",
//                                FormattingUtil.formatNumbers(GTValues.V[tier]),
//                                GTValues.VNF[tier]));
//                        tooltip.add(Component.translatable("gtceu.universal.tooltip.energy_storage_capacity",
//                                FormattingUtil.formatNumbers(GTValues.V[tier] * 64L)));
//
//                        tooltip.add(
//                                Component.translatable("gtceu.universal.tooltip.working_area_max", maxArea, maxArea));
//                    })
                    .compassNode("miner")
                    .register(),
            MV);

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
