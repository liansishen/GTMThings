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

import com.hepdd.gtmthings.api.machine.IWirelessEnergyContainerHolder;
import com.hepdd.gtmthings.api.machine.WirelessEnergyReceiveCoverHolder;
import com.hepdd.gtmthings.api.misc.WirelessEnergyContainer;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import static com.gregtechceu.gtceu.api.capability.GTCapabilityHelper.getEnergyContainer;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class WirelessEnergyReceiveCover extends CoverBehavior implements IWirelessEnergyContainerHolder {

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(WirelessEnergyReceiveCover.class,
            CoverBehavior.MANAGED_FIELD_HOLDER);

    private TickableSubscription subscription;

    @Getter
    @Setter
    private WirelessEnergyContainer WirelessEnergyContainerCache;

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
        var machine = getMachine();
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
        } else if (machine instanceof WirelessEnergyReceiveCoverHolder holder) {
            return holder.getTier() >= this.tier;
        } else {
            return false;
        }
    }

    @Override
    public void onAttached(ItemStack itemStack, ServerPlayer player) {
        super.onAttached(itemStack, player);
        this.uuid = player.getUUID();
        var machine = getMachine();
        if (machine instanceof TieredEnergyMachine tieredEnergyMachine) {
            this.machineMaxEnergy = GTValues.VEX[tieredEnergyMachine.getTier()] << 6;
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
            var machine = getMachine();
            if (machine instanceof BatteryBufferMachine || machine instanceof HullMachine || machine instanceof WirelessEnergyReceiveCoverHolder) {
                var changeStored = Math.min(energyContainer.getEnergyCapacity() - energyContainer.getEnergyStored(), this.energyPerTick);
                if (changeStored <= 0) return;
                WirelessEnergyContainer container = getWirelessEnergyContainer();
                if (container == null) return;
                long changeenergy = container.removeEnergy(changeStored, machine);
                if (changeenergy > 0) energyContainer.acceptEnergyFromNetwork(null, changeenergy / this.amperage, this.amperage);
            } else {
                var changeStored = Math.min(this.machineMaxEnergy - energyContainer.getEnergyStored(), this.energyPerTick);
                if (changeStored <= 0) return;
                WirelessEnergyContainer container = getWirelessEnergyContainer();
                if (container == null) return;
                long changeenergy = container.removeEnergy(changeStored, machine);
                if (changeenergy > 0) energyContainer.addEnergy(changeenergy);
            }
        }
        updateCoverSub();
    }

    @Override
    @Nullable
    public UUID getUUID() {
        return uuid;
    }

    @Override
    public boolean cover() {
        return true;
    }

    @Nullable
    private MetaMachine getMachine() {
        return MetaMachine.getMachine(coverHolder.getLevel(), coverHolder.getPos());
    }
}
