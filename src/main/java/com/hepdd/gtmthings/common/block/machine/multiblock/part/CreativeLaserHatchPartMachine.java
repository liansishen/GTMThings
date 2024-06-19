package com.hepdd.gtmthings.common.block.machine.multiblock.part;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.feature.IDataInfoProvider;
import com.gregtechceu.gtceu.api.machine.multiblock.part.TieredIOPartMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableLaserContainer;
import com.gregtechceu.gtceu.common.item.PortableScannerBehavior;
import com.lowdragmc.lowdraglib.syncdata.ISubscription;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import lombok.Getter;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@ParametersAreNonnullByDefault
public class CreativeLaserHatchPartMachine extends TieredIOPartMachine implements IDataInfoProvider {
    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(CreativeLaserHatchPartMachine.class, TieredIOPartMachine.MANAGED_FIELD_HOLDER);

    @Persisted
    private NotifiableLaserContainer buffer;
    @Nullable
    protected ISubscription LaserListener;
    protected TickableSubscription explosionSubs;
    @Getter
    protected int amperage;
    private Long maxEnergy;

    public CreativeLaserHatchPartMachine(IMachineBlockEntity holder, int tier, int amperage) {
        super(holder, tier, IO.IN);
        this.maxEnergy = GTValues.V[tier] * 64L * amperage;
        this.buffer = NotifiableLaserContainer.receiverContainer(this,this.maxEnergy , GTValues.V[tier], amperage);
        this.amperage = amperage;
    }

    @Override
    public void onUnload() {
        super.onUnload();
        if (LaserListener != null) {
            LaserListener.unsubscribe();
            LaserListener = null;
        }
    }

    @Override
    public void onLoad() {
        super.onLoad();
        LaserListener = buffer.addChangedListener(this::AddEngerySubscription);
        AddEngerySubscription();

    }

    protected void AddEngerySubscription() {
        explosionSubs = subscribeServerTick(explosionSubs, this::addEng);
    }

    protected void addEng() {
        if (buffer.getEnergyStored() < this.maxEnergy) {
            buffer.setEnergyStored(this.maxEnergy);
        }

    }

    @Override
    public boolean shouldOpenUI(Player player, InteractionHand hand, BlockHitResult hit) {
        return false;
    }

    @Override
    public boolean canShared() {
        return false;
    }

    @Override
    @NotNull
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    @NotNull
    @Override
    public List<Component> getDataInfo(PortableScannerBehavior.DisplayMode mode) {
        if (mode == PortableScannerBehavior.DisplayMode.SHOW_ALL || mode == PortableScannerBehavior.DisplayMode.SHOW_ELECTRICAL_INFO) {
            return Collections.singletonList(Component.literal(
                    String.format("%d/%d EU", buffer.getEnergyStored(), buffer.getEnergyCapacity())));
        }
        return new ArrayList<>();
    }
}
