package com.hepdd.gtmthings.common.block.machine.multiblock.part.computation;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.IInteractedMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.part.MultiblockPartMachine;
import com.gregtechceu.gtceu.common.data.GTItems;

import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import com.hepdd.gtmthings.api.capability.IGTMTJadeIF;
import com.hepdd.gtmthings.common.block.machine.trait.WirelessNotifiableComputationContainer;
import lombok.Getter;
import lombok.Setter;

import javax.annotation.ParametersAreNonnullByDefault;

@Getter
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class WirelessOpticalComputationHatchMachine extends MultiblockPartMachine implements IInteractedMachine, IGTMTJadeIF {

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            WirelessOpticalComputationHatchMachine.class, MultiblockPartMachine.MANAGED_FIELD_HOLDER);

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    private final boolean transmitter;

    @Setter
    @Persisted
    private BlockPos transmitterPos;
    @Setter
    @Persisted
    private BlockPos receiverPos;
    protected WirelessNotifiableComputationContainer computationContainer;

    public WirelessOpticalComputationHatchMachine(IMachineBlockEntity holder, boolean transmitter) {
        super(holder);
        this.transmitter = transmitter;
        this.computationContainer = createComputationContainer(transmitter);
    }

    protected WirelessNotifiableComputationContainer createComputationContainer(Object... args) {
        IO io = IO.IN;
        if (args.length > 1 && args[args.length - 2] instanceof IO newIo) {
            io = newIo;
        }
        if (args.length > 0 && args[args.length - 1] instanceof Boolean transmitter) {
            return new WirelessNotifiableComputationContainer(this, io, transmitter);
        }
        throw new IllegalArgumentException();
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
    public InteractionResult onUse(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        ItemStack is = player.getItemInHand(hand);
        if (is.isEmpty()) return InteractionResult.PASS;
        if (is.is(GTItems.TOOL_DATA_STICK.asItem())) {
            if (transmitter) {
                if (this.transmitterPos == null) this.transmitterPos = pos;
                var tag = is.getTag();
                if (tag != null) {
                    CompoundTag posTag = new CompoundTag();
                    posTag.putInt("x", this.transmitterPos.getX());
                    posTag.putInt("y", this.transmitterPos.getY());
                    posTag.putInt("z", this.transmitterPos.getZ());
                    tag.put("transmitterPos", posTag);
                    var bindPos = (CompoundTag) tag.get("receiverPos");
                    if (bindPos != null) {
                        BlockPos recPos = new BlockPos(bindPos.getInt("x"), bindPos.getInt("y"), bindPos.getInt("z"));
                        if (MetaMachine.getMachine(getLevel(), recPos) instanceof WirelessOpticalComputationHatchMachine woc && !woc.transmitter) {
                            woc.setTransmitterPos(this.transmitterPos);
                            this.receiverPos = recPos;
                            tag.remove("transmitterPos");
                            tag.remove("receiverPos");
                            if (getLevel().isClientSide()) {
                                player.sendSystemMessage(Component.translatable("gtmthings.machine.wireless_computation_hatch.binded"));
                            }
                        }
                    } else {
                        if (getLevel().isClientSide()) {
                            player.sendSystemMessage(Component.translatable("gtmthings.machine.wireless_computation_transmitter_hatch.tobind"));
                        }
                    }
                    is.setTag(tag);
                } else {
                    tag = new CompoundTag();
                    CompoundTag posTag = new CompoundTag();
                    posTag.putInt("x", this.transmitterPos.getX());
                    posTag.putInt("y", this.transmitterPos.getY());
                    posTag.putInt("z", this.transmitterPos.getZ());
                    tag.put("transmitterPos", posTag);
                    is.setTag(tag);
                    if (getLevel().isClientSide()) {
                        player.sendSystemMessage(Component.translatable("gtmthings.machine.wireless_computation_transmitter_hatch.tobind"));
                    }
                }
                return InteractionResult.SUCCESS;
            } else {
                if (this.receiverPos == null) this.receiverPos = pos;
                var tag = is.getTag();
                if (tag != null) {
                    CompoundTag posTag = new CompoundTag();
                    posTag.putInt("x", this.receiverPos.getX());
                    posTag.putInt("y", this.receiverPos.getY());
                    posTag.putInt("z", this.receiverPos.getZ());
                    tag.put("receiverPos", posTag);
                    var bindPos = (CompoundTag) tag.get("transmitterPos");
                    if (bindPos != null) {
                        BlockPos tranPos = new BlockPos(bindPos.getInt("x"), bindPos.getInt("y"), bindPos.getInt("z"));
                        if (MetaMachine.getMachine(getLevel(), tranPos) instanceof WirelessOpticalComputationHatchMachine woc && woc.transmitter) {
                            woc.setReceiverPos(this.receiverPos);
                            this.transmitterPos = tranPos;
                            tag.remove("transmitterPos");
                            tag.remove("receiverPos");
                            if (getLevel().isClientSide()) {
                                player.sendSystemMessage(Component.translatable("gtmthings.machine.wireless_computation_hatch.binded"));
                            }
                        }
                    } else {
                        if (getLevel().isClientSide()) {
                            player.sendSystemMessage(Component.translatable("gtmthings.machine.wireless_computation_receiver_hatch.tobind"));
                        }
                    }
                    is.setTag(tag);
                } else {
                    tag = new CompoundTag();
                    CompoundTag posTag = new CompoundTag();
                    posTag.putInt("x", this.receiverPos.getX());
                    posTag.putInt("y", this.receiverPos.getY());
                    posTag.putInt("z", this.receiverPos.getZ());
                    tag.put("receiverPos", posTag);
                    is.setTag(tag);
                    if (getLevel().isClientSide()) {
                        player.sendSystemMessage(Component.translatable("gtmthings.machine.wireless_computation_receiver_hatch.tobind"));
                    }
                }
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.PASS;
    }

    @Override
    public boolean isbinded() {
        return (this.transmitterPos != null || this.receiverPos != null);
    }

    @Override
    public String getBindPos() {
        if (this.isTransmitter() && this.receiverPos != null) {
            return this.receiverPos.toShortString();
        } else if (!this.isTransmitter() && this.transmitterPos != null) {
            return this.transmitterPos.toShortString();
        }
        return "";
    }
}
