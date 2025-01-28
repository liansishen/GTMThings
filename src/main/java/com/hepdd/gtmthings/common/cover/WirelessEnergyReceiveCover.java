package com.hepdd.gtmthings.common.cover;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.ICoverable;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.cover.CoverBehavior;
import com.gregtechceu.gtceu.api.cover.CoverDefinition;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.TieredEnergyMachine;
import com.gregtechceu.gtceu.common.machine.electric.BatteryBufferMachine;
import com.gregtechceu.gtceu.common.machine.electric.HullMachine;

import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import com.hepdd.gtmthings.api.misc.WirelessEnergyManager;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

import javax.annotation.ParametersAreNonnullByDefault;

import static com.gregtechceu.gtceu.api.capability.GTCapabilityHelper.getEnergyContainer;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class WirelessEnergyReceiveCover extends CoverBehavior {

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(WirelessEnergyReceiveCover.class,
            CoverBehavior.MANAGED_FIELD_HOLDER);

    private TickableSubscription subscription;
    @Getter
    @Persisted
    private UUID uuid;
    @Persisted
    private final long energyPerTick;
    @Persisted
    private final int tier;
    @Persisted
    private final int amperage;
    @Persisted
    private long machineMaxEnergy;

    @Override
    public @NotNull ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    public WirelessEnergyReceiveCover(CoverDefinition definition, ICoverable coverHolder, Direction attachedSide, int tier, int amperage) {
        super(definition, coverHolder, attachedSide);
        this.tier = tier;
        this.amperage = amperage;
        this.energyPerTick = GTValues.VEX[tier] * amperage;
    }

    @Override
    public boolean canAttach() {
        var machine = MetaMachine.getMachine(coverHolder.getLevel(), coverHolder.getPos());
        if (machine instanceof TieredEnergyMachine tieredEnergyMachine && tieredEnergyMachine.energyContainer.getHandlerIO() == IO.IN && tieredEnergyMachine.getTier() >= this.tier) {
            var covers = tieredEnergyMachine.getCoverContainer().getCovers();
            for (var cover : covers) {
                if (cover instanceof WirelessEnergyReceiveCover) return false;
            }
            return true;
        } else if (machine instanceof BatteryBufferMachine batteryBufferMachine) {
            return batteryBufferMachine.getTier() >= this.tier;
        } else if (machine instanceof HullMachine hullMachine) {
            return hullMachine.getTier() >= this.tier;
        } else {
            return false;
        }
    }

    @Override
    public void onAttached(ItemStack itemStack, ServerPlayer player) {
        super.onAttached(itemStack, player);
        this.uuid = player.getUUID();
        var machine = MetaMachine.getMachine(coverHolder.getLevel(), coverHolder.getPos());
        if (machine instanceof TieredEnergyMachine tieredEnergyMachine) {
            this.machineMaxEnergy = GTValues.VEX[tieredEnergyMachine.getTier()] * 64L;
        }
        updateCoverSub();
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

    private void updateCoverSub() {
        if (this.uuid != null) {
            subscription = coverHolder.subscribeServerTick(subscription, this::updateEnergy);
        } else if (subscription != null) {
            subscription.unsubscribe();
            subscription = null;
        }
    }

    private void updateEnergy() {
        if (uuid == null) return;
        var energyContainer = getEnergyContainer(coverHolder.getLevel(), coverHolder.getPos(), attachedSide);
        if (energyContainer != null) {
            var machine = MetaMachine.getMachine(coverHolder.getLevel(), coverHolder.getPos());
            if (machine instanceof BatteryBufferMachine || machine instanceof HullMachine) {
                var changeStored = Math.min(energyContainer.getEnergyCapacity() - energyContainer.getEnergyStored(), this.energyPerTick);
                if (changeStored <= 0) return;
                long changeenergy = -WirelessEnergyManager.addEUToGlobalEnergyMap(this.uuid, -changeStored, machine);
                if (changeenergy > 0) energyContainer.acceptEnergyFromNetwork(null, (long) ((double) GTValues.VEX[this.tier] * changeenergy / changeStored), this.amperage);
            } else {
                var changeStored = Math.min(this.machineMaxEnergy - energyContainer.getEnergyStored(), this.energyPerTick);
                if (changeStored <= 0) return;
                changeStored = WirelessEnergyManager.addEUToGlobalEnergyMap(this.uuid, -changeStored, machine);
                if (changeStored < 0) energyContainer.addEnergy(-changeStored);
            }
        }
        updateCoverSub();
    }
}
