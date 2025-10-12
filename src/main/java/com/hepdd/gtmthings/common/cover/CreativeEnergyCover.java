package com.hepdd.gtmthings.common.cover;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.ICoverable;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.cover.CoverBehavior;
import com.gregtechceu.gtceu.api.cover.CoverDefinition;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.TieredEnergyMachine;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;

import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import static com.gregtechceu.gtceu.api.capability.GTCapabilityHelper.getEnergyContainer;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class CreativeEnergyCover extends CoverBehavior {

    private TickableSubscription subscription;

    private static final int MAX_AMPERAGE = 16;

    @Persisted
    private long energyPerTick;
    @Persisted
    private int tier;
    @Persisted
    private int amperage;
    @Persisted
    private long machineMaxEnergy;

    public CreativeEnergyCover(CoverDefinition definition, ICoverable coverHolder, Direction attachedSide) {
        super(definition, coverHolder, attachedSide);
        this.tier = GTValues.LV;
        this.amperage = 1;
        this.energyPerTick = GTValues.VEX[tier] * amperage;
    }

    @Override
    public boolean canAttach() {
        var machine = getMachine();
        if (machine instanceof TieredEnergyMachine tieredEnergyMachine && tieredEnergyMachine.energyContainer.getHandlerIO() == IO.IN) {
            var covers = tieredEnergyMachine.getCoverContainer().getCovers();
            for (var cover : covers) {
                if (cover instanceof CreativeEnergyCover) return false;
            }
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void onLoad() {
        super.onLoad();
        updateCoverSub();
    }

    @Override
    public void onRemoved() {
        super.onRemoved();
        if (subscription != null) {
            subscription.unsubscribe();
        }
    }

    @Override
    public void onAttached(ItemStack itemStack, ServerPlayer player) {
        super.onAttached(itemStack, player);
        var machine = getMachine();
        if (machine instanceof TieredEnergyMachine tieredEnergyMachine) {
            this.tier = tieredEnergyMachine.getTier();
            this.energyPerTick = GTValues.VEX[this.tier] * amperage;
            this.machineMaxEnergy = GTValues.VEX[tieredEnergyMachine.getTier()] << 6;
        }
        updateCoverSub();
    }

    @Override
    public InteractionResult onScrewdriverClick(Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (!coverHolder.getLevel().isClientSide()) {
            if (this.amperage >= MAX_AMPERAGE) {
                this.amperage = 1;
            } else {
                this.amperage *= 2;
            }
            updatePower(player);
        }
        return InteractionResult.SUCCESS;
    }

    private void updatePower(Player player) {
        this.energyPerTick = GTValues.VEX[this.tier] * this.amperage;
        player.displayClientMessage(Component.literal(this.amperage + "A"), false);
    }

    private void updateCoverSub() {
        subscription = coverHolder.subscribeServerTick(subscription, this::updateEnergy);
    }

    private void updateEnergy() {
        var energyContainer = getEnergyContainer(coverHolder.holder(), attachedSide);
        if (energyContainer != null) {
            var changeStored = Math.min(this.machineMaxEnergy - energyContainer.getEnergyStored(), this.energyPerTick);
            if (changeStored <= 0) return;
            energyContainer.addEnergy(changeStored);
        }
        updateCoverSub();
    }

    @Nullable
    private MetaMachine getMachine() {
        return MetaMachine.getMachine(coverHolder.holder());
    }
}
