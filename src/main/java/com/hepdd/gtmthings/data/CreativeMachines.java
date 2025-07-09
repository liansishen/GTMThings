package com.hepdd.gtmthings.data;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.item.ComponentItem;
import com.gregtechceu.gtceu.api.item.component.ICustomDescriptionId;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.item.CoverPlaceBehavior;
import com.gregtechceu.gtceu.common.item.ItemFluidContainer;
import com.gregtechceu.gtceu.common.item.TooltipBehavior;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;

import com.hepdd.gtmthings.common.block.machine.multiblock.part.CreativeEnergyHatchPartMachine;
import com.hepdd.gtmthings.common.block.machine.multiblock.part.CreativeInputBusPartMachine;
import com.hepdd.gtmthings.common.block.machine.multiblock.part.CreativeInputHatchPartMachine;
import com.hepdd.gtmthings.common.block.machine.multiblock.part.CreativeLaserHatchPartMachine;
import com.hepdd.gtmthings.common.item.CreativeFluidStats;
import com.tterrag.registrate.providers.ProviderType;
import com.tterrag.registrate.util.entry.ItemEntry;
import com.tterrag.registrate.util.nullness.NonNullBiConsumer;

import static com.gregtechceu.gtceu.api.GTValues.*;
import static com.gregtechceu.gtceu.common.data.GTItems.attach;
import static com.gregtechceu.gtceu.common.data.models.GTMachineModels.OVERLAY_FLUID_HATCH_HALF_PX_TEX;
import static com.gregtechceu.gtceu.common.data.models.GTMachineModels.OVERLAY_ITEM_HATCH;
import static com.hepdd.gtmthings.common.registry.GTMTRegistration.GTMTHINGS_REGISTRATE;
import static com.hepdd.gtmthings.data.CustomMachines.OVER_LAY_PIPE_IN;

public class CreativeMachines {

    static {
        GTMTHINGS_REGISTRATE.creativeModeTab(() -> CreativeModeTabs.CREATIVE_TAB);
    }

    public static final MachineDefinition CREATIVE_FLUID_INPUT_HATCH = GTMTHINGS_REGISTRATE.machine(
            "creative_fluid_input_hatch", CreativeInputHatchPartMachine::new)
            .rotationState(RotationState.ALL)
            .colorOverlayTieredHullModel("overlay_pipe_in_9x",OVERLAY_FLUID_HATCH_HALF_PX_TEX)
            .tooltips(Component.translatable("gtmthings.creative_tooltip"))
            .abilities(PartAbility.IMPORT_FLUIDS, PartAbility.IMPORT_FLUIDS_9X)
            .tier(MAX)
            .register();

    public static final MachineDefinition CREATIVE_ITEM_INPUT_BUS = GTMTHINGS_REGISTRATE.machine(
            "creative_item_input_bus", CreativeInputBusPartMachine::new)
            .rotationState(RotationState.ALL)
            .colorOverlayTieredHullModel("overlay_pipe_in", OVERLAY_ITEM_HATCH)
            .tooltips(Component.translatable("gtmthings.creative_tooltip"))
            .abilities(PartAbility.IMPORT_ITEMS)
            .tier(MAX)
            .register();

    // energy input hatch
    public static final MachineDefinition CREATIVE_ENERGY_INPUT_HATCH = GTMTHINGS_REGISTRATE.machine(
            "creative_energy_hatch", CreativeEnergyHatchPartMachine::new)
            .rotationState(RotationState.ALL)
            .tooltips(Component.translatable("gtmthings.creative_tooltip"))
            .overlayTieredHullModel("energy_input_hatch")
            .abilities(PartAbility.INPUT_ENERGY)
            .tier(MAX)
            .register();

    // laser input hatch
    public static final MachineDefinition CREATIVE_LASER_INPUT_HATCH = GTMTHINGS_REGISTRATE.machine(
            "creative_laser_hatch", CreativeLaserHatchPartMachine::new)
            .rotationState(RotationState.ALL)
            .tooltips(Component.translatable("gtmthings.creative_tooltip"))
            .overlayTieredHullModel("laser_target_hatch")
            .abilities(PartAbility.INPUT_LASER)
            .tier(MAX)
            .register();

    public static ItemEntry<ComponentItem> CREATIVE_ENERGY_COVER = GTMTHINGS_REGISTRATE
            .item("creative_energy_cover", ComponentItem::create)
            .onRegister(attach(new CoverPlaceBehavior(GTMTCovers.CREATIVE_ENERGY),
                    new TooltipBehavior(lines -> lines.add(Component.translatable("gtmthings.creative_tooltip")))))
            .register();

    public static ItemEntry<ComponentItem> CREATIVE_FLUID_CELL = GTMTHINGS_REGISTRATE
            .item("creative_fluid_cell", ComponentItem::create)
            .color(() -> GTItems::cellColor)
            .setData(ProviderType.ITEM_MODEL, NonNullBiConsumer.noop())
            .onRegister(attach(cellName(),
                    new CreativeFluidStats(),
                    new ItemFluidContainer()))
            .register();

    public static ICustomDescriptionId cellName() {
        return new ICustomDescriptionId() {

            @Override
            public Component getItemName(ItemStack stack) {
                Component prefix = FluidUtil.getFluidContained(stack).map(FluidStack::getDisplayName)
                        .orElse(Component.translatable("gtceu.fluid.empty"));
                return Component.translatable(stack.getDescriptionId(), prefix);
            }
        };
    }

    public static void init() {}
}
