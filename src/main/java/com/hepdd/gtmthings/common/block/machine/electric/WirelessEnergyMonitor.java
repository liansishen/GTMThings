package com.hepdd.gtmthings.common.block.machine.electric;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.IFancyUIMachine;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import com.gregtechceu.gtceu.utils.GTUtil;
import com.hepdd.gtmthings.utils.TeamUtil;
import com.lowdragmc.lowdraglib.gui.util.ClickData;
import com.lowdragmc.lowdraglib.gui.widget.*;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import com.mojang.datafixers.util.Pair;
import net.minecraft.ChatFormatting;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;

import javax.annotation.ParametersAreNonnullByDefault;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.time.Duration;
import java.util.*;

import static com.hepdd.gtmthings.api.misc.WirelessEnergyManager.MachineData;
import static com.hepdd.gtmthings.api.misc.WirelessEnergyManager.getUserEU;
import static com.hepdd.gtmthings.utils.TeamUtil.GetName;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class WirelessEnergyMonitor extends MetaMachine
                implements IFancyUIMachine {


    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(WirelessEnergyMonitor.class,
            MetaMachine.MANAGED_FIELD_HOLDER);

    private static final BigInteger BIG_INTEGER_MAX_LONG = BigInteger.valueOf(Long.MAX_VALUE);

    public static int p;
    public static BlockPos pPos;

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

    private List<Map.Entry<Pair<UUID, MetaMachine>, Long>> sortedEntries = null;

    private boolean all = false;

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
    private void handleDisplayClick(String componentData, ClickData clickData) {
        if (!clickData.isRemote) {
            if (componentData.equals("all")) {
                all = !all;
            } else {
                p = 100;
                String[] parts = componentData.split(", ");
                pPos = new BlockPos(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]), Integer.parseInt(parts[2]));
            }
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
        if (this.userid==null || !this.userid.equals(player.getUUID())) {
            this.userid = player.getUUID();
            this.longArrayList = new ArrayList<>();
        }
        this.beforeEnergy = getUserEU(this.userid);
        return true;
    }

    private void addDisplayText(@NotNull List<Component> textList) {

        BigInteger energyTotal = getUserEU(this.userid);
        textList.add(Component.translatable("gtmthings.machine.wireless_energy_monitor.tooltip.0",
                GetName(this.holder.level(),this.userid)).withStyle(ChatFormatting.AQUA));
        textList.add(Component.translatable("gtmthings.machine.wireless_energy_monitor.tooltip.1",
                FormattingUtil.formatNumbers(energyTotal)).withStyle(ChatFormatting.GRAY));
        //average useage
        BigDecimal avgEnergy = getAvgUsage(energyTotal);
        Component voltageName = Component.literal(
                GTValues.VNF[GTUtil.getFloorTierByVoltage(avgEnergy.abs().longValue())]);
        BigDecimal voltageAmperage = avgEnergy.abs().divide(BigDecimal.valueOf(GTValues.V[GTUtil.getFloorTierByVoltage(avgEnergy.abs().longValue())]),1,RoundingMode.FLOOR);

        if (avgEnergy.compareTo(BigDecimal.valueOf(0)) >= 0) {
            textList.add(Component.translatable("gtmthings.machine.wireless_energy_monitor.tooltip.input",
                    FormattingUtil.formatNumbers(avgEnergy.abs()),voltageAmperage,voltageName).withStyle(ChatFormatting.GRAY));
            textList.add(Component.translatable("gtceu.multiblock.power_substation.time_to_fill", Component.translatable("gtmthings.machine.wireless_energy_monitor.tooltip.time_to_fill")).withStyle(ChatFormatting.GRAY));
        } else {
            textList.add(Component.translatable("gtmthings.machine.wireless_energy_monitor.tooltip.output",
                    FormattingUtil.formatNumbers(avgEnergy.abs()),voltageAmperage,voltageName).withStyle(ChatFormatting.GRAY));
            textList.add(Component.translatable("gtceu.multiblock.power_substation.time_to_drain",
                    getTimeToFillDrainText(energyTotal.divide(avgEnergy.abs().toBigInteger().multiply(BigInteger.valueOf(20))))).withStyle(ChatFormatting.GRAY));
        }
        textList.add(Component.translatable("gtmthings.machine.wireless_energy_monitor.tooltip.statistics")
                .append(ComponentPanelWidget.withButton(all ? Component.translatable("gtmthings.machine.wireless_energy_monitor.tooltip.all") : Component.translatable("gtmthings.machine.wireless_energy_monitor.tooltip.team"), "all")));
        for (Map.Entry<Pair<UUID, MetaMachine>, Long> m : getSortedEntries()) {
            UUID uuid = m.getKey().getFirst();
            if (all || TeamUtil.getTeamUUID(uuid) == TeamUtil.getTeamUUID(this.userid)) {
                MetaMachine machine = m.getKey().getSecond();
                long eut = m.getValue();
                String pos = machine.getPos().toShortString();
                if (eut > 0) {
                    textList.add(Component.translatable(machine.getBlockState().getBlock().getDescriptionId())
                            .withStyle(Style.EMPTY.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.translatable("recipe.condition.dimension.tooltip", machine.getLevel().dimension().location()).append(" [").append(pos).append("] ")
                                    .append(Component.translatable("gtmthings.machine.wireless_energy_monitor.tooltip.0", GetName(this.holder.level(), uuid))))))
                            .append(" +").append(FormattingUtil.formatNumbers(eut)).append(" EU/t (").append(GTValues.VNF[GTUtil.getFloorTierByVoltage(eut)]).append(")")
                            .append(ComponentPanelWidget.withButton(Component.literal(" [ ] "), pos)));
                } else {
                    textList.add(Component.translatable(machine.getBlockState().getBlock().getDescriptionId())
                            .withStyle(Style.EMPTY.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.translatable("recipe.condition.dimension.tooltip", machine.getLevel().dimension().location()).append(" [").append(pos).append("] ")
                                    .append(Component.translatable("gtmthings.machine.wireless_energy_monitor.tooltip.0", GetName(this.holder.level(), uuid))))))
                            .append(" -").append(FormattingUtil.formatNumbers(-eut)).append(" EU/t (").append(GTValues.VNF[GTUtil.getFloorTierByVoltage(-eut)]).append(")")
                            .append(ComponentPanelWidget.withButton(Component.literal(" [ ] "), pos)));
                }
            }
        }
    }

    private List<Map.Entry<Pair<UUID, MetaMachine>, Long>> getSortedEntries() {
        if (sortedEntries == null || getOffsetTimer() % 20 == 0) {
            sortedEntries = MachineData.entrySet()
                    .stream()
                    .sorted(Map.Entry.comparingByValue())
                    .toList();
            MachineData.clear();
        }
        return sortedEntries;
    }

    private static Component getTimeToFillDrainText(BigInteger timeToFillSeconds) {
        if (timeToFillSeconds.compareTo(BIG_INTEGER_MAX_LONG) > 0) {
            // too large to represent in a java Duration
            timeToFillSeconds = BIG_INTEGER_MAX_LONG;
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
        if (this.longArrayList.size() >= 20) {
            this.longArrayList.remove(0);
        }
        this.longArrayList.add(changed);

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
