package com.example.examplemod.block.machine.electric;

import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.IFancyUIMachine;
import com.lowdragmc.lowdraglib.gui.util.ClickData;
import com.lowdragmc.lowdraglib.gui.widget.*;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.example.examplemod.api.misc.WirelessEnergyManager.getUserEU;

public class WirelessEnergyMonitor extends MetaMachine
                implements IFancyUIMachine {


    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(WirelessEnergyMonitor.class,
            MetaMachine.MANAGED_FIELD_HOLDER);

    public WirelessEnergyMonitor(IMachineBlockEntity holder) {
        super(holder);
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    private UUID userid;

    private BigInteger beforeEnergy;

    private ArrayList<BigInteger> longArrayList;

    @Override
    public void onLoad() {
        super.onLoad();
    }

    @Override
    public void onUnload() {
        super.onUnload();
    }

    //////////////////////////////////////
    // *********** GUI ***********//
    //////////////////////////////////////
    private void handleDisplayClick(String componentData, ClickData clickData) {}

    @Override
    public Widget createUIWidget() {
        var group = new WidgetGroup(0, 0, 182 + 8, 117 + 8);
        group.addWidget(new DraggableScrollableWidgetGroup(4, 4, 182, 117).setBackground(GuiTextures.DISPLAY)
                .addWidget(new LabelWidget(4, 5, self().getBlockState().getBlock().getDescriptionId()))
                .addWidget(new ComponentPanelWidget(4, 17, this::addDisplayText)
                        .setMaxWidthLimit(150)
                        .clickHandler(this::handleDisplayClick)));
        group.setBackground(GuiTextures.BACKGROUND_INVERSE);
        return group;
    }

    @Override
    public boolean shouldOpenUI(Player player, InteractionHand hand, BlockHitResult hit) {
        if (this.userid==null || !this.userid.equals(player.getUUID())) {
            this.userid = player.getUUID();
            this.longArrayList = new ArrayList<>();
        }
        this.beforeEnergy = getUserEU(this.userid);
        return true;
    }

    private void addDisplayText(@NotNull List<Component> textList) {

        BigInteger energyTotal = getUserEU(this.userid);
        textList.add(Component.translatable("gtceu.machine.wireless_energy_monitor.tooltip.0",this.holder.level().getPlayerByUUID(this.userid).getDisplayName()));
        textList.add(Component.translatable("gtceu.machine.wireless_energy_monitor.tooltip.1",String.valueOf(energyTotal)));
        //average useage
        BigDecimal avgEnergy = getAvgUsage(energyTotal);
        if (avgEnergy.compareTo(BigDecimal.valueOf(0)) > 0) {
            textList.add(Component.translatable("gtceu.machine.wireless_energy_monitor.tooltip.input", String.valueOf(avgEnergy.abs())));
        } else {
            textList.add(Component.translatable("gtceu.machine.wireless_energy_monitor.tooltip.output", String.valueOf(avgEnergy.abs())));
        }
//        if (avgEnergy >= 0L) {
//            textList.add(Component.translatable("gtceu.machine.wireless_energy_monitor.tooltip.input", String.valueOf(Math.abs(avgEnergy))));
//        } else {
//            textList.add(Component.translatable("gtceu.machine.wireless_energy_monitor.tooltip.output", String.valueOf(Math.abs(avgEnergy))));
//        }
    }

    private BigDecimal getAvgUsage(BigInteger now) {
        BigInteger changed = now.subtract(this.beforeEnergy);
        this.beforeEnergy = now;
        if (this.longArrayList.size() >= 20) {
            this.longArrayList.remove(0);
        }
        this.longArrayList.add(changed);

//        return Math.round(this.longArrayList.stream().mapToLong(Number::longValue).average().getAsDouble());
        return calculateAverage(this.longArrayList);
    }

    private static BigDecimal calculateAverage(ArrayList<BigInteger> bigIntegers) {
        BigInteger sum = BigInteger.ZERO;
        for (BigInteger bi : bigIntegers) {
            sum = sum.add(bi);
        }
        // 使用BigDecimal进行除法运算以获得精确的平均值
        return new BigDecimal(sum).divide(new BigDecimal(bigIntegers.size()), RoundingMode.HALF_UP);
    }
}
