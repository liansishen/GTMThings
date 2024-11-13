package com.hepdd.gtmthings.common.block.machine.multiblock.part;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.feature.IDataInfoProvider;
import com.gregtechceu.gtceu.api.machine.multiblock.part.TieredIOPartMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableLaserContainer;
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

import static net.minecraft.ChatFormatting.*;

@ParametersAreNonnullByDefault
public class CreativeLaserHatchPartMachine extends TieredIOPartMachine implements IDataInfoProvider {

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(CreativeLaserHatchPartMachine.class, TieredIOPartMachine.MANAGED_FIELD_HOLDER);

    @Persisted
    private NotifiableLaserContainer buffer;
    @Nullable
    protected ISubscription LaserListener;
    protected TickableSubscription explosionSubs;
    private Long maxEnergy;
    @Persisted
    private long voltage = 0;
    @Persisted
    @Getter
    private int amps = 256;
    @Persisted
    private int setTier = GTValues.IV;

    public static final String[] VNF = new String[] {
            BLUE + "IV",
            LIGHT_PURPLE + "LuV",
            RED + "ZPM",
            DARK_AQUA + "UV",
            DARK_RED + "UHV",
            GREEN + "UEV",
            DARK_GREEN + "UIV",
            YELLOW + "UXV",
            BLUE.toString() + BOLD + "OpV",
            RED.toString() + BOLD + "MAX" };

    public CreativeLaserHatchPartMachine(IMachineBlockEntity holder) {
        super(holder, GTValues.MAX, IO.IN);
        this.voltage = GTValues.V[setTier];
        this.maxEnergy = voltage * 64L * amps;
        this.buffer = NotifiableLaserContainer.receiverContainer(this, this.maxEnergy, voltage, amps);
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
        if (buffer.getInputVoltage() != voltage || buffer.getInputAmperage() != amps) {
            maxEnergy = voltage * 64L * amps;
            buffer.resetBasicInfo(maxEnergy, voltage, amps, 0, 0);
            buffer.setEnergyStored(0);
        }
        if (buffer.getEnergyStored() < this.maxEnergy) {
            buffer.setEnergyStored(this.maxEnergy);
        }
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

    @Override
    public ModularUI createUI(Player entityPlayer) {
        return new ModularUI(176, 136, this, entityPlayer)
                .background(GuiTextures.BACKGROUND)
                .widget(new LabelWidget(7, 32, "gtceu.creative.energy.voltage"))
                .widget(new TextFieldWidget(9, 47, 152, 16, () -> String.valueOf(voltage),
                        value -> {
                            voltage = Long.parseLong(value);
                            setTier = GTUtil.getTierByVoltage(voltage);
                        }).setNumbersOnly(8192L, Long.MAX_VALUE))
                .widget(new LabelWidget(7, 74, "gtceu.creative.energy.amperage"))
                .widget(new ButtonWidget(7, 87, 20, 20,
                        new GuiTextureGroup(ResourceBorderTexture.BUTTON_COMMON, new TextTexture("-")),
                        cd -> amps = --amps == -1 ? 0 : amps))
                .widget(new TextFieldWidget(31, 89, 114, 16, () -> String.valueOf(amps),
                        value -> amps = Integer.parseInt(value)).setNumbersOnly(256, 67108864))
                .widget(new ButtonWidget(149, 87, 20, 20,
                        new GuiTextureGroup(ResourceBorderTexture.BUTTON_COMMON, new TextTexture("+")),
                        cd -> {
                            if (amps < Integer.MAX_VALUE) {
                                amps++;
                            }
                        }))

                .widget(new SelectorWidget(7, 7, 30, 20, Arrays.stream(VNF).toList(), -1)
                        .setOnChanged(tier -> {
                            setTier = ArrayUtils.indexOf(VNF, tier) + 5;
                            voltage = GTValues.V[setTier];
                        })
                        .setSupplier(() -> VNF[setTier - 5])
                        .setButtonBackground(ResourceBorderTexture.BUTTON_COMMON)
                        .setBackground(ColorPattern.BLACK.rectTexture())
                        .setValue(VNF[setTier - 5]));
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
