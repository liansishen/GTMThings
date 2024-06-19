package com.example.examplemod.block.machine.multiblock.part;

import com.example.examplemod.api.capability.IBindable;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.feature.IExplosionMachine;
import com.gregtechceu.gtceu.api.machine.feature.IInteractedMachine;
import com.gregtechceu.gtceu.api.machine.feature.IMachineLife;
import com.gregtechceu.gtceu.api.machine.multiblock.part.TieredIOPartMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableEnergyContainer;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import lombok.Getter;
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
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

import static com.example.examplemod.api.misc.WirelessEnergyManager.addEUToGlobalEnergyMap;

public class WirelessEnergyHatchPartMachine extends TieredIOPartMachine implements IInteractedMachine, IBindable, IExplosionMachine, IMachineLife {

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            WirelessEnergyHatchPartMachine.class, TieredIOPartMachine.MANAGED_FIELD_HOLDER);

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    @Persisted
    public UUID owner_uuid;
    @Persisted
    public final NotifiableEnergyContainer energyContainer;
    @Getter
    protected int amperage;
    private TickableSubscription updEnergySubs;

    public WirelessEnergyHatchPartMachine(IMachineBlockEntity holder, int tier, IO io, int amperage, Object... args) {
        super(holder, tier, io);
        this.amperage = amperage;
        this.energyContainer = createEnergyContainer(args);
    }

    protected NotifiableEnergyContainer createEnergyContainer(Object... args) {
        NotifiableEnergyContainer container;
        if (io == IO.OUT) {
            container = NotifiableEnergyContainer.emitterContainer(this, GTValues.V[tier] * 64L * amperage,
                    GTValues.V[tier], amperage);
        } else {
            container = NotifiableEnergyContainer.receiverContainer(this, GTValues.V[tier] * 16L * amperage,
                    GTValues.V[tier], amperage);
        }
        return container;
    }

    @Override
    public void onLoad() {
        super.onLoad();
        updateEnergySubscription();
    }

    @Override
    public void onUnload() {
        super.onUnload();
        if (updEnergySubs != null) {
            updEnergySubs.unsubscribe();
            updEnergySubs = null;
        }
    }

    private void updateEnergySubscription() {
        if (this.owner_uuid!=null) {
            updEnergySubs = subscribeServerTick(updEnergySubs,this::updateEnergy);
        } else if (updEnergySubs != null) {
            updEnergySubs.unsubscribe();
            updEnergySubs=null;
        }
    }

    private void updateEnergy() {
        if (this.owner_uuid==null) return;
        if (io == IO.IN) {
            useEnergy();
        } else {
            addEnergy();
        }
        updateEnergySubscription();
    }

    private void useEnergy() {
        var currentStored = energyContainer.getEnergyStored();
        var maxStored = GTValues.V[tier] * 16L * amperage;
        var changeStored = maxStored - currentStored;
        if (changeStored <= 0) return;
        if (!addEUToGlobalEnergyMap(this.owner_uuid,-changeStored)) return;
        energyContainer.setEnergyStored(maxStored);
    }

    private void addEnergy() {
        var currentStored = energyContainer.getEnergyStored();
        if (currentStored <= 0) return;
        if(!addEUToGlobalEnergyMap(this.owner_uuid,currentStored)) return;
        energyContainer.setEnergyStored(0L);
    }

    @Override
    public InteractionResult onUse(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        ItemStack is = player.getItemInHand(hand);
        if (!is.isEmpty() && is.is(GTItems.TOOL_DATA_STICK.asItem())) {

            if(player.isShiftKeyDown()) {
                this.owner_uuid = null;
                if (getLevel().isClientSide()) {
                    player.sendSystemMessage(Component.translatable("gtceu.machine.wireless_energy_hatch.tooltip.unbind",player.getName()));
                }
            } else {
                this.owner_uuid = player.getUUID();
                if (getLevel().isClientSide()) {
                    player.sendSystemMessage(Component.translatable("gtceu.machine.wireless_energy_hatch.tooltip.bind",player.getName()));
                }
            }

            updateEnergySubscription();
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    @Override
    public void onMachinePlaced(@Nullable LivingEntity player, ItemStack stack) {
        if (player != null) {
            this.owner_uuid = player.getUUID();
        }
    }

    @Override
    public UUID getUUID() {
        return this.owner_uuid;
    }

    @Override
    public void setUUID(UUID uuid) {
        this.owner_uuid = uuid;
    }

//    @Override
//    protected InteractionResult onScrewdriverClick(Player playerIn, InteractionHand hand, Direction gridSide, BlockHitResult hitResult) {
//        if (io == IO.OUT) energyContainer.setEnergyStored(GTValues.V[tier] * 64L * amperage);
//        return InteractionResult.SUCCESS;
//    }

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
}
