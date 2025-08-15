package com.hepdd.gtmthings.common.block.machine.electric;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.feature.IInteractedMachine;
import com.gregtechceu.gtceu.api.machine.feature.IMachineLife;
import com.gregtechceu.gtceu.api.machine.multiblock.part.TieredIOPartMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableEnergyContainer;
import com.gregtechceu.gtceu.common.data.GTItems;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import com.hepdd.gtmthings.api.machine.IWirelessEnergyContainerHolder;
import com.hepdd.gtmthings.api.misc.WirelessEnergyContainer;
import com.hepdd.gtmthings.utils.BigIntegerUtils;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

import javax.annotation.ParametersAreNonnullByDefault;

import static com.hepdd.gtmthings.utils.TeamUtil.GetName;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class WirelessEnergyInterface extends TieredIOPartMachine implements IInteractedMachine, IMachineLife, IWirelessEnergyContainerHolder {

    @Getter
    @Setter
    @Nullable
    private WirelessEnergyContainer WirelessEnergyContainerCache;

    public final NotifiableEnergyContainer energyContainer;

    public WirelessEnergyInterface(MetaMachineBlockEntity holder) {
        super(holder, GTValues.MAX, IO.IN);
        this.energyContainer = new Interface(this);
    }

    @Override
    public boolean shouldOpenUI(Player player, InteractionHand hand, BlockHitResult hit) {
        return false;
    }

    @Override
    public InteractionResult onUse(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (isRemote()) return InteractionResult.PASS;
        ItemStack is = player.getItemInHand(hand);
        if (is.isEmpty()) return InteractionResult.PASS;
        if (is.is(GTItems.TOOL_DATA_STICK.asItem())) {
            setOwnerUUID(player.getUUID());
            setWirelessEnergyContainerCache(null);
            player.sendSystemMessage(Component.translatable("gtmthings.machine.wireless_energy_hatch.tooltip.bind", GetName(player)));
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    @Override
    public boolean onLeftClick(Player player, Level world, InteractionHand hand, BlockPos pos, Direction direction) {
        if (isRemote()) return false;
        ItemStack is = player.getItemInHand(hand);
        if (is.isEmpty()) return false;
        if (is.is(GTItems.TOOL_DATA_STICK.asItem())) {
            setOwnerUUID(null);
            setWirelessEnergyContainerCache(null);
            player.sendSystemMessage(Component.translatable("gtmthings.machine.wireless_energy_hatch.tooltip.unbind"));
            return true;
        }
        return false;
    }

    @Override
    public void onMachinePlaced(@Nullable LivingEntity player, ItemStack stack) {
        if (player != null) {
            setOwnerUUID(player.getUUID());
        }
    }

    @Override
    public @Nullable UUID getUUID() {
        return this.getOwnerUUID();
    }

    //////////////////////////////////////
    // ********** Misc **********//
    //////////////////////////////////////

    @Override
    public int tintColor(int index) {
        if (index == 2) {
            return GTValues.VC[getTier()];
        }
        return super.tintColor(index);
    }

    private static class Interface extends NotifiableEnergyContainer {

        private final WirelessEnergyInterface energyInterface;

        private Interface(WirelessEnergyInterface machine) {
            super(machine, Long.MAX_VALUE, Long.MAX_VALUE, Long.MAX_VALUE, 0, 0);
            energyInterface = machine;
        }

        @Override
        public long getEnergyCapacity() {
            var c = energyInterface.getWirelessEnergyContainer();
            if (c == null) return 0;
            var cap = c.getCapacity();
            if (cap == null) return Long.MAX_VALUE;
            return BigIntegerUtils.getLongValue(cap);
        }

        @Override
        public long getEnergyStored() {
            var c = energyInterface.getWirelessEnergyContainer();
            if (c == null) return 0;
            return BigIntegerUtils.getLongValue(c.getStorage());
        }

        @Override
        public long acceptEnergyFromNetwork(@Nullable Direction side, long voltage, long energyAdded) {
            if (side == null || inputsEnergy(side)) {
                var c = energyInterface.getWirelessEnergyContainer();
                if (c == null) return 0;
                return c.addEnergy(energyAdded, energyInterface);
            }
            return 0;
        }

        @Override
        public void checkOutputSubscription() {}

        @Override
        public void updateTick() {
            if (updateSubs != null) {
                updateSubs.unsubscribe();
                updateSubs = null;
            }
        }

        @Override
        public boolean inputsEnergy(Direction side) {
            return machine.getFrontFacing() == side;
        }

        @Override
        public boolean outputsEnergy(Direction side) {
            return false;
        }

        @Override
        public long changeEnergy(long energyToAdd) {
            return 0;
        }
    }
}
