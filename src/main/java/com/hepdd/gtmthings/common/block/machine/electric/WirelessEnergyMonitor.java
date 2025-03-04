package com.hepdd.gtmthings.common.block.machine.electric;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.IFancyUIMachine;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import com.gregtechceu.gtceu.utils.GTUtil;

import com.lowdragmc.lowdraglib.gui.util.ClickData;
import com.lowdragmc.lowdraglib.gui.widget.*;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.ChatFormatting;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;

import com.hepdd.gtmthings.api.machine.IWirelessEnergyContainerHolder;
import com.hepdd.gtmthings.api.misc.ITransferData;
import com.hepdd.gtmthings.api.misc.WirelessEnergyContainer;
import com.hepdd.gtmthings.utils.BigIntegerUtils;
import com.hepdd.gtmthings.utils.TeamUtil;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.time.Duration;
import java.util.*;

import javax.annotation.ParametersAreNonnullByDefault;

import static com.hepdd.gtmthings.utils.TeamUtil.GetName;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class WirelessEnergyMonitor extends MetaMachine implements IFancyUIMachine, IWirelessEnergyContainerHolder {

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(WirelessEnergyMonitor.class,
            MetaMachine.MANAGED_FIELD_HOLDER);

    public static int p;
    public static BlockPos pPos;

    public WirelessEnergyMonitor(IMachineBlockEntity holder) {
        super(holder);
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    @Getter
    private UUID UUID;

    @Getter
    @Setter
    private WirelessEnergyContainer WirelessEnergyContainerCache;

    private BigInteger beforeEnergy;

    private List<Component> textListCache;

    @Persisted
    private boolean all;

    //////////////////////////////////////
    // *********** GUI ***********//
    //////////////////////////////////////
    private void handleDisplayClick(String componentData, ClickData clickData) {
        if (componentData.equals("all")) {
            if (!clickData.isRemote) {
                all = !all;
            }
        } else if (clickData.isRemote) {
            p = 100;
            String[] parts = componentData.split(", ");
            pPos = new BlockPos(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]), Integer.parseInt(parts[2]));
        }
    }

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
        if (this.UUID == null) {
            this.UUID = player.getUUID();
        }
        WirelessEnergyContainer container = getWirelessEnergyContainer();
        if (container != null) this.beforeEnergy = container.getStorage();
        return true;
    }

    private void addDisplayText(@NotNull List<Component> textList) {
        if (isRemote()) return;
        if (textListCache == null || getOffsetTimer() % 10 == 0) {
            textListCache = new ArrayList<>();
            WirelessEnergyContainer container = getWirelessEnergyContainer();
            if (container == null) return;
            BigInteger energyTotal = container.getStorage();
            textListCache.add(Component.translatable("gtmthings.machine.wireless_energy_monitor.tooltip.0", GetName(getLevel(), this.UUID)).withStyle(ChatFormatting.AQUA));
            textListCache.add(Component.translatable("gtmthings.machine.wireless_energy_monitor.tooltip.1", FormattingUtil.formatNumbers(energyTotal)).withStyle(ChatFormatting.GRAY));
            long rate = container.getRate();
            textListCache.add(Component.translatable("gtmthings.machine.wireless_energy_monitor.tooltip.2", FormattingUtil.formatNumbers(rate), rate / GTValues.VEX[GTUtil.getFloorTierByVoltage(rate)], Component.literal(GTValues.VNF[GTUtil.getFloorTierByVoltage(rate)])).withStyle(ChatFormatting.GRAY));
            // average useage
            BigDecimal avgEnergy = getAvgUsage(energyTotal);
            Component voltageName = Component.literal(GTValues.VNF[GTUtil.getFloorTierByVoltage(avgEnergy.abs().longValue())]);
            BigDecimal voltageAmperage = avgEnergy.abs().divide(BigDecimal.valueOf(GTValues.VEX[GTUtil.getFloorTierByVoltage(avgEnergy.abs().longValue())]), 1, RoundingMode.FLOOR);

            if (avgEnergy.compareTo(BigDecimal.valueOf(0)) >= 0) {
                textListCache.add(Component.translatable("gtmthings.machine.wireless_energy_monitor.tooltip.input",
                        FormattingUtil.formatNumbers(avgEnergy.abs()), voltageAmperage, voltageName).withStyle(ChatFormatting.GRAY));
                textListCache.add(Component.translatable("gtceu.multiblock.power_substation.time_to_fill",
                        container.getCapacity() == null ? Component.translatable("gtmthings.machine.wireless_energy_monitor.tooltip.time_to_fill") : getTimeToFillDrainText((container.getCapacity().subtract(energyTotal)).divide(avgEnergy.abs().toBigInteger().multiply(BigInteger.valueOf(20))))).withStyle(ChatFormatting.GRAY));
            } else {
                textListCache.add(Component.translatable("gtmthings.machine.wireless_energy_monitor.tooltip.output",
                        FormattingUtil.formatNumbers(avgEnergy.abs()), voltageAmperage, voltageName).withStyle(ChatFormatting.GRAY));
                textListCache.add(Component.translatable("gtceu.multiblock.power_substation.time_to_drain",
                        getTimeToFillDrainText(energyTotal.divide(avgEnergy.abs().toBigInteger().multiply(BigInteger.valueOf(20))))).withStyle(ChatFormatting.GRAY));
            }
            if (container.getBindPos() != null) {
                String pos = container.getBindPos().pos().toShortString();
                textListCache.add(Component.translatable("gtmthings.machine.wireless_energy_hatch.tooltip.2", Component.translatable("recipe.condition.dimension.tooltip", container.getBindPos().dimension().location().toString()).append(" [").append(pos).append("] ")).withStyle(ChatFormatting.GRAY));
            }
            textListCache.add(Component.translatable("gtmthings.machine.wireless_energy_monitor.tooltip.statistics").append(ComponentPanelWidget.withButton(all ? Component.translatable("gtmthings.machine.wireless_energy_monitor.tooltip.all") : Component.translatable("gtmthings.machine.wireless_energy_monitor.tooltip.team"), "all")));
            WirelessEnergyContainer.observed = true;
            List<Map.Entry<MetaMachine, ITransferData>> sortedEntries = WirelessEnergyContainer.TRANSFER_DATA.entrySet()
                    .stream()
                    .sorted(Comparator.comparingLong(entry -> entry.getValue().Throughput()))
                    .toList();
            WirelessEnergyContainer.TRANSFER_DATA.clear();
            for (Map.Entry<MetaMachine, ITransferData> m : sortedEntries) {
                UUID uuid = m.getValue().UUID();
                if (all || uuid.equals(TeamUtil.getTeamUUID(this.UUID))) {
                    textListCache.add(m.getValue().getInfo());
                }
            }
        }
        textList.addAll(textListCache);
    }

    private static Component getTimeToFillDrainText(BigInteger timeToFillSeconds) {
        if (timeToFillSeconds.compareTo(BigIntegerUtils.BIG_INTEGER_MAX_LONG) > 0) {
            // too large to represent in a java Duration
            timeToFillSeconds = BigIntegerUtils.BIG_INTEGER_MAX_LONG;
        }

        Duration duration = Duration.ofSeconds(timeToFillSeconds.longValue());
        String key;
        long fillTime;
        if (duration.getSeconds() <= 180) {
            fillTime = duration.getSeconds();
            key = "gtceu.multiblock.power_substation.time_seconds";
        } else if (duration.toMinutes() <= 180) {
            fillTime = duration.toMinutes();
            key = "gtceu.multiblock.power_substation.time_minutes";
        } else if (duration.toHours() <= 72) {
            fillTime = duration.toHours();
            key = "gtceu.multiblock.power_substation.time_hours";
        } else if (duration.toDays() <= 730) { // 2 years
            fillTime = duration.toDays();
            key = "gtceu.multiblock.power_substation.time_days";
        } else if (duration.toDays() / 365 < 1_000_000) {
            fillTime = duration.toDays() / 365;
            key = "gtceu.multiblock.power_substation.time_years";
        } else {
            return Component.translatable("gtceu.multiblock.power_substation.time_forever");
        }

        return Component.translatable(key, FormattingUtil.formatNumbers(fillTime));
    }

    private BigDecimal getAvgUsage(BigInteger now) {
        BigInteger changed = now.subtract(this.beforeEnergy);
        this.beforeEnergy = now;
        return new BigDecimal(changed).divide(BigDecimal.valueOf(10), RoundingMode.HALF_UP);
    }

    @Override
    public boolean display() {
        return false;
    }
}
