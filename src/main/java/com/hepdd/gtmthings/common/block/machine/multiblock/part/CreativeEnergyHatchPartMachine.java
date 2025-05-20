package com.hepdd.gtmthings.common.block.machine.multiblock.part;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.IDataInfoProvider;
import com.gregtechceu.gtceu.api.machine.multiblock.part.TieredIOPartMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableEnergyContainer;
import com.gregtechceu.gtceu.api.pattern.MultiblockWorldSavedData;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.common.item.PortableScannerBehavior;
import com.gregtechceu.gtceu.utils.GTUtil;

import com.lowdragmc.lowdraglib.gui.editor.ColorPattern;
import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.texture.GuiTextureGroup;
import com.lowdragmc.lowdraglib.gui.texture.ResourceBorderTexture;
import com.lowdragmc.lowdraglib.gui.texture.TextTexture;
import com.lowdragmc.lowdraglib.gui.widget.ButtonWidget;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.SelectorWidget;
import com.lowdragmc.lowdraglib.gui.widget.TextFieldWidget;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;

import lombok.Getter;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class CreativeEnergyHatchPartMachine extends TieredIOPartMachine implements IDataInfoProvider {

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(CreativeEnergyHatchPartMachine.class, TieredIOPartMachine.MANAGED_FIELD_HOLDER);

    @Persisted
    public final NotifiableEnergyContainer energyContainer;
    @Persisted
    private long maxEnergy;
    @Persisted
    private long voltage = 0;
    @Persisted
    @Getter
    private int amps = 1;
    @Persisted
    private int setTier = 0;

    public CreativeEnergyHatchPartMachine(IMachineBlockEntity holder) {
        super(holder, GTValues.MAX, IO.IN);
        this.energyContainer = createEnergyContainer();
    }

    //////////////////////////////////////
    // ***** Initialization ******//
    //////////////////////////////////////
    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    protected NotifiableEnergyContainer createEnergyContainer() {
        NotifiableEnergyContainer container;
        this.voltage = GTValues.VEX[setTier];
        this.maxEnergy = this.voltage * this.amps;
        container = new InfinityEnergyContainer(this, this.maxEnergy, this.voltage, this.amps, 0L, 0L);
        return container;
    }

    @Override
    public void loadCustomPersistedData(@NotNull CompoundTag tag) {
        super.loadCustomPersistedData(tag);
        updateEnergyContainer();
    }

    @Override
    public ModularUI createUI(Player entityPlayer) {
        return new ModularUI(176, 136, this, entityPlayer)
                .background(GuiTextures.BACKGROUND)
                .widget(new LabelWidget(7, 32, "gtceu.creative.energy.voltage"))
                .widget(new TextFieldWidget(9, 47, 152, 16, () -> String.valueOf(voltage),
                        value -> {
                            setVoltage(Long.parseLong(value));
                            setTier = GTUtil.getTierByVoltage(this.voltage);
                        }).setNumbersOnly(8L, Long.MAX_VALUE))
                .widget(new LabelWidget(7, 74, "gtceu.creative.energy.amperage"))
                .widget(new ButtonWidget(7, 87, 20, 20,
                        new GuiTextureGroup(ResourceBorderTexture.BUTTON_COMMON, new TextTexture("-")),
                        cd -> setAmps(--amps == -1 ? 0 : amps)))
                .widget(new TextFieldWidget(31, 89, 114, 16, () -> String.valueOf(amps),
                        value -> setAmps(Integer.parseInt(value))).setNumbersOnly(1, 67108864))
                .widget(new ButtonWidget(149, 87, 20, 20,
                        new GuiTextureGroup(ResourceBorderTexture.BUTTON_COMMON, new TextTexture("+")),
                        cd -> {
                            if (amps < Integer.MAX_VALUE) {
                                setAmps(++amps);
                            }
                        }))

                .widget(new SelectorWidget(7, 7, 50, 20, Arrays.stream(GTValues.VNF).toList(), -1)
                        .setOnChanged(tier -> {
                            setTier = ArrayUtils.indexOf(GTValues.VNF, tier);
                            setVoltage(GTValues.VEX[setTier]);
                        })
                        .setSupplier(() -> GTValues.VNF[setTier])
                        .setButtonBackground(ResourceBorderTexture.BUTTON_COMMON)
                        .setBackground(ColorPattern.BLACK.rectTexture())
                        .setValue(GTValues.VNF[setTier]));
    }

    private void setVoltage(long voltage) {
        this.voltage = voltage;
        this.maxEnergy = this.voltage * this.amps;
        updateMachine();
    }

    private void setAmps(int amps) {
        this.amps = amps;
        this.maxEnergy = this.voltage * this.amps;
        updateMachine();
    }

    private void updateEnergyContainer() {
        this.energyContainer.resetBasicInfo(this.maxEnergy, this.voltage, this.amps, 0, 0);
        this.energyContainer.setEnergyStored(this.maxEnergy);
    }

    private void updateMachine() {
        updateEnergyContainer();
        if (getLevel() instanceof ServerLevel serverLevel) {
            serverLevel.getServer().execute(() -> {
                for (var c : getControllers()) {
                    if (c.isFormed()) {
                        c.getPatternLock().lock();
                        try {
                            c.onStructureInvalid();
                            var mwsd = MultiblockWorldSavedData.getOrCreate(serverLevel);
                            mwsd.removeMapping(c.getMultiblockState());
                            mwsd.addAsyncLogic(c);
                        } finally {
                            c.getPatternLock().unlock();
                        }
                    }
                }
            });
        }
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

    @NotNull
    @Override
    public List<Component> getDataInfo(PortableScannerBehavior.DisplayMode mode) {
        if (mode == PortableScannerBehavior.DisplayMode.SHOW_ALL || mode == PortableScannerBehavior.DisplayMode.SHOW_ELECTRICAL_INFO) {
            return Collections.singletonList(Component.literal(
                    String.format("%d/%d EU", energyContainer.getEnergyStored(), energyContainer.getEnergyCapacity())));
        }
        return new ArrayList<>();
    }

    private static class InfinityEnergyContainer extends NotifiableEnergyContainer {

        public InfinityEnergyContainer(MetaMachine machine, long maxCapacity, long maxInputVoltage, long maxInputAmperage, long maxOutputVoltage, long maxOutputAmperage) {
            super(machine, maxCapacity, maxInputVoltage, maxInputAmperage, maxOutputVoltage, maxOutputAmperage);
        }

        @Override
        public List<Long> handleRecipeInner(IO io, GTRecipe recipe, List<Long> left, boolean simulate) {
            return super.handleRecipeInner(io, recipe, left, true);
        }

        @Override
        public long changeEnergy(long energyToAdd) {
            long oldEnergyStored = getEnergyStored();
            long newEnergyStored = (getEnergyCapacity() - oldEnergyStored < energyToAdd) ? getEnergyCapacity() : (oldEnergyStored + energyToAdd);
            if (newEnergyStored < 0) newEnergyStored = 0;
            return newEnergyStored - oldEnergyStored;
        }

        @Override
        public void checkOutputSubscription() {}

        @Override
        public void serverTick() {}

        @Override
        public long acceptEnergyFromNetwork(Direction side, long voltage, long amperage) {
            return 0;
        }

        @Override
        public boolean outputsEnergy(Direction side) {
            return false;
        }

        @Override
        public boolean inputsEnergy(Direction side) {
            return false;
        }
    }
}
