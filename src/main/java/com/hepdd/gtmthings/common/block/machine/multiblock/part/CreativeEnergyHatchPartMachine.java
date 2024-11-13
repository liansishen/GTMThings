package com.hepdd.gtmthings.common.block.machine.multiblock.part;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.feature.IDataInfoProvider;
import com.gregtechceu.gtceu.api.machine.multiblock.part.TieredIOPartMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableEnergyContainer;
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
import com.lowdragmc.lowdraglib.syncdata.ISubscription;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

import lombok.Getter;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
    protected TickableSubscription explosionSubs;
    @Nullable
    protected ISubscription energyListener;
    private Long maxEnergy;
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
        this.maxEnergy = GTValues.V[tier] * 16L * amps;
        container = NotifiableEnergyContainer.receiverContainer(this, this.maxEnergy, GTValues.V[tier], amps);
        return container;
    }

    @Override
    public void onLoad() {
        super.onLoad();
        energyListener = energyContainer.addChangedListener(this::InfinityEnergySubscription);
        InfinityEnergySubscription();
    }

    @Override
    public void onUnload() {
        super.onUnload();
        if (energyListener != null) {
            energyListener.unsubscribe();
            energyListener = null;
        }
    }

    protected void InfinityEnergySubscription() {
        explosionSubs = subscribeServerTick(explosionSubs, this::addEnergy);
    }

    protected void addEnergy() {
        if (energyContainer.getInputVoltage() != voltage || energyContainer.getInputAmperage() != amps) {
            maxEnergy = voltage * 16L * amps;
            energyContainer.resetBasicInfo(maxEnergy, voltage, amps, 0, 0);
            energyContainer.setEnergyStored(0);
        }
        if (energyContainer.getEnergyStored() < this.maxEnergy) {
            energyContainer.setEnergyStored(this.maxEnergy);
        }
    }

    @Override
    public ModularUI createUI(Player entityPlayer) {
        return new ModularUI(176, 136, this, entityPlayer)
                .background(GuiTextures.BACKGROUND)
                .widget(new LabelWidget(7, 32, "gtceu.creative.energy.voltage"))
                .widget(new TextFieldWidget(9, 47, 152, 16, () -> String.valueOf(voltage),
                        value -> {
                            voltage = Long.parseLong(value);
                            setTier = GTUtil.getTierByVoltage(voltage);
                        }).setNumbersOnly(8L, Long.MAX_VALUE))
                .widget(new LabelWidget(7, 74, "gtceu.creative.energy.amperage"))
                .widget(new ButtonWidget(7, 87, 20, 20,
                        new GuiTextureGroup(ResourceBorderTexture.BUTTON_COMMON, new TextTexture("-")),
                        cd -> amps = --amps == -1 ? 0 : amps))
                .widget(new TextFieldWidget(31, 89, 114, 16, () -> String.valueOf(amps),
                        value -> amps = Integer.parseInt(value)).setNumbersOnly(1, 67108864))
                .widget(new ButtonWidget(149, 87, 20, 20,
                        new GuiTextureGroup(ResourceBorderTexture.BUTTON_COMMON, new TextTexture("+")),
                        cd -> {
                            if (amps < Integer.MAX_VALUE) {
                                amps++;
                            }
                        }))

                .widget(new SelectorWidget(7, 7, 30, 20, Arrays.stream(GTValues.VNF).toList(), -1)
                        .setOnChanged(tier -> {
                            setTier = ArrayUtils.indexOf(GTValues.VNF, tier);
                            voltage = GTValues.V[setTier];
                        })
                        .setSupplier(() -> GTValues.VNF[setTier])
                        .setButtonBackground(ResourceBorderTexture.BUTTON_COMMON)
                        .setBackground(ColorPattern.BLACK.rectTexture())
                        .setValue(GTValues.VNF[setTier]));
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
}
